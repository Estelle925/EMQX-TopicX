import request from '../utils/request'

// 用户相关接口类型定义
export interface User {
  id: number
  username: string
  email: string
  realName: string
  lastLoginAt: string
  status: 'active' | 'locked'
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  username: string
  password: string
  email: string
  realName: string
}

export interface UpdateUserRequest {
  email: string
  realName: string
  status: 'active' | 'locked'
}

export interface UserListRequest {
  page: number
  size: number
  keyword?: string
}

export interface UserListResponse {
  users: User[]
  total: number
  page: number
  size: number
}

/**
 * 用户管理API
 */
export const UserAPI = {
  /**
   * 获取用户列表
   * @param params 查询参数
   * @returns 用户列表
   */
  getUsers(params: UserListRequest): Promise<UserListResponse> {
    return request({
      url: '/users',
      method: 'GET',
      params
    })
  },

  /**
   * 根据ID获取用户详情
   * @param id 用户ID
   * @returns 用户详情
   */
  getUserById(id: number): Promise<{ data: User }> {
    return request({
      url: `/users/${id}`,
      method: 'GET'
    })
  },

  /**
   * 创建用户
   * @param data 用户数据
   * @returns 创建结果
   */
  createUser(data: CreateUserRequest): Promise<{ data: User }> {
    return request({
      url: '/users',
      method: 'POST',
      data
    })
  },

  /**
   * 更新用户
   * @param id 用户ID
   * @param data 更新数据
   * @returns 更新结果
   */
  updateUser(id: number, data: UpdateUserRequest): Promise<{ data: User }> {
    return request({
      url: `/users/${id}/update`,
      method: 'POST',
      data
    })
  },

  /**
   * 禁用用户
   * @param id 用户ID
   * @returns 操作结果
   */
  disableUser(id: number): Promise<{ data: null }> {
    return request({
      url: `/users/${id}/disable`,
      method: 'POST'
    })
  },

  /**
   * 启用用户
   * @param id 用户ID
   * @returns 操作结果
   */
  enableUser(id: number): Promise<{ data: null }> {
    return request({
      url: `/users/${id}/enable`,
      method: 'POST'
    })
  },

  /**
   * 删除用户
   * @param id 用户ID
   * @returns 删除结果
   */
  deleteUser(id: number): Promise<{ data: null }> {
    return request({
      url: `/users/${id}`,
      method: 'DELETE'
    })
  },

  /**
   * 重置用户密码
   * @param id 用户ID
   * @param newPassword 新密码
   * @returns 操作结果
   */
  resetPassword(id: number, newPassword: string): Promise<{ data: null }> {
    return request({
      url: `/users/${id}/reset-password`,
      method: 'POST',
      data: { password: newPassword }
    })
  },

  /**
   * 批量操作用户
   * @param ids 用户ID列表
   * @param action 操作类型
   * @returns 操作结果
   */
  batchOperation(ids: number[], action: 'enable' | 'disable' | 'delete'): Promise<{ data: null }> {
    return request({
      url: '/users/batch',
      method: 'POST',
      data: { ids, action }
    })
  }
}

export default UserAPI