<template>
  <el-row class="main-row" :gutter="15">
    <el-col :span="16" class="left-col">
      <div class="homework-container">
        <CourseNote />
      </div>
      <div class="statistical" style="margin-top: 15px;">
        <Statistical />
      </div>
    </el-col>
    <el-col :span="8" class="right-col">
      <div class="course-note-container">
        <HomeWork />
      </div>
    </el-col>
  </el-row>
</template>

<script setup>
import HomeWork from '@/components/student/home/HomeWork.vue'
import CourseNote from '@/components/student/home/CourseNote.vue'
import Statistical from '@/components/student/home/Statistical.vue'
</script>

<style>

</style>