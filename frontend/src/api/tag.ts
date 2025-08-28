import request from '../utils/request'

// 标签管理相关的接口类型定义
export interface TagDTO {
  id: number
  name: string
  color: string
  usageCount: number
  createdAt: string
  updatedAt: string
}

export interface TagCreateRequest {
  name: string
  color: string
}

export interface TagUpdateRequest {
  name: string
  color: string
}

/**
 * 标签管理API服务
 */
export const TagAPI = {
  /**
   * 获取所有标签列表
   */
  getAllTags(): Promise<TagDTO[]> {
    return request.get('/tags')
  },

  /**
   * 根据ID获取标签详情
   */
  getTagById(id: number): Promise<TagDTO> {
    return request.get(`/tags/${id}`)
  },

  /**
   * 创建新标签
   */
  createTag(data: TagCreateRequest): Promise<TagDTO> {
    return request.post('/tags', data)
  },

  /**
   * 更新标签信息
   */
  updateTag(id: number, data: TagUpdateRequest): Promise<TagDTO> {
    return request.put(`/tags/${id}`, data)
  },

  /**
   * 删除标签
   */
  deleteTag(id: number): Promise<void> {
    return request.delete(`/tags/${id}`)
  },

  /**
   * 根据关键词搜索标签
   */
  searchTags(keyword?: string): Promise<TagDTO[]> {
    const params = keyword ? { keyword } : {}
    return request.get('/tags/search', { params })
  }
}

export default TagAPI