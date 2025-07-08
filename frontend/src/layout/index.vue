<!-- src/layout/index.vue -->
<template>
  <div class="app-wrapper">
    <div class="sidebar-container">
      <el-menu
        :default-active="activeMenuPath"
        class="sidebar-menu"
        router
        unique-opened
      >
        <div class="logo">
          <h3>拜访管理系统</h3>
        </div>
        
        <template v-for="route in menuRoutes" :key="route.path">
          <el-sub-menu
            v-if="hasVisibleChildren(route)"
            :index="route.path"
          >
            <template #title>
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            
            <el-menu-item
              v-for="child in getVisibleChildren(route)"
              :key="child.path"
              :index="child.path"
            >
              {{ child.meta.title }}
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item
            v-else
            :index="getMenuPath(route)"
          >
            <el-icon><component :is="route.meta.icon" /></el-icon>
            <span>{{ route.meta.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>
    
    <div class="main-container">
      <div class="navbar">
        <div class="navbar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path ? { path: item.path } : null"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="navbar-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userStore.user?.avatarUrl">
                {{ userStore.user?.realName?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ userStore.user?.realName }}</span>
              <el-icon class="arrow-down"><ArrowDown /></el-icon>
            </span>
            
            <template #dropdown>
              <el-dropdown-menu>
                <!-- <el-dropdown-item command="profile">个人中心</el-dropdown-item> -->
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取当前激活的菜单路径
const activeMenuPath = computed(() => {
  const matched = route.matched
  if (matched.length >= 2) {
    // 如果是子页面，返回第一个子路由的路径
    const parentRoute = matched[1]
    if (parentRoute.children && parentRoute.children.length > 0) {
      const firstChild = parentRoute.children.find(child => !child.meta?.hidden)
      return firstChild ? `${parentRoute.path}/${firstChild.path}` : parentRoute.path
    }
  }
  return route.path
})

// 获取菜单路由
const menuRoutes = computed(() => {
  return router.getRoutes().filter(r => 
    r.meta?.title && 
    !r.meta?.hidden &&
    r.path !== '/login' &&
    (!r.meta?.roles || r.meta.roles.includes(userStore.user?.role))
  )
})

// 检查路由是否有可见的子路由
const hasVisibleChildren = (route) => {
  return route.children && 
         route.children.some(child => !child.meta?.hidden) &&
         route.children.filter(child => !child.meta?.hidden).length > 1
}

// 获取可见的子路由
const getVisibleChildren = (route) => {
  return route.children ? route.children.filter(child => !child.meta?.hidden) : []
}

// 获取菜单路径
const getMenuPath = (route) => {
  if (route.children && route.children.length > 0) {
    const firstChild = route.children.find(child => !child.meta?.hidden)
    return firstChild ? `${route.path}/${firstChild.path}` : route.path
  }
  return route.path
}

// 生成面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const breadcrumbList = []
  
  for (let i = 0; i < matched.length; i++) {
    const routeRecord = matched[i]
    
    // 跳过隐藏的路由
    if (routeRecord.meta.hidden) {
      continue
    }
    
    // 如果是Layout组件，跳过
    if (routeRecord.path === '/') {
      continue
    }
    
    // 添加到面包屑
    if (routeRecord.meta.showInBreadcrumb !== false) {
      let breadcrumbPath = null
      
      // 如果是父级路由且有子路由，指向第一个子路由
      if (i === matched.length - 2 && routeRecord.children) {
        const firstChild = routeRecord.children.find(child => !child.meta?.hidden)
        if (firstChild) {
          breadcrumbPath = `${routeRecord.path}/${firstChild.path}`
        }
      } else if (i === matched.length - 1) {
        // 最后一个路由不需要链接
        breadcrumbPath = null
      } else {
        breadcrumbPath = routeRecord.path
      }
      
      breadcrumbList.push({
        title: routeRecord.meta.title,
        path: breadcrumbPath
      })
    }
  }
  
  // 处理特殊的子页面（如编辑、详情等）
  const currentRoute = route
  if (currentRoute.meta?.parentPath) {
    // 查找父级路由信息
    const parentRoute = router.getRoutes().find(r => 
      r.children?.some(child => `${r.path}/${child.path}` === currentRoute.meta.parentPath)
    )
    
    if (parentRoute) {
      const parentChild = parentRoute.children.find(child => 
        `${parentRoute.path}/${child.path}` === currentRoute.meta.parentPath
      )
      
      if (parentChild && breadcrumbList.length > 0) {
        // 替换最后一个面包屑为父级页面
        breadcrumbList[breadcrumbList.length - 1] = {
          title: parentChild.meta.title,
          path: currentRoute.meta.parentPath
        }
        
        // 添加当前页面
        breadcrumbList.push({
          title: currentRoute.meta.title,
          path: null
        })
      }
    }
  }
  
  return breadcrumbList
})

// 用户操作处理
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        userStore.userLogout()
      })
      break
  }
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
}

.sidebar-container {
  width: 210px;
  background: #304156;
  
  .logo {
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #2b2f3a;
    
    h3 {
      color: white;
      margin: 0;
      font-size: 16px;
    }
  }
  
  .sidebar-menu {
    border: none;
    height: calc(100vh - 50px);
    width: 100%;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.navbar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background: white;
  border-bottom: 1px solid #d8dce5;
  
  .navbar-right {
    .user-dropdown {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .user-name {
        margin: 0 8px;
      }
      
      .arrow-down {
        font-size: 12px;
      }
    }
  }
}

.app-main {
  flex: 1;
  padding: 20px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>