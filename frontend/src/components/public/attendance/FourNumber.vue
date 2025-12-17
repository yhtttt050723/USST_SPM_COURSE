<template>
  <div class="number-box">
    <div class="outbox" v-for="(digit, idx) in digits" :key="idx">
      <div class="digit">{{ digit || '-' }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  code: {
    type: String,
    default: ''
  }
})

const digits = computed(() => {
  const chars = (props.code || '').slice(0, 4).split('')
  while (chars.length < 4) chars.push('')
  return chars
})
</script>

<style scoped>
.number-box {
    height: 100px;
    width: 250px;
    border-radius: 25px;
}
.outbox {
    display: inline-block;
    width: 48px;
    height: 85px;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    margin: 0px 7px;
}
.digit {
  display: inline-block;
  font-size: 48px;
  color: white;
  line-height: 85px;
  width: 100%;
  text-align: center;
}
</style>