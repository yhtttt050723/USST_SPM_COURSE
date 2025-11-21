import { ref } from 'vue'

const currentUser = ref(null)

export const useUserStore = () => {
  const setUser = (payload) => {
    currentUser.value = payload
    if (payload) {
      sessionStorage.setItem('spm-user', JSON.stringify(payload))
    } else {
      sessionStorage.removeItem('spm-user')
    }
  }

  const hydrateUserFromCache = () => {
    if (!currentUser.value) {
      const cached = sessionStorage.getItem('spm-user')
      if (cached) {
        currentUser.value = JSON.parse(cached)
      }
    }
    return currentUser.value
  }

  return {
    currentUser,
    setUser,
    hydrateUserFromCache
  }
}

