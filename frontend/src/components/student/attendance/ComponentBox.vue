<template>
  <div class="box">
    <div class="header">
      <FourNumber :code="code" />
    </div>
    <el-form class="form" @submit.prevent="handleSubmit">
      <el-form-item>
        <el-input
          v-model="code"
          maxlength="4"
          placeholder="请输入签到码"
          clearable
          :disabled="loading"
        />
      </el-form-item>
      <el-button
        type="primary"
        class="submit-btn"
        :loading="loading"
        :disabled="loading"
        native-type="submit"
        @click="handleSubmit"
      >
        提交签到
      </el-button>
      <div class="hint">签到仅限当前课程，超时或重复会提示</div>
      <div v-if="message" :class="['result', messageType]">{{ message }}</div>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import FourNumber from '@/components/public/attendance/FourNumber.vue';
import { checkinAttendance } from '@/api/attendance';

const emit = defineEmits(['checked']);
const code = ref('');
const loading = ref(false);
const message = ref('');
const messageType = ref('info');

const parseErrorMessage = (err) => {
  const status = err?.response?.status;
  if (status === 400) return '签到码格式错误，请输入 4 位数字';
  if (status === 401) return '请先登录后再签到';
  if (status === 403) return '只有学生可以签到';
  if (status === 404) return '未找到有效的签到任务或已过期';
  if (status === 409) return '签到已结束或已签到过';
  return err?.response?.data?.message || err?.message || '签到失败';
};

const handleSubmit = async () => {
  if (!/^\d{4}$/.test(code.value)) {
    ElMessage.warning('请输入 4 位数字签到码');
    message.value = '请输入 4 位数字签到码';
    messageType.value = 'warn';
    return;
  }
  loading.value = true;
  message.value = '';
  try {
    const res = await checkinAttendance({ code: code.value, courseId: 1 });
    const data = res?.data || res || {};
    const msg = data.message || '签到成功';
    message.value = msg;
    messageType.value = 'success';
    ElMessage.success(msg);
    emit('checked');
  } catch (err) {
    const msg = parseErrorMessage(err);
    message.value = msg;
    messageType.value = 'error';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.box {
  height: auto;
  padding: 20px;
  background-color: #1f2937;
  border-radius: 25px;
  color: #fff;
}
.header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.form {
  background: rgba(255, 255, 255, 0.04);
  padding: 12px;
  border-radius: 25px;
}
.submit-btn {
  width: 100%;
}
.hint {
  margin-top: 8px;
  font-size: 12px;
  color: #d1d5db;
}
.result {
  margin-top: 8px;
  font-size: 13px;
}
.result.success {
  color: #67c23a;
}
.result.error {
  color: #f56c6c;
}
.result.warn {
  color: #e6a23c;
}
</style>