<template>
  <div class="homework-card">
    <div class="card-header">
      <div class="header-left">
        <p class="card-label">待办作业</p>
        <h3 class="card-title">{{ pendingAssignments.length }} 项近期截止</h3>
      </div>
      <el-tag size="small" type="warning">紧急</el-tag>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <el-skeleton :rows="3" animated />
    </div>
    
    <!-- 作业列表 -->
    <div v-else class="homework-content">
      <div v-if="recentPendingAssignments.length > 0" class="homework-list">
        <div 
          v-for="work in recentPendingAssignments" 
          :key="work.id" 
          class="homework-item"
        >
          <div class="homework-info">
            <p class="homework-title">{{ work.title }}</p>
            <p class="homework-deadline">
              <el-icon><Clock /></el-icon>
              截止时间：{{ formatDeadline(work.dueAt) }}
            </p>
          </div>
          <span class="homework-status" :class="getStatusClass(work.submissionStatus)">
            {{ getStatusText(work.submissionStatus) }}
          </span>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无待办作业" :image-size="100" />
      </div>
    </div>
    
    <!-- 查看更多按钮 -->
    <div v-if="pendingAssignments.length > 3" class="card-footer">
      <el-button type="primary" text @click="viewAllAssignments">
        查看全部 {{ pendingAssignments.length }} 项作业
        <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, ArrowRight } from '@element-plus/icons-vue'
import { getAssignments } from '@/api/assignment'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const assignments = ref([])
const loading = ref(false)

// 当前用户信息
const currentUser = computed(() => userStore.currentUser || {})
const userId = computed(() => currentUser.value.id)

// 计算属性：获取待办作业（submissionStatus 为 'progress' 的作业）
const pendingAssignments = computed(() => {
  return assignments.value
    .filter(assignment => assignment.submissionStatus === 'progress')
    .sort((a, b) => new Date(a.dueAt) - new Date(b.dueAt)) // 按截止时间排序
})

// 计算属性：获取最近的三条待办作业
const recentPendingAssignments = computed(() => {
  return pendingAssignments.value.slice(0, 3)
})

// 获取作业数据
const fetchAssignments = async () => {
  if (!userId.value) {
    console.warn('用户ID不存在，无法获取作业')
    return
  }

  loading.value = true
  try {
    const response = await getAssignments('all', userId.value, 'STUDENT')
    assignments.value = response.data || []
  } catch (error) {
    console.error('获取作业数据失败:', error)
    // 静默失败，不显示错误提示，只在控制台记录
    assignments.value = []
  } finally {
    loading.value = false
  }
}

// 格式化截止时间
const formatDeadline = (dueAt) => {
  if (!dueAt) return '未设置'
  
  const deadline = new Date(dueAt)
  const now = new Date()
  const diffTime = deadline.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) {
    return '已过期'
  } else if (diffDays === 0) {
    return '今天 ' + deadline.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else if (diffDays === 1) {
    return '明天 ' + deadline.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else if (diffDays <= 7) {
    return `${diffDays}天后`
  } else {
    return deadline.toLocaleDateString('zh-CN', { 
      year: 'numeric',
      month: '2-digit', 
      day: '2-digit'
    })
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'progress': '进行中',
    'ONGOING': '进行中',
    'submitted': '已提交',
    'ENDED': '已结束',
    'graded': '已批改'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    'progress': 'status-ongoing',
    'ONGOING': 'status-ongoing',
    'submitted': 'status-submitted',
    'ENDED': 'status-ended',
    'graded': 'status-graded'
  }
  return classMap[status] || 'status-default'
}

// 查看全部作业
const viewAllAssignments = () => {
  router.push('/homework')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAssignments()
})
</script>

<style scoped>
.homework-card {
  background: #ffffff;
  border-radius: 28px;
  padding: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
  transition: all 0.3s ease;
  height: 720px;
}

.homework-card:hover {
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.card-label {
  color: #64748b;
  font-size: 14px;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.card-title {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  font-weight: 600;
}

.loading-section {
  padding: 20px 0;
}

.homework-content {
  min-height: 200px;
}

.homework-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.homework-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.homework-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.homework-info {
  flex: 1;
  min-width: 0;
}

.homework-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.homework-deadline {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.homework-deadline .el-icon {
  font-size: 14px;
}

.homework-status {
  padding: 6px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
  margin-left: 12px;
}

.status-ongoing {
  background: #fff7ed;
  color: #ea580c;
}

.status-submitted {
  background: #eff6ff;
  color: #2563eb;
}

.status-ended {
  background: #f0fdf4;
  color: #16a34a;
}

.status-graded {
  background: #faf5ff;
  color: #9333ea;
}

.status-default {
  background: #f1f5f9;
  color: #64748b;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.card-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
  text-align: center;
}

.card-footer .el-button {
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .homework-card {
    padding: 20px;
    border-radius: 20px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .card-title {
    font-size: 20px;
  }
  
  .homework-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 14px;
  }
  
  .homework-status {
    align-self: flex-end;
    margin-left: 0;
  }
  
  .homework-title {
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

@media (max-width: 480px) {
  .homework-card {
    padding: 16px;
  }
  
  .card-title {
    font-size: 18px;
  }
  
  .homework-item {
    padding: 12px;
  }
}
</style>
