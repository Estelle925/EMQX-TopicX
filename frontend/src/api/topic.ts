import request from '../utils/request'
import type { TagDTO } from './tag'

// Topic管理相关的接口类型定义
export interface TopicDTO {
  id: number
  name: string
  path: string
  systemId: number
  groupId?: number
  groupName?: string
  lastActivity?: string
  payloadDoc?: string
  tags: TagDTO[]
  status: 'enabled' | 'disabled'
  createdAt: string
  updatedAt: string
}

export interface TopicCreateRequest {
  name: string
  path: string
  systemId: number
  groupId?: number
  payloadDoc?: string
  tagIds?: number[]
}

export interface TopicUpdateRequest {
  name: string
  path: string
  groupId?: number
  payloadDoc?: string
  tagIds?: number[]
}

export interface TopicSearchRequest {
  keyword?: string
  groupId?: number
  tagIds?: number[]
  status?: string
  systemId?: number
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

export interface TopicBatchRequest {
  topicIds: number[]
  action: 'assignGroup' | 'addTags' | 'removeTags'
  groupId?: number
  tagIds?: number[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * Topic管理API服务
 */
export const TopicAPI = {
  /**
   * 分页搜索Topic列表
   */
  searchTopics(data: TopicSearchRequest): Promise<PageResult<TopicDTO>> {
    return request.post('/topics/search', data)
  },

  /**
   * 根据ID获取Topic详情
   */
  getTopicById(id: number): Promise<TopicDTO> {
    return request.get(`/topics/${id}`)
  },

  /**
   * 创建新Topic
   */
  createTopic(data: TopicCreateRequest): Promise<TopicDTO> {
    return request.post('/topics', data)
  },

  /**
   * 更新Topic信息
   */
  updateTopic(id: number, data: TopicUpdateRequest): Promise<TopicDTO> {
    return request.put(`/topics/${id}`, data)
  },

  /**
   * 删除Topic
   */
  deleteTopic(id: number): Promise<void> {
    return request.delete(`/topics/${id}`)
  },

  /**
   * 批量操作Topic
   */
  batchOperation(data: TopicBatchRequest): Promise<void> {
    return request.post('/topics/batch', data)
  },

  /**
   * 获取Topic的标签列表
   */
  getTopicTags(id: number): Promise<TagDTO[]> {
    return request.get(`/topics/${id}/tags`)
  },

  /**
   * 为Topic添加标签
   */
  addTopicTags(id: number, tagIds: number[]): Promise<void> {
    return request.post(`/topics/${id}/tags`, tagIds)
  },

  /**
   * 移除Topic的标签
   */
  removeTopicTags(id: number, tagIds: number[]): Promise<void> {
    return request.delete(`/topics/${id}/tags`, { data: tagIds })
  },

  /**
   * 批量分配分组
   */
  batchAssignGroup(topicIds: number[], groupId: number): Promise<void> {
    return this.batchOperation({
      topicIds,
      action: 'assignGroup',
      groupId
    })
  },

  /**
   * 批量添加标签
   */
  batchAddTags(topicIds: number[], tagIds: number[]): Promise<void> {
    return this.batchOperation({
      topicIds,
      action: 'addTags',
      tagIds
    })
  },

  /**
   * 批量移除标签
   */
  batchRemoveTags(topicIds: number[], tagIds: number[]): Promise<void> {
    return this.batchOperation({
      topicIds,
      action: 'removeTags',
      tagIds
    })
  }
}

export default TopicAPI