<template>
  <div class="teacher-attendance-page">
    <!-- 警告通知 -->
    <div class="warning" v-if="pendingLeaveCount > 0">
      <p class="content">
        您有 {{ pendingLeaveCount }} 条请假记录待审批
      </p>
    </div>

    <!-- 表头 -->
    <el-row class="teacher-attendance-header">
      <el-col class="left" :span="18">
        <el-row class="left-header">
          <el-col class="time" :span="6">开始时间</el-col>
          <el-col class="time" :span="6">结束时间</el-col>
          <el-col class="count" :span="6"></el-col>
          <el-col class="status" :span="6">状态</el-col>
        </el-row>
      </el-col>
      <el-col class="component" :span="6"></el-col>
    </el-row>

    <!-- 主体内容 -->
    <el-row class="body" :gutter="20">
      <!-- 左侧签到列表 -->
      <el-col class="left-body" :span="18">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        
        <!-- 签到列表 -->
        <div v-else-if="sessions.length > 0" class="attendance-list">
          <AttendanceBox 
            v-for="session in sessions" 
            :key="session.id" 
            :session="session"
            :present-count="session.presentCount || 0"
            @click="viewRecords"
          />
        </div>
        
        <!-- 空状态 -->
        <div v-else class="empty">
          <el-empty description="暂无签到记录" />
        </div>
      </el-col>

      <!-- 右侧组件区域 -->
      <el-col class="right-body" :span="6">
        <ComponentBox 
          class="component" 
          @refresh="loadSessions"
        />
      </el-col>
    </el-row>

    <!-- 记录抽屉 -->
    <el-drawer v-model="recordDrawer" size="50%" title="签到记录">
      <el-table :data="records" v-loading="recordLoading">
        <el-table-column prop="studentName" label="学生" width="140" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="checkinTime" label="签到时间" width="200">
          <template #default="{ row }">{{ formatTime(row.checkinTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="result" label="结果" width="100" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import dayjs from 'dayjs';
import {
  listAttendanceSessions,
  listAttendanceRecords
} from '@/api/attendance';
import { ElMessage } from 'element-plus';
import ComponentBox from '@/components/teacher/attendance/ComponentBox.vue';
import AttendanceBox from '@/components/teacher/attendance/AttendanceBox.vue';

const sessions = ref([]);
const loading = ref(false);
const pendingLeaveCount = ref(0);

const recordDrawer = ref(false);
const recordLoading = ref(false);
const records = ref([]);
const currentSessionId = ref(null);

const formatTime = (val) => {
  if (!val) return '--';
  return dayjs(val).format('YYYY-MM-DD HH:mm');
};

const loadSessions = async () => {
  loading.value = true;
  try {
    const res = await listAttendanceSessions({
      courseId: 1,
      page: 0,
      size: 50
    });
    const pageData = res?.data || res;
    // 兼容 Page 对象或数组
    if (pageData?.content) {
      sessions.value = pageData.content;
    } else if (Array.isArray(pageData)) {
      sessions.value = pageData;
    } else {
      sessions.value = [];
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const viewRecords = async (row) => {
  currentSessionId.value = row.id;
  recordDrawer.value = true;
  await loadRecords();
};

const loadRecords = async () => {
  if (!currentSessionId.value) return;
  recordLoading.value = true;
  try {
    const res = await listAttendanceRecords(currentSessionId.value, {
      page: 0,
      size: 100
    });
    const data = res?.data || res;
    if (data?.content) {
      records.value = data.content;
    } else if (Array.isArray(data)) {
      records.value = data;
    } else {
      records.value = [];
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '加载记录失败');
  } finally {
    recordLoading.value = false;
  }
};

// 页面加载时获取数据
onMounted(() => {
  loadSessions();
});
</script>

<style scoped>
.teacher-attendance-page {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.warning {
  background-color: #fff3cd;
  border: 1px solid #ffeeba;
  border-radius: 24px;
  padding: 15px 20px;
  margin-bottom: 20px;
}

.warning .content {
  margin: 0;
  color: #856404;
  font-size: 14px;
  font-weight: 500;
}

.teacher-attendance-header {
  margin: 15px 0;
}

.teacher-attendance-header .el-col {
  padding: 0 20px;
}

.el-col.time {
  font-size: 15px;
  color: #999;
  text-align: center;
}

.el-col.count, .el-col.status {
  font-size: 15px;
  color: #999;
  text-align: center;
}

.body {
  margin: 0;
}

.left-body {
  background-color: white;
  border-radius: 25px;
  padding: 20px;
  min-height: 400px;
}

.loading {
  padding: 20px;
}

.attendance-list {
  display: flex;
  flex-direction: column;
}

.empty {
  padding: 60px 20px;
  text-align: center;
}

.right-body {
  display: flex;
  flex-direction: column;
}

.component {
  width: 100%;
}


</style>
