import http from 'k6/http';
import { getUser, headers } from './utils.js';

export let options = {
  vus: 200,
  duration: '1m',
};

export default function () {
  const user = getUser(Math.floor(Math.random() * 1000));

  const login = http.post('http://localhost:8080/p3/login',
    JSON.stringify(user),
    { headers: headers() }
  );

  if (login.status !== 202) return;

  const body = JSON.parse(login.body);

  for (let i = 0; i < 3; i++) {
    http.post('http://localhost:8080/p3/token',
      JSON.stringify({
        userID: user.userID,
        deviceID: user.deviceID,
        token: body.token
      }),
      { headers: headers() }
    );
  }
}