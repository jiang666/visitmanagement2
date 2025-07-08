<template>
    <div class="visit-detail">
      <el-card v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>拜访记录详情</span>
            <div>
              <el-button @click="handleBack">返回</el-button>
              <el-button type="primary" @click="handleEdit">编辑</el-button>
            </div>
          </div>
        </template>
  
        <div class="detail-content" v-if="data">
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
                    {{ data.durationMinutes }}分钟
                  </el-descriptions-item>
                  <el-descriptions-item label="拜访类型">
                    <el-tag>{{ getVisitTypeText(data.visitType) }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="拜访状态">
                    <el-tag :type="getStatusType(data.status)">
                      {{ getStatusText(data.status) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="意向等级">
                    <el-tag :type="getIntentLevelType(data.intentLevel)">
                      {{ data.intentLevel }}类
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="拜访评分">
                    <el-rate :model-value="data.rating" disabled />
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>
          </el-row>
  
          <div class="info-section">
            <h3>详细内容</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="content-item">
                  <h4>可办事项</h4>
                  <p>{{ data.businessItems || '-' }}</p>
                </div>
                
                <div class="content-item">
                  <h4>需求痛点</h4>
                  <p>{{ data.painPoints || '-' }}</p>
                </div>
                
                <div class="content-item">
                  <h4>竞品信息</h4>
                  <p>{{ data.competitors || '-' }}</p>
                </div>
              </el-col>
              
              <el-col :span="12">
                <div class="content-item">
                  <h4>预算范围</h4>
                  <p>{{ data.budgetRange || '-' }}</p>
                </div>
                
                <div class="content-item">
                  <h4>决策时间线</h4>
                  <p>{{ data.decisionTimeline || '-' }}</p>
                </div>
                
                <div class="content-item">
                  <h4>下一步计划</h4>
                  <p>{{ data.nextStep || '-' }}</p>
                </div>
              </el-col>
            </el-row>
          </div>
  
          <div class="info-section">
            <h3>其他信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="拜访地点">
                {{ data.location || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="天气情况">
                {{ data.weather || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="跟进日期">
                {{ data.followUpDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="添加微信">
                <el-tag v-if="data.wechatAdded" type="success">已添加</el-tag>
                <el-tag v-else type="info">未添加</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="留下资料" :span="2">
                {{ data.materialsLeft || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="拜访备注" :span="2">
                {{ data.notes || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { getVisitDetail } from '@/api/visits'
  
  const route = useRoute()
  const router = useRouter()
  
  const loading = ref(false)
  const data = ref(null)
  
  const loadData = async () => {
    loading.value = true
    try {
      const response = await getVisitDetail(route.params.id)
      data.value = response.data
    } catch (error) {
      ElMessage.error('加载数据失败')
      router.back()
    } finally {
      loading.value = false
    }
  }
  
  const handleBack = () => {
    router.back()
  }
  
  const handleEdit = () => {
    router.push(`/visits/edit/${route.params.id}`)
  }
  
  // 状态相关方法
  const getStatusType = (status) => {
    const typeMap = {
      'COMPLETED': 'success',
      'SCHEDULED': 'warning',
      'CANCELLED': 'danger',
      'POSTPONED': 'info'
    }
    return typeMap[status] || ''
  }
  
  const getStatusText = (status) => {
    const textMap = {
      'COMPLETED': '已完成',
      'SCHEDULED': '已安排',
      'CANCELLED': '已取消',
      'POSTPONED': '已延期'
    }
    return textMap[status] || status
  }
  
  const getVisitTypeText = (type) => {
    const textMap = {
      'FIRST_VISIT': '首次拜访',
      'FOLLOW_UP': '跟进拜访',
      'TECHNICAL': '技术交流',
      'BUSINESS': '商务谈判',
      'AFTER_SALES': '售后服务'
    }
    return textMap[type] || type
  }
  
  const getIntentLevelType = (level) => {
    const typeMap = {
      'A': 'danger',
      'B': 'warning',
      'C': 'info',
      'D': ''
    }
    return typeMap[level] || ''
  }
  
  onMounted(() => {
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .visit-detail {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .info-section {
      margin-bottom: 30px;
      
      h3 {
        margin-bottom: 16px;
        color: #333;
        border-bottom: 2px solid #409eff;
        padding-bottom: 8px;
      }
    }
    
    .content-item {
      margin-bottom: 20px;
      
      h4 {
        color: #666;
        margin-bottom: 8px;
        font-size: 14px;
      }
      
      p {
        background: #f5f7fa;
        padding: 12px;
        border-radius: 4px;
        min-height: 80px;
        margin: 0;
        line-height: 1.6;
      }
    }
  }
  </style>