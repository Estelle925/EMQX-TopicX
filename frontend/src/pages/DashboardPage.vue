<template>
  <div class="dashboard-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <div class="logo" :class="{ 'logo-collapsed': isCollapsed }">
          <div class="logo-content">
            <img src="/logo.svg" alt="Logo" class="logo-icon" />
            <h2 v-if="!isCollapsed">EMQX Hub</h2>
          </div>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :class="{ 'menu-collapsed': isCollapsed }"
          :collapse="isCollapsed"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          
          <el-menu-item index="/systems">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
          
          <el-menu-item index="/topics">
            <el-icon><Document /></el-icon>
            <span>主题管理</span>
          </el-menu-item>
          
          <el-menu-item index="/groups">
            <el-icon><Folder /></el-icon>
            <span>业务管理</span>
          </el-menu-item>
          
          <el-menu-item index="/payload-templates">
            <el-icon><Document /></el-icon>
            <span>Payload模板</span>
          </el-menu-item>
        </el-menu>
        
        <!-- 折叠按钮移到底部 -->
        <div class="sidebar-footer">
          <button class="collapse-button" :class="{ 'button-collapsed': isCollapsed }" @click="toggleSidebar">
            <el-icon>
              <Expand v-if="!isCollapsed" />
              <Fold v-if="isCollapsed" />
            </el-icon>
          </button>
        </div>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item 
                v-if="route.path.startsWith('/topics/') && route.path !== '/topics'"
                :to="{ path: '/topics' }"
              >
                Topic总览
              </el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <div class="user-avatar">{{ authStore.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
                {{ authStore.username }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="userManagement">
                    <el-icon><User /></el-icon>
                    用户管理
                  </el-dropdown-item>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 用户管理弹窗 -->
    <UserManagement v-model="showUserManagement" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  Setting,
  Document,
  Folder,
  User,
  ArrowDown,
  Expand,
  Fold,
  SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import UserManagement from '../components/UserManagement.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 用户管理弹窗状态
const showUserManagement = ref(false)

const activeMenu = computed(() => route.path)

// 侧边栏宽度计算
const sidebarWidth = computed(() => isCollapsed.value ? '56px' : '200px')

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const currentPageTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/dashboard': '仪表板',
    '/systems': '系统管理',
    '/topics': 'Topic总览',
    '/groups': '业务管理',
    '/payload-templates': 'Payload模板'
  }
  
  // 处理动态路由
  if (route.path.startsWith('/topics/')) {
    return 'Topic详情'
  }
  
  return titleMap[route.path] || '未知页面'
})

const handleCommand = async (command: string) => {
  if (command === 'userManagement') {
    showUserManagement.value = true
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      await authStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (error) {
      // 用户取消操作
    }
  }
}

