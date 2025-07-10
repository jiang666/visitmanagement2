<template>
  <MobileLayout title="拜访详情">
    <el-card v-if="data">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="客户">{{ data.customerName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ data.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeMap[data.visitType] || data.visitType }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[data.status] || data.status }}</el-descriptions-item>
      </el-descriptions>
      <div class="content" v-if="data.content">
        <h4>交流内容</h4>
        <p>{{ data.content }}</p>
      </div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getVisitDetail } from '@/api/visits'

const route = useRoute()
const data = ref(null)

const typeMap = { PHONE: '电话', VISIT: '上门拜访' }
const statusMap = {
  PLANNED: '已安排',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

onMounted(async () => {
  const { data: res } = await getVisitDetail(route.params.id)
  data.value = res
})
</script>

<style scoped>
.content {
  margin-top: 12px;
  line-height: 1.4;
}
</style>
