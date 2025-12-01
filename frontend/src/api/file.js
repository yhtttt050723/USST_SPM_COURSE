import http from './http';

/**
 * 上传文件
 * @param {FormData} formData - 包含文件和 uploaderId 的 FormData
 */
export function uploadFile(formData) {
  return http.post('/files', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000
  });
}

/**
 * 下载文件
 * @param {number} fileId - 文件ID
 */
export function downloadFile(fileId) {
  return http.get(`/files/${fileId}`, {
    responseType: 'blob'
  });
}

