// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layout/index.vue'


const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { 
      title: '登录', 
      requiresAuth: false,
      hidden: true // 在菜单和面包屑中隐藏
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { 
      requiresAuth: true,
      hidden: true // 根路径在面包屑中隐藏
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { 
          title: '仪表盘', 
          icon: 'Odometer',
          showInBreadcrumb: true // 显示在面包屑中
        }
      }
    ]
  },
  {
    path: '/visits',
    component: Layout,
    redirect: '/visits/list',
    meta: { 
      title: '拜访管理', 
      icon: 'User', 
      requiresAuth: true 
    },
    children: [
      {
        path: 'list',
        name: 'VisitList',
        component: () => import('@/views/visits/index.vue'),
        meta: { 
          title: '拜访记录',
          showInBreadcrumb: true
        }
      },
      {
        path: 'create',
        name: 'VisitCreate',
        component: () => import('@/views/visits/form.vue'),
        meta: { 
          title: '新增拜访',
          hidden: true, // 在菜单中隐藏
          showInBreadcrumb: true,
          parentPath: '/visits/list' // 指定父级路径用于面包屑
        }
      },
      {
        path: 'edit/:id',
        name: 'VisitEdit',
        component: () => import('@/views/visits/form.vue'),
        meta: { 
          title: '编辑拜访',
          hidden: true,
          showInBreadcrumb: true,
          parentPath: '/visits/list'
        }
      },
      {
        path: 'detail/:id',
        name: 'VisitDetail',
        component: () => import('@/views/visits/detail.vue'),
        meta: { 
          title: '拜访详情',
          hidden: true,
          showInBreadcrumb: true,
          parentPath: '/visits/list'
        }
      }
    ]
  },
  {
    path: '/customers',
    component: Layout,
    redirect: '/customers/list',
    meta: { 
      title: '客户管理', 
      icon: 'UserFilled', 
      requiresAuth: true 
    },
    children: [
      {
        path: 'list',
        name: 'CustomerList',
        component: () => import('@/views/customers/index.vue'),
        meta: { 
          title: '客户列表',
          showInBreadcrumb: true
        }
      },
      {
        path: 'create',
        name: 'CustomerCreate',
        component: () => import('@/views/customers/form.vue'),
        meta: { 
          title: '新增客户',
          hidden: true,
          showInBreadcrumb: true,
          parentPath: '/customers/list'
        }
      },
      {
        path: 'edit/:id',
        name: 'CustomerEdit',
        component: () => import('@/views/customers/form.vue'),
        meta: { 
          title: '编辑客户',
          hidden: true,
          showInBreadcrumb: true,
          parentPath: '/customers/list'
        }
      },
      {
        path: 'detail/:id',
        name: 'CustomerDetail',
        component: () => import('@/views/customers/detail.vue'),
        meta: { 
          title: '客户详情',
          hidden: true,
          showInBreadcrumb: true,
          parentPath: '/customers/list'
        }
      }
    ]
  },
  {
    path: '/analysis',
    component: Layout,
    redirect: '/analysis/index',
    meta: { 
      title: '数据分析', 
      icon: 'TrendCharts', 
      requiresAuth: true 
    },
    children: [
      {
        path: 'index',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { 
          title: '数据分析',
          showInBreadcrumb: true
        }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: {
      title: '系统管理',
      icon: 'Setting', 
      requiresAuth: true, 
      roles: ['ADMIN', 'MANAGER'] 
    },
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/system/users.vue'),
        meta: { 
          title: '用户管理', 
          roles: ['ADMIN'],
          showInBreadcrumb: true
        }
      },
      {
        path: 'schools',
        name: 'SchoolManagement',
        component: () => import('@/views/system/schools.vue'),
        meta: { 
          title: '学校管理',
          showInBreadcrumb: true
        }
      }
    ]
  },
  // 移动端路由
  {
    path: '/m/login',
    component: () => import('@/views/mobile/LoginMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/dashboard',
    component: () => import('@/views/mobile/DashboardMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/visits/list',
    component: () => import('@/views/mobile/VisitListMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/visits/create',
    component: () => import('@/views/mobile/VisitFormMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/visits/edit/:id',
    component: () => import('@/views/mobile/VisitFormMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/visits/detail/:id',
    component: () => import('@/views/mobile/VisitDetailMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/customers/list',
    component: () => import('@/views/mobile/CustomerListMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/customers/create',
    component: () => import('@/views/mobile/CustomerFormMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/customers/edit/:id',
    component: () => import('@/views/mobile/CustomerFormMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/customers/detail/:id',
    component: () => import('@/views/mobile/CustomerDetailMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/system/users',
    component: () => import('@/views/mobile/UserListMobile.vue'),
    meta: { hidden: true }
  },
  {
    path: '/m/system/schools',
    component: () => import('@/views/mobile/SchoolListMobile.vue'),
    meta: { hidden: true }
  },
//   {
//     path: '/profile',
//     component: Layout,
//     redirect: '/profile/index',
//     meta: { 
//       title: '个人中心',
//       requiresAuth: true,
//       hidden: true // 在菜单中隐藏
//     },
//     children: [
//       {
//         path: 'index',
//         name: 'Profile',
//         component: () => import('@/views/profile/index.vue'),
//         meta: { 
//           title: '个人中心',
//           showInBreadcrumb: true
//         }
//       }
//     ]
//   }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
// router.beforeEach((to, from, next) => {
//   const userStore = useUserStore()
  
//   // 设置页面标题
//   document.title = to.meta.title ? `${to.meta.title} - 客户拜访管理系统` : '客户拜访管理系统'
  
//   // 检查是否需要登录
//   if (to.meta.requiresAuth && !userStore.isAuthenticated) {
//     next('/login')
//     return
//   }
  
//   // 检查角色权限
//   if (to.meta.roles && !to.meta.roles.includes(userStore.user?.role)) {
//     ElMessage.error('您没有权限访问该页面')
//     next('/dashboard')
//     return
//   }
  
//   // 已登录用户访问登录页，重定向到首页
//   if (to.path === '/login' && userStore.isAuthenticated) {
//     next('/dashboard')
//     return
//   }
  
//   next()
// })

export default router