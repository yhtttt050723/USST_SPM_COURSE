import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000,
});

export function login(payload) {
  return client.post('/auth/login', payload);
}

export function register(payload) {
  return client.post('/auth/register', payload);
}
