<template>
  <div class="visit-list">
    <el-card class="search-card">
      <!-- 搜索表单 -->
      <el-form
          :model="searchForm"
          class="search-form"
          :label-width="isMobile ? '0' : '80px'"
      >
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item :label="isMobile ? '' : '关键词'">
              <el-input
                  v-model="searchForm.keyword"
                  :placeholder="isMobile ? '搜索客户姓名、学校' : '请输入客户姓名、学校'"
                  clearable
              />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item :label="isMobile ? '' : '拜访类型'">
              <el-select
                  v-model="searchForm.visitType"
                  :placeholder="isMobile ? '选择拜访类型' : '请选择拜访类型'"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="type in visitTypeOptions"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item :label="isMobile ? '' : '状态'">
              <el-select
                  v-model="searchForm.status"
                  :placeholder="isMobile ? '选择状态' : '请选择状态'"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="status in statusOptions"
                    :key="status.value"
                    :label="status.label"
                    :value="status.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item :label="isMobile ? '' : '意向等级'">
              <el-select
                  v-model="searchForm.intentLevel"
                  :placeholder="isMobile ? '选择意向等级' : '请选择意向等级'"
                  clearable
                  style="width: 100%"
              >
                <el-option label="非常高" value="VERY_HIGH" />
                <el-option label="高" value="HIGH" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="低" value="LOW" />
                <el-option label="非常低" value="VERY_LOW" />
                <el-option label="无意向" value="NO_INTENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 操作按钮 -->
        <div class="search-actions" :class="{ 'mobile-actions': isMobile }">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <!-- 操作栏 -->
      <div class="action-bar" :class="{ 'mobile-action-bar': isMobile }">
        <div class="action-left">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增拜访
          </el-button>
          <el-button
              type="danger"
              :disabled="!selectedRows.length"
              @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button @click="handleExport" v-if="!isMobile">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <!-- 移动端卡片列表 -->
      <div v-if="isMobile" class="mobile-list">
        <div
            v-for="item in tableData"
            :key="item.id"
            class="mobile-card"
            @click="handleView(item)"
        >
          <div class="card-header">
            <div class="customer-info">
              <div class="customer-name">{{ item.customerName }}</div>
              <div class="customer-detail">{{ item.customerPosition }} | {{ item.schoolName }}</div>
            </div>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>

          <div class="card-content">
            <div class="info-row">
              <span class="label">拜访日期：</span>
              <span>{{ item.visitDate }}</span>
            </div>
            <div class="info-row">
              <span class="label">拜访类型：</span>
              <span>{{ getVisitTypeText(item.visitType) }}</span>
            </div>
            <div class="info-row">
              <span class="label">意向等级：</span>
              <el-tag :type="getIntentLevelType(item.intentLevel)" size="small">
                {{ getIntentLevelText(item.intentLevel) }}
              </el-tag>
            </div>
            <div class="info-row">
              <span class="label">销售人员：</span>
              <span>{{ item.salesName }}</span>
            </div>
          </div>

          <div class="card-actions">
            <el-button size="small" @click.stop="handleEdit(item)">编辑</el-button>
            <el-button size="small" type="danger" @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </div>

        <div v-if="!tableData.length && !loading" class="empty-state">
          <el-empty description="暂无数据" />
        </div>
      </div>

      <!-- 桌面端表格 -->
      <el-table
          v-else
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="客户信息" min-width="200">
          <template #default="{ row }">
            <div>
              <div class="customer-name">{{ row.customerName }}</div>
              <div class="customer-info">
                {{ row.customerPosition }} | {{ row.schoolName }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="visitDate" label="拜访日期" width="120" />
        <el-table-column prop="visitType" label="拜访类型" width="100">
          <template #default="{ row }">
            {{ getVisitTypeText(row.visitType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="intentLevel" label="意向等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getIntentLevelType(row.intentLevel)">
              {{ getIntentLevelText(row.intentLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salesName" label="销售人员" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" :class="{ 'mobile-pagination': isMobile }">
        <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="isMobile ? [10, 20] : [10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            :small="isMobile"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Download } from '@element-plus/icons-vue'
import { getVisitList, deleteVisit, batchDeleteVisits, exportVisits } from '@/api/visits'

const router = useRouter()

// 响应式状态
const isMobile = ref(false)
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  visitType: null,
  status: null,
  intentLevel: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 选项数据
const visitTypeOptions = [
  { label: '面对面拜访', value: 'FACE_TO_FACE' },
  { label: '电话拜访', value: 'PHONE_CALL' },
  { label: '视频拜访', value: 'VIDEO_CALL' },
  { label: '邮件沟通', value: 'EMAIL' },
  { label: '微信沟通', value: 'WECHAT' },
  { label: '其他', value: 'OTHER' }
]

const statusOptions = [
  { label: '已安排', value: 'SCHEDULED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已延期', value: 'POSTPONED' },
  { label: '未出现', value: 'NO_SHOW' }
]

// 工具方法
const getStatusType = (status) => {
  const typeMap = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger',
    'POSTPONED': 'warning',
    'NO_SHOW': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'SCHEDULED': '已安排',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'POSTPONED': '已延期',
    'NO_SHOW': '未出现'
  }
  return textMap[status] || status
}

const getVisitTypeText = (type) => {
  const textMap = {
    'FACE_TO_FACE': '面对面拜访',
    'PHONE_CALL': '电话拜访',
    'VIDEO_CALL': '视频拜访',
    'EMAIL': '邮件沟通',
    'WECHAT': '微信沟通',
    'OTHER': '其他'
  }
  return textMap[type] || type
}

const getIntentLevelType = (level) => {
  const typeMap = {
    'VERY_HIGH': 'danger',
    'HIGH': 'warning', 
    'MEDIUM': 'success',
    'LOW': 'info',
    'VERY_LOW': 'info',
    'NO_INTENT': 'info'
  }
  return typeMap[level] || 'info'
}

const getIntentLevelText = (level) => {
  const textMap = {
    'VERY_HIGH': '非常高',
    'HIGH': '高',
    'MEDIUM': '中等',
    'LOW': '低',
    'VERY_LOW': '非常低',
    'NO_INTENT': '无意向'
  }
  return textMap[level] || level
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    visitType: null,
    status: null,
    intentLevel: null
  })
  handleSearch()
}

const handleCreate = () => {
  router.push('/visits/create')
}

const handleView = (row) => {
  router.push(`/visits/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/visits/edit/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条拜访记录吗？', '警告', {
      type: 'warning'
    })
    
    await deleteVisit(row.id)
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条记录吗？`, '警告', {
      type: 'warning'
    })
    
    const ids = selectedRows.value.map(row => row.id)
    await batchDeleteVisits(ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleExport = async () => {
  try {
    const params = {
      keyword: searchForm.keyword,
      visitType: searchForm.visitType,
      status: searchForm.status,
      intentLevel: searchForm.intentLevel
    }
    
    const response = await exportVisits(params)
    
    // 创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `拜访记录_${new Date().getTime()}.xlsx`
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

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      visitType: searchForm.visitType,
      status: searchForm.status,
      intentLevel: searchForm.intentLevel
    }
    
    const response = await getVisitList(params)
    const { content, totalElements } = response.data
    
    tableData.value = content
    pagination.total = totalElements
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style lang="scss" scoped>
.visit-list {
  .search-card {
    margin-bottom: 20px;

    @media (max-width: 768px) {
      margin-bottom: 10px;
    }
  }

  .search-form {
    .search-actions {
      display: flex;
      gap: 10px;
      margin-top: 16px;

      &.mobile-actions {
        flex-direction: column;

        .el-button {
          width: 100%;
        }
      }
    }
  }

  .table-card {
    .action-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      &.mobile-action-bar {
        flex-direction: column;
        gap: 10px;

        .action-left {
          display: flex;
          flex-direction: column;
          gap: 10px;
          width: 100%;

          .el-button {
            width: 100%;
          }
        }
      }
    }

    .mobile-list {
      .mobile-card {
        background: white;
        border: 1px solid #ebeef5;
        border-radius: 8px;
        padding: 16px;
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        }

        &:last-child {
          margin-bottom: 0;
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;

          .customer-info {
            flex: 1;

            .customer-name {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .customer-detail {
              font-size: 12px;
              color: #909399;
            }
          }
        }

        .card-content {
          .info-row {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            font-size: 14px;

            &:last-child {
              margin-bottom: 0;
            }

            .label {
              color: #909399;
              min-width: 70px;
            }
          }
        }

        .card-actions {
          display: flex;
          gap: 8px;
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid #f0f0f0;

          .el-button {
            flex: 1;
          }
        }
      }

      .empty-state {
        text-align: center;
        padding: 40px 0;
      }
    }

    .pagination {
      margin-top: 20px;
      text-align: right;

      &.mobile-pagination {
        text-align: center;

        .el-pagination {
          justify-content: center;
        }
      }
    }
  }
}

// 桌面端表格样式优化
.customer-name {
  font-weight: 600;
  color: #303133;
}

.customer-info {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>