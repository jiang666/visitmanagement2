<template>
  <MobileLayout title="数据分析" :showBack="false">
    <el-card v-if="stats">
      <div class="stats">
        <div class="stat-item">
          <div class="value">{{ stats.totalVisits }}</div>
          <div class="label">总次数</div>
        </div>
        <div class="stat-item">
          <div class="value">{{ stats.completionRate }}%</div>
          <div class="label">完成率</div>
        </div>
        <div class="stat-item">
          <div class="value">{{ stats.intentLevel }}</div>
          <div class="label">意向等级</div>
        </div>
      </div>
    </el-card>
    <el-card style="margin-top:12px">
      <v-chart class="chart" :option="trendOption" />
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getAnalysisData } from '@/api/analysis'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { TitleComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, TitleComponent, TooltipComponent])

const stats = ref(null)
const trendOption = ref({
  title: { text: '趋势图', left: 'center' },
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{ type: 'line', data: [] }]
})

const loadData = async () => {
  const { data } = await getAnalysisData({})
  stats.value = {
    totalVisits: data?.stats?.totalVisits || 0,
    completionRate: data?.stats?.completionRate || 0,
    intentLevel: data?.stats?.topIntentLevel || '-'
  }
  const trend = Array.isArray(data?.visitTrend) ? data.visitTrend : []
  trendOption.value.xAxis.data = trend.map(t => t.date)
  trendOption.value.series[0].data = trend.map(t => t.count)
}

onMounted(loadData)
</script>

<style scoped>
.stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}
.stat-item {
  flex: 1;
}
.value {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}
.chart {
  width: 100%;
  height: 260px;
}
</style>
