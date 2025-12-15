<template>
  <el-row class="box">
    <el-col class="time-box" :span="16">
      <div class="time">{{ timeText }}</div>
    </el-col>
    <el-col class="status-box" :span="8">
      <div class="status">{{ statusText }}</div>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed } from 'vue';
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

const statusText = computed(() => {
  if (!props.record) return '—';
  // 后端字段 result/status 兼容
  return props.record.result || props.record.status || '—';
});
</script>

<style scoped>
.box {
    width: 100%;
    min-height: 40px;
    background-color: white;
    margin-top: 12px;
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
    padding: 0px 20px;
}

.time {
    font-size: 14px;
    color: #64748b;
    
}
.status {
    font-size: 14px;
    color: #64748b;
}
.status-box {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 20px;
}
</style>