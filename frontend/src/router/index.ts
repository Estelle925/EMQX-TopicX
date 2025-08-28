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
        requiresAuth: true
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
          path: '/payload-templates',
          name: 'PayloadTemplate',
          component: () => import('../views/PayloadTemplate.vue')
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
      path: '/public/topic/:id/doc',
      name: 'PublicTopicDoc',
      component: () => import('../pages/PublicTopicDoc.vue'),
      meta: {
        requiresAuth: false
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../pages/NotFoundPage.vue'),
      meta: {
        requiresAuth: false
      }
    }
  ]
})

// 路由守卫 - 认证检查
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 初始化认证状态
  authStore.initAuth()
  
  // 检查路由是否需要认证（默认需要认证，除非明确设置为false）
  const requiresAuth = to.matched.every(record => record.meta.requiresAuth !== false)
  
  if (requiresAuth && !authStore.isLoggedIn) {
    // 需要认证但未登录，重定向到登录页
    next('/login')
  } else if (to.path === '/login' && authStore.isLoggedIn) {
    // 已登录用户访问登录页，重定向到仪表板
    next('/dashboard')
  } else {
    // 允许访问
    next()
  }
})

export default router