import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000, // 文件上传可能需要更长时间
});

/**
 * 上传文件
 * @param {FormData} formData - 包含文件和 uploaderId 的 FormData
 */
export function uploadFile(formData) {
  return client.post('/files', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 下载文件
 * @param {number} fileId - 文件ID
 */
export function downloadFile(fileId) {
  return client.get(`/files/${fileId}`, {
    responseType: 'blob'
  });
}

