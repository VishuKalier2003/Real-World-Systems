import http from 'k6/http';

export let options = {
  vus: 100,
  duration: '30s',
};

export default function () {
  http.post('http://localhost:8080/p3/login',
    JSON.stringify({
      userID: "fake",
      phone: "000",
      password: "wrong",
      deviceID: "device"
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );
}