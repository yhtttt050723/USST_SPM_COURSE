<template>
  <div class="teacher-grading-page">
    <div class="page-header">
      <div>
        <p class="subtitle">作业批改</p>
        <h2>待批改作业列表</h2>
      </div>
      <el-select v-model="selectedAssignmentId" placeholder="选择作业" style="width: 200px" @change="loadSubmissions">
        <el-option
          v-for="assignment in assignments"
          :key="assignment.id"
          :label="assignment.title"
          :value="assignment.id"
        />
      </el-select>
    </div>

    <div v-if="selectedAssignmentId" class="submissions-container">
      <el-table :data="submissions" style="width: 100%" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="120">
          <template #default="{ row }">
            <div>
              <p style="margin: 0; font-weight: 600;">{{ row.studentName }}</p>
              <p style="margin: 0; font-size: 12px; color: #9ca3af;">{{ row.studentNo }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="提交内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.content || '无内容' }}
          </template>
        </el-table-column>
        <el-table-column label="文件" width="150">
          <template #default="{ row }">
            <div v-if="row.files && row.files.length > 0" class="file-count">
              <el-icon><Document /></el-icon>
              <span>{{ row.files.length }} 个文件</span>
            </div>
            <span v-else style="color: #9ca3af;">无文件</span>
          </template>
        </el-table-column>
        <el-table-column prop="graded" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.graded" type="success">已批改</el-tag>
            <el-tag v-else type="warning">待批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="80">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</span>
            <span v-else style="color: #9ca3af;">--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="openGradingDialog(row)"
            >
              {{ row.graded ? '修改成绩' : '批改' }}
            </el-button>
            <el-button
              v-if="row.files && row.files.length > 0"
              size="small"
              @click="viewFiles(row)"
            >
              查看文件
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="submissions.length === 0" class="empty-state">
        <p>该作业暂无提交记录</p>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>请先选择一个作业</p>
    </div>

    <!-- 批改对话框 -->
    <el-dialog
      v-model="showGradingDialog"
      :title="`批改作业 - ${currentSubmission?.studentName}`"
      width="600px"
    >
      <el-form :model="gradingForm" label-width="100px">
        <el-form-item label="成绩" required>
          <el-input-number
            v-model="gradingForm.score"
            :min="0"
            :max="currentAssignment?.totalScore || 100"
            placeholder="请输入成绩"
          />
          <span style="margin-left: 10px; color: #9ca3af;">
            / {{ currentAssignment?.totalScore || 100 }} 分
          </span>
        </el-form-item>
        <el-form-item label="评语">
          <el-input
            v-model="gradingForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请输入评语"
          />
        </el-form-item>
        <el-form-item label="发布成绩">
          <el-switch v-model="gradingForm.released" />
          <span style="margin-left: 10px; color: #9ca3af; font-size: 12px;">
            开启后学生可以看到成绩
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGradingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitGrading" :loading="grading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElButton, ElTag, ElSelect, ElOption, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSwitch, ElIcon, ElMessage, ElTable, ElTableColumn } from 'element-plus'
import { getAssignments as getTeacherAssignments, getSubmissions, gradeSubmission } from '@/api/teacher'
import { downloadFile as downloadFileApi } from '@/api/file'
import { useUserStore } from '@/stores/userStore'

defineOptions({
  name: 'TeacherGradingView'
})

const { currentUser } = useUserStore()
const assignments = ref([])
const selectedAssignmentId = ref(null)
const submissions = ref([])
const showGradingDialog = ref(false)
const showFilesDialog = ref(false)
const grading = ref(false)
const currentSubmission = ref(null)
const currentAssignment = ref(null)

const gradingForm = ref({
  score: null,
  feedback: '',
  released: true
})

const formatTime = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const loadAssignments = async () => {
  try {
    const response = await getTeacherAssignments()
    assignments.value = response.data || []
    // 如果只有一个作业，自动选择
    if (assignments.value.length === 1 && !selectedAssignmentId.value) {
      selectedAssignmentId.value = assignments.value[0].id
      loadSubmissions()
    }
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  }
}

