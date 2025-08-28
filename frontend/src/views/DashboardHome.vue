<template>
  <div class="dashboard-home">
    <!-- 数据统计卡片区域 -->
    <div class="stats-section">
      <h2 class="section-title">
        <el-icon class="title-icon"><TrendCharts /></el-icon>
        数据统计
      </h2>
      <el-row :gutter="24" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statsItems" :key="item.key">
          <div class="stat-card" :class="`stat-card-${index + 1}`" @click="handleStatClick(item.key)">
            <div class="stat-card-inner">
              <div class="stat-icon-wrapper">
                <div class="stat-icon" :class="item.iconClass">
                  <el-icon><component :is="item.icon" /></el-icon>
                </div>

              </div>
              <div class="stat-info">
                <div class="stat-number" :data-target="item.value">{{ animatedStats[item.key] }}</div>
                <div class="stat-label">{{ item.label }}</div>
                <div class="stat-description">{{ item.description }}</div>
              </div>
            </div>
            <div class="stat-card-glow"></div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 快速操作和系统状态区域 -->
    <el-row :gutter="24" class="content-row">
      <!-- 快速操作 -->
      <el-col :xs="24" :lg="12">
        <div class="action-section">
          <h3 class="section-subtitle">
            <el-icon class="subtitle-icon"><Lightning /></el-icon>
            快速操作
          </h3>
          <div class="quick-actions">
            <div class="action-card" @click="$router.push('/systems')">
              <div class="action-icon primary">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">添加EMQX系统</div>
                <div class="action-desc">连接新的EMQX服务器</div>
              </div>
              <div class="action-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
            
            <div class="action-card" @click="$router.push('/groups')">
              <div class="action-icon success">
                <el-icon><FolderAdd /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">创建Topic业务</div>
                <div class="action-desc">组织管理Topic结构</div>
              </div>
              <div class="action-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
            
            <div class="action-card" @click="$router.push('/topics')">
              <div class="action-icon info">
                <el-icon><Refresh /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">同步Topic数据</div>
                <div class="action-desc">获取最新Topic信息</div>
              </div>
              <div class="action-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      
      <!-- 系统状态 -->
      <el-col :xs="24" :lg="12">
        <div class="status-section">
          <h3 class="section-subtitle">
            <el-icon class="subtitle-icon"><Monitor /></el-icon>
            系统状态
            <el-button size="small" text @click="refreshSystemStatus" class="refresh-btn">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </h3>
          
          <div class="system-status">
            <div class="status-item" v-for="system in systemStatus" :key="system.id">
              <div class="status-avatar" :class="system.status">
                <el-icon><Setting /></el-icon>
              </div>
              <div class="status-info">
                <div class="system-name">{{ system.name }}</div>
                <a :href="system.url" target="_blank" class="system-url">{{ system.url }}</a>
              </div>
              <div class="status-metrics">
                <span class="metric">主题数量: {{ system.topicCount || 0 }}</span>
              </div>
              <div class="status-indicator">
                <div class="status-dot" :class="system.status"></div>
                <span class="status-text" :class="system.status">
                  {{ system.status === 'online' ? '在线' : '离线' }}
                </span>
              </div>
            </div>
            
            <div v-if="systemStatus.length === 0" class="empty-status">
              <div class="empty-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="empty-text">暂无EMQX系统</div>
              <el-button type="primary" size="small" @click="$router.push('/systems')">
                立即添加
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cardIconColors } from './colors.config.js'
import {
  Setting,
  Document,
  Folder,
  CollectionTag,
  Plus,
  FolderAdd,
  Refresh,
  TrendCharts,
  Lightning,
  Monitor,
  ArrowRight,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()

interface Stats {
  systemCount: number
  topicCount: number
  groupCount: number
  tagCount: number
}

interface SystemStatus {
  id: number
  name: string
  url: string
  status: 'online' | 'offline'
  topicCount?: number
}

interface StatItem {
  key: keyof Stats
  label: string
  description: string
  icon: any
  iconClass: string
  value: number
  trend: 'up' | 'down'
  trendValue: number
}

const stats = ref<Stats>({
  systemCount: 0,
  topicCount: 0,
  groupCount: 0,
  tagCount: 0
})

const animatedStats = ref<Stats>({
  systemCount: 0,
  topicCount: 0,
  groupCount: 0,
  tagCount: 0
})

const systemStatus = ref<SystemStatus[]>([])

const statsItems = computed<StatItem[]>(() => [
  {
    key: 'systemCount',
    label: 'EMQX系统',
    description: '已连接的EMQX服务器数量',
    icon: Setting,
    iconClass: 'system',
    value: stats.value.systemCount,
    trend: 'up',
    trendValue: 12
  },
  {
    key: 'topicCount',
    label: 'Topic总数',
    description: '所有系统中的Topic数量',
    icon: Document,
    iconClass: 'topic',
    value: stats.value.topicCount,
    trend: 'up',
    trendValue: 8
  },
  {
    key: 'groupCount',
    label: '业务数量',
    description: 'Topic业务管理数量',
    icon: Folder,
    iconClass: 'group',
    value: stats.value.groupCount,
    trend: 'up',
    trendValue: 5
  },
  {
    key: 'tagCount',
    label: '标签数量',
    description: '用于分类的标签总数',
    icon: CollectionTag,
    iconClass: 'tag',
    value: stats.value.tagCount,
    trend: 'down',
    trendValue: 2
  }
])

// 数字动画函数
const animateNumber = (target: number, key: keyof Stats) => {
  const start = animatedStats.value[key]
  const duration = 1000
  const startTime = Date.now()
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    
    animatedStats.value[key] = Math.floor(start + (target - start) * easeOutQuart)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  requestAnimationFrame(animate)
}

const handleStatClick = (key: keyof Stats) => {
  switch (key) {
    case 'systemCount':
      router.push('/systems')
      break
    case 'topicCount':
      router.push('/topics')
      break
    case 'groupCount':
      router.push('/groups')
      break
    case 'tagCount':
      router.push('/topics')
      break
  }
}

const refreshSystemStatus = async () => {
  try {
    // 调用后端刷新系统状态接口
    const response = await fetch('/api/dashboard/systems/refresh')
    const result = await response.json()
    
    if (result.code === 200) {
      systemStatus.value = result.data
    }
  } catch (error) {
    console.error('刷新系统状态失败:', error)
    // 如果刷新失败，重新加载所有数据
    await loadDashboardData()
  }
}

const loadDashboardData = async () => {
  try {
    // 获取统计数据
    const statsResponse = await fetch('/api/dashboard/stats')
    const statsResult = await statsResponse.json()
    
    if (statsResult.code === 200) {
      const newStats = statsResult.data
      stats.value = newStats
      
      // 启动数字动画
      Object.keys(newStats).forEach(key => {
        animateNumber(newStats[key as keyof Stats], key as keyof Stats)
      })
    }
    
    // 获取系统状态
    const statusResponse = await fetch('/api/dashboard/systems/status')
    const statusResult = await statusResponse.json()
    
    if (statusResult.code === 200) {
      systemStatus.value = statusResult.data
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载仪表板数据失败')
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-home {
  padding: 0;
  background: transparent;
  min-height: 100vh;
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin-bottom: 24px;
}

.title-icon {
  margin-right: 12px;
  font-size: 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stats-row {
  margin: 0 -12px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 24px;
  margin: 0 12px 24px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid #e0e0e0;
}





.stat-card-inner {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
}

.stat-icon-wrapper {
  position: relative;
  margin-right: 20px;
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-icon.system {
  background: v-bind('cardIconColors.system.background');
  box-shadow: v-bind('cardIconColors.system.shadow');
}

.stat-icon.topic {
  background: v-bind('cardIconColors.topic.background');
  box-shadow: v-bind('cardIconColors.topic.shadow');
}

.stat-icon.group {
  background: v-bind('cardIconColors.group.background');
  box-shadow: v-bind('cardIconColors.group.shadow');
}

.stat-icon.tag {
  background: v-bind('cardIconColors.tag.background');
  box-shadow: v-bind('cardIconColors.tag.shadow');
}

.stat-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
}

.stat-trend {
  position: absolute;
  top: -8px;
  right: -8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
  backdrop-filter: blur(10px);
}

.stat-trend.up {
  color: #10b981;
}

.stat-trend.down {
  color: #ef4444;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #1f2937, #374151);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-description {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.stat-card-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}



/* 内容区域 */
.content-row {
  margin-top: 32px;
}

.section-subtitle {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  position: relative;
}

.subtitle-icon {
  margin-right: 8px;
  font-size: 20px;
  color: var(--primary-color);
}

.refresh-btn {
  margin-left: auto;
  color: var(--text-secondary);
}

/* 快速操作区域 */
.action-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid #e0e0e0;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}



.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.action-icon.primary {
  background: linear-gradient(135deg, #10b981, #059669);
}

.action-icon.success {
  background: linear-gradient(135deg, #34d399, #10b981);
}

.action-icon.info {
  background: linear-gradient(135deg, #6ee7b7, #34d399);
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.action-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.action-arrow {
  color: var(--text-secondary);
}



/* 系统状态区域 */
.status-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid #e0e0e0;
}

.system-status {
  max-height: 320px;
  overflow-y: auto;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.status-item:last-child {
  border-bottom: none;
}



.status-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
  position: relative;
}

.status-avatar.online {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.status-avatar.offline {
  background: linear-gradient(135deg, #f87171, #ef4444);
}

.status-avatar::after {
  content: '';
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-avatar.online::after {
  background: #22c55e;
}

.status-avatar.offline::after {
  background: #f87171;
}

.status-info {
  flex: 1;
}

.system-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.system-url {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s ease;
}

.system-url:hover {
  color: #22c55e;
  text-decoration: underline;
}

.status-metrics {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  margin: 0 16px;
}

.metric {
  font-size: 11px;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 8px;
  border-radius: 6px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-dot.online {
  background: #22c55e;
}

.status-dot.offline {
  background: #f87171;
}

.status-text {
  font-size: 12px;
  font-weight: 600;
}

.status-text.online {
  color: #22c55e;
}

.status-text.offline {
  color: #f87171;
}

.empty-status {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

/* 动画效果 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card {
  animation: slideInUp 0.6s ease forwards;
}

.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }
.stat-card:nth-child(4) { animation-delay: 0.4s; }

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-home {
    padding: 16px;
  }
  
  .section-title {
    font-size: 20px;
    margin-bottom: 16px;
  }
  
  .stat-card {
    margin: 0 0 16px;
    padding: 20px;
  }
  
  .stat-card-inner {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon-wrapper {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  .content-row {
    margin-top: 24px;
  }
  
  .action-section,
  .status-section {
    margin-bottom: 20px;
    padding: 20px;
  }
  
  .action-card {
    padding: 12px;
  }
  
  .action-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .status-avatar {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .stats-row {
    margin: 0;
  }
  
  .stat-card {
    margin: 0 0 12px;
    padding: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .system-metrics {
    flex-direction: column;
    gap: 4px;
  }
}
</style>