import request from './request';

/**
 * 获取讨论帖列表
 * @param {Object} params - 查询参数
 * @param {number} params.courseId - 课程ID
 * @param {boolean} params.includeDeleted - 是否包含已删除的（仅教师）
 */
export function getDiscussions(params = {}) {
  return request.get('/discussions', { params });
}

/**
 * 教师端：获取所有讨论帖（包括已删除的）
 */
export function getAllDiscussionsForAdmin(courseId = 1) {
  return request.get('/discussions/admin/all', {
    params: { courseId }
  });
}

/**
 * 获取讨论帖详情
 * @param {number} id - 讨论帖ID
 * @param {boolean} includeDeleted - 是否包含已删除的（仅教师）
 */
export function getDiscussionById(id, includeDeleted = false) {
  return request.get(`/discussions/${id}`, {
    params: { includeDeleted }
  });
}

export function getDiscussionReplies(id, params = {}) {
  return request.get(`/discussions/${id}/replies`, { params });
}

/**
 * 创建讨论帖
 * @param {Object} data - 讨论帖数据
 * @param {number} data.courseId - 课程ID
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export function createDiscussion(data) {
  return request.post('/discussions', data);
}

/**
 * 更新讨论帖
 * @param {number} id - 讨论帖ID
 * @param {Object} data - 更新数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export function updateDiscussion(id, data) {
  return request.put(`/discussions/${id}`, data);
}

/**
 * 删除讨论帖
 * @param {number} id - 讨论帖ID
 */
export function deleteDiscussion(id) {
  return request.delete(`/discussions/${id}`);
}

/**
 * 置顶/取消置顶讨论帖（仅教师）
 * @param {number} id - 讨论帖ID
 * @param {boolean} pin - 是否置顶
 */
export function pinDiscussion(id) {
  return request.post(`/discussions/${id}/pin`);
}

export function unpinDiscussion(id) {
  return request.post(`/discussions/${id}/unpin`);
}

export function closeDiscussion(id) {
  return request.post(`/discussions/${id}/close`);
}

export function openDiscussion(id) {
  return request.post(`/discussions/${id}/open`);
}

/**
 * 创建评论
 * @param {number} discussionId - 讨论帖ID
 * @param {Object} data - 评论数据
 * @param {string} data.content - 评论内容
 * @param {number} data.parentId - 父评论ID（可选，用于回复）
 */
export function createComment(discussionId, data) {
  return request.post(`/discussions/${discussionId}/comments`, data);
}

/**
 * 更新评论
 * @param {number} discussionId - 讨论帖ID
 * @param {number} commentId - 评论ID
 * @param {Object} data - 更新数据
 * @param {string} data.content - 评论内容
 */
export function updateComment(discussionId, commentId, data) {
  return request.put(`/discussions/${discussionId}/comments/${commentId}`, data);
}

/**
 * 删除评论
 * @param {number} discussionId - 讨论帖ID
 * @param {number} commentId - 评论ID
 */
export function deleteComment(discussionId, commentId) {
  return request.delete(`/discussions/${discussionId}/comments/${commentId}`);
}

