<template>
  <div class="login-page">
    <h2>项目管理课程登录</h2>
    <form @submit.prevent="handleLogin">
      <label>学号</label>
      <input v-model="form.studentNo" placeholder="请输入学号" required />

      <label>密码</label>
      <input v-model="form.password" type="password" placeholder="请输入密码" required />

      <button type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>
    <p class="tip">后续会接入学校SSO，这里先做本地调试。</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

const form = reactive({
  studentNo: '',
  password: ''
})
const loading = ref(false)

const handleLogin = () => {
  loading.value = true
  // 先占位，后面对接后端
  console.log('准备调用后端登录接口', form)
  setTimeout(() => {
    loading.value = false
    alert('模拟登录成功（后续对接后端）')
  }, 800)
}
</script>

<style scoped>
.login-page {
  max-width: 320px;
  margin: 80px auto;
  padding: 32px;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.08);
}
label {
  display: block;
  margin-top: 12px;
  margin-bottom: 4px;
  font-weight: 600;
}
input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #dcdcdc;
  border-radius: 4px;
  box-sizing: border-box;
}
button {
  width: 100%;
  margin-top: 18px;
  padding: 10px;
  border: none;
  background: #3b82f6;
  color: #fff;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
}
button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.tip {
  margin-top: 16px;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
}
</style>