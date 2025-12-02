<template>
  <!-- 评论区回复盒子 -->
    <div class="replay-box">
        <div class="content">回复内容</div>
        <div class="user-info">
            <div class="username">发布者</div>
            <div class="time">发布时间</div>
        </div>
    </div>
    
</template>

<script setup>

</script>

<style scoped>
.replay-box {
    min-height: 60px;
    background-color: white;
    border-radius: 20px;
    padding: 20px;
    margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
    width: 100%;
    min-height: 40px;
    padding: 15px 20px;
    border-radius: 25px;
    margin-bottom: 10px;
    font-size: 14px;
    color: #64748b;
    background-color: rgb(247, 247, 247);
    line-height: 1.5;
    box-sizing: border-box; 
}
</style>