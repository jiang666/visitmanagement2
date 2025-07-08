<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { computed } from 'vue'

const userStore = useUserStore()
const router = useRouter()

const user = computed(() => userStore.user)

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning'
  }).then(() => {
    userStore.userLogout()
    router.push('/login')
  })
}
</script>

<template>
  <div class="profile-page" v-if="user">
    <h2>个人中心</h2>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user.role }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-button type="danger" class="logout-btn" @click="logout">退出登录</el-button>
  </div>
</template>

  
  <style scoped>
  .profile-page {
    max-width: 600px;
    margin: 0 auto;
    padding: 20px;
  }
  .logout-btn {
    margin-top: 20px;
  }
  </style>