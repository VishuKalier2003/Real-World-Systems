import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    vus: 10,
    duration: '30s'
};

export default function () {
    let res = http.get('http://localhost:8080/fetch/1?page=0&size=10');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}