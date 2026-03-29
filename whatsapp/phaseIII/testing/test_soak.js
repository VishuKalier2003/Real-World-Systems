import http from 'k6/http';
import { sleep, check } from 'k6';
import { getUser, headers } from './utils.js';

export let options = {
  vus: 200,                 // steady concurrent users
  duration: '10m',          // long-running stability test
  thresholds: {
    http_req_duration: ['p(95)<300', 'p(99)<500'], // latency SLO
    http_req_failed: ['rate<0.02'],                // <2% errors
  },
};

export default function () {
  // pick a random existing user
  const id = Math.floor(Math.random() * 1000);
  const user = getUser(id);

  // --- STEP 1: LOGIN ---
  const loginRes = http.post(
    'http://localhost:8080/p3/login',
    JSON.stringify(user),
    { headers: headers() }
  );

  check(loginRes, {
    'login success': (r) => r.status === 200 || r.status === 202,
  });

  if (loginRes.status !== 202) {
    sleep(1);
    return;
  }

  const body = JSON.parse(loginRes.body);

  // --- STEP 2: TOKEN VALIDATION / REFRESH ---
  const tokenRes = http.post(
    'http://localhost:8080/p3/token',
    JSON.stringify({
      userID: user.userID,
      deviceID: user.deviceID,
      token: body.token,
    }),
    { headers: headers() }
  );

  check(tokenRes, {
    'token success': (r) => r.status === 200 || r.status === 202,
  });

  // --- STEP 3: THINK TIME (VERY IMPORTANT) ---
  sleep(Math.random() * 2 + 1); // 1–3 sec realistic user delay
}