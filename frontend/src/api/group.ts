import request from '../utils/request'

// 分组管理相关的接口类型定义
export interface GroupDTO {
  id: number
  name: string
  description?: string
  topicCount: number
  createdAt: string
  updatedAt: string
}

export interface GroupCreateRequest {
  name: string
  description?: string
}

export interface GroupUpdateRequest {
  name: string
  description?: string
}

/**
 * 分组管理API服务
 */
export const GroupAPI = {
  /**
   * 获取所有分组列表
   */
  getAllGroups(): Promise<GroupDTO[]> {
    return request.get('/groups')
  },

  /**
   * 根据ID获取分组详情
   */
  getGroupById(id: number): Promise<GroupDTO> {
    return request.get(`/groups/${id}`)
  },

  /**
   * 创建新分组
   */
  createGroup(data: GroupCreateRequest): Promise<GroupDTO> {
    return request.post('/groups', data)
  },

  /**
   * 更新分组信息
   */
  updateGroup(id: number, data: GroupUpdateRequest): Promise<GroupDTO> {
    return request.put(`/groups/${id}`, data)
  },

  /**
   * 删除分组
   */
  deleteGroup(id: number): Promise<void> {
    return request.delete(`/groups/${id}`)
  },

  /**
   * 根据关键词搜索分组
   */
  searchGroups(keyword?: string): Promise<GroupDTO[]> {
    const params = keyword ? { keyword } : {}
    return request.get('/groups/search', { params })
  }
}

export default GroupAPI