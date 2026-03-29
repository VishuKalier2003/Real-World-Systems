import http from 'k6/http';

export let options = {
  vus: 100,
  duration: '30s',
};

export default function () {
  let payload = JSON.stringify({
    userID: "user1",
    phone: "9999999999",
    password: "wrong",
    deviceID: "device1"
  });

  http.post('http://localhost:8080/p3/login', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
}