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

    <div v-if="selectedAssignmentId" class="submissions-list">
      <div
        v-for="submission in submissions"
        :key="submission.id"
        class="submission-card"
        :class="{ graded: submission.graded }"
      >
        <div class="submission-header">
          <div>
            <p class="student-name">{{ submission.studentName }} ({{ submission.studentNo }})</p>
            <p class="submit-time">提交时间：{{ formatTime(submission.submittedAt) }}</p>
          </div>
          <el-tag v-if="submission.graded" type="success">已批改</el-tag>
          <el-tag v-else type="warning">待批改</el-tag>
        </div>

        <div class="submission-content">
          <p class="content-label">提交内容：</p>
          <div class="content-text">{{ submission.content || '无内容' }}</div>
        </div>

        <div v-if="submission.graded" class="grade-info">
          <p>成绩：<strong>{{ submission.score }}</strong> 分</p>
          <p>评语：{{ submission.feedback || '无评语' }}</p>
        </div>

        <div class="submission-actions">
          <el-button
            v-if="!submission.graded"
            type="primary"
            @click="openGradingDialog(submission)"
          >
            批改
          </el-button>
          <el-button
            v-else
            type="primary"
            plain
            @click="openGradingDialog(submission)"
          >
            修改成绩
          </el-button>
        </div>
      </div>

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
import { ref, onMounted } from 'vue'
import { ElButton, ElTag, ElSelect, ElOption, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSwitch } from 'element-plus'
import { getAssignments as getTeacherAssignments, getSubmissions, gradeSubmission } from '@/api/teacher'
import { useUserStore } from '@/stores/userStore'

defineOptions({
  name: 'TeacherGradingView'
})

const { currentUser } = useUserStore()
const assignments = ref([])
const selectedAssignmentId = ref(null)
const submissions = ref([])
const showGradingDialog = ref(false)
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
  } catch (error) {
    console.error('获取作业列表失败:', error)
  }
}

const loadSubmissions = async () => {
  if (!selectedAssignmentId.value) return

  try {
    const response = await getSubmissions(selectedAssignmentId.value)
    submissions.value = response.data || []
    currentAssignment.value = assignments.value.find(a => a.id === selectedAssignmentId.value)
  } catch (error) {
    console.error('获取提交列表失败:', error)
    // 使用模拟数据
    submissions.value = [
      {
        id: 1,
        studentId: 1,
        studentName: '张三',
        studentNo: '2021001',
        content: '这是学生的作业提交内容...',
        submittedAt: '2025-09-15T10:30:00',
        graded: false
      },
      {
        id: 2,
        studentId: 2,
        studentName: '李四',
        studentNo: '2021002',
        content: '这是另一个学生的作业提交内容...',
        submittedAt: '2025-09-15T14:20:00',
        graded: true,
        score: 92,
        feedback: '分析充分，注意格式。'
      }
    ]
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

const submitGrading = async () => {
  if (gradingForm.value.score === null) {
    alert('请输入成绩')
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
    showGradingDialog.value = false
    loadSubmissions()
  } catch (error) {
    console.error('批改失败:', error)
    alert('批改失败，请重试')
  } finally {
    grading.value = false
  }
}

onMounted(() => {
  loadAssignments()
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}
</style>

