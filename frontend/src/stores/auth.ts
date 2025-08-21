import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export interface LoginRequest {
  username: string
  password: string
  emqxSystemId?: number
}

export interface LoginResponse {
  token: string
  username: string
  expiresAt: number
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('mock-token')
  const username = ref<string>('admin')
  const isLoggedIn = ref<boolean>(true)

  // 从localStorage恢复登录状态
  const initAuth = () => {
    // 默认用户已登录，无需验证
    token.value = 'mock-token'
    username.value = 'admin'
    isLoggedIn.value = true
    
    // 设置axios默认header
    axios.defaults.headers.common['Authorization'] = `Bearer mock-token`
  }

  // 登录 - 简化版本，直接设置为已登录状态
  const login = async (loginData: LoginRequest): Promise<LoginResponse> => {
    // 模拟登录成功，直接设置登录状态
    const mockResponse: LoginResponse = {
      token: 'mock-token',
      username: loginData.username || 'admin',
      expiresAt: Date.now() + 24 * 60 * 60 * 1000 // 24小时后过期
    }
    
    token.value = mockResponse.token
    username.value = mockResponse.username
    isLoggedIn.value = true
    
    // 保存到localStorage
    localStorage.setItem('token', mockResponse.token)
    localStorage.setItem('username', mockResponse.username)
    
    // 设置axios默认header
    axios.defaults.headers.common['Authorization'] = `Bearer ${mockResponse.token}`
    
    return mockResponse
  }

  // 登出
  const logout = async () => {
    try {
      await axios.post('/api/auth/logout')
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除状态
      token.value = ''
      username.value = ''
      isLoggedIn.value = false
      
      // 清除localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      
      // 清除axios默认header
      delete axios.defaults.headers.common['Authorization']
    }
  }

  return {
    token,
    username,
    isLoggedIn,
    initAuth,
    login,
    logout
  }
})