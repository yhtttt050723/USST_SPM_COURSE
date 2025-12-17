<template>
  <div class="teacher-assignments-page">
    <div class="page-header">
      <div>
        <p class="subtitle">作业管理</p>
        <h2>本学期作业总览</h2>
      </div>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        发布新作业
      </el-button>
    </div>

    <div class="assignments-table">
      <table>
        <thead>
          <tr>
            <th>作业名称</th>
            <th>截止时间</th>
            <th>提交人数</th>
            <th>已批改</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="assignment in assignments" :key="assignment.id">
            <td>
              <div class="assignment-info">
                <p class="title">{{ assignment.title }}</p>
                <p class="desc">{{ assignment.description || '无描述' }}</p>
              </div>
            </td>
            <td>{{ formatDeadline(assignment.dueAt) }}</td>
            <td>{{ assignment.submissionCount || 0 }} / {{ assignment.totalStudents || 0 }}</td>
            <td>{{ assignment.gradedCount || 0 }}</td>
            <td>
              <el-button size="small" @click="viewSubmissions(assignment.id)">查看提交</el-button>
              <el-button size="small" type="primary" @click="editAssignment(assignment)">编辑</el-button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 发布/编辑作业对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingAssignment ? '编辑作业' : '发布新作业'"
      width="600px"
    >
      <el-form :model="assignmentForm" label-width="100px">
        <el-form-item label="作业标题" required>
          <el-input v-model="assignmentForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="作业描述">
          <el-input
            v-model="assignmentForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入作业描述"
          />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="assignmentForm.dueAt"
            type="datetime"
            placeholder="选择截止时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="assignmentForm.totalScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="允许重复提交">
          <el-switch v-model="assignmentForm.allowResubmit" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAssignment" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElButton, ElIcon, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElDatePicker, ElSwitch, ElMessage } from 'element-plus'
import { getTeacherAssignments, createAssignment } from '@/api/teacher'
import { useUserStore } from '@/stores/useUserStore'

defineOptions({
  name: 'TeacherAssignmentsView'
})

const assignments = ref([])
const userStore = useUserStore()
const showCreateDialog = ref(false)
const saving = ref(false)
const editingAssignment = ref(null)

const assignmentForm = ref({
  title: '',
  description: '',
  dueAt: '',
  totalScore: 100,
  allowResubmit: false
})

const formatDeadline = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const fetchAssignments = async () => {
  const courseId = userStore.currentCourse?.id
  if (!courseId) {
    ElMessage.error('请选择课程')
    return
  }
  try {
    const response = await getTeacherAssignments(courseId)
    assignments.value = response.data || []
  } catch (error) {
    console.error('获取作业列表失败:', error)
    // 使用模拟数据
    assignments.value = [
      {
        id: 1,
        title: '项目需求调研报告',
        description: '提交访谈纪要与痛点分析 PPT',
        dueAt: '2025-09-18T18:00:00',
        submissionCount: 25,
        totalStudents: 30,
        gradedCount: 20
      },
      {
        id: 2,
        title: '数据库设计文档',
        description: '包含 ER 图 / 物理结构 / 索引策略',
        dueAt: '2025-09-21T23:59:00',
        submissionCount: 28,
        totalStudents: 30,
        gradedCount: 15
      }
    ]
  }
}

const saveAssignment = async () => {
  const courseId = userStore.currentCourse?.id
  if (!courseId) {
    ElMessage.error('请选择课程')
    return
  }
  if (!assignmentForm.value.title) {
    alert('请输入作业标题')
    return
  }
  if (!assignmentForm.value.dueAt) {
    alert('请选择截止时间')
    return
  }

  saving.value = true
  try {
    if (editingAssignment.value) {
      // 更新作业逻辑（待实现）
      console.log('更新作业:', assignmentForm.value)
    } else {
      await createAssignment({
        ...assignmentForm.value,
        courseId
      })
    }
    showCreateDialog.value = false
    resetForm()
    fetchAssignments()
  } catch (error) {
    console.error('保存作业失败:', error)
    alert('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const editAssignment = (assignment) => {
  editingAssignment.value = assignment
  assignmentForm.value = {
    title: assignment.title,
    description: assignment.description || '',
    dueAt: assignment.dueAt,
    totalScore: assignment.totalScore || 100,
    allowResubmit: assignment.allowResubmit || false
  }
  showCreateDialog.value = true
}

const viewSubmissions = (assignmentId) => {
  // 触发父组件切换导航到批改作业页面
  // 通过事件通知父组件
  const event = new CustomEvent('view-submissions', { detail: { assignmentId } })
  window.dispatchEvent(event)
}

const resetForm = () => {
  assignmentForm.value = {
    title: '',
    description: '',
    dueAt: '',
    totalScore: 100,
    allowResubmit: false
  }
  editingAssignment.value = null
}

onMounted(() => {
  fetchAssignments()
})
</script>

<style scoped>
.teacher-assignments-page {
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

.assignments-table {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f9fafb;
}

th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
}

td {
  padding: 16px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.assignment-info .title {
  margin: 0 0 4px;
  font-weight: 600;
  color: #111827;
}

.assignment-info .desc {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}
</style>

