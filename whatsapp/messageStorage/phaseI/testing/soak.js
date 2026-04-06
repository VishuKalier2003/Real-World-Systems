import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
    vus: 5,
    duration: '2m',
};

export default function () {
    http.get('http://localhost:8080/fetch/1?page=0&size=10');
    sleep(1);
}