<template>
  <div class="payload-template">
    <div class="page-header transparent">
      <div class="header-left">
        <h2>Payload模板</h2>
        <p class="page-description">管理和使用Payload模板，支持按业务分组组织</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          创建模板
        </el-button>
      </div>
    </div>
    

    <!-- 搜索和筛选 -->
    <div class="filter-section transparent">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索模板名称或描述..."
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="selectedGroup"
          placeholder="选择业务分组"
          style="width: 200px; margin-left: 16px;"
          clearable
        >
          <el-option label="全部分组" :value="undefined" />
          <el-option
            v-for="group in businessGroups"
            :key="group.id"
            :label="group.name"
            :value="group.id"
          />
        </el-select>
      </div>
      
      <div class="filter-right">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="card">卡片视图</el-radio-button>
          <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 模板内容区域 -->
    <div class="template-content" v-loading="loading">
      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="card-view">
        <div v-for="group in filteredGroupedTemplates" :key="group.groupId" class="group-section">
          <div class="group-header">
            <div class="group-title">
              <el-icon><Folder /></el-icon>
              <span>{{ group.groupName }}</span>
              <el-tag size="small" type="info">{{ group.templates.length }} 个模板</el-tag>
            </div>
          </div>
          
          <div class="template-cards">
            <div
              v-for="template in group.templates"
              :key="template.id"
              class="template-card"
              @click="viewTemplate(template)"
            >
              <div class="card-header">
                <div class="card-title">
                  <span>{{ template.name }}</span>
                  <div class="card-actions">
                    <el-button
                      size="small"
                      text
                      @click.stop="toggleFavorite(template)"
                      :class="{ 'is-favorite': template.favorite }"
                    >
                      <el-icon><Star /></el-icon>
                    </el-button>
                    <el-dropdown @command="(command) => handleTemplateAction(command, template)" @click.stop>
                      <el-button size="small" text>
                        <el-icon><MoreFilled /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="edit">编辑</el-dropdown-item>
                          <el-dropdown-item command="copy">复制</el-dropdown-item>
                          <el-dropdown-item command="export">导出</el-dropdown-item>
                          <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
                <div class="card-meta">
                  <el-tag size="small" :type="getTemplateTypeColor(template.type)">{{ template.type }}</el-tag>
                  <span class="update-time">{{ formatTime(template.updatedAt) }}</span>
                </div>
              </div>
              
              <div class="card-content">
                <p class="template-description">{{ template.description || '暂无描述' }}</p>
                <div class="payload-preview">
                  <pre>{{ getPayloadPreview(template.payload) }}</pre>
                </div>
              </div>
              
              <div class="card-footer">
                <div class="usage-stats">
                  <span class="usage-count">
                    <el-icon><View /></el-icon>
                    使用 {{ template.usageCount || 0 }} 次
                  </span>
                  <span class="last-used" v-if="template.lastUsed">
                    最近使用: {{ formatTime(template.lastUsed) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table :data="filteredTemplates" stripe>
          <el-table-column label="模板名称" min-width="150">
            <template #default="{ row }">
              <div class="template-name">
                <span>{{ row.name }}</span>
                <el-icon v-if="row.favorite" class="favorite-icon"><Star /></el-icon>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="业务分组" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ getGroupName(row.groupId) }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="getTemplateTypeColor(row.type)">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="描述" min-width="200">
            <template #default="{ row }">
              <span class="template-description">{{ row.description || '暂无描述' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="使用次数" width="100">
            <template #default="{ row }">
              <span>{{ row.usageCount || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="更新时间" width="150">
            <template #default="{ row }">
              <span>{{ formatTime(row.updatedAt) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewTemplate(row)">查看</el-button>
              <el-button size="small" @click="editTemplate(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteTemplate(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 创建/编辑模板弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingTemplate ? '编辑模板' : '创建模板'"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="templateForm" :rules="templateRules" ref="templateFormRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        
        <el-form-item label="业务分组" prop="groupId">
          <el-select v-model="templateForm.groupId" placeholder="选择业务分组" style="width: 100%;">
            <el-option
              v-for="group in businessGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="templateForm.type" placeholder="选择模板类型" style="width: 100%;">
            <el-option label="JSON" value="JSON" />
            <el-option label="XML" value="XML" />
            <el-option label="Text" value="Text" />
            <el-option label="Binary" value="Binary" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        
        <el-form-item label="Payload内容" prop="payload">
          <el-input
            v-model="templateForm.payload"
            type="textarea"
            :rows="10"
            placeholder="请输入Payload内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 模板详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="模板详情" width="800px">
      <div v-if="selectedTemplate" class="template-detail">
        <div class="detail-header">
          <h3>{{ selectedTemplate.name }}</h3>
          <div class="detail-meta">
            <el-tag :type="getTemplateTypeColor(selectedTemplate.type)">{{ selectedTemplate.type }}</el-tag>
            <span class="group-name">{{ getGroupName(selectedTemplate.groupId) }}</span>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="detail-section">
            <h4>描述</h4>
            <p>{{ selectedTemplate.description || '暂无描述' }}</p>
          </div>
          
          <div class="detail-section">
            <h4>Payload内容</h4>
            <pre class="payload-content">{{ selectedTemplate.payload }}</pre>
          </div>
          
          <div class="detail-section">
            <h4>使用统计</h4>
            <div class="usage-info">
              <span>使用次数: {{ selectedTemplate.usageCount || 0 }}</span>
              <span v-if="selectedTemplate.lastUsed">最近使用: {{ formatTime(selectedTemplate.lastUsed) }}</span>
              <span>创建时间: {{ formatTime(selectedTemplate.createdAt) }}</span>
              <span>更新时间: {{ formatTime(selectedTemplate.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Plus,
  Document,
  FolderOpened,
  Clock,
  Star,
  Search,
  Folder,
  MoreFilled,
  View
} from '@element-plus/icons-vue'
import { PayloadTemplateAPI, type PayloadTemplateDTO, type PayloadTemplateCreateRequest, type PayloadTemplateUpdateRequest, type PayloadTemplateSearchRequest } from '../api/payloadTemplate'
import { GroupAPI, type GroupDTO } from '../api/group'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const selectedGroup = ref<number | undefined>(undefined)
const viewMode = ref('card')
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const editingTemplate = ref<PayloadTemplateDTO | null>(null)
const selectedTemplate = ref<PayloadTemplateDTO | null>(null)
const templateFormRef = ref()

// 模板表单
const templateForm = ref<PayloadTemplateCreateRequest>({
  name: '',
  groupId: 0,
  type: 'JSON',
  description: '',
  payload: ''
})

// 表单验证规则
const templateRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  groupId: [{ required: true, message: '请选择业务分组', trigger: 'change' }],
  type: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  payload: [{ required: true, message: '请输入Payload内容', trigger: 'blur' }]
}

// 数据
const businessGroups = ref<GroupDTO[]>([])
const templates = ref<PayloadTemplateDTO[]>([])
const statistics = ref({
  totalTemplates: 0,
  groupCount: 0,
  recentUsedCount: 0,
  favoriteCount: 0
})

// 计算属性
const filteredTemplates = computed(() => {
  let filtered = templates.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(t => 
      t.name.toLowerCase().includes(keyword) || 
      (t.description && t.description.toLowerCase().includes(keyword))
    )
  }
  
  if (selectedGroup.value) {
    filtered = filtered.filter(t => t.groupId === selectedGroup.value)
  }
  
  return filtered
})

// 按分组组织的模板
const filteredGroupedTemplates = computed(() => {
  const grouped = new Map()
  
  filteredTemplates.value.forEach(template => {
    const groupId = template.groupId
    if (!grouped.has(groupId)) {
      const group = businessGroups.value.find(g => g.id === groupId)
      grouped.set(groupId, {
        groupId,
        groupName: group?.name || '未分组',
        templates: []
      })
    }
    grouped.get(groupId)?.templates.push(template)
  })
  
  return Array.from(grouped.values())
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    // 加载业务分组
    const groups = await GroupAPI.getAllGroups()
    businessGroups.value = groups
    
    // 加载模板列表
    const searchRequest: PayloadTemplateSearchRequest = {
      page: 1,
      size: 1000
    }
    const templateResult = await PayloadTemplateAPI.searchTemplates(searchRequest)
    templates.value = templateResult.records
    
    // 加载统计信息
    const stats = await PayloadTemplateAPI.getStatistics()
    statistics.value = stats
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await loadData()
  ElMessage.success('数据刷新成功')
}

const getGroupName = (groupId: number) => {
  const group = businessGroups.value.find(g => g.id === groupId)
  return group?.name || '未分组'
}

const getTemplateTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'JSON': 'success',
    'XML': 'warning',
    'Text': 'info',
    'Binary': 'danger'
  }
  return colorMap[type] || 'info'
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getPayloadPreview = (payload: string) => {
  if (!payload) return ''
  return payload.length > 100 ? payload.substring(0, 100) + '...' : payload
}

const toggleFavorite = async (template: PayloadTemplateDTO) => {
  try {
    await PayloadTemplateAPI.toggleFavorite(template.id)
    template.favorite = !template.favorite
    ElMessage.success(template.favorite ? '已添加到收藏' : '已取消收藏')
    // 刷新统计信息
    const stats = await PayloadTemplateAPI.getStatistics()
    statistics.value = stats
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleTemplateAction = (command: string, template: PayloadTemplateDTO) => {
  switch (command) {
    case 'edit':
      editTemplate(template)
      break
    case 'copy':
      copyTemplate(template)
      break
    case 'export':
      exportTemplate(template)
      break
    case 'delete':
      deleteTemplate(template)
      break
  }
}

const viewTemplate = async (template: PayloadTemplateDTO) => {
  selectedTemplate.value = template
  showDetailDialog.value = true
}

const editTemplate = (template: PayloadTemplateDTO) => {
  editingTemplate.value = template
  templateForm.value = {
    name: template.name,
    groupId: template.groupId,
    type: template.type,
    description: template.description || '',
    payload: template.payload
  }
  showAddDialog.value = true
}

const copyTemplate = async (template: PayloadTemplateDTO) => {
  try {
    const newTemplate = await PayloadTemplateAPI.copyTemplate(template.id)
    templates.value.push(newTemplate)
    ElMessage.success('模板复制成功')
  } catch (error) {
    ElMessage.error('模板复制失败')
  }
}

const exportTemplate = (template: PayloadTemplateDTO) => {
  const dataStr = JSON.stringify(template, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${template.name}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('模板导出成功')
}

const deleteTemplate = async (template: PayloadTemplateDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板 "${template.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await PayloadTemplateAPI.deleteTemplate(template.id)
    const index = templates.value.findIndex(t => t.id === template.id)
    if (index > -1) {
      templates.value.splice(index, 1)
    }
    ElMessage.success('模板删除成功')
    
    // 刷新统计信息
    const stats = await PayloadTemplateAPI.getStatistics()
    statistics.value = stats
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('模板删除失败')
    }
  }
}

const saveTemplate = async () => {
  if (!templateFormRef.value) return
  
  try {
    await templateFormRef.value.validate()
    saving.value = true
    
    if (editingTemplate.value) {
      // 更新模板
      const updateRequest: PayloadTemplateUpdateRequest = {
        name: templateForm.value.name,
        groupId: templateForm.value.groupId,
        type: templateForm.value.type,
        description: templateForm.value.description,
        payload: templateForm.value.payload
      }
      const updatedTemplate = await PayloadTemplateAPI.updateTemplate(editingTemplate.value.id, updateRequest)
      const index = templates.value.findIndex(t => t.id === editingTemplate.value!.id)
      if (index > -1) {
        templates.value[index] = updatedTemplate
      }
      ElMessage.success('模板更新成功')
    } else {
      // 创建模板
      const newTemplate = await PayloadTemplateAPI.createTemplate(templateForm.value)
      templates.value.push(newTemplate)
      ElMessage.success('模板创建成功')
    }
    
    showAddDialog.value = false
    resetForm()
    
    // 刷新统计信息
    const stats = await PayloadTemplateAPI.getStatistics()
    statistics.value = stats
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const useTemplate = async (template: PayloadTemplateDTO) => {
  try {
    await PayloadTemplateAPI.useTemplate(template.id)
    template.usageCount = (template.usageCount || 0) + 1
    template.lastUsed = new Date().toISOString()
    ElMessage.success('模板使用成功')
    showDetailDialog.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetForm = () => {
  templateForm.value = {
    name: '',
    groupId: 0,
    type: 'JSON',
    description: '',
    payload: ''
  }
  editingTemplate.value = null
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.payload-template {
  padding: 24px;
  background-color: transparent;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header.transparent {
  background: transparent;
  box-shadow: none;
}

.header-left h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-title {
  color: #606266;
  font-size: 14px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-icon.templates {
  background: #e1f3ff;
  color: #409eff;
}

.stat-icon.groups {
  background: #f0f9ff;
  color: #67c23a;
}

.stat-icon.recent {
  background: #fef0e6;
  color: #e6a23c;
}

.stat-icon.favorites {
  background: #fde2e2;
  color: #f56c6c;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-section.transparent {
  background: transparent;
  box-shadow: none;
}

.filter-left {
  display: flex;
  align-items: center;
}

.template-content {
  background: transparent;
  border-radius: 8px;
  box-shadow: none;
  min-height: 400px;
}

.card-view {
  padding: 24px;
}

.group-section {
  margin-bottom: 32px;
}

.group-header {
  margin-bottom: 16px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.template-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.template-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.template-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.card-header {
  margin-bottom: 12px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title span {
  font-weight: 600;
  color: #303133;
}

.card-actions {
  display: flex;
  gap: 4px;
}

.card-actions .is-favorite {
  color: #f56c6c;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.update-time {
  font-size: 12px;
  color: #909399;
}

.card-content {
  margin-bottom: 12px;
}

.template-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.payload-preview {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  max-height: 80px;
  overflow: hidden;
}

.payload-preview pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #606266;
}

.card-footer {
  border-top: 1px solid #e4e7ed;
  padding-top: 12px;
}

.usage-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.usage-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.list-view {
  padding: 24px;
  background: transparent;
}

.list-view :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e4e7ed;
}

.list-view :deep(.el-table__header-wrapper) {
  border-radius: 12px 12px 0 0;
}

.list-view :deep(.el-table__body-wrapper) {
  border-radius: 0 0 12px 12px;
}

.template-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.favorite-icon {
  color: #f56c6c;
}

.template-detail {
  padding: 16px 0;
}

.detail-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-name {
  color: #606266;
  font-size: 14px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.detail-section p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.payload-content {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #303133;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
}

.usage-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #606266;
  font-size: 14px;
}

.usage-info span {
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .payload-template {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-left {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-left .el-input,
  .filter-left .el-select {
    width: 100% !important;
    margin-left: 0 !important;
  }
  
  .template-cards {
    grid-template-columns: 1fr;
  }
  
  .usage-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style>