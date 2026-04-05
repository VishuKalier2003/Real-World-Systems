import http from 'k6/http';

export let options = {
  vus: 200,
  duration: '2m',
};

export default function () {
  let loginPayload = JSON.stringify({
    userID: "user1",
    phone: "9999999999",
    password: "pass",
    deviceID: "device1"
  });

  let loginRes = http.post('http://localhost:8080/p3/login', loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  let body = JSON.parse(loginRes.body || "{}");

  if (!body.token) return;

  let tokenPayload = JSON.stringify({
    userID: "user1",
    deviceID: "device1",
    token: body.token
  });

  http.post('http://localhost:8080/p3/token', tokenPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
}