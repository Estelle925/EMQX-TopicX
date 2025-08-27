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
            placeholder="选择EMQX系统"
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
              <label class="filter-label">选择业务</label>
              <el-select
                v-model="searchForm.groupId"
                placeholder="选择业务"
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
          批量分配业务
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
        
        <el-button
          type="warning"
          :icon="Edit"
          @click="showBatchPayloadDialog = true"
          :disabled="!hasSelection"
        >
          批量设置Payload
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
            label="所属业务"
            width="150"
          >
            <template #default="{ row }">
              <el-tag v-if="row.groupName" type="info" size="small">
                {{ row.groupName }}
              </el-tag>
              <span v-else class="text-gray-400">未业务</span>
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
            prop="status"
            label="状态"
            width="100"
          >
            <template #default="{ row }">
              <el-tag
                :type="row.status === 'enabled' ? 'success' : 'danger'"
                size="small"
              >
                {{ row.status === 'enabled' ? '启用' : '禁用' }}
              </el-tag>
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
                  :color="tag.color"
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

    <!-- 批量分配业务对话框 -->
    <el-dialog
      v-model="showBatchGroupDialog"
      title="批量分配业务"
      width="500px"
    >
      <el-form :model="batchGroupForm" label-width="80px">
        <el-form-item label="选择业务">
          <el-select
            v-model="batchGroupForm.groupId"
            placeholder="请选择业务"
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="batchTagForm" label-width="120px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchTagForm.action">
            <el-radio value="add">添加标签</el-radio>
            <el-radio value="remove">移除标签</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择标签">
          <el-select
            v-model="batchTagForm.tagIds"
            multiple
            placeholder="请选择标签"
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
        <span class="dialog-footer">
          <el-button @click="showBatchTagDialog = false">取消</el-button>
          <el-button type="primary" @click="handleBatchTag">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量设置Payload对话框 -->
    <el-dialog
      v-model="showBatchPayloadDialog"
      title="批量设置Payload"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="batchPayloadForm" label-width="120px">
        <el-form-item label="设置方式">
          <el-radio-group v-model="batchPayloadForm.mode">
            <el-radio value="template">使用模板</el-radio>
            <el-radio value="custom">自定义内容</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="batchPayloadForm.mode === 'template'" label="选择模板">
          <el-select
            v-model="batchPayloadForm.templateId"
            placeholder="请选择Payload模板"
            style="width: 100%"
            filterable
            @change="handleTemplateChange"
          >
            <el-option
              v-for="template in payloadTemplates"
              :key="template.id"
              :label="`${template.name} (${template.groupName || '未分组'})`"
              :value="template.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ template.name }}</span>
                <el-tag size="small" type="info">{{ template.type }}</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Payload内容">
          <el-input
            v-model="batchPayloadForm.payloadDoc"
            type="textarea"
            :rows="8"
            placeholder="请输入Payload内容"
            :readonly="batchPayloadForm.mode === 'template' && batchPayloadForm.templateId"
          />
        </el-form-item>
        <el-form-item v-if="batchPayloadForm.mode === 'template' && selectedTemplate" label="模板信息">
          <div class="template-info">
            <p><strong>模板名称：</strong>{{ selectedTemplate.name }}</p>
            <p><strong>模板类型：</strong>{{ selectedTemplate.type }}</p>
            <p v-if="selectedTemplate.description"><strong>描述：</strong>{{ selectedTemplate.description }}</p>
            <p><strong>使用次数：</strong>{{ selectedTemplate.usageCount }}</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchPayloadDialog = false">取消</el-button>
          <el-button type="primary" @click="handleBatchPayload" :disabled="!batchPayloadForm.payloadDoc">确定</el-button>
        </span>
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
  Document,
  Edit
} from '@element-plus/icons-vue'
import TopicAPI, { type TopicDTO, type TopicSearchRequest, type TopicBatchRequest } from '@/api/topic'
import { GroupAPI, type GroupDTO } from '@/api/group'
import { TagAPI, type TagDTO } from '@/api/tag'
import { SystemAPI, type SystemManagementDTO } from '@/api/system'
import { PayloadTemplateAPI, type PayloadTemplateDTO } from '@/api/payloadTemplate'

