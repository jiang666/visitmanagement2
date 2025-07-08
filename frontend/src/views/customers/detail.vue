<template>
    <div class="customer-detail">
      <el-card v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>客户详情</span>
            <div>
              <el-button @click="handleBack">返回</el-button>
              <el-button type="primary" @click="handleEdit">编辑</el-button>
              <el-button type="success" @click="handleCreateVisit">新增拜访</el-button>
            </div>
          </div>
        </template>
  
        <div class="detail-content" v-if="data">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-section">
                <h3>基本信息</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="姓名">
                    {{ data.name }}
                  </el-descriptions-item>
                  <el-descriptions-item label="职位">
                    {{ data.position || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="职称">
                    {{ data.title || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="联系电话">
                    {{ data.phone || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="邮箱">
                    {{ data.email || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="微信号">
                    {{ data.wechat || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="生日">
                    {{ data.birthday || '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>
            
            <el-col :span="12">
              <div class="info-section">
                <h3>工作信息</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="所在学校">
                    {{ data.schoolName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="所在院系">
                    {{ data.departmentName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="办公地点">
                    {{ data.officeLocation || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="楼层房间">
                    {{ data.floorRoom || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="影响力等级">
                    <el-tag :type="getInfluenceLevelType(data.influenceLevel)">
                      {{ getInfluenceLevelText(data.influenceLevel) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="决策权力">
                    <el-tag :type="getDecisionPowerType(data.decisionPower)">
                      {{ getDecisionPowerText(data.decisionPower) }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>
          </el-row>
  
          <div class="info-section">
            <h3>研究方向</h3>
            <div class="content-box">
              {{ data.researchDirection || '暂无信息' }}
            </div>
          </div>
  
          <div class="info-section">
            <h3>备注信息</h3>
            <div class="content-box">
              {{ data.notes || '暂无备注' }}
            </div>
          </div>
  
          <!-- 拜访记录 -->
          <div class="info-section">
            <div class="section-header">
              <h3>拜访记录</h3>
              <el-button type="primary" size="small" @click="handleCreateVisit">
                新增拜访
              </el-button>
            </div>
            
            <el-table :data="visitRecords" v-loading="visitLoading">
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
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { getCustomerDetail } from '@/api/customers'
  import { getCustomerVisits } from '@/api/visits'
  
  const route = useRoute()
  const router = useRouter()
  
  const loading = ref(false)
  const visitLoading = ref(false)
  const data = ref(null)
  const visitRecords = ref([])
  
  const loadData = async () => {
    loading.value = true
    try {
      const response = await getCustomerDetail(route.params.id)
      data.value = response.data
    } catch (error) {
      ElMessage.error('加载数据失败')
      router.back()
    } finally {
      loading.value = false
    }
  }
  
  const loadVisitRecords = async () => {
    visitLoading.value = true
    try {
      const response = await getCustomerVisits(route.params.id)
      visitRecords.value = response.data.content || []
    } catch (error) {
      console.error('加载拜访记录失败:', error)
    } finally {
      visitLoading.value = false
    }
  }
  
  const handleBack = () => {
    router.back()
  }
  
  const handleEdit = () => {
    router.push(`/customers/edit/${route.params.id}`)
  }
  
  const handleCreateVisit = () => {
    router.push(`/visits/create?customerId=${route.params.id}`)
  }
  
  const handleViewVisit = (row) => {
    router.push(`/visits/detail/${row.id}`)
  }
  
  // 各种状态文本转换方法
  const getInfluenceLevelType = (level) => {
    const typeMap = { 'HIGH': 'danger', 'MEDIUM': 'warning', 'LOW': 'info' }
    return typeMap[level] || ''
  }
  
  const getInfluenceLevelText = (level) => {
    const textMap = { 'HIGH': '高', 'MEDIUM': '中', 'LOW': '低' }
    return textMap[level] || level
  }
  
  const getDecisionPowerType = (power) => {
    const typeMap = { 'DECISION_MAKER': 'danger', 'INFLUENCER': 'warning', 'USER': 'info', 'OTHER': '' }
    return typeMap[power] || ''
  }
  
  const getDecisionPowerText = (power) => {
    const textMap = { 'DECISION_MAKER': '决策者', 'INFLUENCER': '影响者', 'USER': '使用者', 'OTHER': '其他' }
    return textMap[power] || power
  }
  
  const getVisitTypeText = (type) => {
    const textMap = {
      'FIRST_VISIT': '首次拜访', 'FOLLOW_UP': '跟进拜访', 'TECHNICAL': '技术交流',
      'BUSINESS': '商务谈判', 'AFTER_SALES': '售后服务'
    }
    return textMap[type] || type
  }
  
  const getStatusType = (status) => {
    const typeMap = { 'COMPLETED': 'success', 'SCHEDULED': 'warning', 'CANCELLED': 'danger', 'POSTPONED': 'info' }
    return typeMap[status] || ''
  }
  
  const getStatusText = (status) => {
    const textMap = { 'COMPLETED': '已完成', 'SCHEDULED': '已安排', 'CANCELLED': '已取消', 'POSTPONED': '已延期' }
    return textMap[status] || status
  }
  
  const getIntentLevelType = (level) => {
    const typeMap = { 'A': 'danger', 'B': 'warning', 'C': 'info', 'D': '' }
    return typeMap[level] || ''
  }
  
  onMounted(() => {
    loadData()
    loadVisitRecords()
  })
  </script>
  
  <style lang="scss" scoped>
  .customer-detail {
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
      
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        h3 {
          margin: 0;
        }
      }
    }
    
    .content-box {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 4px;
      min-height: 60px;
      line-height: 1.6;
    }
  }
  </style>