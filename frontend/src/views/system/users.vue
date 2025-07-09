<template>
    <div class="user-management">
      <el-card>
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :model="searchForm" inline>
            <el-form-item label="关键词">
              <el-input
                v-model="searchForm.keyword"
                placeholder="用户名、姓名、邮箱"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            
            <el-form-item label="角色">
              <el-select
                v-model="searchForm.role"
                placeholder="全部角色"
                clearable
                class="standard-select"
              >
                <el-option
                  v-for="item in roleOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="全部状态"
                clearable
                class="standard-select"
              >
                <el-option label="激活" value="ACTIVE" />
                <el-option label="禁用" value="INACTIVE" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="部门">
              <el-input
                v-model="searchForm.department"
                placeholder="部门名称"
                clearable
                style="width: 150px"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 操作栏 -->
        <div class="action-bar">
          <div class="action-left">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
            <el-button
              type="danger"
              :disabled="!selectedRows.length"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
        
        <!-- 数据表格 -->
        <el-table
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column label="用户信息" min-width="200">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="40" :src="row.avatarUrl">
                  {{ row.realName?.charAt(0) }}
                </el-avatar>
                <div class="info-text">
                  <div class="name">{{ row.realName }}</div>
                  <div class="username">{{ row.username }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="email" label="邮箱" width="180" />
          
          <el-table-column prop="phone" label="手机号" width="120" />
          
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleType(row.role)">
                {{ getRoleText(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="department" label="部门" width="120" />
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="ACTIVE"
                inactive-value="INACTIVE"
                @change="handleStatusChange(row)"
              />
            </template>
          </el-table-column>
          
          <el-table-column prop="lastLoginAt" label="最后登录" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.lastLoginAt) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="actions">
                <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button size="small" type="warning" @click="handleResetPassword(row)">重置密码</el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(row)"
                  :disabled="row.role === 'ADMIN' && row.id === currentUserId"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </el-card>
  
      <!-- 用户表单对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑用户' : '新增用户'"
        width="600px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="userForm"
          :rules="userRules"
          label-width="100px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" placeholder="请输入用户名" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password" v-if="!isEdit">
            <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
          </el-form-item>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          
          <el-form-item label="角色" prop="role">
            <el-select
              v-model="userForm.role"
              placeholder="请选择角色"
              style="width: 100%"
              class="standard-select"
            >
              <el-option
                v-for="item in roleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="部门" prop="department">
            <el-input v-model="userForm.department" placeholder="请输入部门" />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { useUserStore } from '@/stores/user'
  import {
    Search, Refresh, Plus, Delete
  } from '@element-plus/icons-vue'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  updateUserStatus,
  resetUserPassword
} from '@/api/users'
import { ElMessage, ElMessageBox } from 'element-plus'

  const userStore = useUserStore()

  const roleOptions = [
    { label: '管理员', value: 'ADMIN' },
    { label: '经理', value: 'MANAGER' },
    { label: '销售', value: 'SALES' }
  ]
  
  const loading = ref(false)
  const dialogVisible = ref(false)
  const submitting = ref(false)
  const tableData = ref([])
  const selectedRows = ref([])
  const formRef = ref()
  
  const currentUserId = computed(() => userStore.user?.id)
  const isEdit = ref(false)
  
  const searchForm = reactive({
    keyword: '',
    role: null,
    status: null,
    department: ''
  })
  
  const pagination = reactive({
    page: 1,
    size: 20,
    total: 0
  })
  
  const userForm = reactive({
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    role: 'SALES',
    department: ''
  })
  
  const userRules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    realName: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' }
    ],
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ],
    role: [
      { required: true, message: '请选择角色', trigger: 'change' }
    ]
  }
  
  const loadData = async () => {
    loading.value = true
    try {
      const params = {
        page: pagination.page - 1,
        size: pagination.size,
        keyword: searchForm.keyword,
        role: searchForm.role,
        status: searchForm.status,
        department: searchForm.department
      }
      
      const response = await getUserList(params)
      const { content, totalElements } = response.data
      
      tableData.value = content
      pagination.total = totalElements
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      loading.value = false
    }
  }
  
  const handleSearch = () => {
    pagination.page = 1
    loadData()
  }
  
  const handleReset = () => {
    Object.assign(searchForm, {
      keyword: '',
      role: null,
      status: null,
      department: ''
    })
    pagination.page = 1
    loadData()
  }
  
  const handleCreate = () => {
    isEdit.value = false
    Object.assign(userForm, {
      username: '',
      password: '',
      realName: '',
      email: '',
      phone: '',
      role: 'SALES',
      department: ''
    })
    dialogVisible.value = true
  }
  
  const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(userForm, { ...row, password: '' })
    dialogVisible.value = true
  }
  
  const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      
      submitting.value = true
      try {
        if (isEdit.value) {
          const payload = { ...userForm }
          if (!payload.password) {
            delete payload.password
          }
          await updateUser(userForm.id, payload)
          ElMessage.success('更新成功')
        } else {
          await createUser(userForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    })
  }
  
  const handleDelete = async (row) => {
    try {
      await ElMessageBox.confirm('确定要删除这个用户吗？', '确认删除', {
        type: 'warning'
      })
      
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
  
  const handleBatchDelete = async () => {
    try {
      await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？`, '确认删除', {
        type: 'warning'
      })
      
      const ids = selectedRows.value.map(row => row.id)
      await batchDeleteUsers(ids)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
  
  const handleStatusChange = async (row) => {
    const previous = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    try {
      await updateUserStatus(row.id, row.status)
      ElMessage.success('状态更新成功')
    } catch (error) {
      row.status = previous
      ElMessage.error('状态更新失败')
    }
  }
  
  const handleResetPassword = async (row) => {
    try {
      await ElMessageBox.prompt('请输入新密码', '重置密码', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
        inputValidator: (value) => {
          if (!value || value.length < 6) {
            return '密码长度不能少于6位'
          }
          return true
        }
      })
      
      await resetUserPassword(row.id, result.value)
      ElMessage.success('密码重置成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('密码重置失败')
      }
    }
  }
  
  const handleSelectionChange = (selection) => {
    selectedRows.value = selection
  }
  
  const handleDialogClose = () => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
  }
  
  const getRoleType = (role) => {
    const typeMap = {
      'ADMIN': 'danger',
      'MANAGER': 'warning',
      'SALES': 'success'
    }
    return typeMap[role] || ''
  }
  
  const getRoleText = (role) => {
    const textMap = {
      'ADMIN': '管理员',
      'MANAGER': '经理',
      'SALES': '销售'
    }
    return textMap[role] || role
  }
  
  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
  
  onMounted(() => {
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .user-management {
    .search-bar {
      margin-bottom: 20px;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 4px;
    }
    
    .action-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      
      .info-text {
        margin-left: 12px;
        
        .name {
          font-weight: bold;
          margin-bottom: 4px;
        }
        
        .username {
          font-size: 12px;
          color: #999;
        }
      }
    }
    
    .pagination {
      margin-top: 20px;
      text-align: right;
    }

    .actions {
      display: flex;
      justify-content: center;
      gap: 8px;
    }
  }
  </style>
