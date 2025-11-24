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
import { login, register } from '@/api/auth'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const { setUser } = useUserStore()
const form = reactive({
  studentNo: '',
  name: '',
  password: ''
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
  resetNotice()
  loading.value = true
  try {
    if (mode.value === 'login') {
      const { data } = await login({
        studentNo: form.studentNo,
        password: form.password
      })
      message.value = `登录成功：${data.name} (${data.studentNo})`
      setUser(data)
      router.push('/main')
    } else {
      const { data } = await register({
        studentNo: form.studentNo,
        name: form.name,
        password: form.password
      })
      message.value = `注册成功：${data.name} (${data.studentNo})`
      mode.value = 'login'
      form.name = ''
    }
  } catch (err) {
    error.value =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      err.message ||
      '操作失败'
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