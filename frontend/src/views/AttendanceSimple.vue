<template>
  <div class="attendance-page">
    <div class="page-header">
      <div>
        <p class="subtitle">出勤概览</p>
        <h2>课堂记录与提醒</h2>
      </div>
      <el-progress :percentage="attendanceRate" type="dashboard" :width="140" color="#22c55e">
        <template #default="{ percentage }">
          <span class="rate-text">{{ percentage }}%</span>
        </template>
      </el-progress>
    </div>

    <div class="timeline">
      <div v-for="item in records" :key="item.id" class="timeline-item" :class="item.status">
        <div class="dot" />
        <div class="content">
          <div class="content-head">
            <h3>{{ item.topic }}</h3>
            <el-tag :type="item.status === 'present' ? 'success' : 'danger'" size="small">
              {{ statusLabel(item.status) }}
            </el-tag>
          </div>
          <p class="time">{{ item.date }} · {{ item.time }}</p>
          <p class="notes">{{ item.notes }}</p>
        </div>
      </div>
    </div>

    <div class="alert-card">
      <h3>补签提醒</h3>
      <p>本月缺勤 1 次，请在本周五前联系助教完成补签流程并提交说明。</p>
      <el-button type="primary" round>查看补签指南</el-button>
    </div>
  </div>
</template>

<script setup>
defineOptions({
  name: 'AttendanceView'
})

const attendanceRate = 92

const records = [
  {
    id: 1,
    topic: '敏捷迭代与用户故事',
    date: '09/12 周四',
    time: '10:00 - 12:00',
    status: 'present',
    notes: '课堂表现积极，按时完成小组讨论'
  },
  {
    id: 2,
    topic: '需求分析与建模',
    date: '09/10 周二',
    time: '14:00 - 16:00',
    status: 'present',
    notes: '提交了高质量的需求文档'
  },
  {
    id: 3,
    topic: '项目管理工具实践',
    date: '09/05 周四',
    time: '10:00 - 12:00',
    status: 'absent',
    notes: '因病缺勤，已提交请假条'
  }
]

const statusLabel = (status) => {
  const labels = {
    present: '到课',
    absent: '缺勤',
    leave: '请假'
  }
  return labels[status] || '未知'
}
</script>

<style scoped>
.attendance-page {
  padding: 0;
  max-width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.subtitle {
  color: #9ca3af;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #111827;
}

.rate-text {
  font-size: 18px;
  font-weight: 700;
  color: #22c55e;
}

.timeline {
  margin-bottom: 32px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #22c55e;
  margin-top: 6px;
  flex-shrink: 0;
}

.timeline-item.absent .dot {
  background: #ef4444;
}

.content {
  flex: 1;
}

.content-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.content-head h3 {
  margin: 0;
  font-size: 16px;
  color: #111827;
}

.time {
  margin: 0 0 8px 0;
  color: #6b7280;
  font-size: 14px;
}

.notes {
  margin: 0;
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
}

.alert-card {
  padding: 20px;
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 12px;
  text-align: center;
}

.alert-card h3 {
  margin: 0 0 12px 0;
  color: #92400e;
  font-size: 18px;
}

.alert-card p {
  margin: 0 0 16px 0;
  color: #92400e;
  line-height: 1.5;
}
</style>
