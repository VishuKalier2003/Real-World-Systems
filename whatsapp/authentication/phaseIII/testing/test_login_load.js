import http from 'k6/http';
import { getUser, headers, validate } from './utils.js';

export let options = {
  stages: [
    { duration: '30s', target: 100 },
    { duration: '30s', target: 200 },
    { duration: '30s', target: 300 },
  ],
};

export default function () {
  const id = Math.floor(Math.random() * 1000);
  const user = getUser(id);

  const res = http.post('http://localhost:8080/p3/login',
    JSON.stringify(user),
    { headers: headers() }
  );

  validate(res, "login");
}