<template>
  <div class="system-management">
    <div class="page-header">
      <div class="header-left">
        <h2>EMQX系统管理</h2>
        <p class="page-description">管理和配置多个EMQX服务实例</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshSystems" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加系统
        </el-button>
      </div>
    </div>
    
    <!-- 系统统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card online" shadow="never">
            <div class="stat-content">
              <div class="stat-icon online">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ onlineCount }}</div>
                <div class="stat-label">在线系统</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card offline" shadow="never">
            <div class="stat-content">
              <div class="stat-icon offline">
                <el-icon><Close /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ offlineCount }}</div>
                <div class="stat-label">离线系统</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card total" shadow="never">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ totalSystems }}</div>
                <div class="stat-label">总系统数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card topics" shadow="never">
            <div class="stat-content">
              <div class="stat-icon topics">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ totalTopics }}</div>
                <div class="stat-label">Topic总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 系统列表 -->
    <el-card class="systems-table" shadow="never">
      <template #header>
        <div class="card-header">
          <span>系统列表</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索系统名称或地址"
            style="width: 250px"
            clearable
            @input="updateFilteredSystems"
            @clear="updateFilteredSystems"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
      <el-table :data="filteredSystems" v-loading="loading" class="systems-table">
        <el-table-column prop="name" label="系统名称" min-width="200">
          <template #default="{ row }">
            <div class="system-name">
              <div class="name">{{ row.name }}</div>
              <div class="description" v-if="row.description">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="API地址" min-width="250">
          <template #default="{ row }">
            <el-link :href="row.url" target="_blank" type="primary">{{ row.url }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="connectionCount" label="连接数量">
          <template #default="{ row }">
            <span>{{ row.connectionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="topicCount" label="主题数量">
          <template #default="{ row }">
            <span>{{ row.topicCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="连接状态" min-width="120">
          <template #default="{ row }">
            <div class="status-indicator">
              <div :class="['status-dot', row.status]" />
              <span :class="['status-text', row.status]">
                {{ row.status === 'online' ? '在线' : '离线' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastCheck" label="最后检查" min-width="150" />
        <el-table-column prop="createdAt" label="创建时间" min-width="150" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="testConnection(row)" :loading="row.testing">
              <el-icon><Connection /></el-icon>
              测试连接
            </el-button>
            <el-button size="small" type="primary" @click="editSystem(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteSystem(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑系统对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingSystem ? '编辑系统' : '添加系统'"
      width="500px"
    >
      <el-form
        ref="systemFormRef"
        :model="systemForm"
        :rules="systemRules"
        label-width="100px"
      >
        <el-form-item label="系统名称" prop="name">
          <el-input v-model="systemForm.name" placeholder="请输入系统名称" />
        </el-form-item>
        
        <el-form-item label="API地址" prop="url">
          <el-input v-model="systemForm.url" placeholder="http://localhost:18083" />
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="systemForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="systemForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="systemForm.description"
            type="textarea"
            placeholder="请输入系统描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSystem" :loading="saving">
          {{ editingSystem ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { cardIconColors } from './colors.config.js'
import { 
  Plus, 
  Refresh, 
  Connection, 
  Close, 
  Monitor, 
  Document, 
  Search, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue'
import { SystemAPI, type SystemManagementDTO, type SystemCreateRequest, type SystemUpdateRequest, type SystemStatsDTO } from '../api/system'

interface EmqxSystem extends SystemManagementDTO {
  password?: string
  testing?: boolean
}

const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingSystem = ref<EmqxSystem | null>(null)
const systemFormRef = ref<FormInstance>()
const searchKeyword = ref('')

const systems = ref<EmqxSystem[]>([])
const systemStats = ref<SystemStatsDTO>({
  totalSystems: 0,
  onlineCount: 0,
  offlineCount: 0,
  totalTopics: 0
})

// 计算属性
const onlineCount = computed(() => systemStats.value.onlineCount)
const offlineCount = computed(() => systemStats.value.offlineCount)
const totalSystems = computed(() => systemStats.value.totalSystems)
const totalTopics = computed(() => systemStats.value.totalTopics)

const filteredSystems = ref<EmqxSystem[]>([])

console.info('onlineCount', onlineCount.value)
// 监听搜索关键词变化
const searchSystems = async () => {
  loading.value = true
  try {
    const searchResults = await SystemAPI.searchSystems(searchKeyword.value)
    filteredSystems.value = searchResults.map(system => ({
      ...system,
      testing: false
    }))
  } catch (error) {
    console.error('搜索系统失败:', error)
    ElMessage.error('搜索系统失败')
  } finally {
    loading.value = false
  }
}

// 当搜索关键词为空时，显示所有系统
const updateFilteredSystems = () => {
  if (!searchKeyword.value.trim()) {
    filteredSystems.value = systems.value
  } else {
    searchSystems()
  }
}

const systemForm = reactive({
  name: '',
  url: '',
  username: '',
  password: '',
  description: ''
})

const systemRules: FormRules = {
  name: [
    { required: true, message: '请输入系统名称', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入API地址', trigger: 'blur' },
    { type: 'url', message: '请输入正确的URL格式', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const loadSystems = async () => {
  loading.value = true
  try {
    // 调用后端API获取系统列表
    const systemsData = await SystemAPI.getAllSystems()
    systems.value = systemsData.map(system => ({
      ...system,
      testing: false
    }))
    
    // 更新过滤后的系统列表
    updateFilteredSystems()
    
    // 获取系统统计信息
    const stats = await SystemAPI.getSystemStats()
    systemStats.value = stats
  } catch (error) {
    console.error('加载系统列表失败:', error)
    // ElMessage.error('加载系统列表失败')
  } finally {
    loading.value = false
  }
}

const refreshSystems = async () => {
  loading.value = true
  try {
    // 调用后端API刷新所有系统状态
    const refreshedSystems = await SystemAPI.refreshSystemStatus()
    systems.value = refreshedSystems.map(system => ({
      ...system,
      testing: false
    }))
    
    // 重新获取统计信息
    const stats = await SystemAPI.getSystemStats()
    systemStats.value = stats
    
    ElMessage.success('系统状态已刷新')
  } catch (error) {
    console.error('刷新系统状态失败:', error)
    ElMessage.error('刷新系统状态失败')
  } finally {
    loading.value = false
  }
}

const testConnection = async (system: EmqxSystem) => {
  if (!system.id) return
  
  system.testing = true
  try {
    // 调用后端API测试连接
    const result = await SystemAPI.testConnection(system.id)
    
    // 更新系统状态
    system.status = result.success ? 'online' : 'offline'
    system.lastCheck = result.testTime
    
    if (result.success) {
       ElMessage.success(`${system.name} 连接测试成功 (响应时间: ${result.responseTime}ms${result.version ? `, 版本: ${result.version}` : ''})`)
     } else {
       ElMessage.error(`${system.name} 连接测试失败: ${result.errorMessage || '连接失败'}`)
     }
    
    // 重新获取统计信息
    const stats = await SystemAPI.getSystemStats()
    systemStats.value = stats
  } catch (error) {
    console.error('连接测试异常:', error)
    ElMessage.error(`${system.name} 连接测试异常`)
  } finally {
    system.testing = false
  }
}

const editSystem = (system: EmqxSystem) => {
  editingSystem.value = system
  Object.assign(systemForm, {
    name: system.name,
    url: system.url,
    username: system.username,
    password: '',
    description: system.description || ''
  })
  showAddDialog.value = true
}

const deleteSystem = async (system: EmqxSystem) => {
  if (!system.id) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除系统 "${system.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端API删除系统
    await SystemAPI.deleteSystem(system.id)
    ElMessage.success('删除成功')
    
    // 重新加载系统列表
    await loadSystems()
  } catch (error: any) {
    // 如果不是用户取消操作，显示错误信息
    if (error !== 'cancel') {
      console.error('删除系统失败:', error)
      ElMessage.error('删除系统失败')
    }
  }
}

const saveSystem = async () => {
  if (!systemFormRef.value) return
  
  try {
    await systemFormRef.value.validate()
    saving.value = true
    
    if (editingSystem.value && editingSystem.value.id) {
      // 更新系统
      const updateData: SystemUpdateRequest = {
        name: systemForm.name,
        url: systemForm.url,
        username: systemForm.username,
        description: systemForm.description
      }
      
      // 只有在密码不为空时才包含密码字段
      if (systemForm.password) {
        updateData.password = systemForm.password
      }
      
      await SystemAPI.updateSystem(editingSystem.value.id, updateData)
      ElMessage.success('更新成功')
    } else {
      // 创建新系统
      const createData: SystemCreateRequest = {
        name: systemForm.name,
        url: systemForm.url,
        username: systemForm.username,
        password: systemForm.password,
        description: systemForm.description
      }
      
      await SystemAPI.createSystem(createData)
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    await loadSystems()
  } catch (error: any) {
    console.error('保存系统失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  editingSystem.value = null
  Object.assign(systemForm, {
    name: '',
    url: '',
    username: '',
    password: '',
    description: ''
  })
  systemFormRef.value?.resetFields()
}

onMounted(() => {
  loadSystems()
})
</script>

<style scoped>
/* CSS变量定义 */
/* 页面容器样式 */
.system-management {
  --primary-gradient: linear-gradient(135deg, #10b981, #059669);
  --success-gradient: linear-gradient(135deg, #22c55e, #16a34a);
  --danger-gradient: linear-gradient(135deg, #f87171, #ef4444);
  --warning-gradient: linear-gradient(135deg, #34d399, #10b981);
  --info-gradient: linear-gradient(135deg, #6ee7b7, #34d399);
  --text-primary: #1e293b;
  --text-secondary: #475569;
  --text-muted: #64748b;
  --border-color: #e0e0e0;
  --bg-glass: rgba(255, 255, 255, 0.95);
  --bg-hover: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  --shadow-sm: none;
  --shadow-md: none;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --spacing-xs: 8px;
  --spacing-sm: 12px;
  --spacing-md: 16px;
  --spacing-lg: 20px;
  --spacing-xl: 24px;
  --spacing-2xl: 32px;
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100vh;
  position: relative;
}

.system-management::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: -1;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-2xl);
  padding: var(--spacing-2xl);
  background: transparent;
  border-radius: var(--radius-lg);
  position: relative;
  overflow: hidden;
}



.header-left h2 {
  margin: 0 0 var(--spacing-xs) 0;
  color: #22c55e;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.page-description {
  margin: 0;
  color: var(--text-muted);
  font-size: 16px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: var(--spacing-2xl);
  background: transparent;
}

.stat-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: white;
  transition: none !important;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: none !important;
  box-shadow: none !important;
}





.stat-content {
  display: flex;
  align-items: center;
  padding: var(--spacing-xl);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--spacing-lg);
  font-size: 32px;
  color: #ffffff;
  position: relative;
  transition: all 0.3s ease;
}

.stat-icon.online {
  background: v-bind('cardIconColors.online.background');
  box-shadow: v-bind('cardIconColors.online.shadow');
}

.stat-icon.offline {
  background: v-bind('cardIconColors.offline.background');
  box-shadow: v-bind('cardIconColors.offline.shadow');
}

.stat-icon.total {
  background: v-bind('cardIconColors.total.background');
  box-shadow: v-bind('cardIconColors.total.shadow');
}

.stat-icon.topics {
  background: v-bind('cardIconColors.topic.background');
  box-shadow: v-bind('cardIconColors.topic.shadow');
}

.stat-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: var(--spacing-xs);
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 16px;
  color: var(--text-muted);
  line-height: 1;
  font-weight: 500;
}

/* 系统表格样式 */
.systems-table {
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  transition: none !important;
}

.systems-table:hover {
  box-shadow: none !important;
  transform: none !important;
}



.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  color: #1e293b;
  font-size: 18px;
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.system-name .name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  font-size: 16px;
}

.system-name .description {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 0;
  background: transparent;
  border: none;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
}

.status-dot::after {
  content: '';
  position: absolute;
  inset: -3px;
  border-radius: 50%;
  background: inherit;
  opacity: 0.2;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.2; }
  50% { transform: scale(1.2); opacity: 0.1; }
}

.status-dot.online {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: none;
}

.status-dot.offline {
  background: linear-gradient(135deg, #f87171, #ef4444);
  box-shadow: none;
}

.status-text.online {
  color: #22c55e;
  font-weight: 600;
  font-size: 14px;
}

.status-text.offline {
  color: #f87171;
  font-weight: 600;
  font-size: 14px;
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: 20px;
}

.form-row .el-form-item {
  flex: 1;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-table) {
  border-radius: 0;
  background: transparent;
  border: none;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  color: #1e293b;
  font-weight: 700;
  font-size: 16px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  position: relative;
}





:deep(.el-table td) {
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  padding: 20px 16px;
  color: #475569;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-table tr:hover > td) {
  background: transparent !important;
  transform: none !important;
}

:deep(.el-table tr:last-child td) {
  border-bottom: none;
}

:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #059669, #047857);
  transform: none !important;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f87171, #ef4444);
  border: none;
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  transform: none !important;
}

:deep(.el-button--small) {
  padding: 8px 16px;
  font-size: 13px;
}

:deep(.el-card__header) {
  padding: 0;
  border-bottom: none;
}

:deep(.el-card__body) {
  padding: 0;
  background: transparent;
}

:deep(.el-link) {
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-link:hover) {
   transform: none !important;
 }

 /* 响应式设计 */
 @media (max-width: 1200px) {
   .stats-cards .el-col {
     margin-bottom: var(--spacing-md);
   }
 }

 /* 平板设备 - 2列布局 */
 @media (max-width: 992px) {
   .stats-cards .el-col {
     width: 50% !important;
     flex: 0 0 50% !important;
     max-width: 50% !important;
   }
 }

 @media (max-width: 768px) {
   .system-management {
     padding: var(--spacing-md);
   }
   
   .page-header {
     padding: var(--spacing-lg);
     margin-bottom: var(--spacing-lg);
     flex-direction: column;
     gap: var(--spacing-md);
     align-items: stretch;
   }
   
   .header-actions {
     justify-content: center;
   }
   
   .header-left h2 {
     font-size: 24px;
   }
   
   .page-description {
     font-size: 14px;
   }
   
   .stats-cards {
     margin-bottom: var(--spacing-lg);
   }
   
   .stat-content {
     padding: var(--spacing-lg);
   }
   
   .stat-icon {
     width: 48px;
     height: 48px;
     font-size: 20px;
     margin-right: var(--spacing-md);
   }
   
   .stat-value {
     font-size: 24px;
   }
   
   .stat-label {
     font-size: 14px;
   }
 }

 /* 手机设备 - 单列布局 */
 @media (max-width: 576px) {
   .stats-cards .el-col {
     width: 100% !important;
     flex: 0 0 100% !important;
     max-width: 100% !important;
     margin-bottom: var(--spacing-md);
   }
 }

 @media (max-width: 480px) {
   .system-management {
     padding: var(--spacing-sm);
   }
   
   .page-header {
     padding: var(--spacing-md);
   }
   
   .header-left h2 {
     font-size: 20px;
   }
   
   .header-actions {
     flex-direction: column;
     gap: var(--spacing-sm);
   }
   
   .stat-content {
     flex-direction: column;
     text-align: center;
     padding: var(--spacing-md);
   }
   
   .stat-icon {
     margin-right: 0;
     margin-bottom: var(--spacing-sm);
     width: 40px;
     height: 40px;
     font-size: 18px;
   }
   
   .stat-value {
     font-size: 20px;
   }
   
   .stat-label {
     font-size: 12px;
   }
 }

 /* 动画效果 */
 @keyframes fadeInUp {
   from {
     opacity: 0;
     transform: translateY(30px);
   }
   to {
     opacity: 1;
     transform: translateY(0);
   }
 }

 @keyframes slideInLeft {
   from {
     opacity: 0;
     transform: translateX(-30px);
   }
   to {
     opacity: 1;
     transform: translateX(0);
   }
 }

 @keyframes pulse {
   0%, 100% {
     transform: scale(1);
     opacity: 0.2;
   }
   50% {
     transform: scale(1.2);
     opacity: 0.1;
   }
 }

 @keyframes shimmer {
   0% {
     background-position: -200px 0;
   }
   100% {
     background-position: calc(200px + 100%) 0;
   }
 }

 /* 应用动画 */
 .page-header {
   animation: fadeInUp 0.6s ease-out;
 }

 .stats-cards .el-col {
   animation: fadeInUp 0.6s ease-out;
 }

 .stats-cards .el-col:nth-child(1) { animation-delay: 0.1s; }
 .stats-cards .el-col:nth-child(2) { animation-delay: 0.2s; }
 .stats-cards .el-col:nth-child(3) { animation-delay: 0.3s; }
 .stats-cards .el-col:nth-child(4) { animation-delay: 0.4s; }

 .systems-table {
   animation: slideInLeft 0.6s ease-out 0.5s both;
 }

 /* 加载动画 */
 .stat-card::after {
   content: '';
   position: absolute;
   top: 0;
   left: -200px;
   width: 200px;
   height: 100%;
   background: linear-gradient(
     90deg,
     transparent,
     rgba(255, 255, 255, 0.2),
     transparent
   );
   animation: shimmer 2s infinite;
 }

 .stat-card:hover::after {
   animation-play-state: running;
 }

 /* 平滑滚动 */
 html {
   scroll-behavior: smooth;
 }

 /* 焦点样式 */
 :deep(.el-button:focus),
 :deep(.el-input__inner:focus) {
   outline: 2px solid rgba(102, 126, 234, 0.3);
   outline-offset: 2px;
 }

 /* 过渡效果 */
 * {
   transition: color 0.3s ease, background-color 0.3s ease, border-color 0.3s ease;
 }
 
 /* 禁用特定元素的transform和box-shadow过渡效果 */
 .stat-card,
 .systems-table,
 :deep(.el-table tr),
 :deep(.el-button),
 :deep(.el-link) {
   transition: color 0.3s ease, background-color 0.3s ease, border-color 0.3s ease !important;
 }
 </style>