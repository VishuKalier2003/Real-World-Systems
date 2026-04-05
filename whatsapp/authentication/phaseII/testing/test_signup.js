/**
 * k6 Load Test — POST /users/signup
 *
 * WHAT THIS TESTS:
 *   - BCrypt hashing throughput under concurrent load
 *   - DB write + 2x read performance
 *   - Rate limiter behaviour per unique phone
 *
 * HOW TO RUN:
 *   k6 run test_signup.js
 *
 * BENCHMARKS TO WATCH:
 *   - http_req_duration p(95) should stay under 600ms (BCrypt adds ~200ms)
 *   - http_req_failed   should stay at 0% for unique phones
 *   - Rate: expect ~10-20 RPS max before BCrypt becomes the ceiling
 *
 * PHASES:
 *   0-30s  : ramp up to 10 virtual users  (normal load)
 *   30-60s : hold at 10 VUs               (steady state)
 *   60-90s : ramp up to 50 VUs            (stress)
 *   90-120s: hold at 50 VUs               (stress steady state)
 *   120-140s: ramp down to 0              (cooldown)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ── Custom metrics ────────────────────────────────────────────────────────────
const signupSuccess    = new Counter('signup_success');
const signupFail       = new Counter('signup_failure');
const signupDuplicate  = new Counter('signup_duplicate');   // 208 MULTI_STATUS
const signupRateLimited = new Counter('signup_rate_limited'); // 208 LOOP_DETECTED
const signupLatency    = new Trend('signup_latency_ms', true);
const successRate      = new Rate('signup_success_rate');

// ── Test configuration ────────────────────────────────────────────────────────
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // ramp to 10 VUs
    { duration: '30s', target: 10 },   // hold — observe steady state
    { duration: '30s', target: 50 },   // ramp to 50 VUs — stress test
    { duration: '30s', target: 50 },   // hold under stress
    { duration: '20s', target: 0  },   // ramp down
  ],
  thresholds: {
    // FAIL the test if p(95) latency exceeds 800ms
    // BCrypt alone is ~200ms, so 800ms gives 600ms for network + DB
    'signup_latency_ms': ['p(95)<800'],

    // FAIL if more than 5% of requests fail (exclude duplicates & rate-limits)
    'signup_success_rate': ['rate>0.95'],

    // Standard k6 thresholds
    'http_req_duration': ['p(95)<900'],
    'http_req_failed':   ['rate<0.05'],
  },
};

const BASE_URL = 'http://localhost:8080';

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Generates a unique phone number per VU + iteration.
 * Using __VU (virtual user ID) and __ITER (iteration count) from k6
 * ensures each signup hits a brand-new user → no MULTI_STATUS collisions.
 */
function uniquePhone() {
  // Format: 9VUVU_ITER — 10 digits
  const vu   = String(__VU).padStart(3, '0');
  const iter = String(__ITER).padStart(6, '0');
  return `9${vu}${iter}`;
}

function uniqueUser() {
  return `user_vu${__VU}_iter${__ITER}`;
}

// ── Main test function ────────────────────────────────────────────────────────
export default function () {
  const phone    = uniquePhone();
  const username = uniqueUser();

  const payload = JSON.stringify({
    username: username,
    phone:    phone,
    email:    `${username}@test.com`,
    password: 'TestPass@123',
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
    timeout: '10s',
  };

  const start = Date.now();
  const res   = http.post(`${BASE_URL}/users/signup`, payload, params);
  const elapsed = Date.now() - start;

  signupLatency.add(elapsed);

  const status = res.status;
  const body   = res.json();  // { message: string, flag: boolean }

  // ── Status-based checks ───────────────────────────────────────────────────
  const isAccepted     = status === 202;
  const isDuplicate    = status === 208;
  const isRateLimited  = status === 208 && body.message && body.message.includes('rate limiter');
  const isServerError  = status === 500;

  check(res, {
    'status is 202 or 208':       (r) => r.status === 202 || r.status === 208,
    'response has message field': (r) => r.json('message') !== undefined,
    'response has flag field':    (r) => r.json('flag') !== undefined,
    'no 500 errors':              (r) => r.status !== 500,
  });

  // Track custom counters
  if (isAccepted)    { signupSuccess.add(1);    successRate.add(1); }
  if (isDuplicate && !isRateLimited)  { signupDuplicate.add(1);  successRate.add(0); }
  if (isRateLimited) { signupRateLimited.add(1); successRate.add(0); }
  if (isServerError) { signupFail.add(1);        successRate.add(0); }

  // Small sleep to avoid artificially hammering at max CPU
  // Set to 0 if you want to find the true throughput ceiling
  sleep(0.1);
}

// ── Teardown: print summary ───────────────────────────────────────────────────
export function handleSummary(data) {
  console.log('\n====== SIGNUP TEST SUMMARY ======');
  console.log(`Total requests:       ${data.metrics.http_reqs.values.count}`);
  console.log(`Avg latency:          ${data.metrics.http_req_duration.values.avg.toFixed(1)}ms`);
  console.log(`p(95) latency:        ${data.metrics.http_req_duration.values['p(95)'].toFixed(1)}ms`);
  console.log(`p(99) latency:        ${data.metrics.http_req_duration.values['p(99)'].toFixed(1)}ms`);
  console.log(`Failed requests:      ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);

  // Expected Phase II benchmarks:
  // p(95) < 600ms  → BCrypt ceiling healthy
  // p(95) 600-900ms → BCrypt saturating threads, reduce concurrency or tune pool
  // p(95) > 900ms  → DB connection pool exhausted or CPU pinned on BCrypt

  return {
    'stdout': JSON.stringify(data, null, 2),
  };
}