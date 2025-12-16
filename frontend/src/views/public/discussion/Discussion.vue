<template>
  <div class="discussion-page">
    <div class="page-header">
      <div>
        <p class="subtitle">课程讨论区</p>
        <h2>最新话题与互动</h2>
      </div>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="关键字（标题/内容）"
          clearable
          class="keyword-input"
          @keyup.enter="refreshDiscussions"
        >
          <template #prefix>
            <el-icon><ChatDotRound /></el-icon>
          </template>
        </el-input>
        <el-button @click="refreshDiscussions" :loading="loading">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
        </el-button>
        <el-button
          v-if="canCreate"
          type="primary"
          round
          @click="showCreateDialog = true"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>发起话题</span>
        </el-button>
      </div>
    </div>

    <!-- 教师端：显示所有讨论帖（包括已删除）的切换 -->
    <div v-if="isTeacher" class="admin-controls">
      <el-switch
        v-model="includeDeleted"
        active-text="显示已删除"
        inactive-text="仅显示正常"
        @change="refreshDiscussions"
      />
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && discussions.length === 0" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 讨论帖列表 -->
    <div v-else class="topic-list">
      <div v-for="topic in discussions" :key="topic.id" class="topic-card" :class="{ 'deleted': topic.deleted }" >
        <div class="topic-head" @click="viewDetail(topic.id)" >
          <div class="avatar">{{ topic.authorName ? topic.authorName[0] : '?' }}</div>
          <div class="author-info">
            <p class="author">{{ topic.authorName || '未知用户' }}</p>
            <small>{{ formatTime(topic.createdAt) }}</small>
          </div>
          <div class="topic-tags">
            <el-tag v-if="topic.pin" type="warning" size="small" round>置顶</el-tag>
            <el-tag
              v-if="topic.status === 'CLOSED'"
              type="info"
              size="small"
              round
            >已关闭</el-tag>
            <el-tag
              v-if="topic.deleted"
              type="danger"
              size="small"
              round
            >已删除</el-tag>
          </div>
        </div>
        <h3 @click="viewDetail(topic.id)" class="topic-title">
          {{ topic.title }}
        </h3>
        <p class="content" @click="viewDetail(topic.id)">{{ topic.content || '无内容' }}</p>
        <div class="topic-footer" @click="viewDetail(topic.id)">
          <span>
            {{ topic.commentCount || 0 }} 条回复
            <span v-if="topic.replyCount"> · 最新：{{ formatTime(topic.lastReplyAt) }}</span>
          </span>
          <div class="topic-actions">
            <el-button text type="primary" size="small" @click="viewDetail(topic.id)">查看详情</el-button>
            <el-button v-if="topic.canEdit" text type="warning" size="small" @click="editDiscussion(topic)">编辑</el-button>
            <el-button v-if="topic.canToggleComment" text type="success" size="small" @click="togglePin(topic)">
              {{ topic.pin ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button
              v-if="topic.canToggleComment"
              text
              type="info"
              size="small"
              @click="toggleComment(topic)"
            >
              {{ topic.status === 'CLOSED' ? '开启评论' : '关闭评论' }}
            </el-button>
            <el-button v-if="topic.canDelete" text type="danger" size="small" @click="confirmDelete(topic)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && discussions.length === 0" description="暂无讨论帖" />
    </div>

    <div class="pagination-controls">
      <span>每页：</span>
      <el-input-number
        v-model="size"
        :min="5"
        :max="50"
        :step="5"
        size="small"
        @change="changePageSize"
      />
      <el-button size="small" @click="changePage(-1)" :disabled="page <= 1">上一页</el-button>
      <el-button size="small" @click="changePage(1)" :disabled="!hasMore">下一页</el-button>
      <span class="page-text">第 {{ page }} 页</span>
    </div>

    <!-- 创建讨论帖对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="发起新话题"
      width="600px"
      @close="resetCreateForm"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="createForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入讨论内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="置顶" v-if="isTeacher">
          <el-switch v-model="createForm.pinned" />
        </el-form-item>
        <el-form-item label="允许评论" v-if="isTeacher">
          <el-switch v-model="createForm.allowComment" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">发布</el-button>
      </template>
    </el-dialog>

    <!-- 编辑讨论帖对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑讨论帖"
      width="600px"
      @close="resetEditForm"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入讨论内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="允许评论" v-if="isTeacher">
          <el-switch v-model="editForm.allowComment" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChatDotRound, Refresh, Search } from '@element-plus/icons-vue'
import { ElButton, ElIcon, ElTag, ElMessage, ElMessageBox, ElDialog, ElForm, ElFormItem, ElInput, ElSwitch, ElSkeleton, ElEmpty, ElInputNumber } from 'element-plus'
import {
  getDiscussions,
  getAllDiscussionsForAdmin,
  createDiscussion,
  updateDiscussion,
  deleteDiscussion,
  pinDiscussion,
  unpinDiscussion,
  closeDiscussion,
  openDiscussion
} from '@/api/discussion'

const router = useRouter()
const route = useRoute()

// 用户信息
const currentUser = ref(null)
const isTeacher = computed(() => ['TEACHER', 'ADMIN'].includes(currentUser.value?.role))
const canCreate = computed(() => !!currentUser.value)

// 数据
const discussions = ref([])
const loading = ref(false)
const includeDeleted = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const hasMore = ref(false)

// 创建表单
const showCreateDialog = ref(false)
const creating = ref(false)
const createForm = ref({
  title: '',
  content: '',
  pinned: false,
  allowComment: true
})

// 编辑表单
const showEditDialog = ref(false)
const updating = ref(false)
const editForm = ref({
  id: null,
  title: '',
  content: '',
  allowComment: true
})

// 加载用户信息
const loadUserInfo = () => {
  const userStr = localStorage.getItem('spm-user')
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr)
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }
}