// 类型定义
type Topic = TopicDTO
type Tag = TagDTO
type Group = GroupDTO

interface Environment {
  label: string
  value: number
  systemId: number
}

// 响应式数据
const router = useRouter()
const tableRef = ref()

// 环境选择（EMQX系统）
const selectedEnvironment = ref<number | undefined>(undefined)
const environments = ref<Environment[]>([])

// 加载状态
const refreshLoading = ref(false)
const syncLoading = ref(false)
const tableLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  groupId: undefined as number | undefined,
  tags: [] as number[],
  status: '',
  systemId: undefined as number | undefined
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

// 业务和标签数据
const groups = ref<Group[]>([])
const tags = ref<Tag[]>([])
const payloadTemplates = ref<PayloadTemplateDTO[]>([])
const selectedTemplate = ref<PayloadTemplateDTO | null>(null)

// 批量操作对话框
const showBatchGroupDialog = ref(false)
const showBatchTagDialog = ref(false)
const showBatchPayloadDialog = ref(false)

const batchGroupForm = reactive({
  groupId: undefined as number | undefined
})

const batchTagForm = reactive({
  action: 'add' as 'add' | 'remove',
  tagIds: [] as number[]
})

const batchPayloadForm = reactive({
  mode: 'template' as 'template' | 'custom',
  templateId: undefined as number | undefined,
  payloadDoc: ''
})

// 计算属性
const hasSelection = computed(() => selectedTopics.value.length > 0)

// 数据加载方法
const loadGroups = async () => {
  try {
    groups.value = await GroupAPI.getAllGroups()
  } catch (error) {
    console.error('加载业务失败:', error)
    ElMessage.error('加载业务失败')
  }
}

const loadTags = async () => {
  try {
    tags.value = await TagAPI.getAllTags()
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  }
}

const loadPayloadTemplates = async () => {
  try {
    const result = await PayloadTemplateAPI.searchTemplates({
      page: 1,
      size: 1000 // 获取所有模板
    })
    payloadTemplates.value = result.records
  } catch (error) {
    console.error('加载Payload模板失败:', error)
    ElMessage.error('加载Payload模板失败')
  }
}

const loadSystems = async () => {
  try {
    const systems = await SystemAPI.getAllSystems()
    environments.value = systems
      .filter(system => system.status === 'online')
      .map(system => ({
        label: system.name,
        value: system.id,
        systemId: system.id
      }))
    
    // 如果有可用系统，默认选择第一个
    if (environments.value.length > 0 && !selectedEnvironment.value) {
      selectedEnvironment.value = environments.value[0].value
      searchForm.systemId = environments.value[0].systemId
      // 设置默认环境后立即加载表格数据
      await loadTableData()
    }
  } catch (error) {
    console.error('加载EMQX系统失败:', error)
    ElMessage.error('加载EMQX系统失败')
  }
}

// 方法
const handleEnvironmentChange = (value: number) => {
  const selectedSystem = environments.value.find(env => env.value === value)
  if (selectedSystem) {
    ElMessage.success(`切换到${selectedSystem.label}`)
    // 更新搜索表单中的systemId
    searchForm.systemId = selectedSystem.systemId
    loadTableData()
  }
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
  if (!selectedEnvironment.value) {
    ElMessage.warning('请先选择EMQX系统')
    return
  }

  syncLoading.value = true
  try {
    const result = await TopicAPI.syncTopicsFromEmqx(selectedEnvironment.value)
    await loadTableData()
    ElMessage.success(`同步完成：新增 ${result.syncedCount} 个Topic，更新 ${result.updatedCount} 个Topic`)
  } catch (error: any) {
    console.error('同步Topic失败:', error)
    
    // 检查是否是限流错误
    if (error?.response?.data?.message && error.response.data.message.includes('同步操作过于频繁')) {
      ElMessage.warning(error.response.data.message)
    } else {
      ElMessage.error('同步Topic失败，请检查EMQX系统连接状态')
    }
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
    const searchRequest = {
      keyword: searchForm.keyword,
      groupId: searchForm.groupId || undefined,
      tagIds: searchForm.tags.length > 0 ? searchForm.tags : undefined,
      status: searchForm.status || undefined,
      systemId: selectedEnvironment.value || undefined,
      page: pagination.currentPage - 1, 
      size: pagination.pageSize,
      sortBy: 'createdAt',
      sortDir: 'desc' as 'desc'
    }
    
    const result = await TopicAPI.searchTopics(searchRequest)
    tableData.value = result.records
    pagination.total = result.total
  } catch (error) {
    console.error('加载Topic数据失败:', error)
    ElMessage.error('加载Topic数据失败')
    tableData.value = []
    pagination.total = 0
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
    ElMessage.warning('请选择业务')
    return
  }
  
  try {
    const topicIds = selectedTopics.value.map(topic => topic.id)
    await TopicAPI.batchAssignGroup(topicIds, batchGroupForm.groupId)
    ElMessage.success(`成功为 ${selectedTopics.value.length} 个 Topic 分配业务`)
    showBatchGroupDialog.value = false
    batchGroupForm.groupId = undefined
    loadTableData()
  } catch (error) {
    console.error('批量分配业务失败:', error)
    ElMessage.error('批量分配业务失败')
  }
}

