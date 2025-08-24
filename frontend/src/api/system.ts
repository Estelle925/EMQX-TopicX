import request from '../utils/request'

// 系统管理相关的接口类型定义
export interface SystemManagementDTO {
  id: number
  name: string
  url: string
  username: string
  description?: string
  status: 'online' | 'offline'
  lastCheck?: string
  createdAt: string
  topicCount?: number
  connectionCount?: number
  testing?: boolean
}

export interface SystemCreateRequest {
  name: string
  url: string
  username: string
  password: string
  description?: string
}

export interface SystemUpdateRequest {
  name: string
  url: string
  username: string
  password?: string
  description?: string
}

export interface ConnectionTestResult {
  systemId: number
  systemName: string
  success: boolean
  status: string
  responseTime: number
  testTime: string
  errorMessage?: string
  version?: string
  nodeInfo?: string
}

export interface SystemStatsDTO {
  totalSystems: number
  onlineCount: number
  offlineCount: number
  totalTopics: number
}

// 系统管理API服务
export class SystemAPI {
  /**
   * 获取所有系统列表
   */
  static async getAllSystems(): Promise<SystemManagementDTO[]> {
    return await request.get('/systems')
  }

  /**
   * 根据关键词搜索系统
   */
  static async searchSystems(keyword?: string): Promise<SystemManagementDTO[]> {
    return await request.get('/systems/search', {
      params: { keyword }
    })
  }

  /**
   * 根据ID获取系统详情
   */
  static async getSystemById(id: number): Promise<SystemManagementDTO> {
    return await request.get(`/systems/${id}`)
  }

  /**
   * 创建新系统
   */
  static async createSystem(data: SystemCreateRequest): Promise<SystemManagementDTO> {
    return await request.post('/systems', data)
  }

  /**
   * 更新系统信息
   */
  static async updateSystem(id: number, data: SystemUpdateRequest): Promise<SystemManagementDTO> {
    return await request.put(`/systems/${id}`, data)
  }

  /**
   * 删除系统
   */
  static async deleteSystem(id: number): Promise<void> {
    await request.delete(`/systems/${id}`)
  }

  /**
   * 测试系统连接
   */
  static async testConnection(id: number): Promise<ConnectionTestResult> {
    return await request.post(`/systems/${id}/test-connection`)
  }

  /**
   * 刷新所有系统状态
   */
  static async refreshSystemStatus(): Promise<SystemManagementDTO[]> {
    return await request.post('/systems/refresh-status')
  }

  /**
   * 获取系统统计信息
   */
  static async getSystemStats(): Promise<SystemStatsDTO> {
    return await request.get('/systems/stats')
  }
}

export default SystemAPI