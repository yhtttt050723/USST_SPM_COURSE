<template>
  <div class="announcement-page">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="openCreateDialog">发布公告</el-button>
    </div>

    <!-- 课程选择 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="选择课程/班级">
          <el-select v-model="selectedCourseId" placeholder="请选择" @change="loadAnnouncements" style="width: 300px">
            <el-option label="全校公告" :value="0" />
            <el-option
              v-for="course in myCourses"
              :key="course.id"
              :label="`${course.name} (${course.code})`"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
          <span class="count">共 {{ announcements.length }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="announcements" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="发布范围" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.courseId === 0" type="warning">全校公告</el-tag>
            <el-tag v-else type="success">课程公告</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isPinned" type="danger" size="small">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="发布人" width="120" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editAnnouncement(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deleteAnnouncement(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && announcements.length === 0" class="empty">
        <el-empty description="暂无公告" />
      </div>
    </el-card>

    <!-- 创建/编辑公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑公告' : '发布公告'"
      width="700px"
      @close="resetForm"
    >
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="发布范围" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择" style="width: 100%">
            <el-option label="全校公告" :value="0" />
            <el-option
              v-for="course in myCourses"
              :key="course.id"
              :label="`${course.name} (${course.code})`"
              :value="course.id"
            />
          </el-select>
          <div class="tip">选择"全校公告"将向所有学生显示</div>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isPinned" />
          <div class="tip">置顶的公告会显示在列表顶部</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/useUserStore'
import { listMyCourses } from '@/api/course'
import {
  getAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement as deleteAnnouncementApi
} from '@/api/announcement'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const selectedCourseId = ref(0) // 默认选择全校公告
const myCourses = ref([])
const announcements = ref([])

const formRef = ref(null)
const form = reactive({
  courseId: 0,
  title: '',
  content: '',
  isPinned: false
})

const rules = {
  courseId: [{ required: true, message: '请选择发布范围', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 加载课程列表
const loadCourses = async () => {
  try {
    const resp = await listMyCourses()
    myCourses.value = resp.data || resp || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
  }
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    // 教师端查看公告时，如果选择了具体课程，只显示该课程的公告；如果选择全校公告，只显示全校公告
    const includeGlobal = false
    const resp = await getAnnouncements(selectedCourseId.value, includeGlobal)
    announcements.value = resp.data || resp || []
  } catch (error) {
    console.error('加载公告列表失败:', error)
    ElMessage.error('加载公告列表失败')
    announcements.value = []
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 打开创建对话框
const openCreateDialog = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

// 编辑公告
const editAnnouncement = (row) => {
  editingId.value = row.id
  form.courseId = row.courseId
  form.title = row.title
  form.content = row.content
  form.isPinned = row.isPinned || false
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  editingId.value = null
  form.courseId = selectedCourseId.value
  form.title = ''
  form.content = ''
  form.isPinned = false
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    if (editingId.value) {
      // 更新公告
      await updateAnnouncement(editingId.value, {
        title: form.title,
        content: form.content,
        isPinned: form.isPinned
      })
      ElMessage.success('更新公告成功')
    } else {
      // 创建公告
      await createAnnouncement({
        courseId: form.courseId,
        title: form.title,
        content: form.content,
        isPinned: form.isPinned
      })
      ElMessage.success('发布公告成功')
    }
    dialogVisible.value = false
    await loadAnnouncements()
  } catch (error) {
    console.error('提交公告失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除公告
const deleteAnnouncement = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除公告"${row.title}"吗？`, '确认删除', {
      type: 'warning'
    })
    await deleteAnnouncementApi(row.id)
    ElMessage.success('删除成功')
    await loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await loadCourses()
  await loadAnnouncements()
})
</script>

<style scoped>
.announcement-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.filter-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count {
  color: #6b7280;
  font-size: 14px;
}

.empty {
  padding: 40px 0;
  text-align: center;
}

.tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>

