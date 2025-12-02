<template>
    <!-- 后续添加统计功能？ -->
     <el-row class="box" :gutter="16">
        <el-col :span="12">
            <div class="box-item">
                <p class="ititle">出勤统计</p>
                <div class="progress-box">
                    <el-progress type="circle" :percentage="attendancePercentage" />
                </div>
                <div class="stats-info">
                    <span class="stats-text">已出勤 {{ attendanceCount }} / {{ totalAttendance }} 次</span>
                </div>
            </div>
        </el-col>
        <el-col :span="12">
            <div class="box-item">
                <p class="ititle">作业完成度</p>
                <div class="progress-box">
                    <el-progress 
                    type="circle" 
                    :percentage="homeworkPercentage"
                    :color="homeworkColor"
                />
                </div>
                <div class="stats-info">
                    <span class="stats-text">已提交 {{ submittedCount }} / {{ totalAssignments }} 份</span>
                </div>
            </div>
        </el-col>
     </el-row>
    
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAssignments } from '@/api/assignment'
import { useUserStore } from '@/stores/useUserStore'

const userStore = useUserStore()
const currentUser = computed(() => userStore.currentUser || {})

// 作业统计数据
const totalAssignments = ref(0)
const submittedCount = ref(0)

// 出勤统计数据（暂时使用模拟数据）
const totalAttendance = ref(0)
const attendanceCount = ref(0)

// 计算作业完成百分比
const homeworkPercentage = computed(() => {
    if (totalAssignments.value === 0) return 0
    return Math.round((submittedCount.value / totalAssignments.value) * 100)
})

const attendancePercentage = computed(() => {
    if (totalAttendance.value === 0) return 0
    return Math.round((attendanceCount.value / totalAttendance.value) * 100)
})

const homeworkColor = computed(() => {
    const percentage = homeworkPercentage.value
    if (percentage >= 80) return '#67c23a' // 绿色
    if (percentage >= 60) return '#e6a23c' // 橙色
    return '#f56c6c' // 红色
})

// 获取作业统计数据
const fetchAssignmentStats = async () => {
    try {
        const userId = currentUser.value.id
        if (!userId) return

        // 获取作业列表（包含提交状态）
        const response = await getAssignments('all', userId, 'STUDENT')
        const assignments = response.data || []
        
        // 统计总作业数
        totalAssignments.value = assignments.length
        
        // 统计已提交作业数（状态为 submitted 或 graded）
        submittedCount.value = assignments.filter(assignment => {
            const status = (assignment.submissionStatus || '').toLowerCase()
            return status === 'submitted' || status === 'graded'
        }).length
    } catch (error) {
        console.error('获取作业统计失败:', error)
    }
}

onMounted(() => {
    fetchAssignmentStats()
})
</script>
    
<style scoped>
.demo-progress .el-progress--circle {
  margin-right: 15px;
}
.progress-box {
    display: flex;
    justify-content: center;
    align-items: center;
}

.box {
    height: 330px;
}
.box-item {
    background-color: white;
    border-radius: 20px;
    padding: 24px;
    height: 100%;
}
.ititle {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 30px;
    text-align: center;
}

.stats-info {
    margin-top: 20px;
    text-align: center;
}

.stats-text {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
}
</style>