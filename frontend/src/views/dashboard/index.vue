<template>
    <div class="dashboard">
      <el-row :gutter="20">
        <!-- 统计卡片 -->
        <el-col :span="6" v-for="item in statsCards" :key="item.title">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :style="{ background: item.color }">
                <el-icon :size="24"><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-title">{{ item.title }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 拜访趋势图 -->
        <el-col :span="12">
          <el-card title="拜访趋势">
            <template #header>
              <span>拜访趋势</span>
            </template>
            <v-chart class="chart" :option="visitTrendOption" />
          </el-card>
        </el-col>
        
        <!-- 意向分布图 -->
        <el-col :span="12">
          <el-card title="意向分布">
            <template #header>
              <span>意向分布</span>
            </template>
            <v-chart class="chart" :option="intentDistributionOption" />
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 最近拜访 -->
        <el-col :span="12">
          <el-card title="最近拜访">
            <template #header>
              <div class="card-header">
                <span>最近拜访</span>
                <el-button type="primary" size="small" @click="$router.push('/visits')">
                  查看更多
                </el-button>
              </div>
            </template>
            
            <el-table :data="recentVisits" style="width: 100%">
              <el-table-column prop="customerName" label="客户" />
              <el-table-column prop="visitDate" label="拜访日期" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <!-- 待办提醒 -->
        <el-col :span="12">
          <el-card title="待办提醒">
            <template #header>
              <span>待办提醒</span>
            </template>
            
            <el-timeline v-if="reminders.length">
              <el-timeline-item
                v-for="item in reminders"
                :key="item.id"
                :timestamp="item.reminderDate"
              >
                {{ item.title }}
              </el-timeline-item>
            </el-timeline>
            
            <el-empty v-else description="暂无提醒" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { use } from 'echarts/core'
  import { CanvasRenderer } from 'echarts/renderers'
  import { LineChart, PieChart } from 'echarts/charts'
  import {
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent
  } from 'echarts/components'
  import VChart from 'vue-echarts'
  import { getDashboardData } from '@/api/dashboard'
  
  use([
    CanvasRenderer,
    LineChart,
    PieChart,
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent
  ])
  
  // 统计卡片数据
  const statsCards = ref([
    { title: '总拜访次数', value: 0, icon: 'User', color: '#409EFF' },
    { title: '已完成', value: 0, icon: 'CircleCheck', color: '#67C23A' },
    { title: '已安排', value: 0, icon: 'Clock', color: '#E6A23C' },
    { title: 'A类客户', value: 0, icon: 'Star', color: '#F56C6C' }
  ])
  
  const recentVisits = ref([])
  const reminders = ref([])
  
  // 图表配置
  const visitTrendOption = ref({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{
      name: '拜访次数',
      type: 'line',
      data: [],
      smooth: true
    }]
  })
  
  const intentDistributionOption = ref({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '意向分布',
      type: 'pie',
      radius: '50%',
      data: []
    }]
  })
  
  // 获取状态类型
  const getStatusType = (status) => {
    const typeMap = {
      'COMPLETED': 'success',
      'SCHEDULED': 'warning',
      'CANCELLED': 'danger',
      'POSTPONED': 'info'
    }
    return typeMap[status] || ''
  }
  
  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      'COMPLETED': '已完成',
      'SCHEDULED': '已安排',
      'CANCELLED': '已取消',
      'POSTPONED': '已延期'
    }
    return textMap[status] || status
  }
  
  // 加载仪表盘数据
  const loadDashboardData = async () => {
    try {
      const response = await getDashboardData()
      const { stats, visitTrend, intentDistribution, recentVisitList, reminderList } = response.data
      
      // 更新统计卡片
      statsCards.value[0].value = stats.totalVisits
      statsCards.value[1].value = stats.completedVisits
      statsCards.value[2].value = stats.scheduledVisits
      statsCards.value[3].value = stats.aLevelCustomers
      
      // 更新图表数据
      visitTrendOption.value.xAxis.data = visitTrend.map(item => item.date)
      visitTrendOption.value.series[0].data = visitTrend.map(item => item.count)
      
      intentDistributionOption.value.series[0].data = intentDistribution.map(item => ({
        name: `${item.level}类客户`,
        value: item.count
      }))
      
      // 更新列表数据
      recentVisits.value = recentVisitList
      reminders.value = reminderList
      
    } catch (error) {
      console.error('加载仪表盘数据失败:', error)
    }
  }
  
  onMounted(() => {
    loadDashboardData()
  })
  </script>
  
  <style lang="scss" scoped>
  .dashboard {
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          margin-right: 16px;
        }
        
        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #333;
          }
          
          .stat-title {
            font-size: 14px;
            color: #666;
            margin-top: 4px;
          }
        }
      }
    }
    
    .chart {
      width: 100%;
      height: 300px;
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  </style>
  
  