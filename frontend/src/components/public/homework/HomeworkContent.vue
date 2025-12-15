<template>
  <div class="homework-content">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 作业内容 -->
    <div v-else-if="assignment" class="content">
      <div class="header">
        <h2 class="title">{{ assignment.title }}</h2>
      </div>

      <div class="info-row">
        <div class="info-item">
          <span class="label">截止时间：</span>
          <span class="value">{{ formatTime(assignment.dueAt) }}</span>
        </div>
        <div class="info-item">
          <span class="label">总分：</span>
          <span class="value">{{ assignment.totalScore || 100 }} 分</span>
        </div>
      </div>

      <!-- 成绩显示（已批改） -->
      <div v-if="assignment.score !== null && assignment.score !== undefined" class="score-section">
        <div class="score-display">
          <span class="score-label">得分：</span>
          <span class="score-value">{{ assignment.score }}</span>
          <span class="score-total"> / {{ assignment.totalScore || 100 }}</span>
        </div>
        <div v-if="assignment.feedback" class="feedback">
          <div class="feedback-label">教师评语：</div>
          <div class="feedback-content">{{ assignment.feedback }}</div>
        </div>
      </div>

      <!-- 作业描述 -->
      <div class="describe">
        <div class="describe-title">作业描述</div>
        <div class="describe-content">
          {{ assignment.description || '暂无描述' }}
        </div>
      </div>

      <!-- 附件 -->
      <div v-if="assignment.files && assignment.files.length > 0" class="attachment">
        <div class="attachment-title">附件</div>
        <div class="attachment-list">
          <div v-for="file in assignment.files" :key="file.id" class="file-item">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.originalName }}</span>
            <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
          </div>
        </div>
      </div>

      <!-- 提交信息 -->
      <div v-if="assignment.submittedAt" class="submission-info">
        <span class="label">提交时间：</span>
        <span class="value">{{ formatTime(assignment.submittedAt) }}</span>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error">
      <el-empty description="作业不存在或已被删除" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getAssignmentById } from '@/api/assignment'
import { useUserStore } from '@/stores/useUserStore'

const props = defineProps({
  assignmentId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['submit', 'viewSubmission'])

const userStore = useUserStore()
const loading = ref(false)
const assignment = ref(null)
const user = computed(() => userStore.currentUser || {})

// 获取作业详情
const fetchAssignment = async () => {
  if (!props.assignmentId) return
  
  const userId = user.value.id
  const userRole = user.value.role
  
  // 学生端需要userId，教师端不需要
  if (!userId && userRole !== 'TEACHER') {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    // 教师端不传studentId，学生端传studentId
    const studentId = userRole === 'TEACHER' ? null : userId
    const response = await getAssignmentById(props.assignmentId, studentId)
    console.log('获取作业详情响应:', response)
    
    // 处理响应数据：可能是直接返回对象，也可能是 {data: {}} 格式
    if (response && response.id) {
      // 直接返回AssignmentResponse对象
      assignment.value = response
    } else if (response && response.data && response.data.id) {
      // 标准格式：{code, data: AssignmentResponse}
      assignment.value = response.data
    } else {
      console.warn('响应数据格式异常:', response)
      assignment.value = null
    }
    
    console.log('解析后的作业详情:', assignment.value)
  } catch (error) {
    console.error('获取作业详情失败:', error)
    ElMessage.error(error.message || '获取作业详情失败')
    assignment.value = null
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '未设置'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'progress': 'warning',
    'submitted': 'primary',
    'graded': 'success',
    'ended': 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'progress': '未提交',
    'submitted': '已提交',
    'graded': '已批改',
    'ended': '已截止'
  }
  return map[status] || '未知'
}

// 监听 assignmentId 变化
watch(() => props.assignmentId, () => {
  fetchAssignment()
}, { immediate: true })

onMounted(() => {
  fetchAssignment()
})
</script>

<style scoped>
.loading {
  padding: 20px;
}

.content {
  background: white;
  border-radius: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
}

.info-row {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-size: 14px;
  color: #64748b;
  margin-right: 8px;
}

.value {
  font-size: 14px;
  color: #0f172a;
  font-weight: 500;
}

.score-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.score-display {
  display: flex;
  align-items: baseline;
  margin-bottom: 12px;
}

.score-label {
  font-size: 16px;
  color: #64748b;
  margin-right: 8px;
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  color: #10b981;
}

.score-total {
  font-size: 18px;
  color: #64748b;
  margin-left: 4px;
}

.feedback {
  margin-top: 12px;
}

.feedback-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.feedback-content {
  font-size: 14px;
  color: #0f172a;
  line-height: 1.6;
  white-space: pre-wrap;
}

.describe,
.attachment {
  margin-bottom: 20px;
}

.describe-title,
.attachment-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
  display: block;
}

.describe-content {
  font-size: 14px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.file-item:hover {
  background: #f1f5f9;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #0f172a;
}

.file-size {
  font-size: 12px;
  color: #94a3b8;
}

.submission-info {
  padding: 12px 0;
  border-top: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.error {
  padding: 40px;
  text-align: center;
}
</style>