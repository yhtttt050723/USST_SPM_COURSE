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
        class="buttonOfSubmitDetail" 
        :class="{ active: !isdetail }"
        @click="switchToSubmitDetail" 
        :span="4"
      >
        提交详情
      </el-col>
    </el-row>

    <div class="body">
      <!-- 作业内容板块 -->
      <div class="detail" v-if="isdetail" >
      <HomeworkContent 
      :assignmentId="assignmentId"
      />
      </div>
    <!-- 提交详情板块 -->
    <div class="submit-detail" v-else>
      <SubmitDetail 
      :assignmentId="assignmentId"
      />
    </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import HomeworkContent from '@/components/public/homework/HomeworkContent.vue'
import SubmitDetail from '@/components/teacher/homework/SubmitDetail.vue'
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
const switchToSubmitDetail = () => {
  isdetail.value = false
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

.buttonOfDetail, .buttonOfSubmitDetail {
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

.buttonOfDetail:hover, .buttonOfSubmitDetail:hover {
  background-color: #aba9a9;
  color: #333;
}

.buttonOfDetail.active, .buttonOfSubmitDetail.active {
  background-color: white;
  color: #409EFF;
  font-weight: 600;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.detail, .submit-detail {
  min-height: 650px;
  background-color: white;
  border-radius: 25px;
  border-top-left-radius: 0;
  padding: 50px;
}
</style>