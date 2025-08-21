import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../pages/LoginPage.vue'),
      meta: {
        requiresAuth: false
      }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../pages/DashboardPage.vue'),
      meta: {
        requiresAuth: false
      },
      children: [
        {
          path: '',
          name: 'DashboardHome',
          component: () => import('../views/DashboardHome.vue')
        },
        {
          path: '/systems',
          name: 'SystemManagement',
          component: () => import('../views/SystemManagement.vue')
        },
        {
          path: '/topics',
          name: 'TopicOverview',
          component: () => import('../views/TopicOverview.vue')
        },
        {
          path: '/groups',
          name: 'GroupManagement',
          component: () => import('../views/GroupManagement.vue')
        },
        {
          path: '/topics/:id',
          name: 'TopicDetail',
          component: () => import('../views/TopicDetail.vue'),
          props: true
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      redirect: '/dashboard'
    }
  ]
})

// 路由守卫 - 简化版本，无需认证检查
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 初始化认证状态（默认已登录）
  authStore.initAuth()
  
  // 直接允许访问所有页面
  next()
})

export default router