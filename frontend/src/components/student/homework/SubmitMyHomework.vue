<template>
  <div class="submit-homework-page">
    <div class="empty-state">
      <el-alert 
        v-if="!isResubmit" 
        title="您还没有提交作业" 
        type="warning" 
        class="empty-text"
      ></el-alert>
      <div class="submit-form">
        <div class="form-title">{{ isResubmit ? '重新提交作业' : '提交作业' }}</div>
        
        <div class="form-item">
          <div class="form-label">作业内容</div>
          <el-input
            v-model="submitContent"
            type="textarea"
            :rows="6"
            placeholder="请输入作业内容..."
            maxlength="5000"
            show-word-limit
          />
        </div>

        <div class="form-item">
          <div class="form-label">附件</div>
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            multiple
            :limit="5"
            accept=".pdf,.doc,.docx,.txt,.xls,.xlsx,.ppt,.pptx,.zip,.rar"
          >
            <el-button type="primary" :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、DOC、DOCX、TXT 等格式，最多5个文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </div>

        <div class="form-actions">
          <el-button @click="handleBack">返回</el-button>
          <el-button 
            type="primary" 
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交作业' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { submitAssignment } from '@/api/assignment'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/useUserStore'

const props = defineProps({
  assignmentId: {
    type: Number,
    required: true
  },
  isResubmit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submitted', 'back'])

const userStore = useUserStore()
const currentUser = computed(() => userStore.currentUser || {})
const submitting = ref(false)
const uploading = ref(false)

// 提交表单数据
const submitContent = ref('')
const fileList = ref([])
const uploadRef = ref(null)

// 处理文件选择
const handleFileChange = (file, files) => {
  // 检查文件大小
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    // 移除超大文件
    const index = files.indexOf(file)
    if (index > -1) {
      files.splice(index, 1)
    }
    return false
  }
  // 更新 fileList
  fileList.value = files
}

// 处理文件移除
const handleFileRemove = (file, files) => {
  // 更新 fileList
  fileList.value = files
}

// 返回
const handleBack = () => {
  emit('back')
}

// 提交作业
const handleSubmit = async () => {
  console.log('开始提交作业，当前文件列表:', fileList.value)
  
  if (!submitContent.value.trim() && fileList.value.length === 0) {
    ElMessage.warning('请填写作业内容或上传附件')
    return
  }

  await userStore.hydrateUserFromCache()
  const userId = currentUser.value.id
  const course = userStore.currentCourse
  const courseId = course?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  if (!courseId) {
    ElMessage.warning('请先选择课程')
    return
  }

  submitting.value = true
  uploading.value = true

  try {
    // 先上传文件
    const attachmentIds = []
    console.log('准备上传文件，文件数量:', fileList.value.length)
    
    for (const file of fileList.value) {
      console.log('处理文件:', file.name, 'raw:', file.raw)
      if (file.raw) {
        try {
          const formData = new FormData()
          formData.append('file', file.raw)
          formData.append('uploaderId', userId.toString())
          
          const uploadResponse = await uploadFile(formData)
          // 兼容两种返回格式：
          // 1) 直接返回文件对象 { id, fileName, ... }
          // 2) 标准格式 { code, data: { id, fileName, ... } }
          let fileId = null
          if (uploadResponse && uploadResponse.id) {
            fileId = uploadResponse.id
          } else if (uploadResponse && uploadResponse.data && uploadResponse.data.id) {
            fileId = uploadResponse.data.id
          }
          
          if (fileId) {
            attachmentIds.push(fileId)
          } else {
            console.warn('上传响应缺少文件ID，响应内容:', uploadResponse)
            ElMessage.error(`文件 ${file.name} 上传成功但未返回文件ID`)
          }
        } catch (error) {
          console.error('文件上传失败:', error)
          ElMessage.error(`文件 ${file.name} 上传失败`)
          uploading.value = false
          submitting.value = false
          return
        }
      }
    }
    
    uploading.value = false

    // 提交作业
    const payload = {
      content: submitContent.value,
      studentId: userId,
      attachmentIds: attachmentIds,
      courseId
    }
    console.log('[submit] 提交作业 payload:', payload)
    await submitAssignment(props.assignmentId, payload)
    
    ElMessage.success('作业提交成功')
    
    // 清空表单
    submitContent.value = ''
    fileList.value = []
    
    emit('submitted')
  } catch (error) {
    console.error('提交作业失败:', error)
    const msg = error?.response?.data?.message || error.message || '提交作业失败，请重试'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
    uploading.value = false
  }
}
</script>

<style scoped>
.submit-homework-page {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  margin-bottom: 32px;
}

.submit-form {
  max-width: 800px;
  margin: 0 auto;
  text-align: left;
}

.form-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  text-align: center;
}

.form-item {
  margin-bottom: 24px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.upload-demo {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
}
</style>
