<template>
  <div class="component">
    <!-- 签到发布区 -->
    <div class="box">
      <div class="title-small">签 到</div>
      <div class="setTime">
        <el-select 
          v-model="time" 
          class="select" 
          placeholder="请选择持续时长" 
          style="width: 240px;"
          :disabled="isPublishing || currentSession !== null"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div class="time-info" v-if="currentSession">
        <div class="time-label">结束时间</div>
        <div class="time-value">{{ endTimeText }}</div>
      </div>
      <div 
        class="button" 
        :class="{ active: !currentSession, disabled: isPublishing }" 
        @click="handleAttendance"
      >
        <el-icon v-if="isPublishing" class="is-loading"><Loading /></el-icon>
        <span v-else>{{ currentSession ? '结 束 签 到' : '发 布 签 到' }}</span>
      </div>

      <!-- 签到密码区 -->
      <div class="number" v-if="currentSession && attendanceCode">
        <FourNumber class="password" :code="attendanceCode" />
      </div>
    </div>

    <!-- 统计信息区 -->
    <div class="box stats-box">
      <div class="title-small">统 计 信 息</div>
      <div class="stats-content">
        <div class="stat-item">
          <div class="stat-label">签到人数</div>
          <div class="stat-value">{{ currentStats.presentCount || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">总人数</div>
          <div class="stat-value">{{ currentStats.totalCount || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">签到率</div>
          <div class="stat-value">{{ attendanceRate }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import FourNumber from '@/components/public/attendance/FourNumber.vue';
import { ref, onMounted, computed, watch, onUnmounted } from 'vue';
import { createAttendanceSession, endAttendanceSession, getAttendanceStats, listAttendanceSessions } from '@/api/attendance';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import dayjs from 'dayjs';

const emit = defineEmits(['refresh']);

const options = [
  { value: 1, label: '1 分钟' },
  { value: 3, label: '3 分钟' },
  { value: 5, label: '5 分钟' },
  { value: 10, label: '10 分钟' },
  { value: 15, label: '15 分钟' },
  { value: 20, label: '20 分钟' },
  { value: 30, label: '30 分钟' },
  { value: 60, label: '60 分钟' },
];

const time = ref(10);
const isPublishing = ref(false);
const currentSession = ref(null);
const attendanceCode = ref('');
const currentStats = ref({
  presentCount: 0,
  totalCount: 0
});
let statsTimer = null;

// 计算签到率
const attendanceRate = computed(() => {
  if (!currentStats.value.totalCount) return '0%';
  const rate = ((currentStats.value.presentCount / currentStats.value.totalCount) * 100).toFixed(1);
  return `${rate}%`;
});

// 结束时间显示
const endTimeText = computed(() => {
  if (!currentSession.value?.endTime) return '--';
  return dayjs(currentSession.value.endTime).format('HH:mm');
});

// 发布签到
const publishAttendance = async () => {
  isPublishing.value = true;
  try {
    const response = await createAttendanceSession({
      courseId: 1,
      title: '课堂签到',
      durationMinutes: time.value
    });
    
    const session = response?.data || response;
    currentSession.value = session;
    attendanceCode.value = session.code;
    
    ElMessage.success('签到已发布');
    emit('refresh');
    
    // 开始定时获取统计信息
    startStatsPolling();
  } catch (error) {
    console.error('发布签到失败:', error);
    ElMessage.error(error.message || '发布签到失败');
  } finally {
    isPublishing.value = false;
  }
};

// 结束签到
const endAttendance = async () => {
  if (!currentSession.value) return;
  
  isPublishing.value = true;
  try {
    await endAttendanceSession(currentSession.value.id);
    ElMessage.success('签到已结束');
    
    // 清除当前会话
    currentSession.value = null;
    attendanceCode.value = '';
    stopStatsPolling();
    
    emit('refresh');
  } catch (error) {
    console.error('结束签到失败:', error);
    ElMessage.error(error.message || '结束签到失败');
  } finally {
    isPublishing.value = false;
  }
};

// 处理签到按钮点击
const handleAttendance = () => {
  if (isPublishing.value) return;
  
  if (currentSession.value) {
    endAttendance();
  } else {
    publishAttendance();
  }
};

// 获取统计信息
const fetchStats = async () => {
  if (!currentSession.value) return;
  
  try {
    const response = await getAttendanceStats(currentSession.value.id);
    const stats = response?.data || response;
    currentStats.value = {
      presentCount: stats.presentCount || 0,
      totalCount: stats.totalCount || 50 // 默认总人数，可以从课程信息获取
    };
  } catch (error) {
    console.error('获取统计信息失败:', error);
  }
};

// 开始定时获取统计
const startStatsPolling = () => {
  fetchStats(); // 立即获取一次
  statsTimer = setInterval(fetchStats, 5000); // 每5秒更新一次
};

// 停止定时获取统计
const stopStatsPolling = () => {
  if (statsTimer) {
    clearInterval(statsTimer);
    statsTimer = null;
  }
  currentStats.value = { presentCount: 0, totalCount: 0 };
};

// 检查是否有进行中的签到
const checkActiveSession = async () => {
  try {
    const response = await listAttendanceSessions({
      courseId: 1,
      page: 0,
      size: 10
    });
    
    const pageData = response?.data || response;
    const sessions = pageData?.content || (Array.isArray(pageData) ? pageData : []);
    
    // 查找状态为ACTIVE的签到会话
    const activeSession = sessions.find(session => session.status === 'ACTIVE');
    
    if (activeSession) {
      currentSession.value = activeSession;
      attendanceCode.value = activeSession.code;
      
      // 开始统计轮询
      startStatsPolling();
      
      console.log('检测到进行中的签到:', activeSession);
    }
  } catch (error) {
    console.error('检查进行中签到失败:', error);
  }
};

onMounted(() => {
  // 组件加载时检查是否有进行中的签到
  checkActiveSession();
});

onUnmounted(() => {
  stopStatsPolling();
});
</script>

<style scoped>
.component {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.box {
  min-height: 200px;
  background: linear-gradient(135deg, rgb(31, 41, 55) 0%, rgb(55, 65, 81) 100%);
  border-radius: 25px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.box:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.stats-box {
  min-height: 200px;
}

.number {
  display: block;
  margin: 30px auto;
  width: 100%;
}

.empty-code {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80px;
}

.title, .button {
  display: block;
  margin: 20px auto;
  width: 100%;
  height: 60px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  text-align: center;
  font-size: 28px;
  line-height: 60px;
  color: white;
  font-weight: 500;
  letter-spacing: 8px;
}

.title-small {
  display: block;
  margin: 0 auto 20px;
  width: 100%;
  height: 35px;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  text-align: center;
  font-size: 18px;
  line-height: 35px;
  color: white;
  font-weight: 500;
  letter-spacing: 6px;
}

.setTime {
  display: block;
  margin: 15px auto;
  width: 100%;
  height: 45px;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 auto;
}

.time-info {
  display: block;
  margin: 10px auto;
  width: 250px;
  text-align: center;
}

.time-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-bottom: 5px;
}

.time-value {
  color: white;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
}

.button {
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.button:not(.disabled):hover {
  cursor: pointer;
  transform: scale(1.05);
}

.button.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  transition: background-color 0.3s ease;
}

.stat-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.stat-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  font-weight: 400;
}

.stat-value {
  color: white;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: none;
  border-radius: 8px;
}

:deep(.el-input__inner) {
  color: rgb(31, 41, 55);
  font-weight: 500;
}

.is-loading {
  animation: rotating 2s linear infinite;
}
.password {
  width: 100%;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>