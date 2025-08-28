import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { AuthAPI, type LoginRequest, type LoginResponse } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const username = ref<string>('')
  const isLoggedIn = ref<boolean>(false)

  // 从localStorage恢复登录状态
  const initAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUsername = localStorage.getItem('username')
    const savedExpiresAt = localStorage.getItem('expiresAt')
    
    if (savedToken && savedUsername && savedExpiresAt) {
      const expiresAt = parseInt(savedExpiresAt)
      if (Date.now() < expiresAt) {
        token.value = savedToken
        username.value = savedUsername
        isLoggedIn.value = true
        
        // 设置axios默认header
        axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
      } else {
        // Token已过期，清除存储
        logout()
      }
    }
  }

  // 登录
  const login = async (loginData: LoginRequest): Promise<LoginResponse> => {
    try {
      const response = await AuthAPI.login(loginData)
      
      token.value = response.token
      username.value = response.username
      isLoggedIn.value = true
      
      // 计算过期时间戳
      const expiresAt = new Date(response.expiresAt).getTime()
      
      // 保存到localStorage
      localStorage.setItem('token', response.token)
      localStorage.setItem('username', response.username)
      localStorage.setItem('expiresAt', expiresAt.toString())
      
      // 设置axios默认header
      axios.defaults.headers.common['Authorization'] = `Bearer ${response.token}`
      
      return response
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      if (isLoggedIn.value) {
        await AuthAPI.logout()
      }
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
      localStorage.removeItem('expiresAt')
      
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