import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)

  const setUser = (payload) => {
    if (payload) {
      // 使用localStorage替代sessionStorage，增加登录时间戳
      const userData = {
        ...payload,
        loginTime: Date.now()
      }
      currentUser.value = userData
      localStorage.setItem('spm-user', JSON.stringify(userData))
    } else {
      currentUser.value = null
      localStorage.removeItem('spm-user')
    }
  }

  const hydrateUserFromCache = () => {
    if (!currentUser.value) {
      const cached = localStorage.getItem('spm-user')
      if (cached) {
        const userData = JSON.parse(cached)
        // 检查是否超过7天（可调整）
        const sevenDays = 7 * 24 * 60 * 60 * 1000
        if (userData.loginTime && Date.now() - userData.loginTime < sevenDays) {
          currentUser.value = userData
        } else {
          // 超时则清除
          localStorage.removeItem('spm-user')
        }
      }
    }
    return currentUser.value
  }

  const clearUser = () => {
    currentUser.value = null
    localStorage.removeItem('spm-user')
  }

  return {
    currentUser,
    setUser,
    hydrateUserFromCache,
    clearUser
  }
})