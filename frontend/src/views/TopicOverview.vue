<template>
  <div class="topic-overview">
    <div class="container compact">
    <div class="page-header">
      <div class="header-left">
        <h2>Topic总览</h2>
        <p class="page-description">查看和管理所有EMQX系统的Topic信息</p>
      </div>
      <div class="header-actions">
        <el-select v-model="selectedSystem" placeholder="选择EMQX系统" style="width: 220px" clearable>
          <el-option
            v-for="system in systems"
            :key="system.id"
            :label="system.name"
            :value="system.id"
          >
            <div class="system-option">
              <span class="system-name">{{ system.name }}</span>
              <el-tag :type="system.status === 'online' ? 'success' : 'danger'" size="small">
                {{ system.status === 'online' ? '在线' : '离线' }}
              </el-tag>
            </div>
          </el-option>
        </el-select>
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="syncTopics" :disabled="!selectedSystem">
          <el-icon><Download /></el-icon>
          同步Topic
        </el-button>
      </div>
    </div>
    


    <!-- Topic列表 -->
    <el-card class="topics-table" shadow="never">
      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <div class="filter-header">
          <span class="filter-title">筛选条件</span>
        </div>
        <el-row :gutter="12" class="filter-row">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索Topic名称"
              clearable
              size="small"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedGroup" placeholder="选择分组" clearable size="small">
              <el-option label="全部分组" value="" />
              <el-option
                v-for="group in groups"
                :key="group.id"
                :label="group.name"
                :value="group.id"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedTag" placeholder="选择标签" clearable size="small">
              <el-option label="全部标签" value="" />
              <el-option
                v-for="tag in tags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable size="small">
              <el-option label="全部状态" value="" />
              <el-option label="活跃" value="active" />
              <el-option label="非活跃" value="inactive" />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-button type="primary" @click="searchTopics" style="width: 100%" size="small">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-col>

        </el-row>
      </div>

      <!-- 批量操作区域 -->
      <div class="batch-actions">
        <div class="batch-info">
          <el-tag v-if="selectedTopics.length > 0" type="primary" size="small">
            已选择 {{ selectedTopics.length }} 个Topic
          </el-tag>
        </div>
        <div class="batch-buttons">
          <el-button 
            size="small" 
            @click="batchAssignGroup" 
            :disabled="selectedTopics.length === 0"
          >
            <el-icon><FolderAdd /></el-icon>
            批量分配分组
          </el-button>
          <el-button 
            size="small" 
            @click="batchManageTags"
            :disabled="selectedTopics.length === 0"
          >
            <el-icon><PriceTag /></el-icon>
            批量管理标签
          </el-button>
          <el-button size="small" @click="exportTopics">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </div>

      <el-table
        :data="filteredTopics"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        class="topics-table"
        stripe
      >
        <el-table-column type="selection" min-width="55" align="center" />
        <el-table-column prop="name" label="Topic名称" min-width="250" align="center">
          <template #default="{ row }">
            <div class="topic-name-cell">
              <el-link @click="viewTopicDetail(row)" type="primary" class="topic-link">
                {{ row.name }}
              </el-link>
              <div class="topic-path" v-if="row.path !== row.name">
                {{ row.path }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="group" label="所属分组" min-width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.group" type="info" size="small">
              <el-icon><Folder /></el-icon>
              {{ row.group.name }}
            </el-tag>
            <span v-else class="text-muted">未分组</span>
          </template>
        </el-table-column>

        <el-table-column label="标签" min-width="200" align="center">
          <template #default="{ row }">
            <div class="tags-container">
              <el-tag
                v-for="tag in (row.tags || []).slice(0, 3)"
                :key="tag.id"
                size="small"
                :color="tag.color"
                class="topic-tag"
              >
                {{ tag.name }}
              </el-tag>
              <el-tag 
                v-if="(row.tags || []).length > 3" 
                size="small" 
                type="info"
                class="more-tags"
              >
                +{{ (row.tags || []).length - 3 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <div class="pagination-info">
        <span>共 {{ totalCount }} 条记录</span>
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalCount"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 分配分组对话框 -->
    <el-dialog v-model="showGroupDialog" title="分配分组" width="400px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="Topic">
          <el-input :value="selectedTopic?.name" readonly />
        </el-form-item>
        <el-form-item label="分组">
          <el-select v-model="groupForm.groupId" placeholder="选择分组">
            <el-option label="取消分组" :value="null" />
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGroupAssignment">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 管理标签对话框 -->
    <el-dialog v-model="showTagDialog" title="管理标签" width="500px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="Topic">
          <el-input :value="selectedTopic?.name" readonly />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="tagForm.tagIds" multiple placeholder="选择标签">
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTagAssignment">确定</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Search,
  Download,
  FolderAdd,
  PriceTag,
  View,
  Folder,
  RefreshLeft
} from '@element-plus/icons-vue'

const router = useRouter()

interface Topic {
  id: number
  name: string
  path?: string
  group?: { id: number; name: string }
  tags?: { id: number; name: string; color: string }[]
  clientCount: number
  messageCount: number
  lastActivity: string
  status: 'active' | 'inactive'
}

interface Group {
  id: number
  name: string
}

interface Tag {
  id: number
  name: string
  color: string
}

interface System {
  id: number
  name: string
  status: 'online' | 'offline'
}

const loading = ref(false)
const syncing = ref(false)
const showGroupDialog = ref(false)
const showTagDialog = ref(false)
// tableHeight 已移除，表格现在使用自适应高度

const selectedSystem = ref<number>()
const searchKeyword = ref('')
const selectedGroup = ref<number | string>('')
const selectedTag = ref<number>()
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

const selectedTopic = ref<Topic | null>(null)
const selectedTopics = ref<Topic[]>([])

const systems = ref<System[]>([])
const topics = ref<Topic[]>([])
const groups = ref<Group[]>([])
const tags = ref<Tag[]>([])

const groupForm = reactive({
  groupId: null as number | null
})

const tagForm = reactive({
  tagIds: [] as number[]
})

const filteredTopics = computed(() => {
  let result = topics.value
  
  // 搜索过滤
  if (searchKeyword.value) {
    result = result.filter(topic => 
      topic.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  // 分组过滤
  if (selectedGroup.value !== '') {
    if (selectedGroup.value === null) {
      result = result.filter(topic => !topic.group)
    } else {
      result = result.filter(topic => topic.group?.id === selectedGroup.value)
    }
  }
  
  // 标签过滤
  if (selectedTag.value) {
    result = result.filter(topic => 
      topic.tags?.some(tag => tag.id === selectedTag.value)
    )
  }
  
  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(topic => topic.status === statusFilter.value)
  }
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 统计计算属性
const totalCount = computed(() => topics.value.length)

// 格式化方法
const formatNumber = (num: number) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

const formatTime = (timeStr: string) => {
  const now = new Date()
  const time = new Date(timeStr)
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`
  return timeStr.split(' ')[0]
}

const loadSystems = async () => {
  try {
    // TODO: 调用后端API获取系统列表
    systems.value = [
      { id: 1, name: '生产环境EMQX', status: 'online' },
      { id: 2, name: '测试环境EMQX', status: 'offline' }
    ]
    
    if (systems.value.length > 0) {
      selectedSystem.value = systems.value[0].id
    }
  } catch (error) {
    ElMessage.error('加载系统列表失败')
  }
}

const loadTopics = async () => {
  if (!selectedSystem.value) return
  
  loading.value = true
  try {
    // TODO: 调用后端API获取Topic列表
    topics.value = [
      {
        id: 1,
        name: 'device/+/telemetry',
        path: 'device/+/telemetry',
        group: { id: 1, name: '设备遥测' },
        tags: [
          { id: 1, name: '重要', color: '#f56c6c' },
          { id: 2, name: '实时', color: '#67c23a' }
        ],
        clientCount: 25,
        messageCount: 1250,
        lastActivity: '2024-01-15 14:30:00',
        status: 'active'
      },
      {
        id: 2,
        name: 'sensor/temperature/+',
        path: 'sensor/temperature/+',
        group: { id: 2, name: '传感器数据' },
        tags: [{ id: 3, name: '温度', color: '#409eff' }],
        clientCount: 12,
        messageCount: 890,
        lastActivity: '2024-01-15 14:25:00',
        status: 'active'
      },
      {
        id: 3,
        name: 'alert/system/error',
        path: 'alert/system/error',
        clientCount: 5,
        messageCount: 45,
        lastActivity: '2024-01-15 13:45:00',
        status: 'inactive'
      },
      {
        id: 4,
        name: 'device/gateway/status',
        path: 'device/gateway/status',
        group: { id: 1, name: '设备遥测' },
        tags: [
          { id: 2, name: '实时', color: '#67c23a' },
          { id: 4, name: '告警', color: '#e6a23c' }
        ],
        clientCount: 8,
        messageCount: 320,
        lastActivity: '2024-01-15 13:20:00',
        status: 'active'
      }
    ]
  } catch (error) {
    ElMessage.error('加载Topic列表失败')
  } finally {
    loading.value = false
  }
}

const loadGroups = async () => {
  try {
    // TODO: 调用后端API获取分组列表
    groups.value = [
      { id: 1, name: '设备遥测' },
      { id: 2, name: '传感器数据' },
      { id: 3, name: '系统告警' }
    ]
  } catch (error) {
    ElMessage.error('加载分组列表失败')
  }
}

const loadTags = async () => {
  try {
    // TODO: 调用后端API获取标签列表
    tags.value = [
      { id: 1, name: '重要', color: '#f56c6c' },
      { id: 2, name: '实时', color: '#67c23a' },
      { id: 3, name: '温度', color: '#409eff' },
      { id: 4, name: '告警', color: '#e6a23c' }
    ]
  } catch (error) {
    ElMessage.error('加载标签列表失败')
  }
}

const syncTopics = async () => {
  if (!selectedSystem.value) {
    ElMessage.warning('请先选择EMQX系统')
    return
  }
  
  syncing.value = true
  try {
    // TODO: 调用后端API同步Topic
    ElMessage.success('Topic同步成功')
    loadTopics()
  } catch (error) {
    ElMessage.error('Topic同步失败')
  } finally {
    syncing.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const searchTopics = () => {
  currentPage.value = 1
  // 触发filteredTopics重新计算
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadSystems(),
      loadTopics(),
      loadGroups(),
      loadTags()
    ])
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  selectedGroup.value = ''
  selectedTag.value = undefined
  currentPage.value = 1
}

const handleSelectionChange = (selection: Topic[]) => {
  selectedTopics.value = selection
}

const batchAssignGroup = () => {
  if (selectedTopics.value.length === 0) {
    ElMessage.warning('请先选择要分配分组的Topic')
    return
  }
  // TODO: 实现批量分配分组功能
  console.log('批量分配分组:', selectedTopics.value)
  ElMessage.success('批量分配分组功能开发中')
}

const batchManageTags = () => {
  if (selectedTopics.value.length === 0) {
    ElMessage.warning('请先选择要管理标签的Topic')
    return
  }
  // TODO: 实现批量管理标签功能
  console.log('批量管理标签:', selectedTopics.value)
  ElMessage.success('批量管理标签功能开发中')
}

const exportTopics = () => {
  // TODO: 实现导出功能
  console.log('导出Topic数据')
  ElMessage.success('导出功能开发中')
}

const viewTopicDetail = (topic: Topic) => {
  router.push({
    name: 'TopicDetail',
    params: { id: topic.id.toString() }
  })
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

const assignGroup = (topic: Topic) => {
  selectedTopic.value = topic
  groupForm.groupId = topic.group?.id || null
  showGroupDialog.value = true
}

const manageTags = (topic: Topic) => {
  selectedTopic.value = topic
  tagForm.tagIds = topic.tags?.map(tag => tag.id) || []
  showTagDialog.value = true
}

const saveGroupAssignment = async () => {
  try {
    // TODO: 调用后端API保存分组分配
    ElMessage.success('分组分配成功')
    showGroupDialog.value = false
    loadTopics()
  } catch (error) {
    ElMessage.error('分组分配失败')
  }
}

const saveTagAssignment = async () => {
  try {
    // TODO: 调用后端API保存标签分配
    ElMessage.success('标签分配成功')
    showTagDialog.value = false
    loadTopics()
  } catch (error) {
    ElMessage.error('标签分配失败')
  }
}

watch(selectedSystem, () => {
  if (selectedSystem.value) {
    loadTopics()
  }
})

onMounted(() => {
  loadSystems()
  loadGroups()
  loadTags()
  // 移除了 calculateTableHeight 调用，表格现在自适应高度
})

// 计算表格高度 - 现在表格会根据数据量动态调整高度
const calculateTableHeight = () => {
  // 表格现在使用自适应高度，无需固定计算
  // 表格会根据数据行数自动调整高度
}
</script>

<style scoped>
.topic-overview {
  /* CSS变量定义 */
  --primary-color: #10b981;
  --primary-gradient: linear-gradient(135deg, #10b981 0%, #059669 100%);
  --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --success-gradient: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  --warning-gradient: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  --danger-gradient: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --error-color: #f56c6c;
  --info-color: #909399;
  
  --text-primary: #2c3e50;
  --text-secondary: #606266;
  --text-tertiary: #909399;
  --text-inverse: #ffffff;
  
  --bg-primary: #ffffff;
  --bg-secondary: rgba(255, 255, 255, 0.8);
  --bg-tertiary: #f5f7fa;
  --bg-glass: rgba(255, 255, 255, 0.1);
  
  --border-color: rgba(16, 185, 129, 0.2);
  --border-light: rgba(226, 232, 240, 0.6);
  
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
  --shadow-primary: 0 4px 14px rgba(16, 185, 129, 0.25);
  
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 20px;
  --spacing-xl: 24px;
  
  --transition-fast: 0.15s ease;
  --transition-normal: 0.3s ease;
  --transition-slow: 0.5s ease;
  
  padding: 0;
  background: transparent;
  min-height: 100vh;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  background: transparent;
  border: none;
}

.container.compact {
  padding: 0 16px;
  background: transparent;
  border: none;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--spacing-lg) 0;
  background: transparent;
  margin-bottom: var(--spacing-lg);
  border-radius: var(--radius-lg);
  position: relative;
  overflow: hidden;
}

.header-left h2 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-description {
  margin: 0;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.system-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  border-radius: 6px;
  border: none;
}

.system-name {
  flex: 1;
}





/* 筛选区域样式 */
.filter-section {
  padding: var(--spacing-lg) var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.8));
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(10px);
  box-shadow: none;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
}

.filter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: var(--spacing-lg);
  right: var(--spacing-lg);
  height: 3px;
  background: var(--primary-gradient);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.filter-section:hover::before {
  opacity: 0;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.filter-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  padding: var(--spacing-sm) 0;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  border: none;
  background: transparent;
}

.filter-label {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
}

/* Topic表格样式 */
.topics-table {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.8));
  border: 0.5px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-top: var(--spacing-lg);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-md);
  position: relative;
  transition: all var(--transition-normal);
}

.topics-table::before {
  content: '';
  position: absolute;
  top: 0;
  left: var(--spacing-lg);
  right: var(--spacing-lg);
  height: 3px;
  background: var(--primary-gradient);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.topics-table:hover::before {
  opacity: 1;
}

:deep(.el-card) {
  border-radius: var(--radius-lg);
  box-shadow: none;
  background: transparent;
  border: none;
}

.topics-table .el-card__body {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-bottom: 1px solid var(--border-light);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  border: none;
  background: transparent;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: nowrap;
  white-space: nowrap;
  background: transparent;
}

.header-actions .el-button {
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: all var(--transition-normal);
}



/* Topic名称单元格 */
.topic-name-cell {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  border: none;
  background: transparent;
}

.topic-link {
  font-weight: 600;
  color: var(--primary-color);
  text-decoration: none;
  transition: all var(--transition-normal);
}



.topic-path {
  font-size: 12px;
  color: var(--text-tertiary);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: var(--bg-tertiary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  display: inline-block;
}

/* 状态指示器 */
.status-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  white-space: nowrap;
  border: none;
  background: transparent;
}

.status-dot-circle {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
  animation: pulse 2s infinite;
}

.status-dot-circle.active {
  background: var(--success-color);
}

.status-dot-circle.inactive {
  background: var(--error-color);
}

.status-text {
  font-size: 12px;
  font-weight: 500;
  color: #000000;
}

.status-text.active {
  color: #000000;
}

.status-text.inactive {
  color: #000000;
}

/* 数值样式 */
.metric-value {
  font-weight: 600;
  color: var(--text-primary);
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.time-text {
  font-size: 12px;
  color: var(--text-tertiary);
  background: var(--bg-tertiary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

/* 标签容器 */
.tags-container {
  display: flex;
  flex-wrap: nowrap;
  gap: var(--spacing-xs);
  align-items: center;
  overflow-x: auto;
  border: none;
  background: transparent;
}

.topic-tag {
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
  margin-right: 4px;
  margin-bottom: 2px;
  border: none;
  color: white;
  border-radius: 6px;
}



.more-tags {
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  transition: all var(--transition-normal);
  margin-left: 2px;
  border-radius: 6px;
}



/* 批量操作区域样式 */
.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  margin: var(--spacing-md) 0;
  backdrop-filter: none;
  box-shadow: none;
}

.batch-info {
  display: flex;
  align-items: center;
}

.batch-buttons {
  display: flex;
  gap: var(--spacing-sm);
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  background: transparent;
  border: none;
  border-radius: var(--radius-lg);
  backdrop-filter: none;
  box-shadow: none;
}

.pagination-info {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.text-muted {
  color: var(--text-tertiary);
}

/* Element Plus 组件样式覆盖 */
:deep(.el-card__header) {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: none;
  background: var(--bg-secondary);
  position: relative;
}

:deep(.el-card__header::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--primary-gradient);
}

:deep(.el-card__body) {
  padding: 0;
  background: transparent;
}

:deep(.el-table) {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: transparent;
  box-shadow: none;
  border: 1.5px solid var(--border-light);
  width: 100%;
  table-layout: auto;
  /* 表格自适应高度设置 */
  height: auto !important;
  min-height: 200px;
  max-height: none;
}

/* 表格容器自适应高度 */
:deep(.el-table__body-wrapper) {
  height: auto !important;
  max-height: none !important;
  overflow-y: visible !important;
}

/* 表格主体自适应 */
:deep(.el-table__body) {
  height: auto !important;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  color: var(--text-primary);
  font-weight: 700;
  font-size: 14px;
  padding: var(--spacing-lg) var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
  border-right: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
}





/* 排序表头样式优化 */
:deep(.el-table th.is-sortable) {
  white-space: nowrap;
  overflow: visible;
  position: relative;
  padding-right: 24px;
}

:deep(.el-table th.is-sortable .cell) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

:deep(.el-table th.is-sortable .caret-wrapper) {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12px;
  height: 16px;
  flex-shrink: 0;
}

:deep(.el-table th.is-sortable .sort-caret) {
  width: 0;
  height: 0;
  border: 4px solid transparent;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

:deep(.el-table th.is-sortable .sort-caret.ascending) {
  top: -2px;
  border-bottom-color: #c0c4cc;
}

:deep(.el-table th.is-sortable .sort-caret.descending) {
  bottom: -2px;
  border-top-color: #c0c4cc;
}

:deep(.el-table th.is-sortable.ascending .sort-caret.ascending) {
  border-bottom-color: var(--primary-color);
}

:deep(.el-table th.is-sortable.descending .sort-caret.descending) {
  border-top-color: var(--primary-color);
}

:deep(.el-table td) {
  border-bottom: 1px solid var(--border-light);
  border-right: 1px solid var(--border-light);
  padding: var(--spacing-lg) var(--spacing-md);
  color: var(--text-secondary);
  font-size: 14px;
  transition: all var(--transition-normal);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: transparent;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.3), rgba(241, 245, 249, 0.2));
}

:deep(.el-table__body tr:hover > td) {
  background: transparent !important;
  color: var(--text-secondary);
  transform: none;
  box-shadow: none;
}

:deep(.el-table__row) {
  transition: all var(--transition-normal);
  border-radius: 4px;
  background: #ffffff;
}

:deep(.el-table__row:hover) {
  transform: none;
  box-shadow: none;
}



:deep(.el-button--small) {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 8px;
  transition: all var(--transition-normal);
}

:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
  border: none;
}

:deep(.el-button--small) {
  border-radius: 6px;
}

/* 同步Topic按钮 - 蓝色 */
:deep(.header-actions .el-button--primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  color: white;
}

:deep(.header-actions .el-button--primary:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

/* 搜索按钮 - 橙色 */
:deep(.filter-section .el-button--primary) {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border: none;
  color: white;
}

:deep(.filter-section .el-button--primary:hover) {
  background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}



:deep(.el-tag--small) {
  height: 20px;
  line-height: 18px;
  font-size: 11px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

:deep(.el-select) {
  width: 160px;
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: none;
}

:deep(.el-input) {
  border-radius: 8px;
  transition: all var(--transition-normal);
}

:deep(.el-input__wrapper) {
  border: none;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-form-item .el-select .el-input__wrapper) {
  border-radius: 8px;
}



:deep(.el-pagination) {
  --el-pagination-font-size: 14px;
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-border-radius: var(--radius-md);
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-button-bg-color: transparent;
  --el-pagination-button-disabled-color: var(--text-muted);
  --el-pagination-button-disabled-bg-color: transparent;
  --el-pagination-hover-color: var(--primary-color);
  --el-pagination-hover-bg-color: rgba(16, 185, 129, 0.1);
  justify-content: center;
  gap: var(--spacing-xs);
}

:deep(.el-pagination .el-pager li) {
  border-radius: 6px;
  margin: 0 2px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  border-radius: 6px;
}

:deep(.el-pagination .el-select .el-input) {
  border-radius: 6px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8), rgba(248, 250, 252, 0.6));
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  font-weight: 500;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(5, 150, 105, 0.1));
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
}

:deep(.el-pagination .el-pager li) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8), rgba(248, 250, 252, 0.6));
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  margin: 0 var(--spacing-xs);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  font-weight: 500;
}

:deep(.el-pagination .el-pager li:hover) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(5, 150, 105, 0.1));
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--primary-gradient);
  border-color: var(--primary-color);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
    padding: var(--spacing-md);
  }

  .header-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: var(--spacing-sm);
  }



  .filter-row {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-sm);
  }

  .filter-item {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: var(--spacing-xs) var(--spacing-xs);
  }

  /* 移动端排序表头优化 */
  :deep(.el-table th.is-sortable) {
    padding-right: 20px;
  }

  :deep(.el-table th.is-sortable .caret-wrapper) {
    right: 4px;
    width: 10px;
    height: 14px;
  }

  :deep(.el-table th.is-sortable .sort-caret) {
    border-width: 3px;
  }

  .topic-name-cell {
    min-width: 120px;
  }

  .topic-link {
    font-size: 12px;
  }

  .topic-path {
    font-size: 10px;
  }

  .tags-container {
    gap: var(--spacing-xs);
  }

  .topic-tag,
  .more-tags {
    font-size: 10px;
    padding: 1px var(--spacing-xs);
  }

  .pagination-wrapper {
    flex-direction: column;
    gap: var(--spacing-sm);
    padding: var(--spacing-sm);
  }

  :deep(.el-pagination) {
    --el-pagination-font-size: 12px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {

  
  .page-header {
    padding: var(--spacing-sm);
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions .el-button {
    width: 100%;
    margin-bottom: var(--spacing-xs);
  }

  .card-header {
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: stretch;
  }

  .header-actions {
    justify-content: stretch;
  }

  :deep(.el-pagination .btn-prev),
  :deep(.el-pagination .btn-next),
  :deep(.el-pagination .el-pager li) {
    margin: var(--spacing-xs);
    min-width: 32px;
    height: 32px;
  }
}
</style>