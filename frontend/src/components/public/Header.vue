<template>
    <el-header>
      <div class="left-section">
        <div class="website-title">项目管理学习网站</div>
      </div>

      <div class="right-user">
         <el-dropdown>
          <span>
            <el-avatar :size="40" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <h3>{{ user.name || '未登录' }}</h3>
            <el-icon><arrow-down /></el-icon>
          </span>
        <template #dropdown>
        <el-dropdown-menu>
        <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
        </template>
         </el-dropdown>
      </div>
    </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()
    
const user = computed(() => userStore.currentUser || {})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.clearUser()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch {
    // 捕获用户取消操作
  }
}
</script>
<style scoped>
.el-header {
  display: flex;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  z-index: 1000;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.left-section {
  display: flex;
  align-items: center;
}

.website-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.right-user {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.right-user span {
  display: flex;
  align-items: center;
  gap: 10px;
}

.right-user h3 {
  margin: 0;
  color: #333;
  font-size: 14px;
}
</style>

