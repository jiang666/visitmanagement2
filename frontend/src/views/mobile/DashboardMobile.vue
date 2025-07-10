<template>
  <MobileLayout title="首页" :showBack="false">
    <div class="stat-grid">
      <el-card v-for="item in statsCards" :key="item.title" class="stat-card">
        <div class="stat-row">
          <el-icon :size="20" :style="{ color: item.color }">
            <component :is="item.icon" />
          </el-icon>
          <div class="info">
            <div class="value">{{ item.value }}</div>
            <div class="label">{{ item.title }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card style="margin-top:12px">
      <v-chart class="chart" :option="visitTrendOption" />
    </el-card>

    <el-card style="margin-top:12px">
      <v-chart class="chart" :option="intentDistributionOption" />
    </el-card>

    <el-card style="margin-top:12px">
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

    <div class="btn-group">
      <el-button size="large" type="primary" class="action-btn" @click="router.push('/m/analysis')">查看更多</el-button>
      <el-button size="large" class="action-btn" @click="router.push('/m/customers/create')">新建客户</el-button>
    </div>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getDashboardData } from '@/api/dashboard'
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

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const statsCards = ref([
  { title: '总拜访次数', value: 0, icon: 'User', color: '#409EFF' },
  { title: '已完成', value: 0, icon: 'CircleCheck', color: '#67C23A' },
  { title: '已安排', value: 0, icon: 'Clock', color: '#E6A23C' },
  { title: 'A类客户', value: 0, icon: 'Star', color: '#F56C6C' }
])

const visitTrendOption = ref({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{ name: '拜访次数', type: 'line', data: [], smooth: true }]
})

const intentDistributionOption = ref({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', left: 'left' },
  series: [{ name: '意向分布', type: 'pie', radius: '50%', data: [] }]
})

const reminders = ref([])
const router = useRouter()

const loadData = async () => {
  const { data } = await getDashboardData()
  const stats = data.stats || data.basicStats || {}
  const trend = data.visitTrend || data.trendData?.visitTrend || []
  const intent = data.intentDistribution || data.intentStats || []
  reminders.value = data.reminderList || data.reminders || []

  statsCards.value[0].value = stats.totalVisits || 0
  statsCards.value[1].value = stats.completedVisits || 0
  statsCards.value[2].value = stats.scheduledVisits || 0
  statsCards.value[3].value = stats.aLevelCustomers || 0

  visitTrendOption.value.xAxis.data = trend.map(t => t.date)
  visitTrendOption.value.series[0].data = trend.map(t => t.count)

  intentDistributionOption.value.series[0].data = intent.map(i => ({
    name: `${i.level}类客户`,
    value: i.count
  }))
}

onMounted(loadData)
</script>

<style scoped>
.stat-card {
  margin-bottom: 12px;
  width: calc(50% - 6px);
}
.stat-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.stat-row {
  display: flex;
  align-items: center;
}
.info {
  margin-left: 8px;
}
.value {
  font-size: 20px;
  font-weight: bold;
}
.chart {
  width: 100%;
  height: 260px;
}

.btn-group {
  margin-top: 16px;
}
.action-btn {
  border-radius: 12px;
  width: 100%;
  margin-bottom: 12px;
}
</style>
