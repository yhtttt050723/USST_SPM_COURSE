<template>
  <el-row class="box">
    <el-col class="time-box" :span="6">
      <div class="time">{{ timeText }}</div>
    </el-col>
    <el-col class="null" :span="12"></el-col>
    <el-col class="status-box" :span="6">
      <el-tag :type="statusType" size="large">
        {{ statusText }}
      </el-tag>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed } from 'vue';
import { ElTag } from 'element-plus';
import dayjs from 'dayjs';

const props = defineProps({
  record: {
    type: Object,
    default: () => ({})
  }
});

const timeText = computed(() => {
  if (!props.record?.checkinTime) return '--';
  return dayjs(props.record.checkinTime).format('YYYY-MM-DD HH:mm');
});
const statusMap = {
  'PRESENT': '已签到',
  'ABSENT': '缺勤',
  'LATE': '迟到',
  'LEAVE': '请假',
  'EXCUSED': '已销假',
  // 兼容 result 字段
  'SUCCESS': '成功',
  'FAILED': '失败'
};

const statusText = computed(() => {
  if (!props.record) return '—';
  // 优先使用 status 字段（PRESENT/LATE/ABSENT），如果没有则使用 result 字段（SUCCESS/FAILED）
  const rawStatus = props.record.status || props.record.result || '';
  const status = String(rawStatus).toUpperCase();
  
  return statusMap[status] || rawStatus || '—';
});

const statusType = computed(() => {
  if (!props.record) return 'info';
  // 优先使用 status 字段
  const status = String(props.record.status || props.record.result || '').toUpperCase();
  const typeMap = {
    'PRESENT': 'success',
    'LATE': 'warning',
    'ABSENT': 'danger',
    'LEAVE': 'primary',
    'EXCUSED': '',
    // 兼容 result 字段
    'SUCCESS': 'success',
    'FAILED': 'danger'
  };
  return typeMap[status] || 'info';
});
</script>

<style scoped>
.box {
    width: 100%;
    height: 60px;
    background-color: white;
    margin-top: 10px;
    border-top-left-radius: 25px;
    border-top-right-radius: 25px;
    border-bottom: 1px solid #b8bdc4;
    cursor: pointer;
    transition: all 0.3s ease;
}

.time-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.time {
    font-size: 14px;
    color: #64748b;
    
}
.status-box {
    display: flex;
    align-items: center;
    justify-content: center;
}
</style>