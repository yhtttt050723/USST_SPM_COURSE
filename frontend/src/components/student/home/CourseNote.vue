<template>
  <div class="course-note">
    <div class="note-header">
      <p class="title">课程公告</p>
      <el-icon class="icon"><Bell /></el-icon>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="2" animated />
    </div>
    
    <!-- 公告内容 -->
    <div v-else-if="latestAnnouncement" class="content" @click="showDetail">
      <h3 class="announcement-title">{{ latestAnnouncement.title }}</h3>
      <p class="announcement-preview">{{ getPreview(latestAnnouncement.content) }}</p>
      <div class="announcement-footer">
        <span class="time">{{ formatTime(latestAnnouncement.createdAt || latestAnnouncement.timestamp) }}</span>
        <span class="read-more">查看详情 <el-icon><ArrowRight /></el-icon></span>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty">
      <el-empty description="暂无公告" :image-size="80" />
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="latestAnnouncement?.title"
      width="600px"
    >
      <div class="dialog-content">
        <div class="dialog-meta">
          <el-icon><Clock /></el-icon>
          <span>{{ formatFullTime(latestAnnouncement?.createdAt || latestAnnouncement?.timestamp) }}</span>
        </div>
        <el-divider />
        <div class="dialog-body" v-html="latestAnnouncement?.content"></div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Bell, ArrowRight, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const latestAnnouncement = ref(null)
const dialogVisible = ref(false)

// 使用默认公告（后端已移除公告接口，避免 404 日志）
const useDefaultAnnouncement = () => {
  latestAnnouncement.value = {
    id: 1,
    title: '课程安排通知',
    content: '各位同学好，本周四下午将进行期中项目检查，请提前准备好演示材料和相关文档。检查内容包括：1. 项目进度展示 2. 代码质量评审 3. 文档完整性检查。',
    createdAt: new Date().toISOString()
  }
  loading.value = false
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 格式化完整时间
const formatFullTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取内容预览
const getPreview = (content) => {
  if (!content) return ''
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 80 ? text.substring(0, 80) + '...' : text
}

// 显示详情
const showDetail = () => {
  dialogVisible.value = true
}

onMounted(() => {
  useDefaultAnnouncement()
})
</script>

<style scoped>
.course-note {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  color: #0f172a;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  transition: all 0.3s ease;
}

.course-note:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(15, 23, 42, 0.12);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #0f172a;
}

.icon {
  font-size: 24px;
  color: #3b82f6;
}

.loading {
  flex: 1;
  padding: 20px 0;
}

.content {
  flex: 1;
  cursor: pointer;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.content:hover {
  background: #f1f5f9;
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.announcement-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-preview {
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px 0;
  color: #475569;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
}

.time {
  font-size: 12px;
  color: #64748b;
}

.read-more {
  font-size: 13px;
  color: #3b82f6;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

/* 弹窗样式 */
.dialog-content {
  padding: 10px 0;
}

.dialog-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
}

.dialog-body {
  font-size: 15px;
  line-height: 1.8;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .course-note {
    padding: 20px;
    min-height: 180px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .announcement-title {
    font-size: 15px;
  }
  
  .announcement-preview {
    font-size: 13px;
  }
}
</style>