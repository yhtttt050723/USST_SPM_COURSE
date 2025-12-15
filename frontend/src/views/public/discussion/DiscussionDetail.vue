<template>
  <div class="discussion-detail-page">
      <div class="header">
        <el-avatar class="avatar" :size="45" :src="'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <div class="username">用户名</div>
    </div>
    <div class="content-box">
      <div class="info">
        <div class="title">帖子标题</div>
        <div class="time">发布时间：</div>
      </div>
      <div class="content">内容</div>
      <div class="send-replay-box">
        <div class="send-replay-content">
          <el-input 
          v-model="textarea"
          maxlength="300"
          type="textarea"
          show-word-limit
          placeholder="评论帖子..."
          />
        </div>
        <div class="button"> <el-button type="info" round>评论</el-button> </div>
      </div>
    </div>
    <div class="replay">
      <ReplayBox />
      <ReplayBox />
      <ReplayBox />
      <ReplayBox />
      <ReplayBox />
      <ReplayBox />
      <ReplayBox />
    </div>
  </div>
</template>

<script setup>
import ReplayBox from '@/components/public/discussion/ReplayBox.vue';
import { ref } from 'vue'

const textarea = ref('')
</script>

<style scoped>
.header {
  width: 300px;
  height: 60px;
  background-color: pink;
}
.header .avatar {
  float: left;
  margin: 7.5px 2px 7.5px 12px;
}
.header .username {
  float: left;
  margin-left: 10px;
  line-height: 60px;
  color: #64748b;
}
.content-box {
  min-height: 200px;
  width: 100%;
  background-color: white;
  border-radius: 20px;
  margin: 15px 0px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  box-sizing: border-box; 
}
.title {
  background-color: rgb(245, 245, 245);
  min-width: 30%;
  max-width: 50%;
  height: 40px;
  border-radius: 25px;
  padding: 0px 20px;
  margin: 10px 0px;
  line-height: 40px;
  color: #64748b;
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}

.content {
  width: 100%;
  min-height: 60px;
  padding: 15px 20px;
  margin: 10px 0px;
  border-radius: 25px;
  font-size: 14px;
  color: #64748b;
  background-color: rgb(247, 247, 247);
  box-sizing: border-box; 
}
.time {
  right: 20px;
  font-size: 12px;
  color: #94a3b8;
  margin: 10px 20px;
}
.send-replay-box {
  margin-top: 20px;
  min-height: 100px;
  background-color: rgb(239, 238, 238);
  border-radius: 20px;
  padding: 10px;
}
.send-replay-content {
  width: 100%;
  min-height: 40px;
  max-height: 180px;
  padding: 15px 20px;
  border-radius: 20px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #64748b;
  background-color: white;
  line-height: 1.5;
  box-sizing: border-box; 
}
.button {
  display: flex;
}
.button .el-button {
  margin-left: auto;
}
</style>
