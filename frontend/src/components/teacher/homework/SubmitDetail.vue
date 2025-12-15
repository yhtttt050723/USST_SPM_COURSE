<template>
    <div class="submit-detail-page">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>

        <div class="header">
            <div class="filter-section">
                <el-radio-group v-model="filterStatus" size="small">
                    <el-radio-button label="all">全部</el-radio-button>
                    <el-radio-button label="pending">待批改</el-radio-button>
                    <el-radio-button label="graded">已批改</el-radio-button>
                </el-radio-group>
            </div>
            <div class="stats-section">
                <div class="count-submit">提交人数 : {{ submissionCount }}</div>
                <div class="count-wait">待批改人数 : {{ pendingCount }}</div>
            </div>
        </div>
        <div class="body">
            <el-row class="tip">
                <el-col :span="12" class="name">学生姓名</el-col>
                <el-col :span="6" class="time">提交时间</el-col>
                <el-col :span="6" class="status">提交状态</el-col>
            </el-row>

            <div class="detail-box">
                <SubmitDetailBox
                  v-for="submitDetail in filteredSubmitDetails"
                  :key="submitDetail.id"
                  :submitDetail="submitDetail"
                  @click="goToStudentHomework(submitDetail)"
                />
            </div>
        </div>
        <!-- 空状态 -->
        <div v-if="!loading && filteredSubmitDetails.length === 0" class="empty">
          <el-empty description="暂无提交" />
        </div>
    </div>
</template>

<script setup>
import SubmitDetailBox from '@/components/teacher/homework/SubmitDetailBox.vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({
    assignmentId: {
        type: Number,
        required: true
    }
})

// 提交列表数据
const submitDetails = ref([])
const loading = ref(false)
const filterStatus = ref('all')

// 筛选后的提交列表
const filteredSubmitDetails = computed(() => {
  if (filterStatus.value === 'pending') {
    return submitDetails.value.filter(detail => !detail.graded)
  } else if (filterStatus.value === 'graded') {
    return submitDetails.value.filter(detail => detail.graded)
  }
  return submitDetails.value
})

// 提交人数统计
const submissionCount = computed(() => submitDetails.value.length)

// 待批改人数（未评分或未发布成绩的）
const pendingCount = computed(() => {
  return submitDetails.value.filter(detail => !detail.graded).length
})

// 获取作业的所有提交
const fetchSubmissions = async () => {
    if (!props.assignmentId) {
      console.warn('作业ID不存在')
      return
    }
    
    loading.value = true
    try {
      const response = await request.get(`/assignments/${props.assignmentId}/submissions`)
      submitDetails.value = response.data || []
    } catch (error) {
      console.error('获取提交详情失败:', error)
      ElMessage.error('获取提交详情失败')
      submitDetails.value = []
    } finally {
      loading.value = false
    }
}

const goToStudentHomework = (submitDetail) => {
  router.push({ 
    name: 'TeacherStudentHomework', 
    params: { submissionId: submitDetail.id },
    query: { assignmentId: props.assignmentId }
  })
}

onMounted(() => {
  fetchSubmissions()
})
</script>

<style scoped>
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background-color: #f5f5f5;
    border-radius: 25px;
    margin-bottom: 20px;
}

.filter-section {
    flex: 1;
}

.stats-section {
    display: flex;
    gap: 15px;
}

.title {
    margin: 0px;
}
.count-submit, .count-wait {
    font-size: 14px;
    color: #666;
    background-color: #e6e1e1;
    padding: 5px 10px;
    border-radius: 12px;
    text-align: right;
}
.tip {
    font-size: 16px;
    color: #999;
    padding: 10px 20px;
    border-bottom: 1px solid #e5e7eb;
    background-color: #f5f5f5;
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
}
.tip .time, .tip .status {
    font-weight: 400;
    text-align: center;
}
.tip .name {
    font-weight: 400;
    text-align: left;
}
</style>