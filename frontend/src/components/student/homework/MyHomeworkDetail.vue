<template>
  <div class="myhomework-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 显示重新提交表单 -->
    <div v-else-if="showResubmitForm">
      <SubmitMyHomework 
        :assignmentId="props.assignmentId"
        :isResubmit="true"
        @submitted="handleResubmitted"
        @back="cancelResubmit"
      />
    </div>

    <!-- 我的作业内容 -->
    <div v-else-if="submission" class="content">
      <!-- 不允许重新提交的提示 -->
      <div v-if="assignment && !assignment.allowResubmit" class="resubmit-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>该作业不允许重新提交</span>
      </div>

      <!-- 成绩区域 -->
      <div v-if="submission.score !== null && submission.score !== undefined" class="score-section">
        <div class="score-display">
          <span class="score-label">我的得分</span>
          <div class="score-box">
            <span class="score-value">{{ submission.score }}</span>
            <span class="score-total"> / {{ submission.totalScore || 100 }}</span>
          </div>
        </div>
      </div>

      <!-- 教师评语 -->
      <div v-if="submission.feedback" class="feedback-section">
        <div class="feedback-title">教师评语</div>
        <div class="feedback-content">{{ submission.feedback }}</div>
      </div>

      <!-- 我的提交 -->
      <div class="submission-section">
        <div class="section-title">我的作业</div>
        
        <!-- 提交时间 -->
        <div class="info-row">
          <span class="label">提交时间：</span>
          <span class="value">{{ formatTime(submission.submittedAt) }}</span>
        </div>

        <!-- 作业内容 -->
        <div class="content-box">
          <div class="content-label">作业内容</div>
          <div class="content-text">
            {{ submission.content || '无文字内容' }}
          </div>
        </div>

        <!-- 附加文件 -->
        <div v-if="submission.files && submission.files.length > 0" class="files-box">
          <div class="files-label">附加文件</div>
          <div class="files-list">
            <div v-for="file in submission.files" :key="file.id" class="file-item">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{ file.originalName }}</span>
              <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
              <el-button type="primary" link size="small">下载</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button 
          v-if="assignment && assignment.allowResubmit" 
          type="primary" 
          @click="handleResubmitClick"
        >
          重新提交
        </el-button>
        <el-button @click="$emit('back')">
          返回
        </el-button>
      </div>
      
    </div>

    <!-- 空状态 - 未提交作业 -->
    <div v-else>
      <!-- 作业已截止且未提交 -->
      <div v-if="isExpired" class="expired-state">
        <el-result
          icon="warning"
          title="作业已截止"
          sub-title="该作业已经超过截止时间，无法再提交"
        >
        </el-result>
      </div>
      <!-- 作业未截止，可以提交 -->
      <SubmitMyHomework 
        v-else
        :assignmentId="props.assignmentId"
        @submitted="handleSubmitted"
        @back="$emit('back')"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DocumentAdd, Upload, InfoFilled } from '@element-plus/icons-vue'
import { getMySubmission, submitAssignment, getAssignmentById } from '@/api/assignment'
import { useUserStore } from '@/stores/useUserStore'
import SubmitMyHomework from './SubmitMyHomework.vue'

const props = defineProps({
  assignmentId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['resubmit', 'back', 'submitted'])

const userStore = useUserStore()
const currentUser = computed(() => userStore.currentUser || {})
const loading = ref(false)
const submission = ref(null)
const assignment = ref(null)
const showResubmitForm = ref(false)

// 判断作业是否已截止
const isExpired = computed(() => {
  if (!assignment.value || !assignment.value.dueAt) return false
  return new Date(assignment.value.dueAt) < new Date()
})

// 点击重新提交按钮
const handleResubmitClick = () => {
  // 检查是否允许重新提交
  if (!assignment.value || !assignment.value.allowResubmit) {
    ElMessage.warning('该作业不允许重新提交')
    return
  }
  showResubmitForm.value = true
}

// 取消重新提交
const cancelResubmit = () => {
  showResubmitForm.value = false
}

// 重新提交成功
const handleResubmitted = async () => {
  showResubmitForm.value = false
  await fetchSubmission()
  emit('submitted')
}

// 获取提交
const fetchSubmission = async () => {
  if (!props.assignmentId) return
  
  const userId = currentUser.value.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
 try {
    // 先获取作业详情（包含提交状态）
    const assignmentResponse = await getAssignmentById(props.assignmentId, userId)
    assignment.value = assignmentResponse.data
    console.log('作业信息:', assignment.value)
    console.log('提交状态:', assignment.value.submissionStatus)

    const submissionStatus = (assignment.value.submissionStatus || '').toLowerCase()
    
    if (submissionStatus === 'submitted' || submissionStatus === 'graded') {
      try {
        const submissionResponse = await getMySubmission(props.assignmentId, userId)
        submission.value = submissionResponse.data
      } catch (error) {
        submission.value = null
      }
    } else {
      submission.value = null
    }
  } catch (error) {
    ElMessage.error('加载失败，请重试')
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

// 处理提交成功
const handleSubmitted = async () => {
  await fetchSubmission()
  emit('submitted')
}

watch(() => props.assignmentId, () => {
  fetchSubmission()
}, { immediate: true })

onMounted(() => {
  fetchSubmission()
})
</script>

<style scoped>
.myhomework-page {
  width: 100%;
}

.loading {
  padding: 20px;
}

.content {
  width: 100%;
}

.score-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  color: white;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.score-box {
  display: flex;
  align-items: baseline;
}

.score-value {
  font-size: 48px;
  font-weight: 700;
}

.score-total {
  font-size: 24px;
  opacity: 0.8;
  margin-left: 4px;
}

.feedback-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.feedback-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
}

.feedback-content {
  font-size: 14px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.submission-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 16px;
}

.info-row {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
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

.content-box,
.files-box {
  margin-top: 20px;
}

.content-label,
.files-label {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
}

.content-text {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
  min-height: 100px;
}

.files-list {
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

.actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  margin-top: 20px;
}

.expired-state {
  padding: 40px 20px;
}

.expired-state :deep(.el-result) {
  padding: 20px;
}

.expired-state :deep(.el-result__icon svg) {
  width: 80px;
  height: 80px;
}

.expired-state :deep(.el-result__title) {
  font-size: 24px;
  color: #e6a23c;
  margin-top: 20px;
}

.expired-state :deep(.el-result__subtitle) {
  font-size: 16px;
  color: #606266;
  margin-top: 12px;
}

.resubmit-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 14px;
  margin-bottom: 16px;
}

.resubmit-tip .el-icon {
  font-size: 16px;
}
</style>