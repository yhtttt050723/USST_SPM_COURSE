<template>
  <div class="course-switcher">
    <el-select
      v-model="selectedId"
      placeholder="选择课程"
      size="small"
      @change="onChange"
      style="width: 220px"
    >
      <el-option
        v-for="c in courses"
        :key="c.id"
        :label="`${c.name}（${c.academicYear} ${c.term === 'UPPER' ? '上' : '下'}）`"
        :value="c.id"
      />
    </el-select>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/stores/useUserStore'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const courses = computed(() => userStore.myCourses || [])
const selectedId = ref(userStore.currentCourse?.id)

watch(
  () => userStore.currentCourse,
  (val) => {
    if (val) selectedId.value = val.id
  }
)

const onChange = (id) => {
  const target = courses.value.find((c) => c.id === id)
  if (target) {
    userStore.setCurrentCourse(target)
    // 简单刷新当前页逻辑：回到首页
    router.replace('/main')
  }
}
</script>

<style scoped>
.course-switcher {
  display: flex;
  align-items: center;
}
</style>

