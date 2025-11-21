<template>
  <div class="assignment-page">
    <div class="page-header">
      <div>
        <p class="subtitle">本学期 · 作业总览</p>
        <h2>作业进度追踪</h2>
      </div>
      <el-select v-model="filterStatus" placeholder="筛选状态" size="small" class="status-filter" style="width: 160px">
        <el-option label="全部状态" value="all" />
        <el-option label="进行中" value="progress" />
        <el-option label="待批改" value="submitted" />
        <el-option label="已完成" value="graded" />
      </el-select>
    </div>

    <div class="table-head">
      <span>作业名称</span>
      <span>当前状态</span>
      <span>成绩</span>
    </div>

    <div class="assignment-list">
      <div
        v-for="item in filteredAssignments"
        :key="item.id"
        class="assignment-card"
        :class="item.status"
      >
        <div class="meta">
          <p class="title">{{ item.title }}</p>
          <p class="desc">{{ item.desc }}</p>
        </div>
        <div class="status">
          <el-tag :type="statusType(item.status)" round>{{ statusLabel(item.status) }}</el-tag>
          <p class="deadline">截止 {{ item.deadline }}</p>
        </div>
        <div class="score">
          <p class="score-value">{{ item.score ?? '--' }}</p>
          <p class="score-label">{{ item.score ? '已评分' : '未评分' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

defineOptions({
  name: 'AssignmentsView'
})

const filterStatus = ref('all')

const assignments = [
  {
    id: 1,
    title: '项目需求调研报告',
    desc: '提交访谈纪要与痛点分析 PPT',
    status: 'progress',
    deadline: '09-18 18:00',
    score: null
  },
  {
    id: 2,
    title: '数据库设计文档',
    desc: '包含 ER 图 / 物理结构 / 索引策略',
    status: 'submitted',
    deadline: '09-21 23:59',
    score: null
  },
  {
    id: 3,
    title: '第一阶段评审',
    desc: '进行系统 Demo 演示与答辩',
    status: 'graded',
    deadline: '09-08 20:00',
    score: 92
  },
  {
    id: 4,
    title: '迭代回顾总结',
    desc: '撰写 Retrospective 总结文档',
    status: 'progress',
    deadline: '09-25 20:00',
    score: null
  }
]

const filteredAssignments = computed(() =>
  filterStatus.value === 'all'
    ? assignments
    : assignments.filter((item) => item.status === filterStatus.value)
)

const statusLabel = (status) =>
  ({
    progress: '进行中',
    submitted: '待批改',
    graded: '已完成'
  }[status])

const statusType = (status) =>
  ({
    progress: 'warning',
    submitted: 'info',
    graded: 'success'
  }[status])
</script>

<style scoped>
.assignment-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.subtitle {
  margin: 0;
  color: #9ca3af;
}

.table-head {
  display: grid;
  grid-template-columns: 2fr 1fr 120px;
  padding: 0 24px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 12px;
}

.assignment-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.assignment-card {
  display: grid;
  grid-template-columns: 2fr 1fr 120px;
  align-items: center;
  background: #fff;
  border-radius: 24px;
  padding: 20px 24px;
  box-shadow: 0 20px 35px rgba(15, 23, 42, 0.08);
  border-left: 6px solid transparent;
}

.assignment-card.progress {
  border-color: #f97316;
}

.assignment-card.submitted {
  border-color: #0ea5e9;
}

.assignment-card.graded {
  border-color: #10b981;
}

.meta .title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.meta .desc {
  margin: 0;
  color: #6b7280;
}

.status {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.deadline {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.score {
  text-align: right;
}

.score-value {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.score-label {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}
</style>

