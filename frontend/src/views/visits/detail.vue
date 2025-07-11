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
                    {{ data.intentLevel }}类
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
                      {{ data.intentLevel }}类
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
  router.push(`/visits/edit/${route.params.id}`)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟数据
    data.value = {
      id: route.params.id,
      customerName: '张三',
      customerPosition: '教授',
      schoolName: '北京大学',
      departmentName: '计算机学院',
      salesName: '李销售',
      visitDate: '2024-01-15',
      visitTime: '14:30',
      visitDuration: 120,
      visitType: 'VISIT',
      status: 'COMPLETED',
      intentLevel: 'A',
      purpose: '介绍我司最新的教学管理系统解决方案，了解学校的信息化建设需求。',
      content: '详细介绍了我司的教学管理系统功能特点，包括学生管理、课程管理、成绩管理等核心模块。客户对系统的用户界面和操作便捷性表示认可。',
      feedback: '客户对我们的产品很感兴趣，特别是移动端功能。提出希望能够支持多校区管理和数据统计分析功能。',
      nextPlan: '下周三安排技术人员进行现场演示，重点展示多校区管理和数据分析功能。',
      notes: '客户预算充足，决策权在手，是重点跟进客户。'
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