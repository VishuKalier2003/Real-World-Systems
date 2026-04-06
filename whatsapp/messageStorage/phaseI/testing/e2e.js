import { createUser, createConversation, sendMessage } from './shared.js';
import { sleep } from 'k6';

export let options = {
    vus: 5,
    iterations: 10,
};

export default function () {

    let u1 = createUser(__VU);
    let u2 = createUser(__VU + 100);

    let userIds = [1, 2]; // adjust if dynamic parsing needed

    let convo = createConversation(userIds);

    sendMessage(1, 1);

    sleep(1);
}