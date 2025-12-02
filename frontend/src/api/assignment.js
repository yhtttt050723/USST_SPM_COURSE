import request from './request';

/**
 * 获取作业列表
 * @param {string} status - 状态筛选: all, progress, submitted, graded
 * @param {number} studentId - 学生ID
 * @param {string} role - 用户角色: STUDENT, TEACHER
 */
export function getAssignments(status = 'all', studentId, role) {
  return request.get('/assignments', {
    params: { status, studentId, role }
  });
}

/**
 * 获取作业详情
 * @param {number} id - 作业ID
 * @param {number} studentId - 学生ID（可选）
 */
export function getAssignmentById(id, studentId) {
  return request.get(`/assignments/${id}`, {
    params: studentId ? { studentId } : {}
  });
}

/**
 * 提交作业
 * @param {number} id - 作业ID
 * @param {object} payload - { content: string, studentId: number, attachmentIds: number[] }
 */
export function submitAssignment(id, payload) {
  return request.post(`/assignments/${id}/submissions`, payload);
}

/**
 * 查看我的提交
 * @param {number} id - 作业ID
 * @param {number} studentId - 学生ID
 */
export function getMySubmission(id, studentId) {
  return request.get(`/assignments/${id}/submissions/me`, {
    params: { studentId }
  });
}

/**
 * 教师批改作业
 * @param {number} id - 作业ID
 * @param {number} submissionId - 提交ID
 * @param {object} payload - { score: number, feedback: string, released: boolean, teacherId: number }
 */
export function gradeSubmission(id, submissionId, payload) {
  return request.post(`/assignments/${id}/submissions/${submissionId}/grade`, payload);
}

/**
 * 获取我的所有成绩
 * @param {number} studentId - 学生ID
 */
export function getMyGrades(studentId) {
  return request.get('/assignments/grades/me', {
    params: { studentId }
  });
}

