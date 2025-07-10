<template>
  <div class="mobile-layout">
    <header class="mobile-header">
      <el-button text class="back-btn" @click="goBack" v-if="showBack">
        <el-icon><arrow-left /></el-icon>
      </el-button>
      <span class="title">{{ title }}</span>
      <slot name="header-right"></slot>
    </header>
    <main class="mobile-main">
      <slot />
    </main>
    <nav class="mobile-tab-bar">
      <div class="tab-item" :class="{ active: isActive('/m/home') }" @click="router.push('/m/home')">
        <el-icon><Odometer /></el-icon>
        <span>首页</span>
      </div>
      <div class="tab-item" :class="{ active: isActive('/m/customers/list') }" @click="router.push('/m/customers/list')">
        <el-icon><UserFilled /></el-icon>
        <span>客户</span>
      </div>
      <div class="tab-item" :class="{ active: isActive('/m/visits/list') }" @click="router.push('/m/visits/list')">
        <el-icon><Notebook /></el-icon>
        <span>拜访</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Odometer, UserFilled, Notebook } from '@element-plus/icons-vue'

const props = defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: true }
})

const router = useRouter()
const route = useRoute()

const goBack = () => {
  router.back()
}

const isActive = (path) => {
  return route.path.startsWith(path)
}
</script>

<style scoped>
.mobile-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.mobile-header {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 50px;
  background: #409EFF;
  color: #fff;
}

.title {
  flex: 1;
  text-align: center;
  font-weight: bold;
}

.mobile-main {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.mobile-tab-bar {
  display: flex;
  border-top: 1px solid #e0e0e0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  color: #666;
  transition: color 0.3s, transform 0.3s;
}

.tab-item.active {
  color: #409EFF;
  transform: scale(1.05);
}
</style>
