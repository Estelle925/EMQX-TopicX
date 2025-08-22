<template>
  <div class="topic-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button 
          type="text" 
          :icon="ArrowLeft" 
          @click="goBack"
          class="back-button"
        >
          返回
        </el-button>
        <div class="header-info">
          <h2>{{ topicInfo.name }}</h2>
          <p class="page-description">Topic详细信息和标签管理</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button 
          :icon="Refresh" 
          @click="refreshData"
          :loading="loading"
        >
          刷新数据
        </el-button>
        <el-button 
          type="primary" 
          :icon="Edit"
          @click="editTopic"
        >
          编辑Topic
        </el-button>
      </div>
    </div>

    <!-- 基本信息和标签管理 -->
    <div class="info-tags-row">
      <!-- 基本信息卡片 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span class="table-title">基本信息</span>
            </div>
          </div>
        </template>
        
        <div class="info-grid">
          <div class="info-item">
            <label>Topic名称</label>
            <div class="info-value">{{ topicInfo.name }}</div>
          </div>
          <div class="info-item">
            <label>Topic路径</label>
            <div class="info-value topic-path">{{ topicInfo.path }}</div>
          </div>
          <div class="info-item">
            <label>所属分组</label>
            <div class="info-value">
              <el-tag v-if="topicInfo.groupName" type="info" size="small">
                {{ topicInfo.groupName }}
              </el-tag>
              <span v-else class="no-group">未分组</span>
            </div>
          </div>
          <div class="info-item">
            <label>创建时间</label>
            <div class="info-value">{{ formatTime(topicInfo.createdAt) }}</div>
          </div>
          <div class="info-item">
            <label>最后更新</label>
            <div class="info-value">{{ formatTime(topicInfo.updatedAt) }}</div>
          </div>
        </div>
      </el-card>

      <!-- 标签管理卡片 -->
      <el-card class="tags-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><PriceTag /></el-icon>
            <span class="table-title">标签管理</span>
            <el-tag size="small" type="info">{{ topicTags.length }}</el-tag>
          </div>
          <div class="header-actions">
            <el-button 
              type="primary" 
              size="small" 
              :icon="Plus"
              @click="showAddTagDialog = true"
            >
              添加标签
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="tags-content">
        <div v-if="topicTags.length > 0" class="tags-list">
          <div 
            v-for="tag in topicTags" 
            :key="tag.id" 
            class="tag-item"
          >
            <el-tag 
              :color="tag.color" 
              :style="{ color: getTextColor(tag.color) }"
              size="default"
            >
              {{ tag.name }}
            </el-tag>
            <div class="tag-actions">
              <el-button 
                type="text" 
                size="small" 
                :icon="Delete"
                @click="removeTag(tag.id)"
                class="delete-btn"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tags">
          <el-empty description="暂无标签" :image-size="80" />
        </div>
      </div>
    </el-card>
    </div>

    <!-- Payload说明文档卡片 -->
    <el-card class="payload-doc-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Document /></el-icon>
            <span class="table-title">Payload说明文档</span>
          </div>
          <div class="header-actions">
            <el-button 
              v-if="!isEditingDoc" 
              type="primary" 
              size="small" 
              :icon="Edit"
              @click="startEditDoc"
            >
              编辑文档
            </el-button>
            <div v-else class="edit-actions">
              <el-button 
                size="small" 
                @click="cancelEditDoc"
              >
                取消
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                :icon="Check"
                @click="saveDoc"
                :loading="savingDoc"
              >
                保存
              </el-button>
            </div>
          </div>
        </div>
      </template>
      
      <div class="payload-doc-content">
        <div v-if="!isEditingDoc" class="doc-display">
          <div v-if="payloadDoc.trim()" class="doc-text" v-html="formatDocText(payloadDoc)"></div>
          <div v-else class="empty-doc">
            <el-empty description="暂无Payload说明文档" :image-size="80" />
            <el-button type="primary" :icon="Edit" @click="startEditDoc">添加说明文档</el-button>
          </div>
        </div>
        <div v-else class="doc-editor">
          <el-input
            v-model="editingDocContent"
            type="textarea"
            :rows="12"
            placeholder="请输入Topic的Payload说明文档，支持Markdown格式..."
            resize="vertical"
          />
          <div class="editor-tips">
            <el-text size="small" type="info">
              <el-icon><InfoFilled /></el-icon>
              支持Markdown格式，可以使用 **粗体**、*斜体*、`代码`、链接等格式
            </el-text>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加标签对话框 -->
    <el-dialog
      v-model="showAddTagDialog"
      title="添加标签"
      width="500px"
      :before-close="handleCloseAddTagDialog"
    >
      <div class="available-tags">
        <h4>选择已有标签</h4>
        <div class="tags-grid">
          <div 
            v-for="tag in availableTags" 
            :key="tag.id"
            class="available-tag"
            :class="{ selected: selectedTagIds.includes(tag.id) }"
            @click="toggleTagSelection(tag.id)"
          >
            <el-tag 
              :color="tag.color" 
              :style="{ color: getTextColor(tag.color) }"
              size="default"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddTagDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="addSelectedTags"
            :disabled="selectedTagIds.length === 0"
            :loading="saving"
          >
            添加标签
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Refresh,
  Edit,
  InfoFilled,
  PriceTag,
  Plus,
  Delete,
  Document,
  Check
} from '@element-plus/icons-vue'
import { TopicAPI, type TopicDTO, type TopicUpdateRequest } from '../api/topic'
import { TagAPI, type TagDTO } from '../api/tag'

