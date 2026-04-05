/**
 * k6 Load Test — POST /users/token  and  POST /users/login
 *
 * WHAT THIS TESTS:
 *   - JWT generation throughput (CPU-only, no DB)
 *   - JWT validation throughput (fastest endpoint in the system)
 *   - Rate limiter under token-request burst
 *   - End-to-end session flow: get token → validate token
 *
 * HOW TO RUN:
 *   k6 run test_token_login.js
 *
 * BENCHMARKS TO WATCH:
 *   - /token p(95) should be under 50ms   (pure CPU, no I/O)
 *   - /login p(95) should be under 20ms   (even faster — no generation)
 *   - Rate limiter should kick in at 10 req/30s per phone
 *
 * PHASES:
 *   0-20s   : ramp to 20 VUs   (normal)
 *   20-60s  : hold at 20 VUs   (steady)
 *   60-80s  : ramp to 100 VUs  (stress)
 *   80-120s : hold at 100 VUs  (stress — this is your Phase II ceiling)
 *   120-140s: ramp down
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

// ── Custom metrics ────────────────────────────────────────────────────────────
const tokenSuccess     = new Counter('token_issued');
const tokenFail        = new Counter('token_failed');
const loginValid       = new Counter('login_token_valid');
const loginInvalid     = new Counter('login_token_invalid');
const rateLimitHit     = new Counter('rate_limit_blocks');
const tokenLatency     = new Trend('token_latency_ms', true);
const loginLatency     = new Trend('login_latency_ms', true);
const endToEndLatency  = new Trend('end_to_end_session_ms', true);
const tokenSuccessRate = new Rate('token_success_rate');

// ── Test configuration ────────────────────────────────────────────────────────
export const options = {
  stages: [
    { duration: '20s', target: 20  },
    { duration: '40s', target: 20  },
    { duration: '20s', target: 100 },
    { duration: '40s', target: 100 },
    { duration: '20s', target: 0   },
  ],
  thresholds: {
    // Token generation: under 50ms p95 (no DB, just HMAC)
    'token_latency_ms':       ['p(95)<50'],

    // Login validation: under 30ms p95 (even less work)
    'login_latency_ms':       ['p(95)<30'],

    // End-to-end (get token + validate it): under 80ms p95
    'end_to_end_session_ms':  ['p(95)<80'],

    // Standard
    'http_req_duration':      ['p(95)<100'],
    'http_req_failed':        ['rate<0.02'],
  },
};

const BASE_URL = 'http://localhost:8080';

function uniquePhone() {
  const vu   = String(__VU).padStart(3, '0');
  const iter = String(__ITER).padStart(6, '0');
  return `8${vu}${iter}`;
}

// ── Scenario A: normal flow per VU (get token → validate it) ─────────────────
export default function () {
  const phone    = uniquePhone();
  const username = `tokuser_vu${__VU}_iter${__ITER}`;
  const params   = { timeout: '5s' };

  const sessionStart = Date.now();

  // ── Step 1: get a token ───────────────────────────────────────────────────
  let token = null;

  group('get token', () => {
    const start = Date.now();
    const res   = http.post(`${BASE_URL}/users/token/${username}/${phone}`, null, params);
    tokenLatency.add(Date.now() - start);

    const ok = check(res, {
      'token status 202':     (r) => r.status === 202,
      'response has message': (r) => r.json('message') !== undefined,
      'no 500 on token':      (r) => r.status !== 500,
    });

    if (res.status === 202) {
      token = res.json('message');   // the JWT is returned in `message`
      tokenSuccess.add(1);
      tokenSuccessRate.add(1);
    } else if (res.status === 208) {
      // 208 = rate limit
      rateLimitHit.add(1);
      tokenSuccessRate.add(0);
    } else {
      tokenFail.add(1);
      tokenSuccessRate.add(0);
    }
  });

  // ── Step 2: validate the token ────────────────────────────────────────────
  if (token !== null) {
    group('validate token (login)', () => {
      const start = Date.now();
      const res   = http.post(`${BASE_URL}/users/login/${token}/${phone}`, null, params);
      loginLatency.add(Date.now() - start);

      check(res, {
        'login status 202':    (r) => r.status === 202,
        'token validated true': (r) => r.json('message') && r.json('message').includes('true'),
        'no 500 on login':     (r) => r.status !== 500,
      });

      if (res.status === 202 && res.json('message') && res.json('message').includes('true')) {
        loginValid.add(1);
      } else {
        loginInvalid.add(1);
      }
    });
  }

  endToEndLatency.add(Date.now() - sessionStart);

  sleep(0.05); // minimal sleep — we want to stress this endpoint
}

// ── Scenario B: deliberately trigger rate limiter ─────────────────────────────
// Run this separately to test blocking behavior:
//   k6 run --env SCENARIO=ratelimit test_token_login.js
export function rateLimitScenario() {
  const FIXED_PHONE = '8000000001';  // same phone for every request
  const username    = `spammer_vu${__VU}`;
  const params      = { timeout: '3s' };

  const res = http.post(`${BASE_URL}/users/token/${username}/${FIXED_PHONE}`, null, params);

  check(res, {
    'either allowed or rate-limited':  (r) => r.status === 202 || r.status === 208,
    'never a server error on ratelimit': (r) => r.status !== 500,
  });

  if (res.status === 208) {
    rateLimitHit.add(1);
  }

  // No sleep — hammer the single phone to exhaust the 10/30s limit fast
}

// ── Scenario C: expired token test ───────────────────────────────────────────
// Tests that an expired JWT returns false from validation.
// jwt.expiration=60000 (60s) — so this test deliberately waits 65s after token issuance.
// Run standalone: k6 run --env SCENARIO=expired test_token_login.js
export function expiredTokenScenario() {
  const phone    = uniquePhone();
  const username = `expireduser_vu${__VU}`;
  const params   = { timeout: '5s' };

  // Get token
  const tokenRes = http.post(`${BASE_URL}/users/token/${username}/${phone}`, null, params);
  if (tokenRes.status !== 202) return;
  const token = tokenRes.json('message');

  console.log(`Token issued for ${username}. Sleeping 65s to let it expire...`);
  sleep(65); // wait for jwt.expiration (60s) to pass

  // Try to validate the expired token
  const loginRes = http.post(`${BASE_URL}/users/login/${token}/${phone}`, null, params);
  check(loginRes, {
    'expired token returns false': (r) =>
      r.status === 202 && r.json('message') && r.json('message').includes('false'),
  });

  console.log(`Expired token validation result: ${loginRes.json('message')}`);
}

// ── Summary ───────────────────────────────────────────────────────────────────
export function handleSummary(data) {
  const d = data.metrics;
  console.log('\n====== TOKEN + LOGIN TEST SUMMARY ======');

  if (d['token_latency_ms']) {
    console.log(`Token  avg:   ${d['token_latency_ms'].values.avg.toFixed(1)}ms`);
    console.log(`Token  p(95): ${d['token_latency_ms'].values['p(95)'].toFixed(1)}ms`);
  }
  if (d['login_latency_ms']) {
    console.log(`Login  avg:   ${d['login_latency_ms'].values.avg.toFixed(1)}ms`);
    console.log(`Login  p(95): ${d['login_latency_ms'].values['p(95)'].toFixed(1)}ms`);
  }
  if (d['end_to_end_session_ms']) {
    console.log(`E2E    p(95): ${d['end_to_end_session_ms'].values['p(95)'].toFixed(1)}ms`);
  }

  console.log('\n-- What these numbers mean at Phase II --');
  console.log('Token p(95) < 50ms    : HMAC-SHA is not the bottleneck.');
  console.log('Token p(95) 50-100ms  : Thread pool pressure. Check Tomcat active threads.');
  console.log('Login p(95) < 30ms    : Expected. Validation is read-only + CPU-only.');
  console.log('Rate limit blocks > 0 : Limiter working. Review if legitimate VUs were hit.');

  return {};
}