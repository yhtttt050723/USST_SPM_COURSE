<template>
  <div class="home-layout">
    <aside class="sidebar">
      <div class="brand">
        <p class="brand-tag">USST · SPM</p>
        <h1>项目管理学习平台</h1>
      </div>
          <nav class="nav">
            <button
              v-for="item in navItems"
              :key="item.id"
              class="nav-item"
              :class="{ active: activeNav === item.id }"
              @click="handleNavClick(item.id)"
            >
              <el-icon><component :is="markRaw(item.icon)" /></el-icon>
              <span>{{ item.label }}</span>
            </button>
          </nav>
    </aside>

    <section class="main-section">
      <div class="main-scroll">
        <div v-if="activeNav === 'home'" class="dashboard">
          <div class="welcome-card">
            <div>
              <p class="welcome-label">欢迎回来</p>
              <h2>{{ user.name || '同学' }}，继续保持节奏！</h2>
              <p class="welcome-desc">本周共有 {{ pendingAssignments.length }} 个待办作业，{{ scheduleSummary }}。</p>
            </div>
            <img class="welcome-graph" src="https://cdn.jsdelivr.net/gh/itsmrsun/storage@main/illus/grad-graph.svg" alt="progress" />
          </div>

          <div class="stat-grid">
            <div class="stat-card" v-for="stat in homeStats" :key="stat.label">
              <p class="stat-label">{{ stat.label }}</p>
              <h3>{{ stat.value }}</h3>
              <span class="stat-footer">{{ stat.trend }}</span>
            </div>
          </div>
        </div>
        <!-- 学生端内容 -->
        <template v-if="!isTeacher">
          <Assignments v-if="activeNav === 'assignments'" />
          <div v-if="activeNav === 'attendance'">
            <Attendance />
          </div>
          <div v-if="activeNav === 'discussion'">
            <Discussion />
          </div>
        </template>

        <!-- 教师端内容 -->
        <template v-else>
          <TeacherAssignments v-if="activeNav === 'assignments'" />
          <TeacherGrading v-if="activeNav === 'grading'" />
          <div v-if="activeNav === 'attendance'">
            <Attendance />
          </div>
          <div v-if="activeNav === 'discussion'">
            <Discussion />
          </div>
        </template>
      </div>
    </section>

    <aside class="right-section">
      <div class="user-card">
        <div class="avatar">{{ userInitial }}</div>
        <div>
          <p class="user-name">{{ user.name || '未登录' }}</p>
          <p class="user-role">{{ user.role === 'TEACHER' ? '教师' : '学生' }}</p>
        </div>
      </div>

      <div class="todo-card">
        <div class="card-header">
          <div>
            <p class="todo-label">待办作业</p>
            <h3>{{ pendingAssignments.length }} 项近期截止</h3>
          </div>
          <el-tag size="small" type="warning">紧急</el-tag>
        </div>
        <ul class="todo-list">
          <li v-for="work in pendingAssignments" :key="work.id" class="todo-item">
            <div>
              <p class="todo-title">{{ work.title }}</p>
              <p class="todo-deadline">截止：{{ work.deadline }}</p>
            </div>
            <span class="todo-status" :class="work.status">{{ work.status === 'pending' ? '待提交' : '进行中' }}</span>
          </li>
        </ul>
      </div>

      <div class="calendar-card">
        <p class="calendar-label">课程安排</p>
        <ul class="calendar-list">
          <li v-for="item in schedules" :key="item.id">
            <span>{{ item.weekday }}</span>
            <div>
              <p>{{ item.topic }}</p>
              <small>{{ item.time }}</small>
            </div>
          </li>
        </ul>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { computed, markRaw, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { House, Document, UserFilled, ChatDotRound } from '@element-plus/icons-vue'
import { ElIcon, ElTag } from 'element-plus'

import Assignments from './Assignments.vue'
import Attendance from './Attendance.vue'
import Discussion from './Discussion.vue'
import TeacherAssignments from './TeacherAssignments.vue'
import TeacherGrading from './TeacherGrading.vue'
import { useUserStore } from '@/stores/userStore'
import { getAssignments } from '@/api/assignment'

const router = useRouter()
const { currentUser, hydrateUserFromCache } = useUserStore()
const activeNav = ref('home')

const user = computed(() => currentUser.value || {})
const isTeacher = computed(() => user.value.role === 'TEACHER')

// 学生作业数据
const studentAssignments = ref([])
const pendingAssignments = ref([])

const navItems = computed(() => {
  if (isTeacher.value) {
    return [
      { id: 'home', label: '首页', icon: House },
      { id: 'assignments', label: '作业管理', icon: Document },
      { id: 'grading', label: '批改作业', icon: Document },
      { id: 'attendance', label: '出勤管理', icon: UserFilled },
      { id: 'discussion', label: '讨论区', icon: ChatDotRound }
    ]
  } else {
    return [
      { id: 'home', label: '首页', icon: House },
      { id: 'assignments', label: '作业', icon: Document },
      { id: 'attendance', label: '出勤', icon: UserFilled },
      { id: 'discussion', label: '讨论区', icon: ChatDotRound }
    ]
  }
})

const schedules = [
  { id: 1, weekday: '周三', topic: '敏捷开发实践', time: '10:00 - 12:00' },
  { id: 2, weekday: '周四', topic: '需求评审会', time: '14:00 - 16:00' },
  { id: 3, weekday: '周五', topic: '小组站会', time: '09:00 - 09:30' }
]

// 格式化日期为 MM-dd HH:mm
const formatDeadline = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 获取学生作业数据
const fetchStudentAssignments = async () => {
  if (isTeacher.value || !currentUser.value?.id) return

  try {
    const response = await getAssignments('all', currentUser.value.id)
    studentAssignments.value = response.data || []
    
    // 计算待办作业（未提交且未截止的作业）
    const now = new Date()
    pendingAssignments.value = studentAssignments.value
      .filter(item => {
        if (item.submissionStatus === 'graded') return false // 已完成的排除
        if (item.dueAt) {
          const dueDate = new Date(item.dueAt)
          return dueDate > now // 未截止
        }
        return true
      })
      .slice(0, 3) // 只显示前3个
      .map(item => ({
        id: item.id,
        title: item.title,
        deadline: formatDeadline(item.dueAt),
        status: item.submissionStatus === 'submitted' ? 'progress' : 'pending'
      }))
  } catch (error) {
    console.error('获取作业数据失败:', error)
    // 使用默认数据
    pendingAssignments.value = [
      { id: 1, title: '需求规格说明书', deadline: '09-18 18:00', status: 'pending' },
      { id: 2, title: '数据库设计稿', deadline: '09-21 23:59', status: 'pending' },
      { id: 3, title: '迭代计划汇报', deadline: '09-25 20:00', status: 'progress' }
    ]
  }
}

// 计算统计数据
const homeStats = computed(() => {
  if (isTeacher.value) {
    return [
      { label: '已完成作业', value: '12 / 15', trend: '整体完成率 80%' },
      { label: '本周出勤率', value: '92%', trend: '较上周 +3%' },
      { label: '讨论区互动', value: '34 条', trend: '本周新增 8 条' }
    ]
  }

  // 学生端：基于真实数据计算
  const total = studentAssignments.value.length
  const completed = studentAssignments.value.filter(item => item.submissionStatus === 'graded').length
  const completionRate = total > 0 ? Math.round((completed / total) * 100) : 0

  return [
    { 
      label: '已完成作业', 
      value: `${completed} / ${total}`, 
      trend: `整体完成率 ${completionRate}%` 
    },
    { label: '本周出勤率', value: '92%', trend: '较上周 +3%' },
    { label: '讨论区互动', value: '34 条', trend: '本周新增 8 条' }
  ]
})

const scheduleSummary = computed(() => schedules.map((item) => `${item.weekday}${item.topic}`).join('，'))

const userInitial = computed(() => (user.value.name ? user.value.name.slice(0, 1) : '学'))

// 简化逻辑，直接使用 v-if 渲染组件

// 处理导航点击事件
const handleNavClick = (navId) => {
  activeNav.value = navId
}

onMounted(async () => {
  // 非阻塞式用户缓存加载，避免影响视图渲染
  try {
    const cachedUser = hydrateUserFromCache()
    if (!cachedUser) {
      router.replace('/login')
      return
    }
    // 如果是学生，获取作业数据
    if (!isTeacher.value) {
      await fetchStudentAssignments()
    }
  } catch (error) {
    console.error('用户缓存加载失败:', error)
    router.replace('/login')
  }
})
</script>

<style>
:global(body) {
  background: #e5e7eb;
}

.home-layout {
  display: grid;
  grid-template-columns: 240px minmax(640px, 1fr) 320px;
  min-height: 100vh;
  background: #e5e7eb;
  gap: 24px;
  padding: 32px;
  box-sizing: border-box;
}

.sidebar {
  background: #1f2937;
  border-radius: 24px;
  padding: 28px 24px;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand-tag {
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #9ca3af;
  margin-bottom: 8px;
}

.brand h1 {
  font-size: 20px;
  margin: 0;
  line-height: 1.4;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  border: none;
  border-radius: 16px;
  padding: 14px 18px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.08);
  color: inherit;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item.active {
  background: #f97316;
  color: #fff;
  box-shadow: 0 12px 24px rgba(249, 115, 22, 0.4);
}

.main-section {
  background: #f9fafb;
  border-radius: 32px;
  padding: 28px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
}

.main-scroll {
  flex: 1;
  overflow: auto;
  padding-right: 4px;
}

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-card {
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  border-radius: 28px;
  padding: 28px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 180px;
}

.welcome-card h2 {
  margin: 8px 0;
  font-size: 28px;
}

.welcome-desc {
  color: rgba(255, 255, 255, 0.85);
}

.welcome-graph {
  width: 180px;
  height: 120px;
  object-fit: contain;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 15px 30px rgba(15, 23, 42, 0.08);
}

.stat-label {
  color: #6b7280;
  margin-bottom: 12px;
}

.stat-card h3 {
  margin: 0;
  font-size: 28px;
  color: #111827;
}

.stat-footer {
  font-size: 13px;
  color: #10b981;
}

.right-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-card,
.todo-card,
.calendar-card {
  background: #fff;
  border-radius: 28px;
  padding: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f97316;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  font-weight: 700;
}

.user-name {
  font-size: 18px;
  margin: 0;
  color: #111827;
}

.user-role {
  margin: 4px 0 0;
  color: #6b7280;
}

.todo-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.todo-label {
  color: #9ca3af;
  margin: 0;
}

.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.todo-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.todo-title {
  margin: 0 0 6px;
  font-weight: 600;
  color: #1f2937;
}

.todo-deadline {
  margin: 0;
  color: #9ca3af;
  font-size: 13px;
}

.todo-status {
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #fee2e2;
  color: #b91c1c;
}

.todo-status.progress {
  background: #dbeafe;
  color: #1d4ed8;
}

.calendar-label {
  color: #9ca3af;
  margin-bottom: 12px;
}

.calendar-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.calendar-list li {
  display: flex;
  gap: 16px;
  align-items: center;
}

.calendar-list span {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: #111827;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-list p {
  margin: 0;
  font-weight: 600;
  color: #111827;
}

.calendar-list small {
  color: #6b7280;
}

@media (max-width: 1200px) {
  .home-layout {
    grid-template-columns: 220px 1fr;
    grid-template-rows: auto auto;
  }
  .right-section {
    grid-column: 1 / -1;
    flex-direction: row;
    flex-wrap: wrap;
  }
  .right-section > * {
    flex: 1;
    min-width: 280px;
  }
}

@media (max-width: 900px) {
  .home-layout {
    grid-template-columns: 1fr;
  }
  .sidebar,
  .main-section,
  .right-section {
    width: 100%;
  }
  .right-section {
    flex-direction: column;
  }
}
</style>

