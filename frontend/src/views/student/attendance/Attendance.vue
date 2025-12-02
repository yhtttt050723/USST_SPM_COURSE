<template>
  <div class="attendance-page">
    <el-row class="stu-attendance-header">

      <el-col class="time" :span="12">点名时间</el-col>
      <el-col class="state" :span="6">出勤状态</el-col>
      <el-col class="component" :span="6"></el-col>
    </el-row>
    <el-row class="body">
      <!-- 左侧list-->
      <el-col class="left-body" :span="18">
        <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    
    <!-- 出勤列表 -->
    <div v-else class="homework-list">
      <AttendanceBox />
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && attendanceRecords.length === 0" class="empty">
      <el-empty description="暂无出勤记录" />
    </div>
      </el-col>

      <!-- 组件区域 -->
      <el-col class="right-body" :span="5">
        <ComponentBox class="component" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMyAttendance } from '@/api/attendance';
import AttendanceBox from '@/components/student/attendance/AttendanceBox.vue';
import ComponentBox from '@/components/student/attendance/ComponentBox.vue';
const loading = ref(false);
const attendanceRecords = ref([]);

// 获取出勤记录
const fetchAttendanceRecords = async () => {
  loading.value = true;
  try {
    const response = await getMyAttendance();
    attendanceRecords.value = response.data || [];
  } catch (error) {
    console.error('获取出勤记录失败:', error);
    ElMessage.error('获取出勤记录失败');
    attendanceRecords.value = [];
  } finally {
    loading.value = false;
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchAttendanceRecords();
});
</script>

<style scoped>
.attendance-page {
  margin-bottom: 20px;
}

.stu-attendance-header .el-col {
  margin: 15px 0px;
  padding: 0px 50px;
}

.body {
  margin: 0;
}

.body .el-col {
  padding: 0;
}

.el-col.time {
  font-size: 15px;
  color: #999;
  text-align: center;
}
.el-col.state {
  font-size: 15px;
  color: #999;
  text-align: center;
}

.left-body {
  width: 100%;
  height: auto;
  background-color: white;
  border-radius: 25px;
}
.right-body {
  margin-left: 15px;
}
.component {
  width: 100%;
}
</style>
