import http from 'k6/http';
import { headers } from './utils.js';

export let options = {
  vus: 100,
  duration: '1m',
};

export default function () {
  const deviceID = `device_${Math.floor(Math.random() * 50)}`;

  http.post('http://localhost:8080/p3/login',
    JSON.stringify({
      userID: "user_1",
      phone: "9999001",
      password: "test123",
      deviceID
    }),
    { headers: headers() }
  );
}