import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000,
});

export function getMe(token) {
  return client.get('/users/me', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

export function putMe(payload, token) {
  return client.put('/users/me', payload, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}