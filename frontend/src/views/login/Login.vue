<template>
  <div class="login-page">
        <div class="form-box">
      <!-- 登录表单 -->
      <el-form
        label-width="auto"
        class="login"
        ref="loginFormRef"
        v-if="inhere"
        :model="formModel"
        :rules="loginRules"
      >
        <el-form-item prop="studentNo">
          <el-input v-model="formModel.studentNo" placeholder="请输入学号">
            <template #prefix>
              <el-icon><Avatar /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="formModel.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-checkbox v-model="rememberMe"> 记住我 </el-checkbox>
          </el-col>
          <el-col :span="12" style="text-align: right">
            <a href="#" style="font-size: small">忘记密码</a>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button 
            type="primary" 
            style="width: 100%" 
            @click="handleLogin"
            :loading="loginLoading"
          >
            登录
          </el-button>
        </el-form-item>

        <el-button type="primary" plain id="goto" @click="switchToRegister">
          去注册
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        label-width="auto"
        class="register"
        ref="registerFormRef"
        :model="formModel"
        :rules="registerRules"
        v-else
      >
        <el-form-item prop="name">
          <div class="title">姓名:</div>
          <el-input v-model="formModel.name" placeholder="请输入姓名">
            <template #prefix>
              <el-icon><Avatar /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="studentNo">
          <div class="title">学号:</div>
          <el-input v-model="formModel.studentNo" placeholder="请输入学号(仅数字)" >
            <template #prefix>
              <el-icon><Avatar /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <div class="title">密码:</div>
          <el-input 
            v-model="formModel.password" 
            type="password" 
            placeholder="请输入密码(不少于6位)"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <div class="title">确认密码:</div>
          <el-input
            v-model="formModel.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            style="width: 100%" 
            @click="handleRegister"
            :loading="registerLoading"
          >
            注册
          </el-button>
        </el-form-item>

        <el-button type="primary" plain id="back" @click="switchToLogin">
          返回
        </el-button>
      </el-form>
      </div>
  </div>
</template>

<script setup>
import { Avatar, Lock } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { login as loginApi, register as registerApi } from '@/api/auth'
import router from '@/routers/router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/useUserStore'

const route = useRoute()
const userStore = useUserStore()

const inhere = ref(true)
const loginFormRef = ref()
const registerFormRef = ref()
const rememberMe = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)

const formModel = reactive({
  name: '',
  studentNo: '',
  password: '',
  confirmPassword: '',
})

const loginRules = reactive({
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})

const registerRules = reactive({
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 10, max: 10, message: '学号长度为10位数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== formModel.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const switchToRegister = () => {
  inhere.value = false
  formModel.studentNo = ''
  formModel.password = ''
  formModel.confirmPassword = ''
  loginFormRef.value?.resetFields()
}

const switchToLogin = () => {
  inhere.value = true
  formModel.studentNo = ''
  formModel.password = ''
  formModel.confirmPassword = ''
  registerFormRef.value?.resetFields()
}

// 登录逻辑
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true
    
    const res = await loginApi({
      studentNo: formModel.studentNo,
      password: formModel.password
    })

    console.log('登录响应:', res)
    
    // 处理响应数据：响应拦截器已经处理，res可能是LoginResponse对象或{data: LoginResponse}
    let userData
    if (res && res.data && typeof res.data === 'object' && res.data.studentNo) {
      // 标准格式：{code, data: LoginResponse}
      userData = res.data
    } else if (res && res.studentNo) {
      // 直接返回LoginResponse对象
      userData = res
    } else {
      throw new Error('登录响应数据格式错误')
    }
    
    // 验证必要字段
    if (!userData.token) {
      throw new Error('登录成功，但未获取到token，请检查后端响应')
    }
    
    console.log('用户数据:', userData)
    console.log('Token已获取:', userData.token.substring(0, 20) + '...')
    
    // 设置用户信息到store（同步更新 currentUser 和 sessionStorage）
    userStore.setUser(userData)
    console.log('用户信息已保存')
    
    ElMessage.success('登录成功')
    
    // 获取重定向路径
    const redirect = route.query.redirect || null
    const userRole = userData.role
    
    // 延迟一下，让用户看到成功消息
    await new Promise(resolve => setTimeout(resolve, 300))
    
    if (redirect) {
      await router.push(redirect)
    } else if (userRole === 'STUDENT') {
      await router.push('/home')
    } else if (userRole === 'TEACHER') {
      await router.push('/teacher/course')
    } else {
      await router.push('/')
    }
  } catch (error) {
    console.error('登录失败:', error)
    
    // 提取错误消息
    let errorMsg = '登录失败'
    if (error?.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error?.response?.data?.error) {
      errorMsg = error.response.data.error
    } else if (error?.message) {
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    await registerApi({
      studentNo: formModel.studentNo,
      password: formModel.password,
      name: formModel.name
    })
    
    ElMessage.success('注册成功，请登录')
    
    // 切换到登录页面并保留学号
    const studentNo = formModel.studentNo
    switchToLogin()
    formModel.studentNo = studentNo
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
#goto, #back {
  font-size: small;
  color: #66707a;
  text-decoration: none;
}

.login, .register {
  padding: 20px;
}

a {
  text-decoration: none;
  color: #409eff;
}

a:hover {
  color: #66b1ff;
}
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
.form-box {
  width: 400px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
.title {
  font-weight: 500;
  margin-bottom: 5px;
  display: block;
  width: 20%;
}
.register .el-input {
  width: 80%;
}
.login .el-input {
  width: 100%;
}
</style>
