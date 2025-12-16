<template>
  <div class="homework-page">
    <el-row class="teacher-homework-header">
      <el-col class="homeworkTitle" :span="14">
        <el-button class="createNew" type="primary" @click="goToCreateHomework" > 发布新作业 </el-button>
      </el-col>
      <el-col class="time" :span="6">截止时间</el-col>
      <el-col class="count" :span="4">提交人数</el-col>
    </el-row>
    <div class="body">
      <!-- 加载状态 -->
    <div v-if="loading" class="loading">  
      <el-skeleton :rows="3" animated />
    </div>
    
    <!-- 作业列表 -->
    <div v-else class="homework-list">
      <HomeworkBox 
        v-for="assignment in assignments" 
        :key="assignment.id"
        :assignment="assignment"
        @click="goToDetail(assignment.id)"
      />
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && assignments.length === 0" class="empty">
      <el-empty description="暂无作业" />
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssignments } from '@/api/assignment'
import { useUserStore } from '@/stores/useUserStore'
import HomeworkBox from '@/components/teacher/homework/HomeworkBox.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rawAssignments = ref([])
const filterType = ref('all')

// 当前用户信息
const currentUser = computed(() => userStore.currentUser || {})
const userId = computed(() => currentUser.value.id)

// 筛选和排序后的作业列表
const assignments = computed(() => {
  const now = new Date()
  const list = [...rawAssignments.value]
  
  // 分为未截止和已截止两组
  const notExpired = []
  const expired = []
  
  list.forEach(assignment => {
    if (assignment.dueAt && new Date(assignment.dueAt) < now) {
      expired.push(assignment)
    } else {
      notExpired.push(assignment)
    }
  })
  
  // 未截止的作业按截止时间升序（最早的在前）
  notExpired.sort((a, b) => {
    if (!a.dueAt) return 1
    if (!b.dueAt) return -1
    return new Date(a.dueAt) - new Date(b.dueAt)
  })
  
  // 已截止的作业按截止时间降序（最近截止的在前）
  expired.sort((a, b) => {
    if (!a.dueAt) return 1
    if (!b.dueAt) return -1
    return new Date(b.dueAt) - new Date(a.dueAt)
  })
  
  // 未截止的在前，已截止的在后
  return [...notExpired, ...expired]
})

// 获取作业列表
const fetchAssignments = async () => {
  if (!userId.value) {
    console.warn('用户ID不存在，请先登录')
    ElMessage.warning('请先登录')
    return
  }
  
  loading.value = true
  try {
    const response = await getAssignments('all', userId.value, 'TEACHER')
    console.log('获取作业列表响应:', response)
    
    // 处理响应数据：可能是数组直接返回，也可能是 {data: []} 格式
    if (Array.isArray(response)) {
      // 直接返回数组
      rawAssignments.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      // 标准格式：{code, data: []}
      rawAssignments.value = response.data
    } else if (response && Array.isArray(response)) {
      // 双重包装的情况
      rawAssignments.value = response
    } else {
      console.warn('响应数据格式异常:', response)
      rawAssignments.value = []
    }
    
    console.log('解析后的作业列表:', rawAssignments.value)
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error(error.message || '获取作业列表失败')
    rawAssignments.value = []
  } finally {
    loading.value = false
  }
}


// 跳转到作业详情
const goToDetail = (assignmentId) => {
  router.push({
    path: '/teacher/homework/detail',
    query: { id: assignmentId }
  })
}

// 处理筛选变化
const handleFilterChange = () => {
  // 筛选逻辑已在 computed 中处理
}

const goToCreateHomework = () => {
  router.push({ path: '/teacher/homework/create' })
}

onMounted(() => {
  // 确保从缓存恢复用户信息
  if (!userStore.currentUser) {
    userStore.hydrateUserFromCache()
  }
  console.log('当前用户信息:', userStore.currentUser)
  console.log('用户ID:', userId.value)
  fetchAssignments()
})
</script>

<style scoped>
.homework-page {
  margin-bottom: 20px;
}

.el-col {
  margin: 15px 0px;
  padding: 0px 20px;
}
.el-col.homeworkTitle {
  font-size: 15px;
  color: #999;

}
.el-col.time, .el-col.count {
  font-size: 15px;
  color: #999;
  text-align: center;
}
.body {
  width: 100%;
  height: auto;
  background-color: white;
  border-radius: 25px;
}
.createNew {
  margin-left: 20px;
  font-size: 16px; 
  font-weight: bold;
  width: 30%;
}
.homework-list {
  display: flex;
  flex-direction: column;
  padding: 0 20px 20px 20px;
}
</style>
