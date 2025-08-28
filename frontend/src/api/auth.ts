import request from '../utils/request'

// 认证相关的接口类型定义
export interface LoginRequest {
  username: string
  password: string
  emqxSystemId?: number
}

export interface LoginResponse {
  token: string
  username: string
  expiresAt: string
}

/**
 * 认证API服务
 */
export const AuthAPI = {
  /**
   * 用户登录
   */
  login(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/auth/login', data)
  },

  /**
   * 用户登出
   */
  logout(): Promise<void> {
    return request.post('/auth/logout')
  }
}

export default AuthAPI