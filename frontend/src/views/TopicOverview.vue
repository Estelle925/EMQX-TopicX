<template>
  <div class="topic-overview">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">Topic 总览</h1>
          <p class="page-description">可以查看和管理所有 EMQX 系统的 Topic 信息</p>
        </div>
        <div class="header-right">
          <el-select
            v-model="selectedEnvironment"
            placeholder="选择环境"
            class="environment-select"
            @change="handleEnvironmentChange"
          >
            <el-option
              v-for="env in environments"
              :key="env.value"
              :label="env.label"
              :value="env.value"
            />
          </el-select>
          
          <el-button
            type="primary"
            :icon="Refresh"
            :loading="refreshLoading"
            @click="refreshData"
          >
            刷新数据
          </el-button>
          
          <el-button
            type="success"
            :icon="Connection"
            :loading="syncLoading"
            @click="syncTopics"
          >
            同步 Topic
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选条件区域 -->
    <div class="filter-section">
      <el-card class="filter-card" shadow="never">
        <div class="filter-content">
          <div class="filter-row">
            <div class="filter-item">
              <label class="filter-label">搜索 Topic</label>
              <el-input
                v-model="searchForm.keyword"
                placeholder="输入 Topic 名称关键字"
                :prefix-icon="Search"
                clearable
                class="search-input"
              />
            </div>
            
            <div class="filter-item">
              <label class="filter-label">选择分组</label>
              <el-select
                v-model="searchForm.groupId"
                placeholder="选择分组"
                clearable
                class="filter-select"
              >
                <el-option
                  v-for="group in groups"
                  :key="group.id"
                  :label="group.name"
                  :value="group.id"
                />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label class="filter-label">选择标签</label>
              <el-select
                v-model="searchForm.tags"
                placeholder="选择标签"
                multiple
                clearable
                class="filter-select"
              >
                <el-option
                  v-for="tag in tags"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label class="filter-label">状态筛选</label>
              <el-select
                v-model="searchForm.status"
                placeholder="选择状态"
                clearable
                class="filter-select"
              >
                <el-option label="启用" value="enabled" />
                <el-option label="禁用" value="disabled" />
              </el-select>
            </div>
            
            <div class="filter-actions">
              <el-button
                type="primary"
                :icon="Search"
                @click="handleSearch"
              >
                搜索
              </el-button>
              <el-button
                :icon="RefreshLeft"
                @click="resetSearch"
              >
                重置
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-section" v-show="hasSelection">
      <div class="action-left">
        <el-button
          type="primary"
          :icon="FolderAdd"
          :disabled="!hasSelection"
          @click="showBatchGroupDialog = true"
        >
          批量分配分组
        </el-button>
        
        <el-button
          type="success"
          :icon="PriceTag"
          :disabled="!hasSelection"
          @click="showBatchTagDialog = true"
        >
          批量管理标签
        </el-button>
        
        <el-button
          type="info"
          :icon="Download"
          @click="exportData"
        >
          导出数据
        </el-button>
      </div>
      
      <div class="action-right">
        <span class="selection-info" v-if="hasSelection">
          已选择 {{ selectedTopics.length }} 项
        </span>
      </div>
    </div>

    <!-- Topic 列表展示 -->
    <div class="table-section">
      <el-card class="table-card" shadow="never">
        <el-table
          ref="tableRef"
          v-loading="tableLoading"
          :data="tableData"
          @selection-change="handleSelectionChange"
          class="topic-table"
          height="600"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column
            prop="name"
            label="Topic 名称"
            min-width="200"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <div class="topic-name">
                <el-icon class="topic-icon"><Document /></el-icon>
                <span class="name-text">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column
            prop="groupName"
            label="所属分组"
            width="150"
          >
            <template #default="{ row }">
              <el-tag v-if="row.groupName" type="info" size="small">
                {{ row.groupName }}
              </el-tag>
              <span v-else class="text-gray-400">未分组</span>
            </template>
          </el-table-column>
          
          <el-table-column
            prop="path"
            label="Topic 路径"
            min-width="250"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <code class="topic-path">{{ row.path }}</code>
            </template>
          </el-table-column>
          
          <el-table-column
            prop="tags"
            label="标签"
            width="200"
          >
            <template #default="{ row }">
              <div class="tags-container">
                <el-tag
                  v-for="tag in row.tags"
                  :key="tag.id"
                  :type="getTagType(tag.type)"
                  size="small"
                  class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
                <span v-if="!row.tags?.length" class="text-gray-400">无标签</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column
            prop="createdAt"
            label="创建时间"
            width="180"
            sortable
          >
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column
            prop="updatedAt"
            label="更新时间"
            width="180"
            sortable
          >
            <template #default="{ row }">
              {{ formatDateTime(row.updatedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column
            label="操作"
            width="120"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                text
                @click="viewTopicDetail(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 批量分配分组对话框 -->
    <el-dialog
      v-model="showBatchGroupDialog"
      title="批量分配分组"
      width="500px"
    >
      <el-form :model="batchGroupForm" label-width="80px">
        <el-form-item label="选择分组">
          <el-select
            v-model="batchGroupForm.groupId"
            placeholder="请选择分组"
            style="width: 100%"
          >
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
        <el-button @click="showBatchGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchGroup">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量管理标签对话框 -->
    <el-dialog
      v-model="showBatchTagDialog"
      title="批量管理标签"
      width="500px"
    >
      <el-form :model="batchTagForm" label-width="80px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchTagForm.action">
            <el-radio value="add">添加标签</el-radio>
            <el-radio value="remove">移除标签</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="选择标签">
          <el-select
            v-model="batchTagForm.tagIds"
            placeholder="请选择标签"
            multiple
            style="width: 100%"
          >
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
        <el-button @click="showBatchTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  RefreshLeft,
  Connection,
  FolderAdd,
  PriceTag,
  Download,
  Document
} from '@element-plus/icons-vue'

// 类型定义
interface Topic {
  id: string
  name: string
  path: string
  groupId?: string
  groupName?: string
  tags?: Tag[]
  status: 'enabled' | 'disabled'
  createdAt: string
  updatedAt: string
}

interface Tag {
  id: string
  name: string
  type: 'important' | 'realtime' | 'normal'
}

interface Group {
  id: string
  name: string
}

interface Environment {
  label: string
  value: string
}

// 响应式数据
const router = useRouter()
const tableRef = ref()

// 环境选择
const selectedEnvironment = ref('production')
const environments = ref<Environment[]>([
  { label: '生产环境', value: 'production' },
  { label: '测试环境', value: 'test' },
  { label: '开发环境', value: 'development' }
])

// 加载状态
const refreshLoading = ref(false)
const syncLoading = ref(false)
const tableLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  groupId: '',
  tags: [] as string[],
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 表格数据
const tableData = ref<Topic[]>([])
const selectedTopics = ref<Topic[]>([])

// 分组和标签数据
const groups = ref<Group[]>([
  { id: '1', name: '设备遥测' },
  { id: '2', name: '设备控制' },
  { id: '3', name: '系统监控' },
  { id: '4', name: '告警通知' }
])

const tags = ref<Tag[]>([
  { id: '1', name: '重要', type: 'important' },
  { id: '2', name: '实时', type: 'realtime' },
  { id: '3', name: '普通', type: 'normal' }
])

// 批量操作对话框
const showBatchGroupDialog = ref(false)
const showBatchTagDialog = ref(false)

const batchGroupForm = reactive({
  groupId: ''
})

const batchTagForm = reactive({
  action: 'add' as 'add' | 'remove',
  tagIds: [] as string[]
})

// 计算属性
const hasSelection = computed(() => selectedTopics.value.length > 0)

// 模拟数据
const mockData: Topic[] = [
  {
    id: '1',
    name: 'device/+/telemetry',
    path: 'device/sensor001/telemetry',
    groupId: '1',
    groupName: '设备遥测',
    tags: [{ id: '1', name: '重要', type: 'important' }, { id: '2', name: '实时', type: 'realtime' }],
    status: 'enabled',
    createdAt: '2024-01-15 10:30:00',
    updatedAt: '2024-01-20 14:25:00'
  },
  {
    id: '2',
    name: 'device/+/control',
    path: 'device/actuator001/control',
    groupId: '2',
    groupName: '设备控制',
    tags: [{ id: '1', name: '重要', type: 'important' }],
    status: 'enabled',
    createdAt: '2024-01-16 09:15:00',
    updatedAt: '2024-01-18 16:40:00'
  },
  {
    id: '3',
    name: 'system/monitor/+',
    path: 'system/monitor/cpu',
    groupId: '3',
    groupName: '系统监控',
    tags: [{ id: '3', name: '普通', type: 'normal' }],
    status: 'enabled',
    createdAt: '2024-01-17 11:20:00',
    updatedAt: '2024-01-19 13:55:00'
  },
  {
    id: '4',
    name: 'alert/+/notification',
    path: 'alert/critical/notification',
    groupId: '4',
    groupName: '告警通知',
    tags: [{ id: '1', name: '重要', type: 'important' }, { id: '2', name: '实时', type: 'realtime' }],
    status: 'disabled',
    createdAt: '2024-01-18 08:45:00',
    updatedAt: '2024-01-21 10:30:00'
  },
  {
    id: '5',
    name: 'data/+/stream',
    path: 'data/sensor/stream',
    tags: [{ id: '2', name: '实时', type: 'realtime' }],
    status: 'enabled',
    createdAt: '2024-01-19 15:10:00',
    updatedAt: '2024-01-22 09:20:00'
  }
]

// 方法
const handleEnvironmentChange = (value: string) => {
  ElMessage.success(`切换到${environments.value.find(env => env.value === value)?.label}`)
  loadTableData()
}

const refreshData = async () => {
  refreshLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    await loadTableData()
    ElMessage.success('数据刷新成功')
  } finally {
    refreshLoading.value = false
  }
}

const syncTopics = async () => {
  syncLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    await loadTableData()
    ElMessage.success('Topic 同步成功')
  } finally {
    syncLoading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadTableData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    groupId: '',
    tags: [],
    status: ''
  })
  pagination.currentPage = 1
  loadTableData()
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    let filteredData = [...mockData]
    
    // 应用筛选条件
    if (searchForm.keyword) {
      filteredData = filteredData.filter(item => 
        item.name.toLowerCase().includes(searchForm.keyword.toLowerCase()) ||
        item.path.toLowerCase().includes(searchForm.keyword.toLowerCase())
      )
    }
    
    if (searchForm.groupId) {
      filteredData = filteredData.filter(item => item.groupId === searchForm.groupId)
    }
    
    if (searchForm.status) {
      filteredData = filteredData.filter(item => item.status === searchForm.status)
    }
    
    if (searchForm.tags.length > 0) {
      filteredData = filteredData.filter(item => 
        item.tags?.some(tag => searchForm.tags.includes(tag.id))
      )
    }
    
    pagination.total = filteredData.length
    
    // 分页
    const start = (pagination.currentPage - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    tableData.value = filteredData.slice(start, end)
  } finally {
    tableLoading.value = false
  }
}

