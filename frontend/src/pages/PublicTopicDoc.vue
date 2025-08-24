<template>
  <div class="public-doc-container">
    <div class="public-doc-background">
      <div class="public-doc-card">
        <!-- 头部信息 -->
        <div class="public-doc-header">
          <div class="logo-section">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" width="40" height="40">
                <path fill="#00D4AA" d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
            <h1>EMQX Topic Hub</h1>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-section">
            <el-skeleton :rows="3" animated />
          </div>
          
          <!-- 错误状态 -->
          <div v-else-if="error" class="error-section">
            <div class="error-icon">⚠️</div>
            <div class="error-message">
              <h2>加载失败</h2>
              <p>{{ error }}</p>
            </div>
          </div>
          
          <!-- Topic信息 -->
          <div v-else class="topic-section">
            <div class="topic-title">
              <h2>{{ topicInfo.name || 'Topic文档' }}</h2>
              <div class="topic-meta">
                <div class="topic-path">
                  <span class="label">路径:</span>
                  <code>{{ topicInfo.path }}</code>
                </div>
                <div class="topic-id">
                  <span class="label">ID:</span>
                  <span class="value">{{ topicInfo.id }}</span>
                </div>
                <div class="topic-updated" v-if="topicInfo.updatedAt">
                  <span class="label">更新时间:</span>
                  <span class="value">{{ formatTime(topicInfo.updatedAt) }}</span>
                </div>
                <div class="topic-tags" v-if="topicInfo.tags && topicInfo.tags.length > 0">
                  <span class="label">标签:</span>
                  <div class="tags-list">
                    <el-tag v-for="tag in topicInfo.tags" :key="tag.id" size="small" type="info">
                      {{ tag.name }}
                    </el-tag>
                  </div>
                </div>
                <div class="topic-group" v-if="topicInfo.groupName">
                  <span class="label">分组:</span>
                  <span class="value">{{ topicInfo.groupName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 文档内容 -->
        <div class="doc-content-section" v-if="!loading && !error">
          <!-- 有文档内容 -->
          <div v-if="topicInfo.payloadDoc" class="doc-display">
            <div class="doc-title">
              <h3>📄 Payload说明文档</h3>
            </div>
            <div class="doc-text" v-html="formatDocText(topicInfo.payloadDoc)"></div>
          </div>
          
          <!-- 无文档内容 -->
          <div v-else class="empty-doc">
            <div class="empty-icon">📝</div>
            <h3>暂无文档</h3>
            <p>该Topic暂未提供Payload说明文档</p>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="doc-actions" v-if="!loading && !error">
          <el-button type="primary" size="large" class="action-button" @click="copyLink">
            <el-icon><Link /></el-icon>
            复制链接
          </el-button>
          <el-button size="large" class="action-button secondary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
        
        <!-- 重新加载按钮 -->
        <div class="doc-actions" v-if="error">
          <el-button type="primary" size="large" class="action-button" @click="loadTopicDoc">
            <el-icon><Refresh /></el-icon>
            重新加载
          </el-button>
        </div>
        
        <!-- 页脚 -->
        <div class="public-doc-footer">
          <p>EMQX Topic Hub - 公开文档分享</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link, ArrowLeft, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

interface TagInfo {
  id: number
  name: string
}

interface TopicInfo {
  id: number
  name: string
  path: string
  payloadDoc?: string
  updatedAt: string
  createdAt?: string
  lastActivity?: string
  tags?: TagInfo[]
  groupName?: string
}

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')

// Topic信息
const topicInfo = reactive<TopicInfo>({
  id: 0,
  name: '',
  path: '',
  payloadDoc: '',
  updatedAt: '',
  createdAt: '',
  lastActivity: '',
  tags: [],
  groupName: ''
})

// 加载Topic文档
const loadTopicDoc = async () => {
  const topicId = route.params.id
  if (!topicId) {
    error.value = 'Topic ID参数缺失'
    return
  }

  loading.value = true
  error.value = ''
  
  try {
    // 调用公开API获取Topic文档
    const response = await axios.get(`/api/topics/${topicId}/public-doc`)
    const topic = response.data.data || response.data
    
    Object.assign(topicInfo, {
      id: topic.id,
      name: topic.name,
      path: topic.path,
      payloadDoc: topic.payloadDoc || '',
      updatedAt: topic.updatedAt,
      createdAt: topic.createdAt,
      lastActivity: topic.lastActivity,
      tags: topic.tags || [],
      groupName: topic.groupName || ''
    })
  } catch (err: any) {
    console.error('加载Topic文档失败:', err)
    if (err.response?.status === 404) {
      error.value = 'Topic不存在或已被删除'
    } else {
      error.value = '加载文档失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

// 格式化文档文本
const formatDocText = (text: string) => {
  if (!text) return ''
  
  // 简单的Markdown格式化
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // 粗体
    .replace(/\*(.*?)\*/g, '<em>$1</em>') // 斜体
    .replace(/`(.*?)`/g, '<code>$1</code>') // 行内代码
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>') // 代码块
    .replace(/\n/g, '<br>') // 换行
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>') // 链接
}

// 格式化时间
const formatTime = (time: string): string => {
  return new Date(time).toLocaleString('zh-CN')
}

// 复制链接
const copyLink = async () => {
  try {
    const url = window.location.href
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  } catch (err) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = window.location.href
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('链接已复制到剪贴板')
  }
}

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/dashboard')
  }
}

// 生命周期
onMounted(() => {
  loadTopicDoc()
})
</script>

<style scoped>
.public-doc-container {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  position: relative;
  padding: var(--spacing-lg) 0;
}

.public-doc-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  min-height: 100%;
  background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-sidebar) 50%, var(--gray-800) 100%);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: var(--spacing-lg) 0;
}

