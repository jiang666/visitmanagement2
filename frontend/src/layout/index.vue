<template>
  <div class="app-wrapper" :class="{ 'mobile': isMobile }">
    <!-- 移动端遮罩 -->
    <div
        v-if="isMobile && sidebarOpened"
        class="drawer-bg"
        @click="closeSidebar"
    />

    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'hide-sidebar': !sidebarOpened }">
      <div class="logo">
        <h3>{{ isMobile ? '客户管理' : '客户拜访管理系统' }}</h3>
      </div>

      <el-menu
          :default-active="$route.path"
          :collapse="isMobile ? false : collapsed"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :router="true"
          @select="handleMenuSelect"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-sub-menu
              v-if="getVisibleChildren(route).length > 1"
              :index="route.path"
          >
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>

            <el-menu-item
                v-for="child in getVisibleChildren(route)"
                :key="child.path"
                :index="`${route.path}/${child.path}`"
            >
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item
              v-else
              :index="getMenuPath(route)"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <!-- 主容器 -->
    <div class="main-container">
      <!-- 导航栏 -->
      <div class="navbar">
        <div class="navbar-left">
          <!-- 移动端菜单按钮 -->
          <el-button
              v-if="isMobile"
              type="text"
              @click="toggleSidebar"
              class="mobile-menu-btn"
          >
            <el-icon><Menu /></el-icon>
          </el-button>

          <!-- 面包屑 -->
          <el-breadcrumb separator="/" class="breadcrumb" v-if="!isMobile">
            <el-breadcrumb-item
                v-for="breadcrumb in breadcrumbs"
                :key="breadcrumb.title"
                :to="breadcrumb.path"
            >
              {{ breadcrumb.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="navbar-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <span class="user-name">{{ userStore.user?.realName }}</span>
              <el-icon class="arrow-down"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { Menu, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式状态
const isMobile = ref(false)
const sidebarOpened = ref(true)
const collapsed = ref(false)

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
  if (isMobile.value) {
    sidebarOpened.value = false
  } else {
    sidebarOpened.value = true
  }
}

// 切换侧边栏
const toggleSidebar = () => {
  sidebarOpened.value = !sidebarOpened.value
}

const closeSidebar = () => {
  if (isMobile.value) {
    sidebarOpened.value = false
  }
}

// 菜单选择处理
const handleMenuSelect = () => {
  if (isMobile.value) {
    closeSidebar()
  }
}

// 获取菜单路由
const menuRoutes = computed(() => {
  return router.getRoutes().filter(route =>
      route.meta && route.meta.title && !route.meta.hidden && route.meta.requiresAuth
  )
})

// 获取可见子路由
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

    if (routeRecord.meta.hidden) continue
    if (routeRecord.path === '/') continue

    if (routeRecord.meta.showInBreadcrumb !== false) {
      let breadcrumbPath = null

      if (i === matched.length - 2 && routeRecord.children) {
        const firstChild = routeRecord.children.find(child => !child.meta?.hidden)
        if (firstChild) {
          breadcrumbPath = `${routeRecord.path}/${firstChild.path}`
        }
      } else if (i === matched.length - 1) {
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

  const currentRoute = route
  if (currentRoute.meta?.parentPath) {
    const parentRoute = router.getRoutes().find(r =>
        r.children?.some(child => `${r.path}/${child.path}` === currentRoute.meta.parentPath)
    )

    if (parentRoute) {
      const parentChild = parentRoute.children.find(child =>
          `${parentRoute.path}/${child.path}` === currentRoute.meta.parentPath
      )

      if (parentChild && breadcrumbList.length > 0) {
        breadcrumbList[breadcrumbList.length - 1] = {
          title: parentChild.meta.title,
          path: currentRoute.meta.parentPath
        }

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

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  position: relative;

  &.mobile {
    .sidebar-container {
      position: fixed;
      top: 0;
      left: 0;
      z-index: 1001;
      height: 100vh;
      transition: transform 0.3s;

      &.hide-sidebar {
        transform: translateX(-100%);
      }
    }

    .main-container {
      width: 100%;
      margin-left: 0;
    }
  }
}

.drawer-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

.sidebar-container {
  width: 210px;
  background: #304156;
  overflow: hidden;

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

      @media (max-width: 768px) {
        font-size: 14px;
      }
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
  min-width: 0;
}

.navbar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background: white;
  border-bottom: 1px solid #d8dce5;

  .navbar-left {
    display: flex;
    align-items: center;
    flex: 1;

    .mobile-menu-btn {
      margin-right: 10px;
      font-size: 18px;
    }

    .breadcrumb {
      flex: 1;
    }
  }

  .navbar-right {
    .user-dropdown {
      display: flex;
      align-items: center;
      cursor: pointer;

      .user-name {
        margin: 0 8px;

        @media (max-width: 768px) {
          font-size: 14px;
        }
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

  @media (max-width: 768px) {
    padding: 10px;
  }
}
</style>