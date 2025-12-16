<template>
  <div class="teacher-attendance-page">
    <div class="page-header">
      <div>
        <p class="subtitle">出勤管理</p>
        <h2>发布签到、查看记录与统计</h2>
      </div>
      <div class="page-actions">
        <el-select
          v-model="selectedCourseId"
          placeholder="选择课程"
          size="small"
          style="min-width: 220px"
          @change="handleCourseChange"
        >
          <el-option
            v-for="c in userStore.myCourses"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
        <el-button type="primary" @click="openCreate">发布签到</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <div class="filters">
        <div class="field">
          <span>每页</span>
          <el-input-number v-model="filters.size" :min="5" :max="50" size="small" @change="loadSessions" />
        </div>
        <div class="field">
          <el-button @click="loadSessions" :loading="loading">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-table :data="sessions" v-loading="loading" class="mt16">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="签到码" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180">
        <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="180">
        <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="360">
        <template #default="{ row }">
          <el-button size="small" @click="viewRecords(row)">记录</el-button>
          <el-button size="small" @click="viewStats(row)">统计</el-button>
          <el-button size="small" type="primary" @click="goDetail(row)">详情页</el-button>
          <el-button size="small" type="warning" :disabled="row.status !== 'ACTIVE'" @click="endSession(row)">
            结束
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-button size="small" :disabled="filters.page === 1" @click="changePage(-1)">上一页</el-button>
      <span class="page-text">第 {{ filters.page }} 页</span>
      <el-button size="small" :disabled="!hasMore" @click="changePage(1)">下一页</el-button>
    </div>

    <!-- 发布签到弹窗 -->
    <el-dialog v-model="createDialog" title="发布签到" width="500px">
      <el-form label-width="120px">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="可选" />
        </el-form-item>
        <el-form-item label="自定义签到码">
          <el-input v-model="createForm.code" maxlength="4" placeholder="可选4位数字，不填则系统生成" />
        </el-form-item>
        <el-form-item label="有效时长(分钟)">
          <el-input-number v-model="createForm.durationMinutes" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="或结束时间">
          <el-date-picker
            v-model="createForm.endTime"
            type="datetime"
            placeholder="可选，若设置则覆盖时长"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>

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
      <div class="pager">
        <el-button size="small" :disabled="recordPage === 1" @click="changeRecordPage(-1)">上一页</el-button>
        <span class="page-text">第 {{ recordPage }} 页</span>
        <el-button size="small" :disabled="!recordHasMore" @click="changeRecordPage(1)">下一页</el-button>
      </div>
    </el-drawer>

    <!-- 统计弹窗 -->
    <el-dialog v-model="statsDialog" title="签到统计" width="400px">
      <div v-if="stats">
        <p>签到任务ID：{{ stats.sessionId }}</p>
        <p>实到人数：{{ stats.presentCount }}</p>
      </div>
      <template #footer>
        <el-button @click="statsDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import {
  createAttendanceSession,
  endAttendanceSession,
  listAttendanceSessions,
  listAttendanceRecords,
  getAttendanceStats
} from '@/api/attendance';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/useUserStore';

const router = useRouter();
const userStore = useUserStore();
const selectedCourseId = ref(null);
const sessions = ref([]);
const loading = ref(false);
const hasMore = ref(false);
const filters = ref({
  page: 1,
  size: 10
});

const createDialog = ref(false);
const creating = ref(false);
const createForm = ref({
  courseId: null,
  title: '',
  code: '',
  durationMinutes: 10,
  endTime: ''
});

const recordDrawer = ref(false);
const recordLoading = ref(false);
const records = ref([]);
const recordPage = ref(1);
const recordHasMore = ref(false);
const currentSessionId = ref(null);

const statsDialog = ref(false);
const stats = ref(null);

const formatTime = (val) => {
  if (!val) return '--';
  return dayjs(val).format('YYYY-MM-DD HH:mm');
};

const statusType = (status) => {
  const map = {
    ACTIVE: 'success',
    ENDED: 'warning',
    EXPIRED: 'info'
  };
  return map[status] || 'info';
};

const initCourse = async () => {
  await userStore.hydrateUserFromCache();
  // 如果还没有课程列表，可以在别的页面已加载；这里只从本地缓存取
  const current = userStore.currentCourse;
  if (current && current.id) {
    selectedCourseId.value = current.id;
  } else if (userStore.myCourses.length > 0) {
    selectedCourseId.value = userStore.myCourses[0].id;
    userStore.setCurrentCourse(userStore.myCourses[0]);
  }
};

