<template>
  <div class="box">
    <el-input v-model="input" placeholder="" />
  </div>
</template>

<script setup>
import { ref } from 'vue';

const input = ref('');
</script>

<style scoped>
.box {
  width: 240px;
  padding: 5px;
  background-color: white;
  border-radius: 8px;
}
</style>