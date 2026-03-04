import http from "k6/http";
import { sleep, check } from "k6";

export const options = {
    vus: 10,                // 10 virtual users
    duration: "30s",        // run for 30 seconds
};

export default function () {

    const username = "ansh";

    let res = http.get(`http://localhost:8080/users/token/${username}`);

    check(res, {
        "token received": (r) => r.status === 202
    });
}