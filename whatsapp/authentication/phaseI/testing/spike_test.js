import http from "k6/http";
import { sleep, check } from "k6";

export const options = {
  stages: [
    { duration: "10s", target: 10 },   // normal load
    { duration: "5s", target: 100 },   // sudden spike
    { duration: "20s", target: 10 },   // recovery
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"]
  }
};

const BASE_URL = "http://localhost:8080";
const USERNAME = "ansh";

export default function () {

  let tokenRes = http.get(`${BASE_URL}/users/token/${USERNAME}`);

  check(tokenRes, {
    "token received": (r) => r.status === 202
  });

  let token = tokenRes.body;

  http.post(
    `${BASE_URL}/users/login?username=${USERNAME}&token=${token}`
  );

  sleep(1);
}