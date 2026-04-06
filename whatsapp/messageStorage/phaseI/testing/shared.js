import http from 'k6/http';

export const BASE_URL = 'http://localhost:8080';

export function createUser(i) {
    return http.post(`${BASE_URL}/add/user`, JSON.stringify({
        username: `user${i}`,
        phone: `99999${i}`,
        status: "active"
    }), { headers: { 'Content-Type': 'application/json' } });
}

export function createConversation(userIds) {
    return http.post(`${BASE_URL}/start/conversation`, JSON.stringify({
        type: "single",
        participants: userIds
    }), { headers: { 'Content-Type': 'application/json' } });
}

export function sendMessage(conversationId, senderId) {
    return http.post(`${BASE_URL}/send/message`, JSON.stringify({
        conversationId,
        senderId,
        message: "Hello from k6",
        type: "text"
    }), { headers: { 'Content-Type': 'application/json' } });
}