import http from 'k6/http';
import { headers, getUser } from './utils.js';

export let options = {
  vus: 10,
  iterations: 1000, // create 1000 users
};

export default function () {
  const id = __ITER;
  const user = getUser(id);

  http.post('http://localhost:8080/p3/signup',
    JSON.stringify({
      username: user.userID,
      phone: user.phone,
      password: user.password
    }),
    { headers: headers() }
  );
}