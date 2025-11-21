import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/main', component: Home }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authed = !!sessionStorage.getItem('spm-user')
  if (to.path === '/main' && !authed) {
    next('/login')
  } else if (to.path === '/login' && authed) {
    next('/main')
  } else {
    next()
  }
})

export default router