const handleSelectionChange = (selection: Topic[]) => {
  selectedTopics.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadTableData()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  loadTableData()
}

const handleBatchGroup = async () => {
  if (!batchGroupForm.groupId) {
    ElMessage.warning('请选择分组')
    return
  }
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`成功为 ${selectedTopics.value.length} 个 Topic 分配分组`)
    showBatchGroupDialog.value = false
    batchGroupForm.groupId = ''
    loadTableData()
  } catch (error) {
    ElMessage.error('批量分配分组失败')
  }
}

const handleBatchTag = async () => {
  if (!batchTagForm.tagIds.length) {
    ElMessage.warning('请选择标签')
    return
  }
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    const action = batchTagForm.action === 'add' ? '添加' : '移除'
    ElMessage.success(`成功为 ${selectedTopics.value.length} 个 Topic ${action}标签`)
    showBatchTagDialog.value = false
    batchTagForm.tagIds = []
    loadTableData()
  } catch (error) {
    ElMessage.error('批量管理标签失败')
  }
}

const exportData = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('数据导出成功')
  } catch (error) {
    ElMessage.error('数据导出失败')
  }
}

const viewTopicDetail = (topic: Topic) => {
  router.push(`/topics/${topic.id}`)
}

const getTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    important: 'danger',
    realtime: 'warning',
    normal: 'info'
  }
  return typeMap[type] || 'info'
}

