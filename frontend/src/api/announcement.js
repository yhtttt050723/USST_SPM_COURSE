import request from './request'

/**
 * 获取公告列表
 * @param {number|null} courseId - 课程ID，null表示默认课程，0表示全校公告
 * @param {boolean} includeGlobal - 是否包含全校公告（默认true）
 */
export function getAnnouncements(courseId = null, includeGlobal = true) {
  const params = {}
  if (courseId !== null) {
    params.courseId = courseId
  }
  if (includeGlobal !== undefined) {
    params.includeGlobal = includeGlobal
  }
  return request.get('/announcements', { params })
}

/**
 * 创建公告（仅教师）
 * @param {Object} announcement - { courseId, title, content, isPinned }
 */
export function createAnnouncement(announcement) {
  return request.post('/announcements', announcement)
}

/**
 * 获取公告详情
 * @param {number} id - 公告ID
 */
export function getAnnouncementById(id) {
  return request.get(`/announcements/${id}`)
}

/**
 * 更新公告（仅教师）
 * @param {number} id - 公告ID
 * @param {Object} announcement - { title, content, isPinned }
 */
export function updateAnnouncement(id, announcement) {
  return request.put(`/announcements/${id}`, announcement)
}

/**
 * 删除公告（仅教师）
 * @param {number} id - 公告ID
 */
export function deleteAnnouncement(id) {
  return request.delete(`/announcements/${id}`)
}