// 规范化话题数据，兼容后端返回的各种布尔/字段命名
const normalizeTopic = (raw) => {
  if (!raw) return {}
  return {
    ...raw,
    pin: raw.pin ?? raw.pinned ?? raw.isPinned ?? false,
    allowComment:
      raw.allowComment === true ||
      raw.allowComment === 1 ||
      raw.allow_comment === 1 ||
      raw.allow_comment === true,
    deleted: raw.deleted === true || raw.deleted === 1,
    replyCount: raw.replyCount ?? raw.commentCount ?? 0,
    lastReplyAt: raw.lastReplyAt ?? raw.updatedAt ?? raw.createdAt,
    status: raw.status || 'OPEN',
    canEdit: !!raw.canEdit,
    canDelete: !!raw.canDelete,
    canToggleComment: !!raw.canToggleComment
  }
}

// 加载讨论帖列表
const loadDiscussions = async () => {
  loading.value = true
  try {
    let response
    if (isTeacher.value && includeDeleted.value) {
      // 教师端：获取所有（包括已删除）
      response = await getAllDiscussionsForAdmin(1)
    } else {
      // 普通用户或教师端（不包含已删除）
      response = await getDiscussions({
        courseId: 1,
        includeDeleted: includeDeleted.value,
        keyword: keyword.value || undefined,
        page: page.value,
        size: size.value
      })
    }
    
    // 处理响应数据（兼容不同的响应格式）
    if (response) {
      if (response.data !== undefined) {
        // 标准格式：{code, message, data}
        discussions.value = Array.isArray(response.data) ? response.data.map(normalizeTopic) : []
        hasMore.value = Array.isArray(response.data) && response.data.length === size.value
      } else if (Array.isArray(response)) {
        // 直接返回数组
        discussions.value = response.map(normalizeTopic)
        hasMore.value = response.length === size.value
      } else {
        discussions.value = []
        hasMore.value = false
      }
    } else {
      discussions.value = []
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载讨论帖失败', error)
    ElMessage.error(error.message || '加载讨论帖失败')
    discussions.value = []
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 刷新讨论帖
const refreshDiscussions = () => {
  page.value = 1
  loadDiscussions()
}

// 创建讨论帖
const handleCreate = async () => {
  if (!createForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!createForm.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  creating.value = true
  try {
    await createDiscussion({
      courseId: 1,
      title: createForm.value.title,
      content: createForm.value.content,
      pinned: createForm.value.pinned,
      allowComment: createForm.value.allowComment
    })
    ElMessage.success('发布成功')
    showCreateDialog.value = false
    resetCreateForm()
    refreshDiscussions()
  } catch (error) {
    console.error('创建失败', error)
    ElMessage.error(error.message || '发布失败')
  } finally {
    creating.value = false
  }
}

// 编辑讨论帖
const editDiscussion = (topic) => {
  editForm.value = {
    id: topic.id,
    title: topic.title,
    content: topic.content,
    allowComment: topic.allowComment
  }
  showEditDialog.value = true
}

const handleUpdate = async () => {
  if (!editForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!editForm.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  updating.value = true
  try {
    await updateDiscussion(editForm.value.id, {
      title: editForm.value.title,
      content: editForm.value.content,
      allowComment: editForm.value.allowComment
    })
    ElMessage.success('更新成功')
    showEditDialog.value = false
    resetEditForm()
    refreshDiscussions()
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error(error.message || '更新失败')
  } finally {
    updating.value = false
  }
}

// 删除讨论帖
const confirmDelete = (topic) => {
  ElMessageBox.confirm(
    `确定要删除讨论帖"${topic.title}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDiscussion(topic.id)
      ElMessage.success('删除成功')
      refreshDiscussions()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 置顶/取消置顶
const togglePin = async (topic) => {
  try {
    if (topic.pin) {
      await unpinDiscussion(topic.id)
      ElMessage.success('已取消置顶')
    } else {
      await pinDiscussion(topic.id)
      ElMessage.success('已置顶')
    }
    refreshDiscussions()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const toggleComment = async (topic) => {
  try {
    if (topic.status === 'CLOSED') {
      await openDiscussion(topic.id)
      ElMessage.success('已开启评论')
    } else {
      await closeDiscussion(topic.id)
      ElMessage.success('已关闭评论')
    }
    refreshDiscussions()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const changePage = (delta) => {
  const next = page.value + delta
  if (next < 1) return
  if (delta > 0 && !hasMore.value) return
  page.value = next
  loadDiscussions()
}

const changePageSize = (val) => {
  size.value = val || 10
  page.value = 1
  loadDiscussions()
}

const viewDetail = (id) => {
  if (isTeacher.value) {
    router.push(`/teacher/discussion/${id}`)
  } else {
    router.push(`/discussion/${id}`)
  }
}

// 查看详情
// 权限检查
// 重置表单
const resetCreateForm = () => {
  createForm.value = {
    title: '',
    content: '',
    pinned: false,
    allowComment: true
  }
}

const resetEditForm = () => {
  editForm.value = {
    id: null,
    title: '',
    content: '',
    allowComment: true
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
const normalizeTopic = (topic) => {
  if (!topic) return topic
  return {
    ...topic,
    pin: topic.pin === 1 || topic.pin === true,
    allowComment: topic.allowComment === 1 || topic.allowComment === true,
    deleted: topic.deleted === 1 || topic.deleted === true,
    replyCount: topic.replyCount ?? topic.commentCount ?? 0
  }
}
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.toLocaleDateString('zh-CN')
}

// 初始化
onMounted(() => {
  loadUserInfo()
  loadDiscussions()
})
</script>

<style scoped>
.discussion-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.keyword-input {
  width: 240px;
}

.subtitle {
  margin: 0;
  color: #9ca3af;
}

.admin-controls {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.pagination-controls {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-text {
  color: #6b7280;
  font-size: 12px;
}

.loading-container {
  padding: 20px;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.topic-card {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 20px 35px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: all 0.3s;
}

.topic-card:hover {
  box-shadow: 0 20px 35px rgba(15, 23, 42, 0.12);
}

.topic-card.deleted {
  opacity: 0.6;
  background: #f9fafb;
}

.topic-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  flex: 1;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: #1f2937;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.topic-tags {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.author {
  margin: 0;
  font-weight: 600;
}

.topic-title {
  margin: 0;
  font-size: 18px;
  cursor: pointer;
  transition: color 0.2s;
}

.topic-title:hover {
  color: #409eff;
}

.content {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topic-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #9ca3af;
  font-size: 13px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.topic-actions {
  display: flex;
  gap: 8px;
}
</style>