const handleBatchTag = async () => {
  if (!batchTagForm.tagIds.length) {
    ElMessage.warning('请选择标签')
    return
  }
  
  try {
    const topicIds = selectedTopics.value.map(topic => topic.id)
    if (batchTagForm.action === 'add') {
      await TopicAPI.batchAddTags(topicIds, batchTagForm.tagIds)
    } else {
      await TopicAPI.batchRemoveTags(topicIds, batchTagForm.tagIds)
    }
    const action = batchTagForm.action === 'add' ? '添加' : '移除'
    ElMessage.success(`成功为 ${selectedTopics.value.length} 个 Topic ${action}标签`)
    showBatchTagDialog.value = false
    batchTagForm.tagIds = []
    loadTableData()
  } catch (error) {
    console.error('批量管理标签失败:', error)
    ElMessage.error('批量管理标签失败')
  }
}

const handleTemplateChange = (templateId: number) => {
  const template = payloadTemplates.value.find(t => t.id === templateId)
  if (template) {
    selectedTemplate.value = template
    batchPayloadForm.payloadDoc = template.payload
  } else {
    selectedTemplate.value = null
    batchPayloadForm.payloadDoc = ''
  }
}

const handleBatchPayload = async () => {
  if (!batchPayloadForm.payloadDoc.trim()) {
    ElMessage.warning('请输入Payload内容')
    return
  }
  
  try {
    const topicIds = selectedTopics.value.map(topic => topic.id)
    const requestData: TopicBatchRequest = {
      topicIds,
      action: 'updatePayload' as const,
      templateId: batchPayloadForm.mode === 'template' ? batchPayloadForm.templateId : undefined,
      payloadDoc: batchPayloadForm.payloadDoc
    }
    
    await TopicAPI.batchOperation(requestData)
    
    // 如果使用模板模式，增加模板使用次数
    if (batchPayloadForm.mode === 'template' && batchPayloadForm.templateId) {
      try {
        await PayloadTemplateAPI.useTemplate(batchPayloadForm.templateId)
        // 更新本地模板数据
        const template = payloadTemplates.value.find(t => t.id === batchPayloadForm.templateId)
        if (template) {
          template.usageCount = (template.usageCount || 0) + 1
          template.lastUsed = new Date().toISOString()
        }
      } catch (error) {
        console.error('更新模板使用次数失败:', error)
      }
    }
    
    ElMessage.success(`成功为 ${selectedTopics.value.length} 个 Topic 设置Payload`)
    showBatchPayloadDialog.value = false
    
    // 重置表单
    batchPayloadForm.mode = 'template'
    batchPayloadForm.templateId = undefined
    batchPayloadForm.payloadDoc = ''
    selectedTemplate.value = null
    
    loadTableData()
  } catch (error) {
    console.error('批量设置Payload失败:', error)
    ElMessage.error('批量设置Payload失败')
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

const viewTopicDetail = (topic: TopicDTO) => {
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
onMounted(async () => {
  await loadSystems()
  loadGroups()
  loadTags()
  loadPayloadTemplates()
  // 只有在有选中环境时才加载表格数据
  if (selectedEnvironment.value) {
    loadTableData()
  }
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