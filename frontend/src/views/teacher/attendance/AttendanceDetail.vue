<template>
  <div class="teacher-attendance-detail-page">
    <div class="header">
      <div>
        <p class="subtitle">出勤详情</p>
        <h2>签到任务 #{{ sessionId }}</h2>
      </div>
      <el-button @click="$router.back()">返回上一页</el-button>
    </div>

    <el-card class="stats-card" v-loading="loadingStats">
      <template #header>
        <div class="card-header">
          <span>统计信息</span>
        </div>
      </template>
      <div v-if="stats">
        <p>签到任务ID：{{ stats.sessionId }}</p>
        <p>实到人数：{{ stats.presentCount }}</p>
      </div>
      <div v-else>暂无统计数据</div>
    </el-card>

    <el-card class="records-card" v-loading="loadingRecords">
      <template #header>
        <div class="card-header">
          <span>签到记录</span>
        </div>
      </template>
      <el-table :data="records">
        <el-table-column prop="studentName" label="学生" width="140" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="checkinTime" label="签到时间" width="200">
          <template #default="{ row }">{{ formatTime(row.checkinTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="result" label="结果" width="100" />
      </el-table>
      <div class="pager">
        <el-button size="small" :disabled="page === 1" @click="changePage(-1)">上一页</el-button>
        <span class="page-text">第 {{ page }} 页</span>
        <el-button size="small" :disabled="!hasMore" @click="changePage(1)">下一页</el按钮>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import dayjs from 'dayjs';
import { listAttendanceRecords, getAttendanceStats } from '@/api/attendance';
import { ElMessage } from 'element-plus';

const route = useRoute();
const sessionId = route.params.id;

const records = ref([]);
const page = ref(1);
const size = ref(10);
const hasMore = ref(false);
const loadingRecords = ref(false);
const loadingStats = ref(false);
const stats = ref(null);

const formatTime = (val) => {
  if (!val) return '--';
  return dayjs(val).format('YYYY-MM-DD HH:mm');
};

const loadStats = async () => {
  loadingStats.value = true;
  try {
    const res = await getAttendanceStats(sessionId);
    stats.value = res?.data || res;
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '获取统计失败');
  } finally {
    loadingStats.value = false;
  }
};

const loadRecords = async () => {
  loadingRecords.value = true;
  try {
    const res = await listAttendanceRecords(sessionId, {
      page: page.value - 1,
      size: size.value
    });
    const data = res?.data || res;
    if (data?.content) {
      records.value = data.content;
      hasMore.value = !data.last;
    } else if (Array.isArray(data)) {
      records.value = data;
      hasMore.value = data.length === size.value;
    } else {
      records.value = [];
      hasMore.value = false;
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '加载记录失败');
  } finally {
    loadingRecords.value = false;
  }
};

const changePage = (delta) => {
  const next = page.value + delta;
  if (next < 1) return;
  page.value = next;
  loadRecords();
};

onMounted(() => {
  loadStats();
  loadRecords();
});
</script>

<style scoped>
.teacher-attendance-detail-page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.subtitle {
  color: #888;
  margin: 0;
}
.stats-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.records-card {
  margin-top: 12px;
}
.pager {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-text {
  color: #666;
}
</style>
