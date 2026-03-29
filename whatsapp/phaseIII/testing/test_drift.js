import http from 'k6/http';
import { randomUser, headers } from './utils.js';

export let options = {
  stages: [
    { duration: '1m', target: 100 },
    { duration: '1m', target: 200 },
    { duration: '1m', target: 400 },
    { duration: '1m', target: 600 },
  ],
};

export default function () {
  const user = randomUser();

  http.post('http://localhost:8080/p3/login',
    JSON.stringify(user),
    { headers: headers() }
  );
}