import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 200,
  duration: '1m',
};

export default function () {
  let payload = JSON.stringify({
    userID: "user1",
    phone: "9999999999",
    password: "pass",
    deviceID: "device1"
  });

  let res = http.post('http://localhost:8080/p3/login', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'login success': (r) => r.status === 202 });
}