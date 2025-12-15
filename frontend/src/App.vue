<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const userStore = useUserStore()
const { hydrateUserFromCache } = userStore

onMounted(() => {
  hydrateUserFromCache()
  if (userStore.token) {
    userStore.fetchUserInfo().catch(error => {
      console.log('自动登录失败:', error)
    })
  }
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