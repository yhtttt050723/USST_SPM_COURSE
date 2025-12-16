<template>
  <el-row class="box" @click="handleClick">
    <el-col class="time-box" :span="6">
      <div class="time">{{ startTime }}</div>
    </el-col>
    <el-col class="time-box" :span="6">
      <div class="time">{{ endTime }}</div>
    </el-col>
    <!-- 缺失 -->
    <el-col class="count-box" :span="6">
      <div class="count"></div>
    </el-col>
    <el-col class="action-box" :span="6">
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
  session: {
    type: Object,
    default: () => ({})
  },
  presentCount: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['click']);

const startTime = computed(() => {
  if (!props.session?.startTime) return '--';
  return dayjs(props.session.startTime).format('YYYY-MM-DD HH:mm');
});

const endTime = computed(() => {
  if (!props.session?.endTime) return '--';
  return dayjs(props.session.endTime).format('YYYY-MM-DD HH:mm');
});

const statusMap = {
  'ACTIVE': '进行中',
  'ENDED': '已结束',
  'EXPIRED': '已过期',
  'DRAFT': '草稿'
};

const statusText = computed(() => {
  const status = props.session?.status || '';
  return statusMap[status.toUpperCase()] || status || '未知';
});

const statusType = computed(() => {
  const status = (props.session?.status || '').toUpperCase();
  const typeMap = {
    'ACTIVE': 'success',
    'ENDED': 'info',
    'EXPIRED': 'warning',
    'DRAFT': ''
  };
  return typeMap[status] || 'info';
});

const handleClick = () => {
  emit('click', props.session);
};
</script>

<style scoped>
.box {
  width: 100%;
  min-height: 60px;
  background-color: white;
  margin-top: 10px;
  border-radius: 25px;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.3s ease;
}

.box:hover {
  background-color: #f9fafb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.time-box {
  display: flex;
  align-items: center;
  padding: 0 20px 0 80px;
}

.time {
  font-size: 15px;
  color: #64748b;
  font-weight: 500;
}

.count-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.count {
  font-size: 15px;
  color: #374151;
  font-weight: 500;
}

.action-box {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
</style>
