import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "30s", target: 10 },   // ramp to 10 users
    { duration: "30s", target: 20 },   // ramp to 20
    { duration: "30s", target: 50 },   // ramp to 50
    { duration: "30s", target: 100 },  // ramp to 100
  ],
  thresholds: {
    http_req_duration: ["p(95)<500"], 
    http_req_failed: ["rate<0.01"]
  }
};

const BASE_URL = "http://localhost:8080";
const USERNAME = "ansh";

export default function () {

  let tokenRes = http.get(`${BASE_URL}/users/token/${USERNAME}`);

  check(tokenRes, {
    "token generated": (r) => r.status === 202
  });

  let token = tokenRes.body;

  let loginRes = http.post(
    `${BASE_URL}/users/login?username=${USERNAME}&token=${token}`
  );

  check(loginRes, {
    "login valid": (r) => r.status === 202
  });

  sleep(1);
}