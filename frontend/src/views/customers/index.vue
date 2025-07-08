<template>
    <div class="customer-list">
      <el-card>
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :model="searchForm" inline>
            <el-form-item label="关键词">
              <el-input
                v-model="searchForm.keyword"
                placeholder="客户姓名、职位、学校"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            
            <el-form-item label="影响力等级">
              <el-select v-model="searchForm.influenceLevel" placeholder="全部等级" clearable>
                <el-option label="高影响力" value="HIGH" />
                <el-option label="中等影响力" value="MEDIUM" />
                <el-option label="低影响力" value="LOW" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="决策权力">
              <el-select v-model="searchForm.decisionPower" placeholder="全部类型" clearable>
                <el-option label="决策者" value="DECISION_MAKER" />
                <el-option label="影响者" value="INFLUENCER" />
                <el-option label="使用者" value="USER" />
                <el-option label="其他" value="OTHER" />
              </el-select>
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
              新增客户
            </el-button>
            <el-button
              type="danger"
              :disabled="!selectedRows.length"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
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
          
          <el-table-column label="客户信息" min-width="200">
            <template #default="{ row }">
              <div>
                <div class="customer-name">{{ row.name }}</div>
                <div class="customer-info">
                  {{ row.position }} {{ row.title ? `| ${row.title}` : '' }}
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="学校院系" min-width="200">
            <template #default="{ row }">
              <div>
                <div class="school-name">{{ row.schoolName }}</div>
                <div class="department-name">{{ row.departmentName }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="phone" label="联系电话" width="120" />
          
          <el-table-column prop="email" label="邮箱" width="180" />
          
          <el-table-column prop="influenceLevel" label="影响力" width="100">
            <template #default="{ row }">
              <el-tag :type="getInfluenceLevelType(row.influenceLevel)">
                {{ getInfluenceLevelText(row.influenceLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="decisionPower" label="决策权力" width="100">
            <template #default="{ row }">
              <el-tag :type="getDecisionPowerType(row.decisionPower)">
                {{ getDecisionPowerText(row.decisionPower) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="visitCount" label="拜访次数" width="100" />
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">
                删除
              </el-button>
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
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import {
    Search, Refresh, Plus, Delete, Upload, Download
  } from '@element-plus/icons-vue'
  import {
    getCustomerList,
    deleteCustomer,
    batchDeleteCustomers,
    exportCustomers
  } from '@/api/customers'
  
  const router = useRouter()
  
  const loading = ref(false)
  const tableData = ref([])
  const selectedRows = ref([])
  
  const searchForm = reactive({
    keyword: '',
    influenceLevel: '',
    decisionPower: ''
  })
  
  const pagination = reactive({
    page: 1,
    size: 20,
    total: 0
  })
  
  const loadData = async () => {
    loading.value = true
    try {
      const params = {
        page: pagination.page - 1,
        size: pagination.size,
        keyword: searchForm.keyword,
        influenceLevel: searchForm.influenceLevel,
        decisionPower: searchForm.decisionPower
      }
      
      const response = await getCustomerList(params)
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
      influenceLevel: '',
      decisionPower: ''
    })
    pagination.page = 1
    loadData()
  }
  
  const handleCreate = () => {
    router.push('/customers/create')
  }
  
  const handleView = (row) => {
    router.push(`/customers/detail/${row.id}`)
  }
  
  const handleEdit = (row) => {
    router.push(`/customers/edit/${row.id}`)
  }
  
  const handleDelete = async (row) => {
    try {
      await ElMessageBox.confirm('确定要删除这个客户吗？', '确认删除', {
        type: 'warning'
      })
      
      await deleteCustomer(row.id)
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
      await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个客户吗？`, '确认删除', {
        type: 'warning'
      })
      
      const ids = selectedRows.value.map(row => row.id)
      await batchDeleteCustomers(ids)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
  
  const handleImport = () => {
    ElMessage.info('导入功能开发中...')
  }
  
  const handleExport = async () => {
    try {
      const params = {
        keyword: searchForm.keyword,
        influenceLevel: searchForm.influenceLevel,
        decisionPower: searchForm.decisionPower
      }
      
      const response = await exportCustomers(params)
      
      // 创建下载链接
      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `客户列表_${new Date().getTime()}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
    } catch (error) {
      ElMessage.error('导出失败')
    }
  }
  
  const handleSelectionChange = (selection) => {
    selectedRows.value = selection
  }
  
  // 影响力等级相关方法
  const getInfluenceLevelType = (level) => {
    const typeMap = {
      'HIGH': 'danger',
      'MEDIUM': 'warning',
      'LOW': 'info'
    }
    return typeMap[level] || ''
  }
  
  const getInfluenceLevelText = (level) => {
    const textMap = {
      'HIGH': '高',
      'MEDIUM': '中',
      'LOW': '低'
    }
    return textMap[level] || level
  }
  
  const getDecisionPowerType = (power) => {
    const typeMap = {
      'DECISION_MAKER': 'danger',
      'INFLUENCER': 'warning',
      'USER': 'info',
      'OTHER': ''
    }
    return typeMap[power] || ''
  }
  
  const getDecisionPowerText = (power) => {
    const textMap = {
      'DECISION_MAKER': '决策者',
      'INFLUENCER': '影响者',
      'USER': '使用者',
      'OTHER': '其他'
    }
    return textMap[power] || power
  }
  
  onMounted(() => {
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .customer-list {
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
    
    .customer-name {
      font-weight: bold;
      margin-bottom: 4px;
    }
    
    .customer-info {
      font-size: 12px;
      color: #999;
    }
    
    .school-name {
      font-weight: bold;
      margin-bottom: 4px;
    }
    
    .department-name {
      font-size: 12px;
      color: #999;
    }
    
    .pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
  </style>