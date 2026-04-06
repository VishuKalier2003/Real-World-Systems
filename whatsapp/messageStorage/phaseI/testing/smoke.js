import http from 'k6/http';
import { check } from 'k6';

export let options = { vus: 1, iterations: 1 };

export default function () {
    let res = http.get('http://localhost:8080/user/999999');
    check(res, { 'status is 200 or 404': (r) => r.status === 200 || r.status === 404 });
}