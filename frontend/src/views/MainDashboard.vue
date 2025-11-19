<template>
  <div class="layout">
    <aside class="left-sidebar">
      <div class="logo">
        <h1>项目管理与改进课程学习网站</h1>
      </div>
      <div class="nav-menu">
        <button
          v-for="item in navItems"
          :key="item.id"
          class="nav-item"
          :class="{ active: activeNav === item.id }"
          @click="activeNav = item.id"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.name }}</span>
        </button>
      </div>
    </aside>

    <main class="main-content">
      <el-card class="announcement-card">
        <template #header>
          <div class="card-header">
            <span>课程公告</span>
          </div>
        </template>
        <div class="announcement-content">
          <p v-for="(item, idx) in announcements" :key="idx">{{ idx + 1 }}. {{ item }}</p>
        </div>
      </el-card>

      <div class="stats-cards">
          <el-card class="stats-card">
            <template #header>
              <div class="card-header"><span>出勤统计</span></div>
            </template>
            <div class="progress-title">本月出勤率</div>
            <el-progress type="circle" :percentage="attendanceRate" :color="attendanceColor" :width="120" />
          </el-card>
          <el-card class="stats-card">
            <template #header>
              <div class="card-header"><span>作业完成度</span></div>
            </template>
            <div class="progress-title">总体完成情况</div>
            <el-progress type="circle" :percentage="homeworkRate" :color="homeworkColor" :width="120" />
          </el-card>
      </div>
    </main>

    <aside class="right-sidebar">
      <div class="user-info card">
        <div class="user-avatar">{{ userInitial }}</div>
        <div>
          <div class="user-name">{{ user.name || '未登录' }}</div>
          <div class="user-role">{{ user.role === 'TEACHER' ? '教师' : '学生' }}</div>
        </div>
      </div>
      <el-card class="homework-card">
        <template #header>
          <div class="card-header">
            <span>待办作业</span>
          </div>
        </template>
        <div class="homework-list">
          <div class="homework-item" v-for="item in homeworkList" :key="item.id">
            <div>
              <div class="homework-title">{{ item.title }}</div>
              <div class="homework-deadline">截止：{{ item.deadline }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { House, Document, UserFilled, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const user = ref({})

const navItems = [
  { id: 1, name: '首页', icon: House },
  { id: 2, name: '作业', icon: Document },
  { id: 3, name: '出勤', icon: UserFilled },
  { id: 4, name: '讨论区', icon: ChatDotRound }
]
const activeNav = ref(1)

const announcements = [
  '本周五下午将举行项目管理专题讲座，请准时参加。',
  '第一次小组作业提交截止 2025-09-15，请按时完成。',
  '课程教材已更新至第三版，请到资料区下载。',
  '下周一课程调整至实验楼 301 教室，请注意。',
  '期中考试安排已发布，请查看考试安排表。'
]

const attendanceRate = ref(85)
const homeworkRate = ref(65)

const homeworkList = [
  { id: 1, title: '项目需求分析报告', deadline: '2025.09.10' },
  { id: 2, title: '数据库设计文档', deadline: '2025.09.17' },
  { id: 3, title: '系统原型设计', deadline: '2025.09.24' },
  { id: 4, title: '项目进度报告', deadline: '2025.10.01' }
]

const attendanceColor = computed(() => {
  return attendanceRate.value >= 90 ? '#67c23a' : attendanceRate.value >= 80 ? '#e6a23c' : '#f56c6c'
})
const homeworkColor = computed(() => {
  return homeworkRate.value >= 80 ? '#67c23a' : homeworkRate.value >= 60 ? '#e6a23c' : '#f56c6c'
})

const userInitial = computed(() => (user.value.name ? user.value.name.slice(0, 1) : '学'))

onMounted(() => {
  const cached = sessionStorage.getItem('spm-user')
  if (!cached) {
    router.replace('/login')
    return
  }
  user.value = JSON.parse(cached)
})
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  color: #333;
}
.left-sidebar {
  width: 240px;
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}
.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #2a3947;
}
.logo h1 {
  font-size: 18px;
  font-weight: 600;
}
.nav-menu {
  flex: 1;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}
.nav-item {
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  border: none;
  background: none;
  color: inherit;
  font-size: 15px;
  transition: all 0.3s;
}
.nav-item:hover {
  background-color: #2a3947;
}
.nav-item.active {
  background-color: #409eff;
}
.main-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background-color: #f5f7fa;
}
.card-header {
  font-weight: 600;
}
.announcement-content p {
  margin-bottom: 10px;
  color: #606266;
}
.stats-cards {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.stats-card {
  flex: 1;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.progress-title {
  margin-bottom: 20px;
  font-size: 15px;
  color: #606266;
}
.right-sidebar {
  width: 320px;
  background-color: #fff;
  padding: 20px;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.card {
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
}
.user-name {
  font-size: 16px;
  font-weight: 600;
}
.user-role {
  font-size: 13px;
  color: #909399;
}
.homework-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.homework-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}
.homework-item:last-child {
  border-bottom: none;
}
.homework-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}
.homework-deadline {
  color: #e6a23c;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .right-sidebar {
    width: 280px;
  }
}

@media (max-width: 992px) {
  .layout {
    flex-direction: column;
  }
  .left-sidebar {
    width: 100%;
  }
  .nav-menu {
    flex-direction: row;
    flex-wrap: wrap;
  }
  .nav-item {
    flex: 1 1 25%;
    justify-content: center;
  }
  .right-sidebar {
    width: 100%;
  }
  .stats-cards {
    flex-direction: column;
  }
}
</style>