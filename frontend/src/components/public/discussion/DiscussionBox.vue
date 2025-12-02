<template>
  <div class="discussion-box">
    <div class="header">
      <div class="title">帖子标题</div>
      <div class="user-info">
        <div class="username">发布者</div>
        <div class="time">发布时间</div>
      </div>
    </div>
    <div class="content">帖子内容</div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.discussion-box {
    height: 150px;
    background-color: white;
    border-radius: 20px;
    padding: 20px;
    margin-bottom: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 15px;
}

.title {
    background-color: rgb(245, 245, 245);
    min-width: 30%;
    max-width: 50%;
    height: 40px;
    border-radius: 25px;
    padding: 0px 20px;
    line-height: 40px;
    color: #64748b;
    font-size: 18px;
    font-weight: bold;
    flex-shrink: 0;
}

.user-info {
    text-align: right;
    flex-shrink: 0;
}

.username {
    font-size: 14px;
    color: #64748b;
    margin-bottom: 4px;
    font-weight: 500;
}

.time {
    font-size: 12px;
    color: #94a3b8;
}

.content {
    margin-top: 10px;
    width: 100%;
    min-height: 60px;
    padding: 15px 20px;
    border-radius: 25px;
    font-size: 15px;
    color: #64748b;
    background-color: rgb(247, 247, 247);
    line-height: 1.5;
    box-sizing: border-box; 
}
</style>