<template>
  <div class="assignment-page">
    <div class="page-header">
      <div>
        <p class="subtitle">本学期 · 作业总览</p>
        <h2>作业进度追踪</h2>
      </div>
      <el-select v-model="filterStatus" placeholder="筛选状态" size="small" class="status-filter" style="width: 160px" @change="handleStatusChange">
        <el-option label="全部状态" value="all" />
        <el-option label="进行中" value="progress" />
        <el-option label="待批改" value="submitted" />
        <el-option label="已完成" value="graded" />
      </el-select>
    </div>

    <div class="table-head">
      <span>作业名称</span>
      <span>当前状态</span>
      <span>成绩</span>
    </div>

    <div class="assignment-list">
      <div
        v-for="item in filteredAssignments"
        :key="item.id"
        class="assignment-card"
        :class="item.status"
      >
        <div class="meta">
          <p class="title">{{ item.title }}</p>
          <p class="desc">{{ item.desc }}</p>
        </div>
        <div class="status">
          <el-tag :type="statusType(item.status)" round>{{ statusLabel(item.status) }}</el-tag>
          <p class="deadline">截止 {{ item.deadline }}</p>
        </div>
        <div class="score">
          <p class="score-value">{{ item.score ?? '--' }}</p>
          <p class="score-label">{{ item.score ? '已评分' : '未评分' }}</p>
        </div>
        <div class="actions" v-if="item.status !== 'graded'">
          <el-button 
            size="small" 
            type="primary" 
            @click="openSubmitDialog(item)"
            :disabled="item.status === 'submitted' && !item.allowResubmit"
          >
            {{ item.status === 'submitted' ? '重新提交' : '提交作业' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 提交作业对话框 -->
    <el-dialog
      v-model="showSubmitDialog"
      :title="`提交作业 - ${currentAssignment?.title || ''}`"
      width="600px"
    >
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="作业标题">
          <el-input :value="currentAssignment?.title || ''" disabled />
        </el-form-item>
        <el-form-item label="作业描述">
          <el-input 
            :value="currentAssignment?.desc || ''" 
            type="textarea" 
            :rows="2" 
            disabled 
          />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-input :value="currentAssignment?.deadline || ''" disabled />
        </el-form-item>
        <el-form-item label="提交内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入作业内容（可选）..."
          />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            :limit="5"
            accept=".pdf,.doc,.docx,.txt,.xls,.xlsx,.ppt,.pptx"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、DOC、DOCX、TXT、XLS、XLSX、PPT、PPTX 格式，最多5个文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAssignment" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { getAssignments, submitAssignment as submitAssignmentApi } from '@/api/assignment'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/userStore'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage, ElUpload, ElSelect, ElOption } from 'element-plus'

defineOptions({
  name: 'AssignmentsView'
})

const { currentUser } = useUserStore()
const filterStatus = ref('all')
const assignments = ref([])
const loading = ref(false)
const showSubmitDialog = ref(false)
const submitting = ref(false)
const currentAssignment = ref(null)

const submitForm = ref({
  content: ''
})
const fileList = ref([])
const uploadRef = ref(null)
const uploading = ref(false)

// 格式化日期为 MM-dd HH:mm
const formatDeadline = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 获取作业列表
const fetchAssignments = async () => {
  if (!currentUser.value || !currentUser.value.id) {
    console.warn('用户未登录，无法获取作业列表')
    return
  }

  loading.value = true
  try {
    const response = await getAssignments(filterStatus.value, currentUser.value.id)
    assignments.value = response.data.map(item => ({
      id: item.id,
      title: item.title,
      desc: item.description || '',
      status: item.submissionStatus || 'progress',
      deadline: formatDeadline(item.dueAt),
      score: item.score || null
    }))
  } catch (error) {
    console.error('获取作业列表失败:', error)
    // 如果后端未实现，使用模拟数据
    assignments.value = [
      {
        id: 1,
        title: '项目需求调研报告',
        desc: '提交访谈纪要与痛点分析 PPT',
        status: 'progress',
        deadline: '09-18 18:00',
        score: null
      },
      {
        id: 2,
        title: '数据库设计文档',
        desc: '包含 ER 图 / 物理结构 / 索引策略',
        status: 'submitted',
        deadline: '09-21 23:59',
        score: null
      },
      {
        id: 3,
        title: '第一阶段评审',
        desc: '进行系统 Demo 演示与答辩',
        status: 'graded',
        deadline: '09-08 20:00',
        score: 92
      },
      {
        id: 4,
        title: '迭代回顾总结',
        desc: '撰写 Retrospective 总结文档',
        status: 'progress',
        deadline: '09-25 20:00',
        score: null
      }
    ]
  } finally {
    loading.value = false
  }
}

// 监听筛选状态变化
const handleStatusChange = () => {
  fetchAssignments()
}

const filteredAssignments = computed(() => {
  if (filterStatus.value === 'all') {
    return assignments.value
  }
  return assignments.value.filter((item) => item.status === filterStatus.value)
})

const statusLabel = (status) =>
  ({
    progress: '进行中',
    submitted: '待批改',
    graded: '已完成'
  }[status] || '未知')

const statusType = (status) =>
  ({
    progress: 'warning',
    submitted: 'info',
    graded: 'success'
  }[status] || 'info')

// 打开提交作业对话框
const openSubmitDialog = (assignment) => {
  currentAssignment.value = assignment
  submitForm.value.content = ''
  fileList.value = []
  showSubmitDialog.value = true
}

// 处理文件变化
const handleFileChange = (file) => {
  // 文件已添加到列表
}

// 处理文件移除
const handleFileRemove = (file) => {
  // 文件已从列表移除
}

// 提交作业
const submitAssignment = async () => {
  if (!submitForm.value.content.trim() && fileList.value.length === 0) {
    ElMessage.warning('请至少填写提交内容或上传文件')
    return
  }

  if (!currentUser.value?.id || !currentAssignment.value) {
    ElMessage.error('提交失败，请重试')
    return
  }

  submitting.value = true
  uploading.value = true
  
  try {
    // 先上传文件
    const attachmentIds = []
    for (const file of fileList.value) {
      if (file.raw) {
        try {
          const formData = new FormData()
          formData.append('file', file.raw)
          formData.append('uploaderId', currentUser.value.id.toString())
          
          const uploadResponse = await uploadFile(formData)
          attachmentIds.push(uploadResponse.data.id)
        } catch (error) {
          console.error('文件上传失败:', error)
          ElMessage.error(`文件 ${file.name} 上传失败`)
          uploading.value = false
          submitting.value = false
          return
        }
      }
    }
    
    uploading.value = false

    // 提交作业
    await submitAssignmentApi(currentAssignment.value.id, {
      content: submitForm.value.content || '',
      attachmentIds: attachmentIds,
      studentId: currentUser.value.id
    })
    
    ElMessage.success('提交成功')
    showSubmitDialog.value = false
    submitForm.value.content = ''
    fileList.value = []
    // 刷新作业列表
    await fetchAssignments()
  } catch (error) {
    console.error('提交作业失败:', error)
    ElMessage.error(error?.response?.data?.message || '提交失败，请重试')
  } finally {
    submitting.value = false
    uploading.value = false
  }
}

// 添加定时刷新，以便获取最新的成绩
let refreshInterval = null

onMounted(() => {
  fetchAssignments()
  // 每30秒自动刷新一次，获取最新成绩
  refreshInterval = setInterval(() => {
    if (currentUser.value?.id) {
      fetchAssignments()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.assignment-page {
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

.table-head {
  display: grid;
  grid-template-columns: 2fr 1fr 120px;
  padding: 0 24px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 12px;
}

.assignment-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.assignment-card {
  display: grid;
  grid-template-columns: 2fr 1fr 120px auto;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 24px;
  padding: 20px 24px;
  box-shadow: 0 20px 35px rgba(15, 23, 42, 0.08);
  border-left: 6px solid transparent;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.assignment-card.progress {
  border-color: #f97316;
}

.assignment-card.submitted {
  border-color: #0ea5e9;
}

.assignment-card.graded {
  border-color: #10b981;
}

.meta .title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.meta .desc {
  margin: 0;
  color: #6b7280;
}

.status {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.deadline {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.score {
  text-align: right;
}

.score-value {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.score-label {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}
</style>

