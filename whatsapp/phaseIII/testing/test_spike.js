import http from 'k6/http';
import { getUser, headers } from './utils.js';

export let options = {
  stages: [
    { duration: '10s', target: 50 },
    { duration: '5s', target: 800 },
    { duration: '10s', target: 50 },
  ],
};

export default function () {
  const user = getUser(Math.floor(Math.random() * 1000));

  http.post('http://localhost:8080/p3/login',
    JSON.stringify(user),
    { headers: headers() }
  );
}