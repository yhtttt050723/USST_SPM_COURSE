import request from './request';

// 学生：我的签到记录
export function getMyAttendance(params) {
  return request.get('/attendance/sessions/my', { params });
}

// 教师：发布签到
export function createAttendanceSession(data) {
  return request.post('/attendance/sessions', data);
}

// 教师：结束签到
export function endAttendanceSession(id) {
  return request.post(`/attendance/sessions/${id}/end`);
}

// 教师：分页查询签到任务
export function listAttendanceSessions(params) {
  return request.get('/attendance/sessions', { params });
}

// 教师：查看签到记录
export function listAttendanceRecords(sessionId, params) {
  return request.get(`/attendance/sessions/${sessionId}/records`, { params });
}

// 教师：查看签到统计
export function getAttendanceStats(sessionId) {
  return request.get(`/attendance/sessions/${sessionId}/stats`);
}

// 学生：签到
export function checkinAttendance(data) {
  return request.post('/attendance/checkin', data);
}