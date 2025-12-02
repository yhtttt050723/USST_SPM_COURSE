import request from './request';

export function getMyAttendance() {
  return request.get('/attendance-sessions/my');
}