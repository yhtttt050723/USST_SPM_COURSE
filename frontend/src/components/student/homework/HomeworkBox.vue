<template>
  <el-row class="box">
    <el-col class="homeworkTitle-box" :span="14">
        <div class="homeworkTitle">{{ assignment.title }}</div>
    </el-col>
    <el-col class="time-box" :span="6">
        <div class="time">{{ formatTime(assignment.dueAt) }}</div>
    </el-col>
    <el-col class="state-box" :span="4">
        <el-tag :type="getStatusType(assignment.submissionStatus)" size="large">
          {{ getStatusText(assignment.submissionStatus) }}
        </el-tag>
    </el-col>
  </el-row>
</template>

<script setup>
const props = defineProps({
  assignment: {
    type: Object,
    required: true
  }
})

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

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'progress': 'warning',
    'submitted': 'primary',
    'graded': 'success',
    'ended': 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本（和后端确认status）
const getStatusText = (status) => {
  const map = {
    'progress': '未提交',
    'submitted': '已提交',
    'graded': '已批改',
    'ended': '已截止'
  }
  return map[status] || '未知'
}
</script>

<style scoped>
.box {
    width: 100%;
    min-height: 80px;
    background-color: white;
    margin-top: 12px;
    border-top-left-radius: 25px;
    border-top-right-radius: 25px;
    border-bottom: 1px solid #b8bdc4;
    cursor: pointer;
    transition: all 0.3s ease;
}

.homeworkTitle-box {
    display: flex;
    align-items: center;
    padding: 20px 25px;
}

.homeworkTitle {
    font-size: 16px;
    font-weight: 500;
    color: #0f172a;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.time-box {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 25px;
}

.time {
    font-size: 14px;
    color: #64748b;
}

.state-box {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 25px;
}
</style>