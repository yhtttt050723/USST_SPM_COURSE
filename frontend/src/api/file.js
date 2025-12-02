import request from './request';

/**
 * 上传文件
 * @param {FormData} formData - 包含文件和 uploaderId 的 FormData
 */
export function uploadFile(formData) {
  return request.post('/files', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000 // 文件上传需要更长时间
  });
}

/**
 * 下载文件
 * @param {number} fileId - 文件ID
 */
export function downloadFile(fileId) {
  return request.get(`/files/${fileId}`, {
    responseType: 'blob'
  });
}

