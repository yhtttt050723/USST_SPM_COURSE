import request from './request';

/**
 * 获取公告列表
 * @returns {Promise} 返回 {code, message, data, timestamp, traceId}
 */
export function getAnnouncements() {
  return request.get('/course/announcements');
}

/**
 * 发布公告（教师）
 * @param {Object} announcement - 公告信息
 * @param {string} announcement.title - 公告标题（必填）
 * @param {string} announcement.content - 公告内容（必填）
 * @param {string} [announcement.visibleFrom] - 可见开始时间（可选，ISO 8601 格式）
 * @param {string} [announcement.visibleTo] - 可见结束时间（可选，ISO 8601 格式）
 * @returns {Promise} 返回 {code: 201, message, data, timestamp, traceId}
 */
export function createAnnouncement(announcement) {
  return request.post('/course/announcements', announcement);
}
