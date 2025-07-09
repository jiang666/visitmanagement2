<template>
  <div class="visit-list">
    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="客户姓名、学校、院系"
              clearable
              style="width: 200px"
            />
          </el-form-item>

          <el-form-item label="拜访日期">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="全部状态"
              clearable
              class="standard-select"
            >
              <el-option label="已安排" value="SCHEDULED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="已延期" value="POSTPONED" />
            </el-select>
          </el-form-item>

          <el-form-item label="意向等级">
            <el-select
              v-model="searchForm.intentLevel"
              placeholder="全部等级"
              clearable
              class="standard-select"
            >
              <el-option label="A类" value="A" />
              <el-option label="B类" value="B" />
              <el-option label="C类" value="C" />
              <el-option label="D类" value="D" />
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
              {{ row.intentLevel }}类
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <el-rate
              v-if="row.rating"
              :model-value="row.rating"
              disabled
              size="small"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="wechatAdded" label="微信" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.wechatAdded" color="#67C23A">
              <CircleCheck />
            </el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="salesName" label="销售" width="100" />

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
  Search, Refresh, Plus, Delete, Download,
  CircleCheck
} from '@element-plus/icons-vue'
import {
  getVisitList,
  deleteVisit,
  batchDeleteVisits,
  exportVisits
} from '@/api/visits'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

const searchForm = reactive({
  keyword: '',
  dateRange: [],
  status: '',
  intentLevel: ''
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
      status: searchForm.status,
      intentLevel: searchForm.intentLevel
    }

    if (searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const response = await getVisitList(params)
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
  searchForm.keyword = ''
  searchForm.dateRange = []
  searchForm.status = ''
  searchForm.intentLevel = ''
  pagination.page = 1
  loadData()
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
    await ElMessageBox.confirm('确定要删除这条拜访记录吗？', '确认删除', { type: 'warning' })
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条记录吗？`, '确认删除', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await batchDeleteVisits(ids)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleExport = async () => {
  try {
    const params = {
      keyword: searchForm.keyword,
      status: searchForm.status,
      intentLevel: searchForm.intentLevel
    }
    if (searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const response = await exportVisits(params)
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
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

// 辅助文本、样式函数
const getStatusType = (status) => {
  const map = {
    'COMPLETED': 'success',
    'SCHEDULED': 'warning',
    'CANCELLED': 'danger',
    'POSTPONED': 'info'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    'COMPLETED': '已完成',
    'SCHEDULED': '已安排',
    'CANCELLED': '已取消',
    'POSTPONED': '已延期'
  }
  return map[status] || status
}

const getVisitTypeText = (type) => {
  const map = {
    'FIRST_VISIT': '首次拜访',
    'FOLLOW_UP': '跟进拜访',
    'TECHNICAL': '技术交流',
    'BUSINESS': '商务谈判',
    'AFTER_SALES': '售后服务'
  }
  return map[type] || type
}

const getIntentLevelType = (level) => {
  const map = {
    'A': 'danger',
    'B': 'warning',
    'C': 'info',
    'D': ''
  }
  return map[level] || ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.visit-list {
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

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
