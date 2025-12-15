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
    const url = config.url || '';
    const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/register');
    
    // 登录/注册接口不需要token，直接放行
    if (isAuthRequest) {
      return config;
    }
    
    // 其他接口需要token
    // 优先从localStorage读取（与useUserStore保持一致），如果没有则从sessionStorage读取
    let userStr = localStorage.getItem('spm-user');
    if (!userStr) {
      userStr = sessionStorage.getItem('spm-user');
    }
    
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        // 如果有 token，添加到请求头
        if (user.token) {
          config.headers.Authorization = `Bearer ${user.token}`;
        } else {
          // 调试信息：如果token不存在，输出警告
          console.warn('用户信息中缺少token字段，当前用户信息:', user);
        }
      } catch (error) {
        console.error('解析用户信息失败:', error);
      }
    } else {
      // 调试信息：如果没有用户信息（非登录/注册接口）
      console.warn('未找到用户信息（已检查localStorage和sessionStorage），请求可能被拦截:', url);
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
    const url = response.config?.url || '';
    const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/register');
    
    // 调试日志
    if (isAuthRequest) {
      console.log('登录/注册响应:', { url, status: response.status, data });
    }
    
    if (data && typeof data.code !== 'undefined') {
      // 标准格式：{code, message, data, timestamp, traceId}
      if (data.code === 200 || data.code === 201) {
        // 对于登录/注册，返回data字段（LoginResponse对象）
        if (isAuthRequest && data.data) {
          return data.data;
        }
        return data; // 返回完整响应对象 {code, message, data, timestamp, traceId}
      } else {
        // 业务错误
        const errorMsg = data.message || '请求失败';
        // 只在非登录/注册接口显示ElMessage，避免与Login.vue的error消息重复
        if (!isAuthRequest) {
          ElMessage.error(errorMsg);
        }
        return Promise.reject({
          message: errorMsg,
          code: data.code,
          traceId: data.traceId,
          response: response
        });
      }
    }
    
    // 非标准格式：直接返回响应体数据（如 ResponseEntity<LoginResponse> 直接返回 LoginResponse）
    // 对于登录/注册接口，这应该是LoginResponse对象
    if (isAuthRequest) {
      console.log('登录/注册响应（非标准格式）:', data);
    }
    return data;
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
          sessionStorage.removeItem('spm-user');
          setTimeout(() => {
            window.location.href = '/login';
          }, 1500);
          break;
        case 403:
          // 检查是否是 token 过期
          if (data?.data && typeof data.data === 'string' && data.data.includes('JWT expired')) {
            message = 'Token 已过期，请重新登录';
            sessionStorage.removeItem('spm-user');
            setTimeout(() => {
              window.location.href = '/login';
            }, 1500);
          } else {
            message = data?.message || '权限不足';
          }
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          // Spring Boot默认错误格式：{timestamp, status, error, message, path}
          message = data?.message || data?.error || '服务器内部错误';
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
    
    // 只在非登录/注册接口显示ElMessage，避免与Login.vue的error消息重复
    const url = error.config?.url || '';
    if (!url.includes('/auth/login') && !url.includes('/auth/register')) {
      ElMessage.error(message);
    }
    
    return Promise.reject({
      message,
      status: error.response?.status,
      data: error.response?.data,
      traceId: error.response?.data?.traceId
    });
  }
);

export default request;
