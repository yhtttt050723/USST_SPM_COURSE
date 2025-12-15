import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        // 学生端
        path: '/',
        redirect: '/home',
        component: () => import('@/views/layout/layoutOfStudents.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' },
        children: [
            {
                path: '/home',
                name: 'StudentHome',
                component: () => import('@/views/student/home/Home.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/homework',
                name: 'StudentHomework',
                component: () => import('@/views/student/homework/Homework.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/homework/detail',
                name: 'StudentHomeworkDetail',
                component: () => import('@/views/student/homework/HomeworkDetail.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/attendance',
                name: 'StudentAttendance',
                component: () => import('@/views/student/attendance/Attendance.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/attendance/leave',
                name: 'StudentLeave',
                component: () => import('@/views/student/attendance/Leave.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/discussion',
                name: 'StudentDiscussion',
                component: () => import('@/views/public/discussion/Discussion.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/discussion/detail',
                name: 'StudentDiscussionDetail',
                component: () => import('@/views/public/discussion/DiscussionDetail.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: '/profile',
                name: 'StudentProfile',
                component: () => import('@/views/public/profile/Profile.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            }
        ]
    },
    {
        // 教师端
        path: '/teacher',
        redirect: '/teacher/course',
        component: () => import('@/views/layout/layoutOfTeachers.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' },
        children: [
            {
                path: '/teacher/course',
                name: 'TeacherCourse',
                component: () => import('@/views/teacher/course/Course.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/attendance',
                name: 'TeacherAttendance',
                component: () => import('@/views/teacher/attendance/Attendance.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/attendance/detail',
                name: 'TeacherAttendanceDetail',
                component: () => import('@/views/teacher/attendance/AttendanceDetail.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/homework',
                name: 'TeacherHomework',
                component: () => import('@/views/teacher/homework/Homework.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {   
                path: '/teacher/homework/detail',
                name: 'TeacherHomeworkDetail',
                component: () => import('@/views/teacher/homework/HomeworkDetail.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/homework/create',
                name: 'TeacherCreateHomework',
                component: () => import('@/views/teacher/homework/CreatedNewAssignment.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/homework/studentHomework/:submissionId',
                name: 'TeacherStudentHomework',
                component: () => import('@/views/teacher/homework/StudentHomework.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/discussion',
                name: 'TeacherDiscussion',
                component: () => import('@/views/public/discussion/Discussion.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/discussion/detail',
                name: 'TeacherDiscussionDetail',
                component: () => import('@/views/public/discussion/DiscussionDetail.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            },
            {
                path: '/teacher/profile',
                name: 'TeacherProfile',
                component: () => import('@/views/public/profile/Profile.vue'),
                meta: { requiresAuth: true, role: 'TEACHER' }
            }
        ] 
    },
    {
        // 404 页面
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        redirect: '/login'
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 直接获取当前用户或从缓存恢复
  const user = userStore.currentUser || userStore.hydrateUserFromCache()
  const userRole = user?.role || ''
  
  if (to.path === '/login') {
    if (user) {
      if (userRole === 'STUDENT') {
        return next('/home')
      } else if (userRole === 'TEACHER') {
        return next('/teacher/course')
      }
    }
    return next()
  }
  
  const requiresAuth = to.meta.requiresAuth
  
  if (requiresAuth) {
    if (!user) {
      ElMessage.warning('请先登录')
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
    
    const requiredRole = to.meta.role
    if (requiredRole && userRole !== requiredRole) {
      if (userRole === 'STUDENT') {
        return next('/home')
      } else if (userRole === 'TEACHER') {
        return next('/teacher/course')
      } else {
        return next('/login')
      }
    }
  }
  
  next()
})

export default router