const formatDateTime = (dateTime: string) => {
  return dateTime
}

// 生命周期
onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.topic-overview {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.environment-select {
  width: 140px;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: #ffffff;
  transition: none !important;
}

.filter-card:hover {
  box-shadow: none !important;
  transform: none !important;
}

.filter-content {
  padding: 8px;
}

.filter-row {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 180px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 180px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* 操作区域 */
.action-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: transparent;
  border: none;
  border-radius: 16px;
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-right {
  display: flex;
  align-items: center;
}

.selection-info {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

/* 表格区域 */
.table-section {
  margin-bottom: 24px;
}

.table-card {
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: #ffffff;
  overflow: hidden;
  transition: none !important;
}

.table-card:hover {
  box-shadow: none !important;
  transform: none !important;
}

.topic-table {
  width: 100%;
}

/* 查看详情按钮样式 */
:deep(.el-button--primary.is-text) {
  color: white !important;
}

:deep(.el-button--primary.is-text:hover) {
  color: white !important;
}

.topic-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topic-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}

.topic-path {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: var(--text-primary);
  border: 1px solid #e2e8f0;
}

.tags-container {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-top: 1px solid #e2e8f0;
  margin-top: 16px;
  background: #fafbfc;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-item {
    min-width: auto;
  }
  
  .filter-actions {
    margin-left: 0;
    justify-content: flex-start;
  }
  
  .search-input,
  .filter-select {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .topic-overview {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .action-section {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .action-left {
    flex-wrap: wrap;
  }
}
</style>