interface TopicInfo {
  id: number
  name: string
  path: string
  status: 'enabled' | 'disabled'
  groupId?: number
  groupName?: string
  createdAt: string
  updatedAt: string
}



const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddTagDialog = ref(false)
const selectedTagIds = ref<number[]>([])
const isEditingDoc = ref(false)
const savingDoc = ref(false)
const payloadDoc = ref('')
const editingDocContent = ref('')

// Topic信息
const topicInfo = reactive<TopicInfo>({
  id: 0,
  name: '',
  path: '',
  status: 'enabled',
  groupId: undefined,
  groupName: '',
  createdAt: '',
  updatedAt: ''
})

// Topic标签
const topicTags = ref<TagDTO[]>([])

// 可用标签
const availableTags = ref<TagDTO[]>([])



// 方法
const goBack = () => {
  router.back()
}

// Payload文档相关方法
const startEditDoc = () => {
  editingDocContent.value = payloadDoc.value
  isEditingDoc.value = true
}

const cancelEditDoc = () => {
  editingDocContent.value = ''
  isEditingDoc.value = false
}

const saveDoc = async () => {
  try {
    savingDoc.value = true
    const topicId = Number(route.params.id)
    const updateData: TopicUpdateRequest = {
      name: topicInfo.name,
      path: topicInfo.path,
      groupId: topicInfo.groupId,
      payloadDoc: editingDocContent.value
    }
    
    await TopicAPI.updateTopic(topicId, updateData)
    
    payloadDoc.value = editingDocContent.value
    isEditingDoc.value = false
    ElMessage.success('Payload说明文档保存成功')
  } catch (error) {
    console.error('保存文档失败:', error)
    ElMessage.error('保存文档失败，请重试')
  } finally {
    savingDoc.value = false
  }
}

const formatDocText = (text: string) => {
  if (!text) return ''
  
  // 简单的Markdown格式化
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // 粗体
    .replace(/\*(.*?)\*/g, '<em>$1</em>') // 斜体
    .replace(/`(.*?)`/g, '<code>$1</code>') // 行内代码
    .replace(/\n/g, '<br>') // 换行
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadTopicInfo(),
      loadTopicTags(),
      loadTopicStats()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const editTopic = () => {
  // TODO: 实现编辑Topic功能
  ElMessage.info('编辑功能待实现')
}

const loadTopicInfo = async () => {
  try {
    const topicId = Number(route.params.id)
    const topic = await TopicAPI.getTopicById(topicId)
    Object.assign(topicInfo, {
      id: topic.id,
      name: topic.name,
      path: topic.path,
      status: topic.status,
      groupId: topic.groupId,
      groupName: topic.groupName,
      createdAt: topic.createdAt,
      updatedAt: topic.updatedAt
    })
    
    // 加载payload文档
    payloadDoc.value = topic.payloadDoc || ''
  } catch (error) {
    console.error('加载Topic信息失败:', error)
    ElMessage.error('加载Topic信息失败')
  }
}

const loadTopicTags = async () => {
  try {
    const topicId = Number(route.params.id)
    topicTags.value = await TopicAPI.getTopicTags(topicId)
  } catch (error) {
    console.error('加载Topic标签失败:', error)
    ElMessage.error('加载Topic标签失败')
    topicTags.value = []
  }
}

const loadAvailableTags = async () => {
  try {
    const allTags = await TagAPI.getAllTags()
    // 过滤掉已经添加的标签
    availableTags.value = allTags.filter(tag => 
      !topicTags.value.some(topicTag => topicTag.id === tag.id)
    )
  } catch (error) {
    console.error('加载可用标签失败:', error)
    ElMessage.error('加载可用标签失败')
    availableTags.value = []
  }
}

const loadTopicStats = async () => {
  // TODO: 从API加载统计信息
  // 模拟数据
  await new Promise(resolve => setTimeout(resolve, 400))
  
  // 模拟加载payload文档
  payloadDoc.value = '# Payload说明\n\n这个Topic用于传输**传感器数据**，包含以下字段：\n\n- `temperature`: 温度值（摄氏度）\n- `humidity`: 湿度值（百分比）\n- `timestamp`: 时间戳\n\n示例数据：\n```json\n{\n  "temperature": 25.6,\n  "humidity": 60.2,\n  "timestamp": 1642234567890\n}\n```'
}

const toggleTagSelection = (tagId: number) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value.push(tagId)
  }
}

