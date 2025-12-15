<template>
    <el-row class="box">
        <el-col class="name clickable" :span="10" @click="handleClick">
          {{ submitDetail.studentName || '未知学生' }}
        </el-col>
        <el-col class="time" :span="6" @click="handleClick">
          {{ formatTime(submitDetail.submittedAt) }}
        </el-col>
        <el-col class="status" :span="4" @click="handleClick">
            <el-tag :type="getStatusType(submitDetail.graded)" size="small">
                {{ getStatusText(submitDetail.graded) }}
            </el-tag>
        </el-col>
        <el-col class="action" :span="4">
          <el-button type="primary" link @click="handleClick">查看详情</el-button>
        </el-col>
    </el-row>
</template>

<script setup>
const props = defineProps({
    submitDetail: {
        type: Object,
        required: true
    }
})

const emit = defineEmits(['click'])

// 格式化时间
const formatTime = (time) => {
  if (!time) return '未提交'
  // 兼容后端返回的 "yyyy-MM-dd HH:mm:ss" 或时间戳
  let date
  if (typeof time === 'number') {
    date = new Date(time)
  } else if (typeof time === 'string') {
    // 将空格替换为 T，避免无效日期
    date = new Date(time.replace(' ', 'T'))
  } else {
    date = new Date(time)
  }
  if (isNaN(date.getTime())) return '时间格式错误'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (graded) => {
  return graded ? 'success' : 'warning'
}

// 获取状态文本
const getStatusText = (graded) => {
  return graded ? '已批改' : '待批改'
}

// 点击事件
const handleClick = () => {
  emit('click', props.submitDetail)
}
</script>

<style scoped>

.box {
    font-size: 14px;
    color: #333;
    padding: 15px 20px;
    border-bottom: 1px solid #e5e7eb;
    transition: background-color 0.2s ease;
}

.box:hover {
    background-color: #f8f9fa;
}

.box .time, .box .status {
    font-weight: 400;
    text-align: center;
    color: #666;
}

.box .name {
    font-weight: 500;
    text-align: left;
    color: #1f2937;
}

.clickable {
    cursor: pointer;
}

.action {
    text-align: center;
}
</style>