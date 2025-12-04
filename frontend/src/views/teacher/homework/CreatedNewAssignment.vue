<template>
  <div class="created-new-assignment">
    <div class="back-btn">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
    </div>

    <div class="info">
      <div class="item">
        <span class="lable">作业标题 : </span>
        <span class="value">
          <el-input
            v-model="formModel.title"
            placeholder="请输入作业标题..."
            maxlength="128"
            show-word-limit
          />
        </span>
      </div>
      <div class="item">
        <span class="lable">作业类型 : </span>
        <span class="value">
          <el-input
            v-model="formModel.type"
            placeholder="请输入作业类型..."
            default-value="HOMEWORK"
            maxlength="32"
            show-word-limit
          />
        </span>
      </div>
      <div class="item">
        <span class="lable descript">作业描述 : </span>
        <span class="value">
          <el-input
            v-model="formModel.description"
            type="textarea"
            :rows="6"
            style="width: 80%;"
            placeholder="请输入作业描述..."
            maxlength="5000"
            show-word-limit
          />
        </span>
      </div>
    </div>

    <div class="meta">
      <div class="item">
        <span class="lable">总&nbsp; &nbsp; &nbsp; &nbsp;分 : </span>
        <span class="value">
          <el-input-number v-model="formModel.totalScore" :min="1" :max="500" @change="handleChange" />
        </span>
      </div>
      <div class="item">
        <span class="lable">截止时间 : </span>
        <span class="value">
          <el-date-picker
            v-model="formModel.dueDate"
            type="datetime"
            placeholder="请选择截止时间"
            :size="size"
            :default-time="defaultTime"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </span>
      </div>
      <div class="item">
        <span class="lable">提交次数 : </span>
        <span class="value">
          <el-radio-group v-model="formModel.times">
            <el-radio-button label="0" size="small">一次</el-radio-button>
            <el-radio-button label="1" size="small">多次</el-radio-button>
          </el-radio-group>
        </span>
      </div>
    </div>

    <div class="button">
      <el-button type="warning" @click="reset">重置</el-button>
      <el-button type="primary" @click="createAssignment">创建作业</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createAssignment as createAssignmentApi } from '@/api/teacher'

const router = useRouter()

const formModel = ref({
  title: '',
  description: '',
  type: 'HOMEWORK',
  dueDate: null,
  totalScore: 100,
  times: '1' // 修改为字符串以匹配 radio-button 的 value
})
const defaultTime = new Date(2025, 1, 1, 23, 59, 59)

// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 创建作业
const createAssignment = async () => {
  // 验证必填字段
  if (!formModel.value.title || formModel.value.title.trim() === '') {
    ElMessage.warning('请输入作业标题')
    return
  }
  
  if (!formModel.value.dueDate) {
    ElMessage.warning('请选择截止时间')
    return
  }

  try {
    const payload = {
      title: formModel.value.title,
      description: formModel.value.description,
      type: formModel.value.type,
      dueAt: formModel.value.dueDate,
      totalScore: formModel.value.totalScore,
      allowResubmit: formModel.value.times === '1' 
    }
    
    await createAssignmentApi(payload)
    ElMessage.success('作业创建成功')
    
    setTimeout(() => {
      router.push({ name: 'TeacherHomework' })
    }, 1000)
  } catch (error) {
    console.error('作业创建失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '作业创建失败，请重试'
    ElMessage.error(errorMsg)
  }
}

const goBack = () => {
  router.back()
}

const reset = () => {
  formModel.value = {
    title: '',
    description: '',
    type: 'HOMEWORK',
    dueDate: null,
    totalScore: 100,
    times: '1'
  }
  ElMessage.info('表单已重置')
}
</script>

<style scoped>
.created-new-assignment {
  padding: 40px;
  border-radius: 24px;
  background-color: white;
}
.back-btn {
  margin-bottom: 20px;
}
.info, .meta, .button {
  color: #353535;
  margin-bottom: 16px;
  width: 100%;
  padding: 20px;
  background-color: white;
  box-shadow: 0 4px 8px rgba(152, 105, 105, 0.06);
  border-radius: 12px;
}
.lable {
  font-weight: 500;
  color: #666;
  min-width: 80px;
  width: 20%;
  vertical-align: middle;
}
.lable.descript {
  vertical-align: top;
}
.item {
  margin-bottom: 12px;
  width: 100%;
}
.value .el-input {
  width: 80%;
}
.el-date-picker {
  height: 20px;
}
</style>