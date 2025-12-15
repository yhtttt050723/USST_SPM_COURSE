<template>
  <div class="box">
    <span class="number"><FourNumber /></span>
    <span class="button" :class="{ active: ischoice }" @click="handleClick">签 到</span>
  </div>
</template>

<script setup>
import FourNumber from '@/components/public/attendance/FourNumber.vue';
import { ref } from 'vue';

const ischoice = ref(false);
const handleClick = () => {
    ischoice.value = !ischoice.value;
    // 添加签到逻辑
};
</script>

<style scoped>
.box {
    height: 250px;
    background-color: rgb(31, 41, 55);
    border-radius: 25px;
}
.number {
    display: inline-block;
    margin: 20px;
    width: 220px;
}
.button {
    display: inline-block;
    margin: 20px;
    width: 220px;
    height: 60px;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 25px;
    text-align: center;
    font-size: 30px;
    line-height: 60px;
    color: white;
}
.button.active {
    background-color: rgb(249, 115, 22);
    cursor: pointer;
}
</style>