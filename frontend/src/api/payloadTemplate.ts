import request from '../utils/request'

// Payload模板相关的接口类型定义
export interface PayloadTemplateDTO {
  id: number
  name: string
  groupId: number
  groupName?: string
  type: string
  description?: string
  payload: string
  usageCount: number
  isFavorite: boolean
  lastUsed?: string
  createdAt: string
  updatedAt: string
}

export interface PayloadTemplateCreateRequest {
  name: string
  groupId: number
  type: string
  description?: string
  payload: string
}

export interface PayloadTemplateUpdateRequest {
  name: string
  groupId: number
  type: string
  description?: string
  payload: string
}

export interface PayloadTemplateSearchRequest {
  page?: number
  size?: number
  keyword?: string
  groupId?: number
  type?: string
  onlyFavorites?: boolean
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface TemplateStatistics {
  totalTemplates: number
  groupCount: number
  recentUsedCount: number
  favoriteCount: number
}

export interface GroupedTemplate {
  groupId: number
  groupName: string
  templates: PayloadTemplateDTO[]
}

/**
 * Payload模板管理API服务
 */
export const PayloadTemplateAPI = {
  /**
   * 分页搜索Payload模板列表
   */
  searchTemplates(searchRequest: PayloadTemplateSearchRequest): Promise<PageResult<PayloadTemplateDTO>> {
    return request.post('/payload-templates/search', searchRequest)
  },

  /**
   * 根据ID获取Payload模板详情
   */
  getTemplateById(id: number): Promise<PayloadTemplateDTO> {
    return request.get(`/payload-templates/${id}`)
  },

  /**
   * 创建Payload模板
   */
  createTemplate(data: PayloadTemplateCreateRequest): Promise<PayloadTemplateDTO> {
    return request.post('/payload-templates', data)
  },

  /**
   * 更新Payload模板
   */
  updateTemplate(id: number, data: PayloadTemplateUpdateRequest): Promise<PayloadTemplateDTO> {
    return request.put(`/payload-templates/${id}`, data)
  },

  /**
   * 删除Payload模板
   */
  deleteTemplate(id: number): Promise<void> {
    return request.delete(`/payload-templates/${id}`)
  },

  /**
   * 切换模板收藏状态
   */
  toggleFavorite(id: number): Promise<void> {
    return request.post(`/payload-templates/${id}/toggle-favorite`)
  },

  /**
   * 复制模板
   */
  copyTemplate(id: number): Promise<PayloadTemplateDTO> {
    return request.post(`/payload-templates/${id}/copy`)
  },

  /**
   * 使用模板（增加使用次数）
   */
  useTemplate(id: number): Promise<void> {
    return request.post(`/payload-templates/${id}/use`)
  },

  /**
   * 获取模板统计信息
   */
  getStatistics(): Promise<TemplateStatistics> {
    return request.get('/payload-templates/statistics')
  },

  /**
   * 按业务分组获取模板列表
   */
  getGroupedTemplates(): Promise<GroupedTemplate[]> {
    return request.get('/payload-templates/grouped')
  },

  /**
   * 批量删除模板
   */
  batchDeleteTemplates(ids: number[]): Promise<void> {
    return request.delete('/payload-templates/batch', { data: ids })
  }
}

export default PayloadTemplateAPI