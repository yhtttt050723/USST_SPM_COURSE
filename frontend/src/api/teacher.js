import http from './http';

/**
 * 获取所有作业（教师端）
 */
export function getAssignments() {
  return http.get('/assignments', {
    params: { role: 'TEACHER' }
  });
}

/**
 * 创建作业
 * @param {object} payload - { title, description, dueAt, totalScore, allowResubmit }
 */
export function createAssignment(payload) {
  return http.post('/assignments', payload);
}

/**
 * 获取作业的所有提交
 * @param {number} assignmentId - 作业ID
 */
export function getSubmissions(assignmentId) {
  console.log('调用 getSubmissions API，assignmentId:', assignmentId)
  return http.get(`/assignments/${assignmentId}/submissions`, {
    timeout: 10000 // 增加超时时间
  });
}

/**
 * 批改作业
 * @param {number} assignmentId - 作业ID
 * @param {number} submissionId - 提交ID
 * @param {object} payload - { score, feedback, released, teacherId }
 */
export function gradeSubmission(assignmentId, submissionId, payload) {
  return http.post(`/assignments/${assignmentId}/submissions/${submissionId}/grade`, payload);
}

