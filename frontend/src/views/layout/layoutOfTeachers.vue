<template>
  <div class="layout-container">
    <Header/>
    <el-row class="main-row">
      <el-col :span="isCollapse ? 1 : 4" class="left-col">
        <el-menu 
          :collapse="isCollapse" 
          class="the-menu"
          router
          :default-active="currentRoute"
        >
          <el-menu-item class="menu-item" index="/teacher/course">
            <template #title>
              <span class="title">课程管理</span>
            </template>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/teacher/homework">
            <template #title>
              <span class="title">作业管理</span>
            </template>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/teacher/attendance">
            <template #title>
              <span class="title">出勤管理</span>
            </template>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/teacher/announcement">
            <template #title>
              <span class="title">公告管理</span>
            </template>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/teacher/discussion">
            <template #title>
              <span class="title">讨论区</span>
            </template>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/teacher/profile">
            <template #title>
              <span class="title">账户管理</span>
            </template>
          </el-menu-item>
        </el-menu>
      </el-col>

      <el-col :span="isCollapse ? 23 : 20" class="right-col">
        <router-view />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/components/public/Header.vue'

const route = useRoute()
const isCollapse = ref(false)
const currentRoute = computed(() => {
  const path =  route.path
  if(path.startsWith('/teacher/course')) {
    return '/teacher/course'
  } else if(path.startsWith('/teacher/attendance')) {
    return '/teacher/attendance'
  } else if(path.startsWith('/teacher/homework')) {
    return '/teacher/homework'
  } else if(path.startsWith('/teacher/announcement')) {
    return '/teacher/announcement'
  } else if(path.startsWith('/teacher/discussion')) {
    return '/teacher/discussion'
  } else {
    return '/teacher/profile'
  }
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.main-row {
  min-height: calc(100vh - 60px);
  display: flex;
}

.left-col {
  flex-shrink: 0;
}

.right-col {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  min-height: calc(100vh - 76px);
}

:deep(.the-menu) {
  position: fixed;
  top: 60px;
  left: 0;
  z-index: 100;
  width: 16%;
  --el-menu-bg-color: rgb(31, 41, 55) !important;
  --el-menu-text-color: white !important; 
  --el-menu-active-color: white !important;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.2) !important;
  --el-menu-hover-text-color: white !important;
  border-radius: 20px !important;
  border: none !important;
  padding: 8px;
  margin: 8px;
  height: calc(100vh - 76px);
  overflow-y: auto;
}

:deep(.the-menu.el-menu--collapse) {
  width: 4.166667%;
}

:deep(.the-menu .el-menu-item) {
  border-radius: 24px !important;
  margin: 8px !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

:deep(.the-menu .el-menu-item.is-active) {
  background: rgb(249, 115, 22) !important;
  color: white !important;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.4) !important;
}
</style>