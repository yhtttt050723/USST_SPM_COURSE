import axios from 'axios'

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080/api'

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

http.interceptors.request.use(
  (config) => {
    const cachedUser = sessionStorage.getItem('spm-user')
    if (cachedUser) {
      try {
        const parsed = JSON.parse(cachedUser)
        if (parsed?.token) {
          config.headers = config.headers || {}
          config.headers.Authorization = `Bearer ${parsed.token}`
        }
      } catch (error) {
        console.warn('解析本地用户信息失败', error)
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

export default http

