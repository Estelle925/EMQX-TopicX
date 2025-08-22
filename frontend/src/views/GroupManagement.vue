<template>
  <div class="group-management">
    <div class="page-header">
      <div class="header-left">
        <h2>分组管理</h2>
        <p class="page-description">管理Topic分组和标签，优化数据组织结构</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          创建分组
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">总分组数</span>
          <div class="stat-icon groups">
            <el-icon><FolderOpened /></el-icon>
          </div>
        </div>
        <div class="stat-value">{{ groups.length }}</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">总标签数</span>
          <div class="stat-icon tags">
            <el-icon><PriceTag /></el-icon>
          </div>
        </div>
        <div class="stat-value">{{ tags.length }}</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">已分组Topic</span>
          <div class="stat-icon topics">
            <el-icon><Document /></el-icon>
          </div>
        </div>
        <div class="stat-value">{{ totalGroupedTopics }}</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">平均每组Topic</span>
          <div class="stat-icon average">
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
        <div class="stat-value">{{ averageTopicsPerGroup }}</div>
      </div>
    </div>
    
    <!-- 分组列表 -->
    <el-row :gutter="24">
      <el-col :span="16">
        <el-card class="groups-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="table-title">分组列表</span>
                <el-tag size="small" type="info">{{ groups.length }} 个分组</el-tag>
              </div>
              <div class="header-actions">
                <el-input
                  v-model="groupSearchKeyword"
                  placeholder="搜索分组..."
                  size="small"
                  style="width: 200px;"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>
          </template>
          
          <el-table 
            :data="filteredGroups" 
            v-loading="loading"
            stripe
            :header-cell-style="{ background: '#f9fafb', color: '#374151' }"
          >
            <el-table-column label="分组信息" min-width="200">
              <template #default="{ row }">
                <div class="group-info">
                  <div class="group-name">{{ row.name }}</div>
                  <div class="group-description" v-if="row.description">{{ row.description }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="Topic数量" min-width="120" align="center" sortable prop="topicCount">
              <template #default="{ row }">
                <el-tag size="small" :type="getTopicCountType(row.topicCount)">
                  {{ row.topicCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="120" sortable prop="createdAt">
              <template #default="{ row }">
                <div class="time-text">{{ formatDate(row.createdAt) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="160" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button size="small" type="primary" @click="editGroup(row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteGroup(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="tags-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="table-title">标签管理</span>
                <el-tag size="small" type="info">{{ tags.length }} 个标签</el-tag>
              </div>
              <div class="header-actions">
                <el-button size="small" type="primary" @click="showTagDialog = true">
                  <el-icon><Plus /></el-icon>
                  添加标签
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="tag-search">
            <el-input
              v-model="tagSearchKeyword"
              placeholder="搜索标签..."
              size="small"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="tag-list">
            <div
              v-for="tag in filteredTags"
              :key="tag.id"
              class="tag-item"
            >
              <div class="tag-display-wrapper">
                <el-tag
                  :color="tag.color"
                  size="large"
                  class="tag-display"
                >
                  {{ tag.name }}
                </el-tag>
                <div class="tag-usage">使用次数: {{ getTagUsageCount(tag.id) }}</div>
              </div>
              <div class="tag-actions">
                <el-button size="small" type="primary" @click="editTag(tag)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" type="danger" @click="deleteTag(tag)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div v-if="filteredTags.length === 0" class="empty-tags">
              <el-empty description="暂无标签" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 添加/编辑分组对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingGroup ? '编辑分组' : '创建分组'"
      width="400px"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupRules"
        label-width="80px"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="请输入分组名称" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="groupForm.description"
            type="textarea"
            placeholder="请输入分组描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGroup" :loading="saving">
          {{ editingGroup ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 添加/编辑标签对话框 -->
    <el-dialog
      v-model="showTagDialog"
      :title="editingTag ? '编辑标签' : '添加标签'"
      width="400px"
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="tagRules"
        label-width="80px"
      >
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        
        <el-form-item label="标签颜色" prop="color">
          <div class="color-picker-wrapper">
            <el-color-picker v-model="tagForm.color" />
            <div class="color-presets">
              <div
                v-for="color in colorPresets"
                :key="color"
                class="color-preset"
                :style="{ backgroundColor: color }"
                @click="tagForm.color = color"
              />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="预览">
          <el-tag :color="tagForm.color" size="large">
            {{ tagForm.name || '标签预览' }}
          </el-tag>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTag" :loading="saving">
          {{ editingTag ? '更新' : '添加' }}
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
  FolderOpened, 
  PriceTag, 
  Document, 
  TrendCharts,
  Search,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { GroupAPI, type GroupDTO, type GroupCreateRequest, type GroupUpdateRequest } from '../api/group'
import { TagAPI, type TagDTO, type TagCreateRequest, type TagUpdateRequest } from '../api/tag'

// 使用从API导入的类型
type Group = GroupDTO
type Tag = TagDTO

const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showTagDialog = ref(false)
const editingGroup = ref<Group | null>(null)
const editingTag = ref<Tag | null>(null)
const groupFormRef = ref<FormInstance>()
const tagFormRef = ref<FormInstance>()
const groupSearchKeyword = ref('')
const tagSearchKeyword = ref('')

const groups = ref<Group[]>([])
const tags = ref<Tag[]>([])

const groupForm = reactive({
  name: '',
  description: ''
})

const tagForm = reactive({
  name: '',
  color: '#409EFF'
})

const colorPresets = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
  '#909399', '#C71585', '#FF6347', '#32CD32',
  '#1E90FF', '#FF69B4', '#8A2BE2', '#00CED1'
]

// 计算属性
const totalGroupedTopics = computed(() => {
  return groups.value.reduce((total, group) => total + group.topicCount, 0)
})

const averageTopicsPerGroup = computed(() => {
  if (groups.value.length === 0) return 0
  return Math.round(totalGroupedTopics.value / groups.value.length)
})

const filteredGroups = computed(() => {
  if (!groupSearchKeyword.value) return groups.value
  return groups.value.filter(group => 
    group.name.toLowerCase().includes(groupSearchKeyword.value.toLowerCase()) ||
    (group.description && group.description.toLowerCase().includes(groupSearchKeyword.value.toLowerCase()))
  )
})

const filteredTags = computed(() => {
  if (!tagSearchKeyword.value) return tags.value
  return tags.value.filter(tag => 
    tag.name.toLowerCase().includes(tagSearchKeyword.value.toLowerCase())
  )
})

const groupRules: FormRules = {
  name: [
    { required: true, message: '请输入分组名称', trigger: 'blur' }
  ]
}

const tagRules: FormRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择标签颜色', trigger: 'blur' }
  ]
}

const loadGroups = async () => {
  loading.value = true
  try {
    groups.value = await GroupAPI.getAllGroups()
  } catch (error) {
    console.error('加载分组列表失败:', error)
    ElMessage.error('加载分组列表失败')
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    tags.value = await TagAPI.getAllTags()
  } catch (error) {
    console.error('加载标签列表失败:', error)
    ElMessage.error('加载标签列表失败')
  }
}

const editGroup = (group: Group) => {
  editingGroup.value = group
  Object.assign(groupForm, {
    name: group.name,
    description: group.description || ''
  })
  showAddDialog.value = true
}

const deleteGroup = async (group: Group) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分组 "${group.name}" 吗？删除后该分组下的Topic将变为未分组状态。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (group.id) {
      await GroupAPI.deleteGroup(group.id)
      ElMessage.success('删除成功')
      loadGroups()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除分组失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const editTag = (tag: Tag) => {
  editingTag.value = tag
  Object.assign(tagForm, {
    name: tag.name,
    color: tag.color
  })
  showTagDialog.value = true
}

const deleteTag = async (tag: Tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签 "${tag.name}" 吗？删除后相关Topic的标签关联将被移除。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (tag.id) {
      await TagAPI.deleteTag(tag.id)
      ElMessage.success('删除成功')
      loadTags()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除标签失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const saveGroup = async () => {
  if (!groupFormRef.value) return
  
  try {
    await groupFormRef.value.validate()
    saving.value = true
    
    if (editingGroup.value && editingGroup.value.id) {
      // 更新分组
      const updateRequest: GroupUpdateRequest = {
        name: groupForm.name,
        description: groupForm.description
      }
      await GroupAPI.updateGroup(editingGroup.value.id, updateRequest)
      ElMessage.success('更新成功')
    } else {
      // 创建分组
      const createRequest: GroupCreateRequest = {
        name: groupForm.name,
        description: groupForm.description
      }
      await GroupAPI.createGroup(createRequest)
      ElMessage.success('创建成功')
    }
    
    showAddDialog.value = false
    resetGroupForm()
    loadGroups()
  } catch (error) {
    console.error('保存分组失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const saveTag = async () => {
  if (!tagFormRef.value) return
  
  try {
    await tagFormRef.value.validate()
    saving.value = true
    
    if (editingTag.value && editingTag.value.id) {
      // 更新标签
      const updateRequest: TagUpdateRequest = {
        name: tagForm.name,
        color: tagForm.color
      }
      await TagAPI.updateTag(editingTag.value.id, updateRequest)
      ElMessage.success('更新成功')
    } else {
      // 创建标签
      const createRequest: TagCreateRequest = {
        name: tagForm.name,
        color: tagForm.color
      }
      await TagAPI.createTag(createRequest)
      ElMessage.success('添加成功')
    }
    
    showTagDialog.value = false
    resetTagForm()
    loadTags()
  } catch (error) {
    console.error('保存标签失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetGroupForm = () => {
  editingGroup.value = null
  Object.assign(groupForm, {
    name: '',
    description: ''
  })
  groupFormRef.value?.resetFields()
}

const resetTagForm = () => {
  editingTag.value = null
  Object.assign(tagForm, {
    name: '',
    color: '#409EFF'
  })
  tagFormRef.value?.resetFields()
}

// 新增方法
const refreshData = async () => {
  await Promise.all([loadGroups(), loadTags()])
  ElMessage.success('数据刷新成功')
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

const getTopicCountType = (count: number) => {
  if (count >= 10) return 'success'
  if (count >= 5) return 'warning'
  return 'info'
}

const getTagUsageCount = (tagId: number | undefined) => {
  // 从标签对象中获取使用次数，如果没有则返回0
  const tag = tags.value.find(t => t.id === tagId)
  return tag?.usageCount || 0
}

onMounted(() => {
  loadGroups()
  loadTags()
})
</script>

<style scoped>


.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: transparent;
  border-radius: 8px;
}

.header-left h2 {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  color: #6b7280;
  font-size: 14px;
  margin-top: 4px;
  margin-bottom: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions .el-button {
  border-radius: 6px;
  font-weight: 500;
}

/* CSS变量定义 */
.group-management {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --success-gradient: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  --warning-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --info-gradient: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-muted: #9ca3af;
  --border-color: #e5e7eb;
  --bg-glass: rgba(255, 255, 255, 0.95);
  --bg-hover: rgba(255, 255, 255, 0.98);
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  
  padding: 0;
  background: transparent;
  min-height: 100vh;
}

/* 统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  background: transparent;
}

.stat-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: white;
  transition: none !important;
  position: relative;
  overflow: hidden;
  padding: var(--spacing-lg);
}

.stat-card:hover {
  box-shadow: none !important;
  transform: none !important;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-title {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  margin: var(--spacing-xs);
  font-size: 28px;
  position: relative;
  transition: all 0.3s ease;
}

.stat-icon.groups {
  background: v-bind('cardIconColors.group.background');
  box-shadow: v-bind('cardIconColors.group.shadow');
}

.stat-icon.tags {
  background: v-bind('cardIconColors.tag.background');
  box-shadow: v-bind('cardIconColors.tag.shadow');
}

.stat-icon.topics {
  background: v-bind('cardIconColors.topic.background');
  box-shadow: v-bind('cardIconColors.topic.shadow');
}

.stat-icon.average {
  background: v-bind('cardIconColors.average.background');
  box-shadow: v-bind('cardIconColors.average.shadow');
}

.stat-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: var(--spacing-xs);
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.stat-title {
  font-size: 14px;
  color: var(--text-muted);
  font-weight: 500;
  line-height: 1;
}

/* 卡片样式 */
.groups-card,
.tags-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-glass);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  transition: none !important;
}

.groups-card:hover,
.tags-card:hover {
  box-shadow: none !important;
  transform: none !important;
}



.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions .el-button {
  border-radius: 6px;
  font-weight: 500;
}

/* 分组信息样式 */
.group-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.group-description {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.time-text {
  font-size: 12px;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

/* 标签相关样式 */
.tag-search {
  padding: 16px 20px 0;
  margin-bottom: 16px;
}

.tag-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 0 20px 20px;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.tag-item:last-child {
}

.tag-display-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.tag-display {
  align-self: flex-start;
}

.tag-usage {
  font-size: 11px;
  color: #9ca3af;
}

.tag-actions {
  display: flex;
  gap: 6px;
}

.empty-tags {
  text-align: center;
  padding: 40px 0;
}

/* 对话框样式 */
.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-presets {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-preset {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.color-preset:hover {
  transform: none !important;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-card__header) {
  padding: var(--spacing-md) var(--spacing-lg);
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  font-weight: 700;
  color: var(--text-primary);
  font-size: 16px;
}

:deep(.el-card__body) {
  padding: 0;
  background: transparent;
}

:deep(.el-table) {
  border-radius: 0;
  border: none;
  background: transparent;
}

:deep(.el-table__inner-wrapper) {
  border: none;
}

:deep(.el-table--border) {
  border: none;
}

:deep(.el-table--border th) {
  border-right: none;
}

:deep(.el-table--border td) {
  border-right: none;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  color: var(--text-primary);
  font-weight: 700;
  font-size: 14px;
  padding: var(--spacing-lg) var(--spacing-md);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  position: relative;
}





:deep(.el-table td) {
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  padding: var(--spacing-lg) var(--spacing-md);
  color: #475569;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafbfc;
}

:deep(.el-table__body tr:hover > td) {
  background: transparent !important;
  transform: none !important;
}

:deep(.el-table tr:last-child td) {
  border-bottom: none;
}

:deep(.el-button) {
  border: none;
  border-radius: var(--radius-sm);
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
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 12px;
  border-radius: var(--radius-sm);
  border: none;
}

:deep(.el-tag--small) {
  height: 20px;
  line-height: 18px;
  font-size: 11px;
  border-radius: var(--radius-sm);
  border: none;
}

:deep(.el-tag) {
  border: none;
  border-radius: var(--radius-sm);
  font-weight: 500;
}

:deep(.el-input) {
  border-radius: var(--radius-sm);
}

:deep(.el-input__wrapper) {
  border: none;
  box-shadow: none;
  border-radius: var(--radius-sm);
}

:deep(.el-input__inner) {
  border: none;
}

:deep(.el-select) {
  border: none;
}

:deep(.el-select .el-input__wrapper) {
  border: none;
  box-shadow: none;
}

:deep(.el-dialog) {
  border-radius: var(--radius-md);
  border: none;
}

:deep(.el-card) {
  /* 保持默认边框样式，让分组列表和标签管理卡片显示边框 */
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
}

:deep(.el-color-picker__trigger) {
  border-radius: var(--radius-sm);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .tag-actions {
    flex-direction: column;
    gap: 4px;
  }
}
</style>