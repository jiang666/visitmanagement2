import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import router from '@/router'
import { ElMessage } from 'element-plus'
export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // 登录
  const userLogin = async (loginForm) => {
    loading.value = true
    try {
      const response = await login(loginForm)
      const {
        token: authToken,
        userId,
        tokenType,
        ...userInfo
      } = response.data

      token.value = authToken
      user.value = { id: userId, tokenType, ...userInfo }
      setToken(authToken)
      
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 登出
  const userLogout = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('logout error:', error)
    } finally {
      token.value = null
      user.value = null
      removeToken()
      router.push('/login')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    
    try {
      const response = await getUserInfo()
      user.value = response.data
    } catch (error) {
      console.error('fetchUserInfo error:', error)
      await userLogout()
    }
  }

  // 检查认证状态
  const checkAuth = async () => {
    if (token.value && !user.value) {
      await fetchUserInfo()
    }
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    userLogin,
    userLogout,
    fetchUserInfo,
    checkAuth
  }
})

