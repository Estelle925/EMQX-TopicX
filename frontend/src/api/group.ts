import request from '../utils/request'

// 业务管理相关的接口类型定义
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
 * 业务管理API服务
 */
export const GroupAPI = {
  /**
   * 获取所有业务列表
   */
  getAllGroups(): Promise<GroupDTO[]> {
    return request.get('/groups')
  },

  /**
   * 根据ID获取业务详情
   */
  getGroupById(id: number): Promise<GroupDTO> {
    return request.get(`/groups/${id}`)
  },

  /**
   * 创建新业务
   */
  createGroup(data: GroupCreateRequest): Promise<GroupDTO> {
    return request.post('/groups', data)
  },

  /**
   * 更新业务信息
   */
  updateGroup(id: number, data: GroupUpdateRequest): Promise<GroupDTO> {
    return request.put(`/groups/${id}`, data)
  },

  /**
   * 删除业务
   */
  deleteGroup(id: number): Promise<void> {
    return request.delete(`/groups/${id}`)
  },

  /**
   * 根据关键词搜索业务
   */
  searchGroups(keyword?: string): Promise<GroupDTO[]> {
    const params = keyword ? { keyword } : {}
    return request.get('/groups/search', { params })
  }
}

export default GroupAPI