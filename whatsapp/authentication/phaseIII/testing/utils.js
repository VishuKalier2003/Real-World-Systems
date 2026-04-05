import { check } from 'k6';

export function getUser(id) {
  return {
    userID: `user_${id}`,
    phone: `999900${id}`,
    password: "test123",
    deviceID: `device_${id % 10}`
  };
}

export function headers() {
  return { 'Content-Type': 'application/json' };
}

export function validate(res, name) {
  check(res, {
    [`${name} success`]: (r) => r.status === 200 || r.status === 202,
  });
}