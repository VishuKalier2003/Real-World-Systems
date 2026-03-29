import http from 'k6/http';

export let options = {
  stages: [
  { duration: '10s', target: 100 },
  { duration: '10s', target: 500 },
  { duration: '20s', target: 100 },
],
};

export default function () {
  http.get('http://localhost:8080/whoami');
}