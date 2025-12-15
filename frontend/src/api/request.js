import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/useUserStore';

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000,
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取用户信息
    const userStr = localStorage.getItem('spm-user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        // 如果有 token，添加到请求头
        if (user.token) {
          config.headers.Authorization = `Bearer ${user.token}`;
        }
      } catch (error) {
        console.error('解析用户信息失败:', error);
      }
    }
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理标准响应格式 {code, message, data, timestamp, traceId}
request.interceptors.response.use(
  response => {
    const { data } = response;
    
    if (data && typeof data.code !== 'undefined') {
      // 成功
      if (data.code === 200 || data.code === 201) {
        return data; // 返回完整响应对象 {code, message, data, timestamp, traceId}
      } else {
        // 业务错误
        const errorMsg = data.message || '请求失败';
        ElMessage.error(errorMsg);
        return Promise.reject({
          message: errorMsg,
          code: data.code,
          traceId: data.traceId
        });
      }
    }
    
    return response;
  },
  error => {
    // HTTP 错误处理
    let message = '网络请求失败';
    
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      
      switch (status) {
        case 400:
          message = data?.message || '请求参数错误';
          break;
        case 401:
          message = '未授权，请重新登录';
          localStorage.removeItem('spm-user');
          setTimeout(() => {
            window.location.href = '/login';
          }, 1500);
          break;
        case 403:
          // 检查是否是 token 过期
          if (data?.data && typeof data.data === 'string' && data.data.includes('JWT expired')) {
            message = 'Token 已过期，请重新登录';
            localStorage.removeItem('spm-user');
            setTimeout(() => {
              window.location.href = '/login';
            }, 1500);
          } else {
            message = data?.message || '请重新登录';
          }
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = data?.message || '服务器内部错误';
          break;
        case 502:
          message = '网关错误';
          break;
        case 503:
          message = '服务不可用';
          break;
        default:
          message = data?.message || `请求失败 (${status})`;
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络';
    } else {
      message = error.message || '请求配置错误';
    }
    
    ElMessage.error(message);
    
    return Promise.reject({
      message,
      status: error.response?.status,
      data: error.response?.data,
      traceId: error.response?.data?.traceId
    });
  }
);

export default request;
