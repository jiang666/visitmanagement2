<template>
  <div class="visit-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>拜访记录详情</span>
          <div class="header-actions" :class="{ 'mobile-actions': isMobile }">
            <el-button @click="handleBack" :size="isMobile ? 'small' : 'default'">返回</el-button>
            <el-button type="primary" @click="handleEdit" :size="isMobile ? 'small' : 'default'">编辑</el-button>
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
                <span class="value">{{ data.customerName }}</span>
              </div>
              <div class="info-item">
                <span class="label">客户职位：</span>
                <span class="value">{{ data.customerPosition }}</span>
              </div>
              <div class="info-item">
                <span class="label">所在学校：</span>
                <span class="value">{{ data.schoolName }}</span>
              </div>
              <div class="info-item">
                <span class="label">所在院系：</span>
                <span class="value">{{ data.departmentName }}</span>
              </div>
              <div class="info-item">
                <span class="label">销售人员：</span>
                <span class="value">{{ data.salesName }}</span>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h3>拜访信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">拜访日期：</span>
                <span class="value">{{ data.visitDate }}</span>
              </div>
              <div class="info-item">
                <span class="label">拜访时间：</span>
                <span class="value">{{ data.visitTime || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">拜访时长：</span>
                <span class="value">{{ data.visitDuration ? data.visitDuration + '分钟' : '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">拜访类型：</span>
                <span class="value">{{ getVisitTypeText(data.visitType) }}</span>
              </div>
              <div class="info-item">
                <span class="label">状态：</span>
                <span class="value">
                  <el-tag :type="getStatusType(data.status)" size="small">
                    {{ getStatusText(data.status) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">意向等级：</span>
                <span class="value">
                  <el-tag :type="getIntentLevelType(data.intentLevel)" size="small">
                    {{ getIntentLevelText(data.intentLevel) }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>

          <div class="content-section">
            <h3>拜访详情</h3>
            <div class="content-item">
              <h4>拜访目的</h4>
              <p>{{ data.purpose || '暂无' }}</p>
            </div>
            <div class="content-item">
              <h4>交流内容</h4>
              <p>{{ data.content || '暂无' }}</p>
            </div>
            <div class="content-item">
              <h4>客户反馈</h4>
              <p>{{ data.feedback || '暂无' }}</p>
            </div>
            <div class="content-item">
              <h4>下次计划</h4>
              <p>{{ data.nextPlan || '暂无' }}</p>
            </div>
            <div class="content-item" v-if="data.notes">
              <h4>备注</h4>
              <p>{{ data.notes }}</p>
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
                    {{ data.customerName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="客户职位">
                    {{ data.customerPosition }}
                  </el-descriptions-item>
                  <el-descriptions-item label="所在学校">
                    {{ data.schoolName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="所在院系">
                    {{ data.departmentName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="销售人员">
                    {{ data.salesName }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>

            <el-col :span="12">
              <div class="info-section">
                <h3>拜访信息</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="拜访日期">
                    {{ data.visitDate }}
                  </el-descriptions-item>
                  <el-descriptions-item label="拜访时间">
                    {{ data.visitTime || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="拜访时长">
                    {{ data.visitDuration ? data.visitDuration + '分钟' : '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="拜访类型">
                    {{ getVisitTypeText(data.visitType) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="状态">
                    <el-tag :type="getStatusType(data.status)">
                      {{ getStatusText(data.status) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="意向等级">
                    <el-tag :type="getIntentLevelType(data.intentLevel)">
                      {{ getIntentLevelText(data.intentLevel) }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>
          </el-row>

          <div class="content-section">
            <h3>拜访详情</h3>
            <el-row :gutter="20">
              <el-col :span="24">
                <div class="content-item">
                  <h4>拜访目的</h4>
                  <p>{{ data.purpose || '暂无' }}</p>
                </div>
              </el-col>

              <el-col :span="24">
                <div class="content-item">
                  <h4>交流内容</h4>
                  <p>{{ data.content || '暂无' }}</p>
                </div>
              </el-col>

              <el-col :span="24">
                <div class="content-item">
                  <h4>客户反馈</h4>
                  <p>{{ data.feedback || '暂无' }}</p>
                </div>
              </el-col>

              <el-col :span="24">
                <div class="content-item">
                  <h4>下次计划</h4>
                  <p>{{ data.nextPlan || '暂无' }}</p>
                </div>
              </el-col>

              <el-col :span="24" v-if="data.notes">
                <div class="content-item">
                  <h4>备注</h4>
                  <p>{{ data.notes }}</p>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVisitDetail } from '@/api/visits'

const route = useRoute()
const router = useRouter()

// 响应式状态
const isMobile = ref(false)
const loading = ref(false)
const data = ref(null)

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
}

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
const handleBack = () => {
  router.back()
}

const handleEdit = () => {
  router.push(`/visits/edit/${route.params.id}`)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await getVisitDetail(route.params.id)
    const visit = response.data
    data.value = {
      ...visit,
      visitDuration: visit.durationMinutes,
      purpose: visit.businessItems,
      content: visit.painPoints,
      feedback: visit.competitors,
      nextPlan: visit.nextStep
    }
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
.visit-detail {
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

      .content-section {
        h3 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 16px;
          padding-bottom: 8px;
          border-bottom: 2px solid #409eff;
        }

        .content-item {
          margin-bottom: 20px;

          &:last-child {
            margin-bottom: 0;
          }

          h4 {
            font-size: 14px;
            font-weight: 600;
            color: #606266;
            margin-bottom: 8px;
          }

          p {
            color: #303133;
            line-height: 1.6;
            margin: 0;
            padding: 12px;
            background: #fafafa;
            border-radius: 4px;
            font-size: 14px;
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

      .content-section {
        margin-top: 30px;

        h3 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 20px;
        }

        .content-item {
          margin-bottom: 20px;

          h4 {
            font-size: 14px;
            font-weight: 600;
            color: #606266;
            margin-bottom: 8px;
          }

          p {
            color: #303133;
            line-height: 1.6;
            margin: 0;
            padding: 16px;
            background: #fafafa;
            border-radius: 4px;
          }
        }
      }
    }
  }
}
</style>