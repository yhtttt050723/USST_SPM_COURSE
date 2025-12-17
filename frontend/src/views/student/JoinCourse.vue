<template>
  <div class="join-page">
    <el-card class="box">
      <h3>加入课程</h3>
      <p class="desc">输入教师提供的 6-8 位邀请码。</p>
      <el-form :model="form" label-position="top">
        <el-form-item label="邀请码">
          <el-input v-model="form.code" placeholder="如 836241" maxlength="8" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">加入</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { joinCourseByCode, listMyCourses } from '@/api/course'
import { useUserStore } from '@/stores/useUserStore'
import { useRouter } from 'vue-router'

const form = reactive({ code: '' })
const loading = ref(false)
const userStore = useUserStore()
const router = useRouter()

const refreshCourses = async () => {
  const { data } = await listMyCourses()
  userStore.setCourses(data)
  if (!userStore.currentCourse && data.length) {
    userStore.setCurrentCourse(data[0])
  }
}

const submit = async () => {
  if (!form.code) {
    ElMessage.warning('请输入邀请码')
    return
  }
  loading.value = true
  try {
    await joinCourseByCode(form.code)
    ElMessage.success('加入成功')
    await refreshCourses()
    router.push('/')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  userStore.hydrateUserFromCache()
  if (userStore.currentCourse) {
    router.push('/')
  }
})
</script>

<style scoped>
.join-page {
  display: flex;
  justify-content: center;
  padding: 40px;
}
.box {
  width: 360px;
}
.desc {
  color: #666;
  margin: 8px 0 16px;
}
</style>