const handleCourseChange = (val) => {
  const found = userStore.myCourses.find((c) => c.id === val);
  if (found) {
    userStore.setCurrentCourse(found);
    filters.value.page = 1;
    loadSessions();
  }
};

const loadSessions = async () => {
  loading.value = true;
  try {
    await initCourse();
    const course = userStore.currentCourse;
    if (!course || !course.id) {
      ElMessage.warning('请先选择课程');
      sessions.value = [];
      loading.value = false;
      return;
    }
    const res = await listAttendanceSessions({
      courseId: course.id,
      page: filters.value.page - 1,
      size: filters.value.size
    });
    const pageData = res?.data || res;
    // 兼容 Page 对象或数组
    if (pageData?.content) {
      sessions.value = pageData.content;
      hasMore.value = !pageData.last;
    } else if (Array.isArray(pageData)) {
      sessions.value = pageData;
      hasMore.value = pageData.length === filters.value.size;
    } else {
      sessions.value = [];
      hasMore.value = false;
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const changePage = (delta) => {
  const next = filters.value.page + delta;
  if (next < 1) return;
  filters.value.page = next;
  loadSessions();
};

const openCreate = () => {
  createForm.value = {
    courseId: userStore.currentCourse?.id || null,
    title: '',
    code: '',
    durationMinutes: 10,
    endTime: ''
  };
  createDialog.value = true;
};

const handleCreate = async () => {
  if (createForm.value.code && !/^\d{4}$/.test(createForm.value.code)) {
    ElMessage.warning('自定义签到码需为4位数字');
    return;
  }
  creating.value = true;
  try {
    await userStore.hydrateUserFromCache();
    if (!createForm.value.courseId) {
      createForm.value.courseId = userStore.currentCourse?.id || null;
    }
    if (!createForm.value.courseId) {
      ElMessage.warning('请先选择课程');
      creating.value = false;
      return;
    }
    await createAttendanceSession({
      courseId: createForm.value.courseId,
      title: createForm.value.title || undefined,
      code: createForm.value.code || undefined,
      durationMinutes: createForm.value.endTime ? undefined : createForm.value.durationMinutes,
      endTime: createForm.value.endTime || undefined
    });
    ElMessage.success('发布成功');
    createDialog.value = false;
    loadSessions();
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '发布失败');
  } finally {
    creating.value = false;
  }
};

const endSession = async (row) => {
  try {
    await endAttendanceSession(row.id);
    ElMessage.success('已结束');
    loadSessions();
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '操作失败');
  }
};

const viewRecords = async (row) => {
  currentSessionId.value = row.id;
  recordPage.value = 1;
  recordDrawer.value = true;
  await loadRecords();
};

const loadRecords = async () => {
  if (!currentSessionId.value) return;
  recordLoading.value = true;
  try {
    const res = await listAttendanceRecords(currentSessionId.value, {
      page: recordPage.value - 1,
      size: 10
    });
    const data = res?.data || res;
    if (data?.content) {
      records.value = data.content;
      recordHasMore.value = !data.last;
    } else if (Array.isArray(data)) {
      records.value = data;
      recordHasMore.value = data.length === 10;
    } else {
      records.value = [];
      recordHasMore.value = false;
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '加载记录失败');
  } finally {
    recordLoading.value = false;
  }
};

const changeRecordPage = async (delta) => {
  const next = recordPage.value + delta;
  if (next < 1) return;
  recordPage.value = next;
  await loadRecords();
};

const viewStats = async (row) => {
  statsDialog.value = true;
  stats.value = null;
  try {
    const res = await getAttendanceStats(row.id);
    stats.value = res?.data || res;
  } catch (e) {
    console.error(e);
    ElMessage.error(e.message || '获取统计失败');
  }
};

const goDetail = (row) => {
  router.push({
    name: 'TeacherAttendanceDetail',
    params: { id: row.id }
  });
};

loadSessions();
</script>

<style scoped>
.teacher-attendance-page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.subtitle {
  color: #888;
  margin: 0;
}
.page-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.filter-card {
  margin-bottom: 12px;
}
.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}
.filters .field {
  display: flex;
  gap: 6px;
  align-items: center;
}
.mt16 {
  margin-top: 16px;
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
