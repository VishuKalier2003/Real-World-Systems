import http from 'k6/http';

export let options = {
  vus: 300,
  duration: '10m',
};

export default function () {
  http.get('http://localhost:8080/whoami');
}