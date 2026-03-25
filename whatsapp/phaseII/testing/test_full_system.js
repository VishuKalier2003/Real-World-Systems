/**
 * k6 Full System Test — Soak + Spike
 *
 * WHAT THIS TESTS:
 *   - All three endpoints simultaneously with realistic traffic distribution
 *   - Soak test: sustained load over time to catch memory leaks
 *   - Spike test: sudden burst to find breaking point
 *   - Rate limiter's in-memory state stability over time (does the deque grow unbounded?)
 *
 * TRAFFIC DISTRIBUTION (realistic WhatsApp auth pattern):
 *   - 60% login (most common: existing users validating sessions)
 *   - 30% token (users requesting new sessions)
 *   - 10% signup (new user registrations)
 *
 * HOW TO RUN:
 *   Soak:  k6 run --env TEST=soak  test_full_system.js
 *   Spike: k6 run --env TEST=spike test_full_system.js
 *   Mix:   k6 run                  test_full_system.js
 *
 * PHASE II EXPECTED BENCHMARKS:
 *   At 50 VUs steady:
 *     /signup p(95)  < 700ms   (BCrypt dominant)
 *     /token  p(95)  < 60ms    (HMAC dominant)
 *     /login  p(95)  < 30ms    (parse dominant)
 *     Error rate     < 1%
 *
 *   Breaking point (Phase II single instance):
 *     Expect degradation to start around 80-120 concurrent VUs
 *     Cause: Tomcat thread pool (200) + HikariCP pool (10) contention
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ── Custom metrics ────────────────────────────────────────────────────────────
const signupP95   = new Trend('signup_p95', true);
const tokenP95    = new Trend('token_p95',  true);
const loginP95    = new Trend('login_p95',  true);
const totalErrors = new Counter('total_errors');
const rl_blocked  = new Counter('rate_limiter_blocks');
const overallRate = new Rate('overall_success');

// ── Choose scenario via env var ───────────────────────────────────────────────
const TEST = __ENV.TEST || 'mixed';

const scenarios = {
  // Soak: moderate load for 10 minutes — watch for memory growth
  soak: [
    { duration: '1m',  target: 30 },
    { duration: '8m',  target: 30 },
    { duration: '1m',  target: 0  },
  ],
  // Spike: sudden burst then back to normal
  spike: [
    { duration: '30s', target: 10  },  // baseline
    { duration: '10s', target: 200 },  // SPIKE — 200 VUs sudden
    { duration: '30s', target: 200 },  // hold spike
    { duration: '10s', target: 10  },  // recovery
    { duration: '30s', target: 10  },  // confirm recovery
    { duration: '10s', target: 0   },
  ],
  // Mixed: standard ramp-up stress test
  mixed: [
    { duration: '20s', target: 10 },
    { duration: '40s', target: 50 },
    { duration: '20s', target: 80 },
    { duration: '40s', target: 80 },
    { duration: '20s', target: 0  },
  ],
};

export const options = {
  stages: scenarios[TEST],
  thresholds: {
    'signup_p95':     ['p(95)<700'],
    'token_p95':      ['p(95)<60'],
    'login_p95':      ['p(95)<30'],
    'overall_success':['rate>0.97'],
    'http_req_failed':['rate<0.03'],
  },
};

const BASE_URL = 'http://localhost:8080';

// Pre-issued tokens cache: VUs store their token after /token and reuse in /login
// This is realistic — a logged-in client reuses its token for many /login calls
const tokenCache = {};

function uniquePhone(prefix) {
  return `${prefix}${String(__VU).padStart(3, '0')}${String(__ITER).padStart(5, '0')}`;
}

// ── Traffic distribution logic ────────────────────────────────────────────────
export default function () {
  const roll = Math.random();

  if (roll < 0.10) {
    // 10% — SIGNUP
    doSignup();
  } else if (roll < 0.40) {
    // 30% — TOKEN
    doToken();
  } else {
    // 60% — LOGIN
    doLogin();
  }

  // Realistic think time between requests (represents human interaction)
  sleep(Math.random() * 0.5 + 0.05); // 50ms–550ms
}

// ── Signup ────────────────────────────────────────────────────────────────────
function doSignup() {
  const phone    = uniquePhone('7');
  const username = `su_vu${__VU}_i${__ITER}`;

  const start = Date.now();
  const res = http.post(
    `${BASE_URL}/users/signup`,
    JSON.stringify({
      username, phone,
      email: `${username}@loadtest.com`,
      password: 'Load@Test99',
    }),
    { headers: { 'Content-Type': 'application/json' }, timeout: '10s' }
  );
  signupP95.add(Date.now() - start);

  const ok = check(res, {
    'signup: no 500': (r) => r.status !== 500,
    'signup: has body': (r) => r.body && r.body.length > 0,
  });

  if (res.status === 208 && res.json('message') && res.json('message').includes('rate')) {
    rl_blocked.add(1);
  }
  if (res.status === 500) totalErrors.add(1);

  overallRate.add(res.status !== 500 ? 1 : 0);
}

// ── Get Token ─────────────────────────────────────────────────────────────────
function doToken() {
  const phone    = uniquePhone('6');
  const username = `tu_vu${__VU}_i${__ITER}`;

  const start = Date.now();
  const res   = http.post(
    `${BASE_URL}/users/token/${username}/${phone}`,
    null,
    { timeout: '5s' }
  );
  tokenP95.add(Date.now() - start);

  check(res, {
    'token: status 202 or 208': (r) => r.status === 202 || r.status === 208,
    'token: no 500':            (r) => r.status !== 500,
  });

  if (res.status === 202) {
    // Cache the token for this VU's /login calls
    tokenCache[__VU] = res.json('message');
  } else if (res.status === 208 && res.json('message') && res.json('message').includes('rate')) {
    rl_blocked.add(1);
  }
  if (res.status === 500) totalErrors.add(1);

  overallRate.add(res.status !== 500 ? 1 : 0);
}

// ── Login (validate token) ────────────────────────────────────────────────────
function doLogin() {
  const phone = uniquePhone('5');

  // Use this VU's cached token, or a test token if none cached yet
  const token = tokenCache[__VU] || 'no_token_yet';

  const start = Date.now();
  const res   = http.post(
    `${BASE_URL}/users/login/${token}/${phone}`,
    null,
    { timeout: '5s' }
  );
  loginP95.add(Date.now() - start);

  check(res, {
    'login: status 202 or 208': (r) => r.status === 202 || r.status === 208,
    'login: no 500':            (r) => r.status !== 500,
  });

  if (res.status === 208 && res.json('message') && res.json('message').includes('rate')) {
    rl_blocked.add(1);
  }
  if (res.status === 500) totalErrors.add(1);

  overallRate.add(res.status !== 500 ? 1 : 0);
}

// ── Summary with interpretation ───────────────────────────────────────────────
export function handleSummary(data) {
  const d = data.metrics;

  console.log(`\n====== FULL SYSTEM TEST (${TEST.toUpperCase()}) SUMMARY ======`);
  console.log(`Total requests:    ${d.http_reqs.values.count}`);
  console.log(`Global error rate: ${(d.http_req_failed.values.rate * 100).toFixed(2)}%`);
  console.log(`Rate limit blocks: ${d['rate_limiter_blocks'] ? d['rate_limiter_blocks'].values.count : 0}`);

  if (d['signup_p95']) {
    console.log(`\nSignup  p(95): ${d['signup_p95'].values['p(95)'].toFixed(0)}ms`);
    const sp = d['signup_p95'].values['p(95)'];
    if (sp < 700)        console.log('  → PASS: BCrypt + DB comfortably within budget');
    else if (sp < 1200)  console.log('  → WARN: BCrypt saturating. Reduce concurrency or increase HikariCP pool');
    else                 console.log('  → FAIL: Severe BCrypt/DB contention. System overloaded.');
  }
  if (d['token_p95']) {
    console.log(`Token   p(95): ${d['token_p95'].values['p(95)'].toFixed(0)}ms`);
    const tp = d['token_p95'].values['p(95)'];
    if (tp < 60)        console.log('  → PASS: HMAC-SHA well within budget');
    else if (tp < 150)  console.log('  → WARN: Thread pool pressure. Check tomcat.threads.busy');
    else                console.log('  → FAIL: Thread pool exhausted. JWT not the bottleneck — check infra.');
  }
  if (d['login_p95']) {
    console.log(`Login   p(95): ${d['login_p95'].values['p(95)'].toFixed(0)}ms`);
    const lp = d['login_p95'].values['p(95)'];
    if (lp < 30)        console.log('  → PASS: Token parse is fast as expected');
    else if (lp < 80)   console.log('  → WARN: Minor thread contention on validation');
    else                console.log('  → FAIL: Something is blocking the login path. Check rate limiter contention.');
  }

  console.log('\n-- Phase II Capacity Conclusion --');
  const totalRPS = d.http_reqs.values.rate;
  if (totalRPS < 30)     console.log(`RPS: ${totalRPS.toFixed(1)} — Well within Phase II limits (10-100 users).`);
  else if (totalRPS < 80) console.log(`RPS: ${totalRPS.toFixed(1)} — Approaching Phase II ceiling. Plan Phase III migration.`);
  else                    console.log(`RPS: ${totalRPS.toFixed(1)} — Phase II limits exceeded. This system needs Phase III upgrades now.`);

  return {};
}