<template>
  <div class="course-page">
    <div class="header">
      <div>
        <h2>{{ course?.name || '课程管理' }}</h2>
        <p class="sub">
          <span v-if="course">代码：{{ course.code }} ｜ 学年：{{ course.academicYear }} ｜ 学期：{{ termLabel }}</span>
        </p>
      </div>
      <div class="actions">
        <el-button type="primary" @click="openInviteDialog">生成邀请码</el-button>
        <el-button type="danger" @click="revokeInvite" :disabled="!course?.inviteCode">失效当前邀请码</el-button>
      </div>
    </div>

    <el-card class="block">
      <template #header>
        <div class="card-header">
          <span>当前邀请码</span>
          <el-tag v-if="course?.inviteCode" type="success">{{ course.inviteCode }}</el-tag>
          <el-tag v-else type="info">暂无</el-tag>
        </div>
      </template>
      <div v-if="course?.inviteCode">
        <p>有效期至：{{ course.inviteExpireAt ? dayjs(course.inviteExpireAt).format('YYYY-MM-DD HH:mm') : '不限' }}</p>
        <p>课程：{{ course.name }}</p>
      </div>
      <div v-else>请生成新的邀请码</div>
    </el-card>

    <el-dialog v-model="inviteDialogVisible" title="生成邀请码" width="400px">
      <el-form label-width="100px">
        <el-form-item label="有效天数">
          <el-input-number v-model="inviteForm.expireDays" :min="1" :max="90" />
        </el-form-item>
        <el-form-item label="使用上限">
          <el-input-number v-model="inviteForm.maxUse" :min="0" :max="500" />
          <div class="tip">0 表示不限制</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitInvite">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/useUserStore'
import { getCourse, listMyCourses, createInvite, revokeInvite as revokeInviteApi } from '@/api/course'

const userStore = useUserStore()
const course = ref(null)
const loading = ref(false)
const inviteDialogVisible = ref(false)
const inviteForm = reactive({
  expireDays: 30,
  maxUse: 200
})

const termLabel = computed(() => {
  if (!course.value) return ''
  return course.value.term === 'UPPER' ? '上学期' : '下学期'
})

const loadCourse = async () => {
  try {
    await userStore.hydrateUserFromCache()
    let current = userStore.currentCourse

    // 如果还没有选择课程，自动拉取课程列表并选择第一个
    if (!current) {
      const resp = await listMyCourses()
      const courses = resp.data || resp || []
      console.log('[course] listMyCourses resp:', resp)
      if (!courses.length) {
        ElMessage.error('当前账号没有可管理的课程')
        return
      }
      current = courses[0]
      userStore.setCourses(courses)
      userStore.setCurrentCourse(current)
    }

    const response = await getCourse(current.id)
    // 拦截器可能返回 {code, data} 或直接返回 CourseResponse
    const courseData = response.data || response
    console.log('[course] loadCourse response:', response, 'courseData:', courseData)
    course.value = courseData
    userStore.setCurrentCourse(courseData)
  } catch (error) {
    console.error('[course] loadCourse error:', error)
    ElMessage.error('加载课程信息失败')
  }
}

const openInviteDialog = () => {
  if (!course.value) {
    // 尝试重新加载课程
    loadCourse()
  }
  inviteDialogVisible.value = true
}

const submitInvite = async () => {
  console.log('[invite] submitInvite called, course.value:', course.value)
  if (!course.value) {
    console.warn('[invite] course.value is null, aborting')
    ElMessage.error('课程信息未加载，请刷新页面')
    return
  }
  console.log('[invite] calling createInvite with courseId:', course.value.id, 'payload:', {
    expireDays: inviteForm.expireDays,
    maxUse: inviteForm.maxUse
  })
  loading.value = true
  try {
    const response = await createInvite(course.value.id, {
      expireDays: inviteForm.expireDays,
      maxUse: inviteForm.maxUse
    })
    console.log('[invite] createInvite returned:', response)
    // 拦截器可能返回 {code, data} 或直接返回 Map
    const result = response.data || response
    console.log('[invite] create resp raw:', response, 'result:', result)
    
    // 生成成功后，直接更新 course.value，避免再次请求
    if (result && result.inviteCode) {
      course.value.inviteCode = result.inviteCode
      course.value.inviteExpireAt = result.expireAt
      userStore.setCurrentCourse(course.value)
      ElMessage.success(`生成成功，邀请码：${result.inviteCode}`)
    } else {
      // 如果直接更新失败，则重新加载
      await loadCourse()
      const code = result.inviteCode || result.code || ''
      if (code) {
        ElMessage.success(`生成成功，邀请码：${code}`)
      }
    }
    inviteDialogVisible.value = false
  } catch (error) {
    console.error('[invite] create error:', error)
    ElMessage.error(error.message || '生成邀请码失败')
  } finally {
    loading.value = false
  }
}

const revokeInviteFn = async () => {
  if (!course.value?.inviteCode) return
  loading.value = true
  try {
    await revokeInviteApi(course.value.id, course.value.inviteCode)
    await loadCourse()
    ElMessage.success('已失效当前邀请码')
  } finally {
    loading.value = false
  }
}

const revokeInvite = () => {
  if (!course.value?.inviteCode) return
  ElMessageBox.confirm('确定失效当前邀请码吗？', '提示', {
    type: 'warning'
  }).then(() => revokeInviteFn())
}

onMounted(() => {
  userStore.hydrateUserFromCache()
  loadCourse()
})
</script>

<style scoped>
.course-page {
  padding: 20px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.sub {
  color: #666;
  margin: 4px 0 0;
}
.actions {
  display: flex;
  gap: 8px;
}
.block {
  margin-top: 12px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.tip {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}
</style>
