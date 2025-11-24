import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000,
});

/**
 * 获取所有作业（教师端）
 */
export function getAssignments() {
  return client.get('/assignments', {
    params: { role: 'TEACHER' }
  });
}

/**
 * 创建作业
 * @param {object} payload - { title, description, dueAt, totalScore, allowResubmit }
 */
export function createAssignment(payload) {
  return client.post('/assignments', payload);
}

/**
 * 获取作业的所有提交
 * @param {number} assignmentId - 作业ID
 */
export function getSubmissions(assignmentId) {
  return client.get(`/assignments/${assignmentId}/submissions`);
}

/**
 * 批改作业
 * @param {number} assignmentId - 作业ID
 * @param {number} submissionId - 提交ID
 * @param {object} payload - { score, feedback, released, teacherId }
 */
export function gradeSubmission(assignmentId, submissionId, payload) {
  return client.post(`/assignments/${assignmentId}/submissions/${submissionId}/grade`, payload);
}

