<template>
  <div class="student-homework-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 主内容 -->
    <div v-else-if="submission" class="content-wrapper">
      <div class="back-btn">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      </div>

      <div class="title">{{ assignment?.title || '作业详情' }}</div>

      <!-- 学生信息和成绩 -->
      <div class="info-box">
        <div class="stu-info">
          <div class="info-item">
            <span class="label">学生姓名：</span>
            <span class="value">{{ submission.studentName || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">学生学号：</span>
            <span class="value">{{ submission.studentNo || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交时间：</span>
            <span class="value">{{ formatTime(submission.submittedAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag v-if="submission.graded" type="success">已批改</el-tag>
            <el-tag v-else type="warning">待批改</el-tag>
          </div>
        </div>
        <div class="grade-info">
          <div class="grade-label">成绩</div>
          <div class="grade-value">
            <span v-if="submission.score !== null && submission.score !== undefined" class="score">
              {{ submission.score }}
            </span>
            <span v-else class="no-score">未评分</span>
            <span class="total-score">/ {{ assignment?.totalScore || 100 }}</span>
          </div>
        </div>
      </div>

      <!-- 作业内容 -->
      <div class="content-box">
        <div class="content-title">作业内容</div>
        <div class="content">
          {{ submission.content || '学生未填写文字内容' }}
        </div>
      </div>

      <!-- 附件列表 -->
      <div v-if="submission.files && submission.files.length > 0" class="files-box">
        <div class="files-title">
          <el-icon><Document /></el-icon>
          附件列表 ({{ submission.files.length }})
        </div>
        <div class="files-list">
          <div 
            v-for="file in submission.files" 
            :key="file.id" 
            class="file-item"
          >
            <div class="file-icon">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="file-info">
              <div class="file-name">{{ file.originalName || file.fileName }}</div>
              <div class="file-size">{{ formatFileSize(file.fileSize) }}</div>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              @click="downloadFile(file.id, file.originalName || file.fileName)"
            >
              下载
            </el-button>
          </div>
        </div>
      </div>

      <!-- 批改区域 -->
      <div class="grading-box">
        <div class="grading-title">批改</div>
        <el-form :model="gradingForm" label-width="80px">
          <el-form-item label="成绩" required>
            <el-input-number
              v-model="gradingForm.score"
              :min="0"
              :max="assignment?.totalScore || 100"
              :step="1"
              placeholder="请输入成绩"
              style="width: 200px"
            />
            <span class="score-hint"> / {{ assignment?.totalScore || 100 }} 分</span>
          </el-form-item>
          <el-form-item label="反馈">
            <el-input
              v-model="gradingForm.feedback"
              type="textarea"
              :rows="5"
              placeholder="请输入批改反馈（可选）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <div class="button-group">
              <el-button 
                type="warning" 
                @click="saveGrading"
                :loading="grading"
                size="large"
              >
                保存批改
              </el-button>
              <el-button 
                type="primary" 
                @click="publishGrading"
                :loading="grading"
                size="large"
                :disabled="!submission.graded && !gradingForm.score"
              >
                发布成绩
              </el-button>
              <el-button @click="goBack" size="large">取消</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 历史批改记录 -->
      <div v-if="submission.graded && submission.feedback" class="history-box">
        <div class="history-title">历史批改记录</div>
        <div class="history-content">
          <div class="history-item">
            <div class="history-label">成绩：</div>
            <div class="history-value">{{ submission.score }} / {{ assignment?.totalScore || 100 }}</div>
          </div>
          <div class="history-item">
            <div class="history-label">反馈：</div>
            <div class="history-value">{{ submission.feedback }}</div>
          </div>
          <div class="history-item">
            <div class="history-label">状态：</div>
            <div class="history-value">
              <el-tag v-if="submission.released" type="success">已发布</el-tag>
              <el-tag v-else type="info">未发布</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="提交记录不存在" />
      <el-button @click="goBack" style="margin-top: 20px">返回列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, DocumentChecked, CircleCheck } from '@element-plus/icons-vue'
import request from '@/api/request'
import { downloadFile as downloadFileApi } from '@/api/file'
import { gradeSubmission } from '@/api/assignment'
import { useUserStore } from '@/stores/useUserStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const grading = ref(false)
const submission = ref(null)
const assignment = ref(null)

// 批改表单
const gradingForm = ref({
  score: null,
  feedback: ''
})

// 将后端 attachments 映射为文件列表，供下载展示
const normalizeSubmissionFiles = (sub) => {
  if (!sub) return
  if (sub.attachments && Array.isArray(sub.attachments)) {
    sub.files = sub.attachments.map(att => ({
      id: att.fileId || att.id,
      fileName: att.fileName || att.originalName,
      originalName: att.originalName || att.fileName,
      storagePath: att.storagePath,
      fileSize: att.fileSize || 0
    }))
  }
}

const fetchSubmissionDetail = async () => {
  const submissionId = route.params.submissionId
  if (!submissionId) {
    ElMessage.error('缺少提交ID参数')
    router.back()
    return
  }

  loading.value = true
  try {
    const assignmentId = route.query.assignmentId
    
    if (assignmentId) {
      const assignmentRes = await request.get(`/assignments/${assignmentId}`, {
        params: { studentId: userStore.currentUser?.id || 1 }
      })
      assignment.value = assignmentRes?.data || assignmentRes

      const submissionsRes = await request.get(`/assignments/${assignmentId}/submissions`)
      const submissions = Array.isArray(submissionsRes)
        ? submissionsRes
        : (submissionsRes?.data || [])
      submission.value = submissions.find(s => s.id == submissionId)
      normalizeSubmissionFiles(submission.value)
      
      if (!submission.value) {
        ElMessage.warning('未找到该提交记录')
      }
    } else {
      // 如果没有传 assignmentId，则需要遍历所有作业以查找该提交
      const assignmentsRes = await request.get('/assignments', {
        params: { role: 'TEACHER' }
      })
      const assignments = assignmentsRes.data || []
      
      let found = false
      for (const assign of assignments) {
        try {
          const submissionsRes = await request.get(`/assignments/${assign.id}/submissions`)
          const submissions = Array.isArray(submissionsRes)
            ? submissionsRes
            : (submissionsRes?.data || [])
          const foundSubmission = submissions.find(s => s.id == submissionId)
          
          if (foundSubmission) {
            submission.value = foundSubmission
            assignment.value = assign
            normalizeSubmissionFiles(submission.value)
            found = true
            break
          }
        } catch (err) {
          console.error(`获取作业 ${assign.id} 的提交列表失败:`, err)
          continue
        }
      }
      
      if (!found) {
        ElMessage.error('未找到该提交记录')
      }
    }

    if (submission.value && assignment.value) {
      // 初始化批改表单
      gradingForm.value = {
        score: submission.value.score || null,
        feedback: submission.value.feedback || ''
      }
    }
  } catch (error) {
    console.error('获取提交详情失败:', error)
    ElMessage.error(error.response?.data?.message || '获取提交详情失败')
  } finally {
    loading.value = false
  }
}

// 保存批改（不发布）
const saveGrading = async () => {
  if (gradingForm.value.score === null || gradingForm.value.score === undefined) {
    ElMessage.warning('请输入成绩')
    return
  }

  grading.value = true
  try {
    await gradeSubmission(
      assignment.value.id,
      submission.value.id,
      {
        score: gradingForm.value.score,
        feedback: gradingForm.value.feedback,
        released: false, // 不发布
        teacherId: userStore.currentUser?.id || 1
      }
    )
    ElMessage.success('批改已保存，尚未发布给学生')
    await fetchSubmissionDetail()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败，请重试')
  } finally {
    grading.value = false
  }
}

// 发布成绩
const publishGrading = async () => {
  if (gradingForm.value.score === null || gradingForm.value.score === undefined) {
    ElMessage.warning('请先输入成绩')
    return
  }

  grading.value = true
  try {
    await gradeSubmission(
      assignment.value.id,
      submission.value.id,
      {
        score: gradingForm.value.score,
        feedback: gradingForm.value.feedback,
        released: true, // 发布
        teacherId: userStore.currentUser?.id || 1
      }
    )
    ElMessage.success('成绩已发布，学生可以查看')
    await fetchSubmissionDetail()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发布失败，请重试')
  } finally {
    grading.value = false
  }
}

// 下载文件 
const downloadFile = async (fileId, fileName) => {
  try {
    const response = await downloadFileApi(fileId)
    
    // 检查响应是否是错误的 JSON 而不是文件
    if (response.data instanceof Blob && response.data.type === 'application/json') {
      // 尝试读取 JSON 错误消息
      const text = await response.data.text()
      try {
        const errorData = JSON.parse(text)
        throw new Error(errorData.message || '文件下载失败')
      } catch (parseError) {
        throw new Error('文件不存在或已被删除')
      }
    }
    
    // 正常下载流程
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    const errorMessage = error.message || error.response?.data?.message || '下载失败，请重试'
    ElMessage.error(errorMessage)
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchSubmissionDetail()
})
</script>

<style scoped>
.student-homework-page {
  min-height: 100vh;
  padding: 40px;
  background-color: white;
  border-radius: 24px;
}

.loading {
  padding: 40px;
  background: white;
  border-radius: 12px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #474747;
  margin-bottom: 24px;
  padding: 20px;
  border-radius: 12px;
  background: rgb(245, 245, 245);
  box-shadow: 0 2px 8px rgba(152, 105, 105, 0.06);
}

.info-box {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(152, 105, 105, 0.06);
}

.stu-info {
  flex: 1;
}

.info-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
  font-weight: 500;
}

.grade-info {
  text-align: right;
  padding: 16px 24px;
  background: #7e91e4;
  border-radius: 12px;
  color: white;
  min-width: 180px;
}

.grade-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.grade-value {
  font-size: 36px;
  font-weight: 700;
}

.score {
  font-size: 42px;
}

.no-score {
  font-size: 20px;
  opacity: 0.8;
}

.total-score {
  font-size: 18px;
  opacity: 0.8;
  margin-left: 4px;
}

.content-box {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.content-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.content {
  color: #555;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 100px;
}

.files-box {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.files-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 8px;
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.file-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.file-icon {
  color: #667eea;
}

.file-info {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: #9ca3af;
}

.grading-box {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.grading-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}


.score-hint {
  margin-left: 12px;
  color: #9ca3af;
  font-size: 14px;
}

.button-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.history-box {
  background: #fffbeb;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #fef3c7;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  color: #92400e;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  gap: 12px;
}

.history-label {
  font-weight: 500;
  color: #78350f;
  min-width: 60px;
}

.history-value {
  color: #92400e;
  flex: 1;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

@media (max-width: 768px) {
  .info-box {
    flex-direction: column;
    gap: 20px;
  }

  .grade-info {
    width: 100%;
    text-align: center;
  }

  .title {
    font-size: 22px;
  }
}
</style>