onMounted(() => {
  // 初始化认证状态
  authStore.initAuth()
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.sidebar {
  background: var(--bg-sidebar);
  color: var(--text-inverse);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
  transition: width var(--transition-normal);
  display: flex;
  flex-direction: column;
}

.sidebar-collapsed {
  width: 56px !important;
}

.sidebar-collapsed .logo h2 {
  font-size: 16px;
}

.logo-collapsed {
  font-size: 16px !important;
}

.sidebar-footer {
  margin-top: auto;
  padding: var(--spacing-md);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 折叠按钮样式 */
.collapse-button {
  width: 100%;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: var(--radius-md);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.collapse-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.collapse-button .el-icon {
  font-size: 18px;
}

.button-collapsed {
  width: 40px;
  height: 40px;
  margin: 0 auto;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.logo {
  padding: var(--spacing-xl);
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
  position: relative;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 2px;
  background: var(--primary-gradient);
}

.logo-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.logo h2 {
  color: var(--text-inverse);
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  white-space: nowrap;
}

.logo-collapsed {
  text-align: center;
  padding: var(--spacing-md);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-collapsed .logo-content {
  flex-direction: column;
  gap: 0;
}

.logo-collapsed .logo-icon {
  width: 28px;
  height: 28px;
}

.sidebar-menu {
  border: none;
  background-color: transparent;
  padding: var(--spacing-md) 0;
  transition: all var(--transition-normal);
  flex: 1;
}



.sidebar-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  margin: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.sidebar-menu .el-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 0;
  height: 100%;
  background: var(--primary-gradient);
  transition: width var(--transition-normal);
  z-index: 0;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: var(--text-inverse);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: var(--text-inverse);
}

.sidebar-menu .el-menu-item.is-active::before {
  width: 4px;
}

/* 菜单图标样式 */
.sidebar-menu .el-menu-item .el-icon {
  margin-right: var(--spacing-sm);
  font-size: 18px;
  transition: all var(--transition-normal);
}

.menu-collapsed .el-menu-item {
  text-align: center;
  margin: var(--spacing-xs) var(--spacing-xs);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md) var(--spacing-xs);
}

.menu-collapsed .el-menu-item .el-icon {
  margin-right: 0 !important;
  font-size: 20px;
}

.menu-collapsed .el-menu-item span {
  display: none;
}



.header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-xl);
  backdrop-filter: blur(10px);
  position: relative;
  height: 64px;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--primary-gradient);
  opacity: 0.6;
}

.header-left {
  flex: 1;
  display: flex;
  align-items: center;
}

.header-left h3 {
  color: var(--text-primary);
  margin: 0;
  font-weight: 600;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-lg);
  background: var(--bg-tertiary);
  transition: all var(--transition-normal);
  font-weight: 500;
}

.user-info:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.user-info .el-icon {
  margin: 0 var(--spacing-xs);
  font-size: 16px;
  transition: all var(--transition-normal);
}

.user-info span {
  opacity: 0;
}



/* 用户头像样式 */
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  margin-right: var(--spacing-sm);
}

.main-content {
  background: var(--bg-secondary);
  padding: var(--spacing-xl);
  min-height: calc(100vh - 64px);
  position: relative;
}

.main-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 10% 20%, rgba(0, 212, 170, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 90% 80%, rgba(99, 102, 241, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

/* 退出按钮样式 */
.logout-btn {
  background: var(--error-color);
  border: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  font-weight: 500;
}

.logout-btn:hover {
  background: #dc2626;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100% !important;
    height: auto;
    order: 2;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    box-shadow: var(--shadow-xl);
  }

  .collapse-button {
    display: none;
  }

  .logo {
    display: none;
  }

  .sidebar-menu {
    display: flex;
    justify-content: space-around;
    padding: var(--spacing-sm) 0;
    background: var(--bg-sidebar);
  }

  .sidebar-menu .el-menu-item {
    flex: 1;
    text-align: center;
    padding: var(--spacing-sm);
    margin: 0;
    border-radius: 0;
    flex-direction: column;
    height: auto;
    line-height: 1.2;
  }

  .sidebar-menu .el-menu-item .el-icon {
    margin-right: 0;
    margin-bottom: var(--spacing-xs);
    font-size: 20px;
  }

  .sidebar-menu .el-menu-item span {
    font-size: 12px;
  }

  .main-layout {
    margin-bottom: 80px;
  }

  .header {
    padding: 0 var(--spacing-md);
    height: 56px;
  }

  .header-left h3 {
    font-size: 16px;
  }

  .user-info {
    padding: var(--spacing-xs) var(--spacing-sm);
    font-size: 12px;
  }

  .user-avatar {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .main-content {
    padding: var(--spacing-md);
    min-height: calc(100vh - 136px);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 var(--spacing-sm);
    height: 48px;
  }

  .header-left h3 {
    font-size: 14px;
  }

  .user-info {
    padding: var(--spacing-xs);
    font-size: 11px;
  }

  .user-info span {
    display: none;
  }

  .user-avatar {
    width: 24px;
    height: 24px;
    font-size: 10px;
    margin-right: 0;
  }

  .main-content {
    padding: var(--spacing-sm);
  }

  .sidebar-menu .el-menu-item span {
    font-size: 10px;
  }

  .sidebar-menu .el-menu-item .el-icon {
    font-size: 18px;
  }
}
</style>