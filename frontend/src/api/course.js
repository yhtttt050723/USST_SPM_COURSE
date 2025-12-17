import request from './request';

// 课程列表（当前用户可访问的）
export function listMyCourses() {
  return request.get('/courses');
}

// 课程详情
export function getCourse(id) {
  return request.get(`/courses/${id}`);
}

// 教师生成邀请码
export function createInvite(courseId, payload) {
  return request.post(`/courses/${courseId}/invites`, payload);
}

// 教师失效邀请码
export function revokeInvite(courseId, code) {
  return request.delete(`/courses/${courseId}/invites/${code}`);
}

// 加入课程（输入邀请码）
export function joinCourseByCode(code) {
  return request.post('/courses/join', { code });
}

// 公告相关（保留旧接口）
export function getAnnouncements() {
  return request.get('/course/announcements');
}

export function createAnnouncement(announcement) {
  return request.post('/course/announcements', announcement);
}
