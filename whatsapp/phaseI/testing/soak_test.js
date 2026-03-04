import http from "k6/http";
import { sleep, check } from "k6";

export const options = {
  vus: 10,
  duration: "15m",
  thresholds: {
    http_req_duration: ["p(95)<300"],
    http_req_failed: ["rate<0.01"]
  }
};

const BASE_URL = "http://localhost:8080";
const USERNAME = "ansh";

export default function () {

  let tokenRes = http.get(`${BASE_URL}/users/token/${USERNAME}`);

  check(tokenRes, {
    "token issued": (r) => r.status === 202
  });

  let token = tokenRes.body;

  http.post(
    `${BASE_URL}/users/login?username=${USERNAME}&token=${token}`
  );

  sleep(2);
}