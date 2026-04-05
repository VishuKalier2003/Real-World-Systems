import http from "k6/http";
import { sleep, check } from "k6";

export const options = {
  vus: 10,
  duration: "60s"
};

export default function () {

  const username = "ansh";

  let tokenRes = http.get(`http://localhost:8080/users/token/${username}`);

  check(tokenRes, {
    "token generated": (r) => r.status === 202
  });

  const token = tokenRes.body;

  let loginRes = http.post(
    `http://localhost:8080/users/login?username=${username}&token=${token}`
  );

  check(loginRes, {
    "login valid": (r) => r.body === "true"
  });

  sleep(1);
}