// 监听选择作业事件（从作业管理页面触发）
window.addEventListener('select-assignment', (event) => {
  if (event.detail && event.detail.assignmentId) {
    selectedAssignmentId.value = event.detail.assignmentId
    loadSubmissions()
  }
})

const loadSubmissions = async () => {
  if (!selectedAssignmentId.value) return

  try {
    console.log('正在获取提交列表，作业ID:', selectedAssignmentId.value)
    const response = await getSubmissions(selectedAssignmentId.value)
    console.log('获取提交列表响应:', response)
    submissions.value = response.data || []
    currentAssignment.value = assignments.value.find(a => a.id === selectedAssignmentId.value)
    
    if (submissions.value.length === 0) {
      ElMessage.info('该作业暂无提交记录')
    } else {
      console.log('成功获取提交列表，共', submissions.value.length, '条记录')
    }
  } catch (error) {
    console.error('获取提交列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    const errorMessage = error.response?.data?.message || error.message || '获取提交列表失败'
    ElMessage.error(errorMessage)
    submissions.value = []
  }
}

const openGradingDialog = (submission) => {
  currentSubmission.value = submission
  gradingForm.value = {
    score: submission.score || null,
    feedback: submission.feedback || '',
    released: submission.released !== false
  }
  showGradingDialog.value = true
}

const viewFiles = (submission) => {
  currentSubmission.value = submission
  showFilesDialog.value = true
}

const submitGrading = async () => {
  if (gradingForm.value.score === null) {
    ElMessage.warning('请输入成绩')
    return
  }

  grading.value = true
  try {
    await gradeSubmission(
      selectedAssignmentId.value,
      currentSubmission.value.id,
      {
        ...gradingForm.value,
        teacherId: currentUser.value?.id
      }
    )
    ElMessage.success('批改成功')
    showGradingDialog.value = false
    // 刷新提交列表
    await loadSubmissions()
  } catch (error) {
    console.error('批改失败:', error)
    ElMessage.error('批改失败，请重试')
  } finally {
    grading.value = false
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

// 下载文件
const downloadFile = async (fileId, fileName) => {
  try {
    const response = await downloadFileApi(fileId)
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
    ElMessage.error('下载失败，请重试')
  }
}

// 监听选择作业事件（从作业管理页面触发）
const handleSelectAssignment = (event) => {
  if (event.detail && event.detail.assignmentId) {
    selectedAssignmentId.value = event.detail.assignmentId
    loadSubmissions()
  }
}

onMounted(() => {
  loadAssignments()
  window.addEventListener('select-assignment', handleSelectAssignment)
})

onUnmounted(() => {
  window.removeEventListener('select-assignment', handleSelectAssignment)
})
</script>

<style scoped>
.teacher-grading-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.subtitle {
  margin: 0;
  color: #9ca3af;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.submission-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #3b82f6;
}

.submission-card.graded {
  border-left-color: #10b981;
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.student-name {
  margin: 0;
  font-weight: 600;
  color: #111827;
}

.submit-time {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.submission-content {
  margin: 16px 0;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.content-label {
  margin: 0 0 8px;
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}

.content-text {
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
}

.grade-info {
  margin: 16px 0;
  padding: 12px;
  background: #ecfdf5;
  border-radius: 8px;
}

.grade-info p {
  margin: 4px 0;
  color: #065f46;
}

.submission-actions {
  margin-top: 16px;
}

.submission-files {
  margin: 16px 0;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #fff;
  border-radius: 6px;
}

.file-name {
  flex: 1;
  color: #374151;
  font-size: 14px;
}

.file-size {
  color: #9ca3af;
  font-size: 12px;
}

.file-list-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item-dialog {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.file-info {
  flex: 1;
}

.file-info .file-name {
  margin: 0 0 4px;
  font-size: 14px;
  color: #374151;
}

.file-info .file-size {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.empty-files {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}
</style>