.public-doc-background::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(0, 212, 170, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(99, 102, 241, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(0, 212, 170, 0.08) 0%, transparent 50%);
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-10px) rotate(1deg); }
  66% { transform: translateY(5px) rotate(-1deg); }
}

.public-doc-card {
  width: 95%;
  max-width: 1000px;
  padding: var(--spacing-2xl);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-2xl);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  text-align: center;
  margin: var(--spacing-lg) 0;
}

.public-doc-header {
  margin-bottom: var(--spacing-xl);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-lg);
}

.logo-icon {
  margin-right: var(--spacing-md);
}

.public-doc-header h1 {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.loading-section {
  margin: var(--spacing-xl) 0;
}

.error-section {
  margin: var(--spacing-xl) 0;
}

.error-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
}

.error-message h2 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 var(--spacing-md) 0;
}

.error-message p {
  color: var(--text-secondary);
  font-size: 15px;
  line-height: 1.6;
  margin: 0;
  font-weight: 500;
}

.topic-section {
  margin: var(--spacing-xl) 0;
  text-align: left;
}

.topic-title h2 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 var(--spacing-lg) 0;
  text-align: center;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.topic-meta {
  display: grid;
  gap: var(--spacing-md);
  background: var(--bg-secondary);
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.topic-meta > div {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.topic-meta .label {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 14px;
  min-width: 80px;
}

.topic-meta .value {
  color: var(--text-primary);
  font-weight: 500;
}

.topic-meta code {
  background: var(--bg-primary);
  color: var(--primary-color);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  border: 1px solid var(--border-light);
}

.tags-list {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.doc-content-section {
  margin: var(--spacing-xl) 0;
  text-align: left;
}

.doc-title {
  margin-bottom: var(--spacing-lg);
  text-align: center;
}

.doc-title h3 {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.doc-display {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.doc-text {
  padding: var(--spacing-xl);
  line-height: 1.8;
  color: var(--text-primary);
  font-size: 15px;
}

.doc-text strong {
  color: var(--text-primary);
  font-weight: 600;
}

.doc-text em {
  color: var(--text-secondary);
  font-style: italic;
}

.doc-text code {
  background: var(--bg-primary);
  color: var(--primary-color);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  border: 1px solid var(--border-light);
}

.doc-text pre {
  background: var(--bg-dark);
  color: var(--text-primary);
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: var(--spacing-lg) 0;
  border: 1px solid var(--border-light);
}

.doc-text pre code {
  background: none;
  color: inherit;
  padding: 0;
  font-size: 14px;
  border: none;
}

.doc-text a {
  color: var(--primary-color);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color var(--transition-normal);
}

.doc-text a:hover {
  border-bottom-color: var(--primary-color);
}

.empty-doc {
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-lg);
}

.empty-doc h3 {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 var(--spacing-md) 0;
}

.empty-doc p {
  color: var(--text-secondary);
  font-size: 15px;
  margin: 0;
}

.doc-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  flex-wrap: wrap;
  margin: var(--spacing-xl) 0 var(--spacing-lg) 0;
}

.action-button {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  position: relative;
  overflow: hidden;
  min-width: 140px;
}

.action-button:not(.secondary) {
  background: var(--primary-gradient);
  border: none;
}

.action-button.secondary {
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
}

.action-button.secondary:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.public-doc-footer {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
  padding: var(--spacing-md) 0;
  border-top: 1px solid var(--border-light);
  margin-top: var(--spacing-lg);
  position: relative;
}

.public-doc-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 1px;
  background: var(--primary-gradient);
}

.public-doc-footer p {
  margin: 0;
  font-weight: 500;
  opacity: 0.8;
  transition: opacity var(--transition-normal);
}

.public-doc-footer:hover p {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .public-doc-card {
    width: 95%;
    padding: var(--spacing-xl);
    margin: var(--spacing-md);
  }

  .public-doc-header h1 {
    font-size: 24px;
  }

  .topic-title h2 {
    font-size: 20px;
  }

  .topic-meta {
    padding: var(--spacing-md);
  }

  .topic-meta > div {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }

  .topic-meta .label {
    min-width: auto;
  }

  .doc-text {
    padding: var(--spacing-lg);
  }

  .action-button {
    height: 44px;
    font-size: 15px;
    min-width: 120px;
  }
}

@media (max-width: 480px) {
  .public-doc-card {
    width: 98%;
    padding: var(--spacing-lg);
    margin: var(--spacing-sm);
  }

  .public-doc-header h1 {
    font-size: 20px;
  }

  .topic-title h2 {
    font-size: 18px;
  }

  .logo-icon {
    margin-right: var(--spacing-sm);
  }

  .doc-actions {
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-sm);
  }

  .action-button {
    width: 100%;
    max-width: 200px;
    height: 42px;
    font-size: 14px;
  }

  .public-doc-footer {
    font-size: 12px;
    padding: var(--spacing-sm) 0;
    margin-top: var(--spacing-md);
  }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-skeleton__item) {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--bg-primary) 50%, var(--bg-secondary) 75%);
}

:deep(.el-tag) {
  background: var(--bg-primary);
  border-color: var(--border-light);
  color: var(--text-primary);
}
</style>