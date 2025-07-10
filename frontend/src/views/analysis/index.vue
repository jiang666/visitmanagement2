<template>
    <div class="analysis">
      <!-- 筛选条件 -->
      <el-card class="filter-card">
        <el-form :model="filterForm" inline>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="销售人员">
            <el-select
              v-model="filterForm.salesId"
              placeholder="全部"
              clearable
              class="standard-select"
            >
              <el-option
                v-for="sales in salesOptions"
                :key="sales.id"
                :label="sales.realName"
                :value="sales.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="loadAnalysisData">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
  
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6" :md="6" :lg="6" v-for="item in statsCards" :key="item.title">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :style="{ background: item.color }">
                <el-icon :size="28"><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-title">{{ item.title }}</div>
                <div class="stat-change" :class="item.changeType">
                  <el-icon><component :is="item.changeIcon" /></el-icon>
                  {{ item.change }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <!-- 拜访趋势图 -->
        <el-col :xs="24" :md="12">
          <el-card title="拜访趋势分析">
            <template #header>
              <div class="card-header">
                <span>拜访趋势分析</span>
                <el-radio-group v-model="trendPeriod" size="small" @change="loadTrendData">
                  <el-radio-button value="week" label="week">最近7天</el-radio-button>
                  <el-radio-button value="month" label="month">最近30天</el-radio-button>
                  <el-radio-button value="quarter" label="quarter">最近3个月</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <v-chart class="chart" :option="visitTrendOption" />
          </el-card>
        </el-col>
        
        <!-- 意向分布图 -->
        <el-col :xs="24" :md="12">
          <el-card title="意向等级分布">
            <template #header>
              <span>意向等级分布</span>
            </template>
            <v-chart class="chart" :option="intentDistributionOption" />
          </el-card>
        </el-col>
      </el-row>
  
      <el-row :gutter="20" class="charts-row">
        <!-- 销售业绩排名 -->
        <el-col :xs="24" :md="12">
          <el-card title="销售业绩排名">
            <template #header>
              <span>销售业绩排名</span>
            </template>
            <v-chart class="chart" :option="salesRankingOption" />
          </el-card>
        </el-col>
        
        <!-- 学校拜访排名 -->
        <el-col :xs="24" :md="12">
          <el-card title="学校拜访排名">
            <template #header>
              <span>学校拜访排名</span>
            </template>
            <v-chart class="chart" :option="schoolRankingOption" />
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 详细报表 -->
      <el-card class="report-card">
        <template #header>
          <div class="card-header">
            <span>详细报表</span>
            <div>
              <el-button @click="handleExportReport">
                <el-icon><Download /></el-icon>
                导出报表
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="reportData" v-loading="reportLoading">
          <el-table-column prop="salesName" label="销售人员" width="120" />
          <el-table-column prop="totalVisits" label="总拜访数" width="100" />
          <el-table-column prop="completedVisits" label="已完成" width="100" />
          <el-table-column prop="completionRate" label="完成率" width="100">
            <template #default="{ row }">
              {{ row.completionRate }}%
            </template>
          </el-table-column>
          <el-table-column prop="aLevelCustomers" label="A类客户" width="100" />
          <el-table-column prop="bLevelCustomers" label="B类客户" width="100" />
          <el-table-column prop="wechatAddedCount" label="微信添加数" width="120" />
          <el-table-column prop="avgRating" label="平均评分" width="100">
            <template #default="{ row }">
              <el-rate :model-value="row.avgRating" disabled size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="department" label="部门" min-width="120" />
        </el-table>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { use } from 'echarts/core'
  import { CanvasRenderer } from 'echarts/renderers'
  import { LineChart, PieChart, BarChart } from 'echarts/charts'
  import * as echarts from 'echarts'
  import {
    TitleComponent, TooltipComponent, LegendComponent,
    GridComponent, DataZoomComponent
  } from 'echarts/components'
  import VChart from 'vue-echarts'
  import { Search, Download, TrendCharts, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
  import { getAnalysisData, getSalesPerformance, exportAnalysisReport } from '@/api/analysis'
  import { getUsersByRole } from '@/api/users'
  
  use([
    CanvasRenderer, LineChart, PieChart, BarChart,
    TitleComponent, TooltipComponent, LegendComponent,
    GridComponent, DataZoomComponent
  ])
  
  const filterForm = reactive({
    dateRange: [],
    salesId: null
  })
  
  const trendPeriod = ref('month')
  const salesOptions = ref([])
  const reportData = ref([])
  const reportLoading = ref(false)
  
  // 统计卡片数据
  const statsCards = ref([
    {
      title: '总拜访次数',
      value: 0,
      icon: 'User',
      color: '#409EFF',
      change: 0,
      changeType: 'positive',
      changeIcon: 'ArrowUp'
    },
    {
      title: '完成率',
      value: '0%',
      icon: 'CircleCheck',
      color: '#67C23A',
      change: 0,
      changeType: 'positive',
      changeIcon: 'ArrowUp'
    },
    {
      title: 'A类客户数',
      value: 0,
      icon: 'Star',
      color: '#F56C6C',
      change: 0,
      changeType: 'positive',
      changeIcon: 'ArrowUp'
    },
    {
      title: '平均评分',
      value: 0,
      icon: 'Medal',
      color: '#E6A23C',
      change: 0,
      changeType: 'positive',
      changeIcon: 'ArrowUp'
    }
  ])
  
  // 图表配置
  const visitTrendOption = ref({
    title: { text: '拜访趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{
      name: '拜访次数',
      type: 'line',
      data: [],
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ]
        }
      }
    }]
  })
  
  const intentDistributionOption = ref({
    title: { text: '意向等级分布', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [{
      name: '意向分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: '18', fontWeight: 'bold' } },
      labelLine: { show: false },
      data: []
    }]
  })
  
  const salesRankingOption = ref({
    title: { text: '销售业绩排名', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: [] },
    series: [{
      name: '拜访次数',
      type: 'bar',
      data: [],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#67C23A' }
        ])
      }
    }]
  })
  
  const schoolRankingOption = ref({
    title: { text: '学校拜访排名', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [], axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{
      name: '拜访次数',
      type: 'bar',
      data: [],
      itemStyle: { color: '#E6A23C' }
    }]
  })
  
  // 加载销售人员选项
  const loadSalesOptions = async () => {
    try {
      const response = await getUsersByRole('SALES')
      salesOptions.value = Array.isArray(response.data?.content)
        ? response.data.content
        : []
    } catch (error) {
      console.error('加载销售人员失败:', error)
    }
  }
  
  // 加载分析数据
  const loadAnalysisData = async () => {
    try {
      const params = {
        startDate: filterForm.dateRange?.[0],
        endDate: filterForm.dateRange?.[1],
        salesId: filterForm.salesId
      }
      
      const response = await getAnalysisData(params)
      const data = response.data || {}
      const stats = data.stats || {}
      const intentDistribution = data.intentDistribution || []
      const salesRanking = data.salesRanking || []
      const schoolRanking = data.schoolRanking || []
      
      // 更新统计卡片
      statsCards.value[0].value = stats.totalVisits || 0
      statsCards.value[0].change = stats.visitChange || 0
      statsCards.value[1].value = stats.completionRate ? `${stats.completionRate}%` : '0%'
      statsCards.value[1].change = stats.completionRateChange || 0
      statsCards.value[2].value = stats.aLevelCustomers || 0
      statsCards.value[2].change = stats.aLevelChange || 0
      statsCards.value[3].value = stats.avgRating ? stats.avgRating.toFixed(1) : 0
      statsCards.value[3].change = stats.ratingChange || 0
      
      // 更新意向分布图
      intentDistributionOption.value.series[0].data = Array.isArray(intentDistribution)
        ? intentDistribution.map(item => ({
            name: `${item.level}类客户`,
            value: item.count
          }))
        : []
      
      // 更新销售排名图
      const rankingList = Array.isArray(salesRanking) ? salesRanking : []
      salesRankingOption.value.yAxis.data = rankingList.map(item => item.salesName)
      salesRankingOption.value.series[0].data = rankingList.map(item => item.visitCount)
      
      // 更新学校排名图
      const schoolList = Array.isArray(schoolRanking) ? schoolRanking : []
      schoolRankingOption.value.xAxis.data = schoolList.map(item => item.schoolName)
      schoolRankingOption.value.series[0].data = schoolList.map(item => item.visitCount)
      
    } catch (error) {
      console.error('加载分析数据失败:', error)
    }
  }
  
  // 加载趋势数据
  const loadTrendData = async () => {
    try {
      const params = {
        period: trendPeriod.value,
        salesId: filterForm.salesId
      }
      
      const response = await getAnalysisData({ ...params, type: 'trend' })
      const visitTrend = Array.isArray(response.data?.visitTrend) ? response.data.visitTrend : []

      visitTrendOption.value.xAxis.data = visitTrend.map(item => item.date)
      visitTrendOption.value.series[0].data = visitTrend.map(item => item.count)
    } catch (error) {
      console.error('加载趋势数据失败:', error)
    }
  }
  
  // 加载报表数据
  const loadReportData = async () => {
    reportLoading.value = true
    try {
      const params = {
        startDate: filterForm.dateRange?.[0],
        endDate: filterForm.dateRange?.[1],
        salesId: filterForm.salesId
      }
      
      const response = await getSalesPerformance(params)
      const list = Array.isArray(response.data) ? response.data : (response.data?.list || [])
      reportData.value = list
    } catch (error) {
      console.error('加载报表数据失败:', error)
    } finally {
      reportLoading.value = false
    }
  }
  
  const handleReset = () => {
    filterForm.dateRange = []
    filterForm.salesId = null
    loadAnalysisData()
    loadTrendData()
    loadReportData()
  }
  
  const handleExportReport = async () => {
    try {
      const params = {
        startDate: filterForm.dateRange?.[0],
        endDate: filterForm.dateRange?.[1],
        salesId: filterForm.salesId
      }
      
      const response = await exportAnalysisReport(params)
      
      // 创建下载链接
      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `数据分析报表_${new Date().getTime()}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
    } catch (error) {
      ElMessage.error('导出失败')
    }
  }
  
  onMounted(() => {
    // 设置默认时间范围为最近30天
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 30)
    filterForm.dateRange = [
      start.toISOString().split('T')[0],
      end.toISOString().split('T')[0]
    ]
    
    loadSalesOptions()
    loadAnalysisData()
    loadTrendData()
    loadReportData()
  })
  </script>
  
  <style lang="scss" scoped>
  .analysis {
    .filter-card {
      margin-bottom: 20px;

      @media (max-width: 768px) {
        .el-form {
          display: flex;
          flex-direction: column;
        }

        .el-form-item {
          margin-right: 0;
          width: 100%;
        }
      }
    }
    
    .stats-row {
      margin-bottom: 20px;

      @media (max-width: 768px) {
        .el-col {
          width: 50%;
        }
      }
      
      .stat-card {
        .stat-content {
          display: flex;
          align-items: center;
          
          .stat-icon {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            margin-right: 16px;
          }
          
          .stat-info {
            flex: 1;
            
            .stat-value {
              font-size: 32px;
              font-weight: bold;
              color: #333;
              line-height: 1;
            }
            
            .stat-title {
              font-size: 14px;
              color: #666;
              margin: 4px 0;
            }
            
            .stat-change {
              font-size: 12px;
              display: flex;
              align-items: center;
              
              &.positive {
                color: #67C23A;
              }
              
              &.negative {
                color: #F56C6C;
              }
            }
          }
        }
      }
    }
    
    .charts-row {
      margin-bottom: 20px;

      @media (max-width: 768px) {
        .el-col {
          width: 100%;
        }
      }
      
      .chart {
        width: 100%;
        height: 350px;
      }
    }
    
    .report-card {
      overflow-x: auto;
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
  </style>