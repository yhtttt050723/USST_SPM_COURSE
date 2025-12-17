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
        <span class="lable">所属课程 :</span>
        <span class="value">
          <el-select
            v-model="selectedCourseId"
            placeholder="请选择课程"
            style="width: 260px"
            filterable
          >
            <el-option
              v-for="c in courseOptions"
              :key="c.id"
              :label="formatCourseLabel(c)"
              :value="c.id"
            />
          </el-select>
        </span>
      </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createAssignment as createAssignmentApi } from '@/api/teacher'
import { useUserStore } from '@/stores/useUserStore'
import { listMyCourses } from '@/api/course'

const router = useRouter()
const userStore = useUserStore()
const selectedCourseId = ref(null)
const courseOptions = ref([])

const formModel = ref({
  title: '',
  description: '',
  type: 'HOMEWORK',
  dueDate: null,
  totalScore: 100,
  times: '1' // 修改为字符串以匹配 radio-button 的 value
})
const defaultTime = new Date(2025, 1, 1, 23, 59, 59)
const size = 'default'

// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const handleChange = () => {}

// 创建作业
const createAssignment = async () => {
  // 验证必填字段
  if (!formModel.value.title || formModel.value.title.trim() === '') {
    ElMessage.warning('请输入作业标题')
    return
  }
  
  if (!selectedCourseId.value) {
    ElMessage.warning('请选择课程')
    return
  }

  if (!formModel.value.dueDate) {
    ElMessage.warning('请选择截止时间')
    return
  }

  try {
    await userStore.hydrateUserFromCache()
    const courseId = selectedCourseId.value

    const payload = {
      title: formModel.value.title,
      description: formModel.value.description,
      type: formModel.value.type,
      dueAt: formModel.value.dueDate,
      totalScore: formModel.value.totalScore,
      allowResubmit: formModel.value.times === '1',
      courseId
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

// 初始化课程列表和默认选中课程
const initCourses = async () => {
  await userStore.hydrateUserFromCache()
  if (!userStore.myCourses.length) {
    try {
      const res = await listMyCourses()
      const data = res?.data || res || []
      userStore.setCourses(data)
    } catch (e) {
      console.error('加载课程失败', e)
    }
  }
  courseOptions.value = userStore.myCourses
  if (userStore.currentCourse?.id) {
    selectedCourseId.value = userStore.currentCourse.id
  } else if (courseOptions.value.length > 0) {
    selectedCourseId.value = courseOptions.value[0].id
    userStore.setCurrentCourse(courseOptions.value[0])
  }
}

onMounted(() => {
  initCourses()
})

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

// 课程显示：名称 + 年份 + 学期
const formatCourseLabel = (c) => {
  if (!c) return ''
  const parts = [c.name]
  if (c.academicYear) parts.push(c.academicYear)
  if (c.semester) parts.push(c.semester)
  if (c.term) parts.push(c.term)
  return parts.filter(Boolean).join(' - ')
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