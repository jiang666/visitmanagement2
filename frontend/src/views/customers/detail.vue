<template>
  <div class="customer-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>客户详情</span>
          <div class="header-actions" :class="{ 'mobile-actions': isMobile }">
            <el-button @click="handleBack" :size="isMobile ? 'small' : 'default'">返回</el-button>
            <el-button type="primary" @click="handleEdit" :size="isMobile ? 'small' : 'default'">编辑</el-button>
            <el-button @click="handleCreateVisit" :size="isMobile ? 'small' : 'default'">新增拜访</el-button>
          </div>
        </div>
      </template>

      <div class="detail-content" v-if="data">
        <!-- 移动端布局 -->
        <div v-if="isMobile" class="mobile-detail">
          <div class="info-section">
            <h3>基本信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">客户姓名：</span>
                <span class="value">{{ data.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">客户职位：</span>
                <span class="value">{{ data.position || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">联系电话：</span>
                <span class="value">{{ data.phone || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱地址：</span>
                <span class="value">{{ data.email || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">客户级别：</span>
                <span class="value">
                  <el-tag :type="getCustomerLevelType(data.level)" size="small">
                    {{ getCustomerLevelText(data.level) }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h3>学校信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">所在学校：</span>
                <span class="value">{{ data.schoolName }}</span>
              </div>
              <div class="info-item">
                <span class="label">学校类型：</span>
                <span class="value">
                  <el-tag :type="getSchoolTypeColor(data.schoolType)" size="small">
                    {{ getSchoolTypeText(data.schoolType) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">所在院系：</span>
                <span class="value">{{ data.departmentName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">办公地址：</span>
                <span class="value">{{ data.address || '-' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 桌面端布局 -->
        <div v-else class="desktop-detail">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-section">
                <h3>基本信息</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="客户姓名">
                    {{ data.name }}
                  </el-descriptions-item>
                  <el-descriptions-item label="客户职位">
                    {{ data.position || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="联系电话">
                    {{ data.phone || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="邮箱地址">
                    {{ data.email || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="客户级别">
                    <el-tag :type="getCustomerLevelType(data.level)">
                      {{ getCustomerLevelText(data.level) }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>

            <el-col :span="12">
              <div class="info-section">
                <h3>学校信息</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="所在学校">
                    {{ data.schoolName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="学校类型">
                    <el-tag :type="getSchoolTypeColor(data.schoolType)">
                      {{ getSchoolTypeText(data.schoolType) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="所在院系">
                    {{ data.departmentName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="办公地址">
                    {{ data.address || '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 拜访记录 -->
        <div class="visit-section">
          <div class="section-header">
            <h3>拜访记录</h3>
            <el-button type="primary" size="small" @click="handleCreateVisit">
              新增拜访
            </el-button>
          </div>

          <!-- 移动端拜访记录 -->
          <div v-if="isMobile" class="mobile-visits">
            <div v-for="visit in visitRecords" :key="visit.id" class="visit-card" @click="handleViewVisit(visit)">
              <div class="visit-header">
                <div class="visit-date">{{ visit.visitDate }}</div>
                <el-tag :type="getStatusType(visit.status)" size="small">
                  {{ getStatusText(visit.status) }}
                </el-tag>
              </div>
              <div class="visit-content">
                <div class="visit-type">{{ getVisitTypeText(visit.visitType) }}</div>
                <div class="visit-notes">{{ visit.notes || '暂无备注' }}</div>
              </div>
            </div>
          </div>

          <!-- 桌面端拜访记录 -->
          <el-table v-else :data="visitRecords" v-loading="visitLoading">
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
            <el-table-column prop="salesName" label="销售人员" width="100" />
            <el-table-column prop="notes" label="备注" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewVisit(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const isMobile = ref(false)
const loading = ref(false)
const visitLoading = ref(false)
const data = ref(null)
const visitRecords = ref([])

const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
}

// 工具方法
const getCustomerLevelType = (level) => {
  const typeMap = {
    'IMPORTANT': 'danger',
    'NORMAL': 'success',
    'POTENTIAL': 'warning'
  }
  return typeMap[level] || 'info'
}

const getCustomerLevelText = (level) => {
  const textMap = {
    'IMPORTANT': '重要客户',
    'NORMAL': '普通客户',
    'POTENTIAL': '潜在客户'
  }
  return textMap[level] || level
}

const getSchoolTypeColor = (type) => {
  const colorMap = {
    'PROJECT_985': 'danger',
    'PROJECT_211': 'warning',
    'DOUBLE_FIRST_CLASS': 'success',
    'REGULAR': 'info'
  }
  return colorMap[type] || 'info'
}

const getSchoolTypeText = (type) => {
  const textMap = {
    'PROJECT_985': '985工程',
    'PROJECT_211': '211工程',
    'DOUBLE_FIRST_CLASS': '双一流',
    'REGULAR': '普通本科'
  }
  return textMap[type] || type
}

const getStatusType = (status) => {
  const typeMap = {
    'PLANNED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PLANNED': '计划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

const getVisitTypeText = (type) => {
  const textMap = {
    'PHONE': '电话拜访',
    'VISIT': '上门拜访',
    'MEETING': '会议交流'
  }
  return textMap[type] || type
}

const getIntentLevelType = (level) => {
  const typeMap = {
    'A': 'danger',
    'B': 'warning',
    'C': 'success',
    'D': 'info'
  }
  return typeMap[level] || 'info'
}

// 事件处理
const handleBack = () => {
  router.back()
}

const handleEdit = () => {
  router.push(`/customers/edit/${route.params.id}`)
}

const handleCreateVisit = () => {
  router.push({
    path: '/visits/create',
    query: { customerId: route.params.id }
  })
}

const handleViewVisit = (visit) => {
  router.push(`/visits/detail/${visit.id}`)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))

    data.value = {
      id: route.params.id,
      name: '张教授',
      position: '计算机学院院长',
      phone: '13800138001',
      email: 'zhang@pku.edu.cn',
      level: 'IMPORTANT',
      schoolName: '北京大学',
      schoolType: 'PROJECT_985',
      departmentName: '计算机学院',
      address: '北京市海淀区颐和园路5号'
    }

    visitRecords.value = [
      {
        id: 1,
        visitDate: '2024-01-15',
        visitType: 'VISIT',
        status: 'COMPLETED',
        intentLevel: 'A',
        salesName: '李销售',
        notes: '客户很感兴趣，有合作意向'
      }
    ]
  } catch (error) {
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
.customer-detail {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 10px;

      &.mobile-actions {
        flex-direction: column;
        gap: 8px;

        .el-button {
          min-width: 60px;
        }
      }
    }
  }

  .detail-content {
    .mobile-detail {
      .info-section {
        margin-bottom: 24px;

        h3 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 16px;
          padding-bottom: 8px;
          border-bottom: 2px solid #409eff;
        }

        .info-grid {
          .info-item {
            display: flex;
            padding: 12px 0;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
              border-bottom: none;
            }

            .label {
              flex-shrink: 0;
              width: 80px;
              color: #909399;
              font-size: 14px;
            }

            .value {
              flex: 1;
              color: #303133;
              font-size: 14px;
            }
          }
        }
      }
    }

    .desktop-detail {
      .info-section {
        margin-bottom: 30px;

        h3 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 16px;
        }
      }
    }

    .visit-section {
      margin-top: 30px;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        h3 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0;
        }
      }

      .mobile-visits {
        .visit-card {
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

          .visit-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .visit-date {
              font-weight: 600;
              color: #303133;
            }
          }

          .visit-content {
            .visit-type {
              font-size: 14px;
              color: #606266;
              margin-bottom: 4px;
            }

            .visit-notes {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }
    }
  }
}
</style>