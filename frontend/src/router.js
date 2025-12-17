import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import Discussion from '@/views/Discussion.vue'
import DiscussionDetail from '@/views/public/discussion/DiscussionDetail.vue'
import JoinCourse from '@/views/student/JoinCourse.vue'
import { useUserStore } from '@/stores/useUserStore'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/main', component: Home },
  { path: '/join', component: JoinCourse },
  { path: '/discussion', component: Discussion },
  { path: '/discussion/:id', component: DiscussionDetail, name: 'discussion-detail' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.hydrateUserFromCache()
  const authed = !!userStore.currentUser

  if (!authed && to.path !== '/login') {
    return next('/login')
  }

  // 已登录但未加入课程，且目标不是 join，则跳转 join
  if (authed && !userStore.currentCourse && to.path !== '/join') {
    return next('/join')
  }

  if (authed && to.path === '/login') {
    return next('/main')
  }

  next()
})

export default router