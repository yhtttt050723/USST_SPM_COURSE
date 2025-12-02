<template>
  <div class="homework-detail-page">
    <el-row class="button" >
      <el-col 
        class="buttonOfDetail" 
        :class="{ active: isdetail }"
        @click="switchToDetail" 
        :span="4"
      >
        作业内容
      </el-col>
      <el-col 
        class="buttonOfMyDetail" 
        :class="{ active: !isdetail }"
        @click="switchToMyDetail" 
        :span="4"
      >
        我的作业
      </el-col>
    </el-row>

    <div class="body">
      <!-- 作业内容板块 -->
      <div class="detail" v-if="isdetail" >
      <HomeworkContent 
      :assignmentId="assignmentId"
      @submit="handleSubmit"
      @viewSubmission="handleViewSubmission"
      />
      </div>
    <!-- 我的作业板块 -->
    <div class="mydetail" v-else>
      <MyHomeworkDetail 
        :assignmentId="assignmentId"
        @resubmit="handleResubmit"
        @back="handleBack"
      />
    </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import HomeworkContent from '@/components/public/homework/HomeworkContent.vue'
import MyHomeworkDetail from '@/components/student/homework/MyHomeworkDetail.vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const userStore = useUserStore()
const currentUser = computed(() => userStore.currentUser || {})

const route = useRoute()
const assignmentId = computed(() => {
  const id = route.query.id
  return id ? Number(id) : 1
})
const isdetail = ref(true)

const switchToDetail = () => {
  isdetail.value = true
}
const switchToMyDetail = () => {
  isdetail.value = false
}

const handleSubmit = (assignment) => {
  // 处理提交逻辑
  console.log('提交作业:', assignment)
}

const handleViewSubmission = (assignment) => {
  // 查看提交详情
  switchToMyDetail()
}

const handleResubmit = () => {
  // 重新提交
  switchToDetail()
}

const handleBack = () => {
  switchToDetail()
}
</script>

<style scoped>
.homework-detail-page {
  width: 100%;
  margin-bottom: 20px;
}

.button {
  margin: 0;
  width: 100%;
}

.buttonOfDetail, .buttonOfMyDetail {
  width: 200px;
  height: 50px;
  border-top-left-radius: 25px;
  border-top-right-radius: 25px;
  background-color: #e8e8e8;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-weight: 500;
}

.buttonOfDetail:hover, .buttonOfMyDetail:hover {
  background-color: #aba9a9;
  color: #333;
}

.buttonOfDetail.active, .buttonOfMyDetail.active {
  background-color: white;
  color: #409EFF;
  font-weight: 600;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.detail, .mydetail {
  min-height: 650px;
  background-color: white;
  border-radius: 25px;
  border-top-left-radius: 0;
  padding: 50px;
}
</style>