<template>
  <div class="discussion-detail-page" v-loading="loading">
    <div class="page-header">
      <el-page-header @back="goBack"  content="讨论详情" />
      <div class="actions" v-if="topic">
        <el-button v-if="topic.canDelete" type="danger" text @click="confirmDeleteTopic">删除</el-button>
        <el-tag v-if="topic.status === 'CLOSED'" type="info" round>已关闭</el-tag>
        <el-tag v-if="topic.pin" type="warning" round>置顶</el-tag>
      </div>
    </div>

    <div v-if="topic" class="content-box">
      <div class="head">
        <div class="user">
          <el-avatar class="avatar" :size="48">{{ topic.authorName?.[0] || '?' }}</el-avatar>
          <div class="meta">
            <div class="name">{{ topic.authorName || '未知用户' }}</div>
            <div class="time">{{ formatTime(topic.createdAt) }}</div>
          </div>
        </div>
        <div class="tags">
          <el-tag v-if="topic.pin" size="small" type="warning" round>置顶</el-tag>
          <el-tag v-if="topic.status === 'CLOSED'" size="small" type="info" round>已关闭</el-tag>
        </div>
      </div>

      <h2 class="title">{{ topic.title }}</h2>
      <div class="content" v-html="topic.content || '无内容'"></div>
    </div>

    <div class="reply-section">
      <div class="reply-header">
        <h3>回复 ({{ replies.length }})</h3>
        <span v-if="!canReply">已关闭评论</span>
      </div>

      <div class="reply-input" v-if="canReply">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="发表评论"
        />
        <div class="reply-actions">
          <el-button type="primary" :loading="replySubmitting" @click="submitReply">发布</el-button>
        </div>
      </div>

      <el-empty v-if="!replyLoading && replies.length === 0" description="暂无回复" />
      <el-skeleton v-if="replyLoading" :rows="3" animated />

      <div class="reply-list">
        <div v-for="item in replies" :key="item.id" class="reply-item">
          <div class="reply-head">
            <div class="user">
              <el-avatar :size="36">{{ item.authorName?.[0] || '?' }}</el-avatar>
              <div class="meta">
                <div class="name">{{ item.authorName || '未知用户' }}</div>
                <div class="time">{{ formatTime(item.createdAt) }}</div>
              </div>
            </div>
            <div class="reply-actions">
              <el-button v-if="item.canDelete" text type="danger" size="small" @click="deleteReply(item)">删除</el-button>
            </div>
          </div>
          <div class="reply-content">{{ item.content }}</div>
        </div>
      </div>

      <div class="pagination-controls">
        <el-button size="small" @click="changeReplyPage(-1)" :disabled="replyPage <= 1">上一页</el-button>
        <el-button size="small" @click="changeReplyPage(1)" :disabled="!replyHasMore">下一页</el-button>
        <span class="page-text">第 {{ replyPage }} 页</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiscussionById, getDiscussionReplies, createComment, deleteComment, deleteDiscussion, closeDiscussion, openDiscussion, pinDiscussion, unpinDiscussion } from '@/api/discussion'

const route = useRoute()
const router = useRouter()

const topic = ref(null)
const replies = ref([])
const loading = ref(false)
const replyLoading = ref(false)
const replySubmitting = ref(false)
const replyContent = ref('')
const replyPage = ref(1)
const replySize = ref(10)
const replyHasMore = ref(false)

const currentUser = ref(null)
const isTeacher = computed(() => ['TEACHER', 'ADMIN'].includes(currentUser.value?.role))
const canReply = computed(() => {
  if (!topic.value) return false
  return topic.value.canReply !== false
})

const loadUser = () => {
  const cache = localStorage.getItem('spm-user')
  if (cache) {
    try {
      currentUser.value = JSON.parse(cache)
    } catch (e) {
      console.error(e)
    }
  }
}

const loadTopic = async () => {
  const id = route.params.id
  loading.value = true
  try {
    const resp = await getDiscussionById(id)
    topic.value = resp.data || resp
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取讨论失败')
  } finally {
    loading.value = false
  }
}

const loadReplies = async () => {
  const id = route.params.id
  replyLoading.value = true
  try {
    const resp = await getDiscussionReplies(id, { page: replyPage.value, size: replySize.value })
    const list = resp.data || resp || []
    replies.value = Array.isArray(list) ? list : []
    replyHasMore.value = replies.value.length === replySize.value
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取回复失败')
  } finally {
    replyLoading.value = false
  }
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  replySubmitting.value = true
  try {
    await createComment(route.params.id, { content: replyContent.value })
    ElMessage.success('已发布')
    replyContent.value = ''
    replyPage.value = 1
    await Promise.all([loadTopic(), loadReplies()])
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  } finally {
    replySubmitting.value = false
  }
}

const deleteReply = async (item) => {
  ElMessageBox.confirm('确认删除此回复吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteComment(route.params.id, item.id)
        ElMessage.success('删除成功')
        await Promise.all([loadTopic(), loadReplies()])
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const confirmDeleteTopic = async () => {
  if (!topic.value) return
  ElMessageBox.confirm(`确认删除讨论“${topic.value.title}”吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteDiscussion(topic.value.id)
        ElMessage.success('已删除')
        router.push('/discussion')
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const changeReplyPage = (delta) => {
  const next = replyPage.value + delta
  if (next < 1) return
  if (delta > 0 && !replyHasMore.value) return
  replyPage.value = next
  loadReplies()
}

const goBack = () => {
  router.back()
}

const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  const time = new Date(timeStr)
  return time.toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  loadUser()
  loadTopic()
  loadReplies()
})
</script>

<style scoped>
.discussion-detail-page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.content-box {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.05);
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.user {
  display: flex;
  gap: 12px;
  align-items: center;
}
.meta .name {
  font-weight: 600;
}
.meta .time {
  color: #9ca3af;
  font-size: 12px;
}
.tags {
  display: flex;
  gap: 8px;
}
.title {
  margin: 12px 0;
}
.content {
  line-height: 1.6;
  color: #4b5563;
  white-space: pre-wrap;
}
.reply-section {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.05);
}
.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.reply-input {
  margin-bottom: 12px;
}
.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
.reply-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.reply-item {
  background: #f8fafc;
  border-radius: 12px;
  padding: 12px;
}
.reply-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.reply-content {
  margin-top: 8px;
  color: #4b5563;
  white-space: pre-wrap;
}
.pagination-controls {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-text {
  color: #6b7280;
  font-size: 12px;
}
</style>
