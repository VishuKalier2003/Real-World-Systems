import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 2 },
        { duration: '10s', target: 10 },
        { duration: '5s', target: 50 },
        { duration: '10s', target: 2 },
    ],
};

export default function () {
    http.get('http://localhost:8080/fetch/1?page=0&size=10');
    sleep(1);
}