const addSelectedTags = async () => {
  if (selectedTagIds.value.length === 0) return
  
  saving.value = true
  try {
    const topicId = Number(route.params.id)
    await TopicAPI.addTopicTags(topicId, selectedTagIds.value)
    
    // 重新加载标签数据
    await loadTopicTags()
    await loadAvailableTags()
    
    ElMessage.success(`成功添加 ${selectedTagIds.value.length} 个标签`)
    showAddTagDialog.value = false
    selectedTagIds.value = []
  } catch (error) {
    console.error('添加标签失败:', error)
    ElMessage.error('添加标签失败')
  } finally {
    saving.value = false
  }
}

const removeTag = async (tagId: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要移除这个标签吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const topicId = Number(route.params.id)
    await TopicAPI.removeTopicTags(topicId, [tagId])
    
    // 重新加载标签数据
    await loadTopicTags()
    await loadAvailableTags()
    
    ElMessage.success('标签移除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除标签失败:', error)
      ElMessage.error('移除标签失败')
    }
  }
}

const handleCloseAddTagDialog = () => {
  selectedTagIds.value = []
  showAddTagDialog.value = false
}



const formatTime = (time: string): string => {
  return new Date(time).toLocaleString('zh-CN')
}

const getTextColor = (backgroundColor: string): string => {
  // 简单的颜色对比度计算
  const hex = backgroundColor.replace('#', '')
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)
  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 128 ? '#000000' : '#ffffff'
}

// 生命周期
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadTopicInfo(),
      loadTopicTags(),
      loadAvailableTags(),
      loadTopicStats()
    ])
  } catch (error) {
    ElMessage.error('页面加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.topic-detail {
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  padding: 8px 12px;
  color: #6b7280;
  font-weight: 500;
}

.back-button:hover {
  color: #10b981;
  background: #ecfdf5;
}

.header-info h2 {
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

/* 基本信息和标签行布局 */
.info-tags-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

@media (max-width: 1024px) {
  .info-tags-row {
    flex-direction: column;
  }
}

/* 卡片样式 */
.info-card {
  background: white;
  border-radius: 8px;
  margin-bottom: 0;
  overflow: hidden;
  flex: 2;
}

.tags-card {
  background: white;
  border-radius: 8px;
  margin-bottom: 0;
  overflow: hidden;
  flex: 1;
}

.payload-doc-card {
  background: white;
  border-radius: 8px;
  margin-bottom: 24px;
  overflow: hidden;
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

.header-icon {
  color: #10b981;
  font-size: 18px;
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

/* 基本信息样式 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  padding: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.topic-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f3f4f6;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
}

.no-group {
  color: #9ca3af;
  font-style: italic;
}

/* 标签管理样式 */
.tags-content {
  padding: 24px;
}

.tags-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.tag-actions {
  display: flex;
  gap: 8px;
}

.delete-btn {
  color: #ef4444;
}

.delete-btn:hover {
  background: #fef2f2;
}

.empty-tags {
  text-align: center;
  padding: 40px 0;
}

/* Payload文档样式 */
.payload-doc-card {
  margin-bottom: 24px;
}

.payload-doc-content {
  min-height: 200px;
}

.doc-display {
  padding: 16px;
}

.doc-text {
  line-height: 1.6;
  color: #374151;
  font-size: 14px;
}

.doc-text strong {
  color: #1f2937;
  font-weight: 600;
}

.doc-text em {
  color: #6b7280;
  font-style: italic;
}

.doc-text code {
  background: #f3f4f6;
  color: #dc2626;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.empty-doc {
  text-align: center;
  padding: 40px 20px;
}

.doc-editor {
  padding: 16px;
}

.editor-tips {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

/* 对话框样式 */
.available-tags h4 {
  margin: 0 0 16px 0;
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.available-tag {
  padding: 8px;
  border: 2px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.available-tag:hover {
  border-color: #10b981;
  background: #ecfdf5;
}

.available-tag.selected {
  border-color: #10b981;
  background: #d1fae5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-card__header) {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

:deep(.el-card__body) {
  padding: 0;
}

:deep(.el-tag) {
  border: none;
  font-weight: 500;
}

:deep(.el-button--text) {
  padding: 6px 8px;
}

:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
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
  
  .tags-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>