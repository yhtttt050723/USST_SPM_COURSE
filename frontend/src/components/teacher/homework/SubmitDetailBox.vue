<template>
    <el-row class="box" @click="handleClick">
        <el-col class="name" :span="12">{{ submitDetail.studentName || '未知学生' }}</el-col>
        <el-col class="time" :span="6">{{ formatTime(submitDetail.submittedAt) }}</el-col>
        <el-col class="status" :span="6">
            <el-tag :type="getStatusType(submitDetail.graded)" size="small">
                {{ getStatusText(submitDetail.graded) }}
            </el-tag>
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
    cursor: pointer;
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
</style>