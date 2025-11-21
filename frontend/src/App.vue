<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const { hydrateUserFromCache } = useUserStore()

onMounted(() => {
  hydrateUserFromCache()
})
</script>

<template>
  <RouterView />
</template>

<style scoped>
:global(body) {
  margin: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: #f5f6fb;
  min-height: 100vh;
}
</style>