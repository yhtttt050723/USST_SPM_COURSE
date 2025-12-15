<template>
  <el-row class="box">
    <el-col class="time-box" :span="16">
        <div class="time">出勤时间</div>
    </el-col>
    <el-col class="status-box" :span="8">
        <div class="status">出勤状态</div>
    </el-col>

  </el-row>
</template>

<script setup>

</script>

<style scoped>
.box {
    width: 100%;
    min-height: 40px;
    background-color: white;
    margin-top: 12px;
    border-top-left-radius: 25px;
    border-top-right-radius: 25px;
    border-bottom: 1px solid #b8bdc4;
    cursor: pointer;
    transition: all 0.3s ease;
}

.time-box {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 20px;
}

.time {
    font-size: 14px;
    color: #64748b;
    
}
.status {
    font-size: 14px;
    color: #64748b;
}
.status-box {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 20px;
}
</style>