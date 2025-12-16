import request from './request';

/**
 * 获取所有作业（教师端）
 * 通过 role=TEACHER 参数获取教师视图的作业列表，包含统计信息
 */
export function getTeacherAssignments(courseId) {
  return request.get('/assignments', {
    params: { role: 'TEACHER', courseId }
  });
}

/**
 * 创建作业
 * @param {object} payload - 作业信息
 * @param {string} payload.title - 作业标题（必填）
 * @param {string} payload.description - 作业描述
 * @param {string} payload.type - 作业类型（如：homework, project, exam）
 * @param {string} payload.dueAt - 截止时间（必填，ISO格式）
 * @param {number} payload.totalScore - 总分（默认100）
 * @param {boolean} payload.allowResubmit - 是否允许重新提交（默认false）
 * @param {number} payload.courseId - 课程ID（可选，默认1）
 */
export function createAssignment(payload) {
  return request.post('/assignments', payload);
}

/**
 * 获取作业的所有提交（教师端）
 * @param {number} assignmentId - 作业ID
 * @returns {Promise} 返回提交列表，包含学生信息、成绩、文件等
 */
export function getSubmissions(assignmentId) {
  console.log('调用 getSubmissions API，assignmentId:', assignmentId)
  return request.get(`/assignments/${assignmentId}/submissions`, {
    timeout: 10000 // 增加超时时间
  });
}

/**
 * 批改作业
 * @param {number} assignmentId - 作业ID
 * @param {number} submissionId - 提交ID
 * @param {object} payload - 批改信息
 * @param {number} payload.score - 分数（必填）
 * @param {string} payload.feedback - 评语
 * @param {boolean} payload.released - 是否发布成绩（必填）
 * @param {number} payload.teacherId - 教师ID（必填）
 */
export function gradeSubmission(assignmentId, submissionId, payload) {
  return request.post(`/assignments/${assignmentId}/submissions/${submissionId}/grade`, payload);
}

