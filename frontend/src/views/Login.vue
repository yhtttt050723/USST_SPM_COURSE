<template>
  <div class="login-page">
    <h2>项目管理课程账号</h2>

    <div class="switcher">
      <button type="button" :class="{ active: mode === 'login' }" @click="() => switchMode('login')">
        登录
      </button>
      <button type="button" :class="{ active: mode === 'register' }" @click="() => switchMode('register')">
        注册
      </button>
    </div>

    <form @submit.prevent="handleSubmit">
      <label>学号</label>
      <input v-model="form.studentNo" placeholder="请输入学号" required />

      <label v-if="mode === 'register'">姓名</label>
      <input
        v-if="mode === 'register'"
        v-model="form.name"
        placeholder="请输入姓名"
        required
      />

      <label v-if="mode === 'register'">邀请码 / 课程编码</label>
      <input
        v-if="mode === 'register'"
        v-model="form.inviteCode"
        placeholder="请输入教师提供的邀请码"
        required
      />

      <label>密码</label>
      <input
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        required
      />

      <button type="submit" :disabled="loading">
        {{ loading ? '处理中...' : mode === 'login' ? '登录' : '注册' }}
      </button>
    </form>

    <p class="tip">
      登陆/注册成功后将进入系统主页，当前使用本地账号体系。
    </p>
    <p class="success" v-if="message">{{ message }}</p>
    <p class="error" v-if="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const { setUser } = useUserStore()
const form = reactive({
  studentNo: '',
  name: '',
  password: '',
  inviteCode: ''
})
const mode = ref('login')
const loading = ref(false)
const message = ref('')
const error = ref('')

const resetNotice = () => {
  message.value = ''
  error.value = ''
}

const switchMode = (nextMode) => {
  if (mode.value === nextMode) return
  mode.value = nextMode
  resetNotice()
}

const handleSubmit = async () => {
  // 清除所有消息
  message.value = ''
  error.value = ''
  loading.value = true
  
  try {
    if (mode.value === 'login') {
      const response = await login({
        studentNo: form.studentNo,
        password: form.password
      })
      
      // 响应拦截器已经处理，直接使用response（已经是LoginResponse对象）
      const userData = response
      
      // 验证响应数据
      if (!userData) {
        throw new Error('登录响应数据格式错误：响应为空')
      }
      
      if (!userData.studentNo) {
        throw new Error('登录响应数据格式错误：缺少学号')
      }
      
      // 确保token存在
      if (!userData.token) {
        throw new Error('登录成功，但未获取到token，请检查后端响应')
      }
      
      console.log('登录成功，Token已获取:', userData.token.substring(0, 20) + '...')
      console.log('用户信息:', { id: userData.id, studentNo: userData.studentNo, name: userData.name, role: userData.role })
      
      // 保存用户信息（必须在跳转前保存）
      setUser(userData)
      console.log('用户信息已保存到sessionStorage')
      
      // 不设置成功消息，直接跳转（避免消息闪烁）
      // message.value = `登录成功：${userData.name} (${userData.studentNo})`
      
      // 立即跳转
      try {
        await router.push('/main')
        console.log('页面跳转成功')
      } catch (routerError) {
        console.error('页面跳转失败:', routerError)
        // 如果跳转失败，显示成功消息
        message.value = `登录成功：${userData.name} (${userData.studentNo})`
      }
      
    } else {
      const response = await register({
        studentNo: form.studentNo,
        name: form.name,
        password: form.password,
        inviteCode: form.inviteCode
      })
      
      // 响应拦截器已经处理，直接使用response
      const userData = response
      
      if (!userData || !userData.studentNo) {
        throw new Error('注册响应数据格式错误')
      }
      
      // 设置成功消息
      message.value = `注册成功：${userData.name} (${userData.studentNo})，请登录`
      
      // 切换到登录模式
      mode.value = 'login'
      form.name = ''
      form.password = ''
      form.inviteCode = ''
    }
  } catch (err) {
    // 登录/注册失败，只显示错误消息
    console.error('登录/注册失败，错误详情:', {
      message: err?.message,
      response: err?.response,
      status: err?.response?.status,
      data: err?.response?.data
    })
    
    // 提取错误消息
    let errorMsg = '操作失败'
    
    if (err?.response) {
      // HTTP错误响应
      const data = err.response.data
      // Spring Boot错误格式：{timestamp, status, error, message, path}
      errorMsg = data?.message || data?.error || `请求失败 (${err.response.status})`
    } else if (err?.message) {
      // 其他错误（包括我们抛出的错误）
      errorMsg = err.message
    }
    
    // 设置错误消息（响应拦截器已经对登录/注册接口禁用了ElMessage）
    error.value = errorMsg
    
    // 确保成功消息被清除
    message.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  max-width: 340px;
  margin: 80px auto;
  padding: 32px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);
}
h2 {
  margin-top: 0;
  text-align: center;
  color: #0f172a;
}
.switcher {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 16px;
}
.switcher button {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #cbd5f5;
  background: #f1f5ff;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
}
.switcher button.active {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}
label {
  display: block;
  margin-top: 14px;
  margin-bottom: 6px;
  font-weight: 600;
  color: #334155;
}
input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d5d9e2;
  border-radius: 6px;
  box-sizing: border-box;
}
button[type='submit'] {
  width: 100%;
  margin-top: 20px;
  padding: 12px;
  border: none;
  border-radius: 6px;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.tip {
  margin-top: 16px;
  font-size: 12px;
  color: #64748b;
  text-align: center;
}
.success {
  margin-top: 12px;
  color: #16a34a;
  text-align: center;
}
.error {
  margin-top: 12px;
  color: #dc2626;
  text-align: center;
}
</style>