import http from "k6/http";
import { sleep } from "k6";

export const options = {
  vus: 5,
  iterations: 50,
};

export default function () {

  const payload = JSON.stringify({
    username: "user" + __ITER,
    email: `user${__ITER}@mail.com`,
    phone: `999999${__ITER}`,
    password: "password123"
  });

  http.post("http://localhost:8080/users/register", payload, {
    headers: { "Content-Type": "application/json" }
  });

  sleep(1);
}