<template>
  <MobileLayout title="拜访记录">
    <template #header-right>
      <el-button text @click="add"><el-icon><Plus /></el-icon></el-button>
    </template>
    <el-card v-for="item in list" :key="item.id" class="mb-2" @click="view(item)">
      <div class="title-row">
        <span>{{ item.customerName }}</span>
        <el-tag :type="item.status === 'COMPLETED' ? 'success' : 'info'" size="small">{{ statusMap[item.status] || item.status }}</el-tag>
      </div>
      <div class="info">{{ item.visitDate }} | {{ typeMap[item.visitType] || item.visitType }}</div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getVisitList } from '@/api/visits'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const list = ref([])

const load = async () => {
  const { data } = await getVisitList({ page: 0, size: 20 })
  list.value = data.content || []
}

const view = (item) => {
  router.push(`/m/visits/detail/${item.id}`)
}

const add = () => {
  router.push('/m/visits/create')
}

const typeMap = {
  PHONE: '电话',
  VISIT: '上门拜访'
}

const statusMap = {
  PLANNED: '已安排',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

onMounted(load)
</script>

<style scoped>
.mb-2 {
  margin-bottom: 12px;
}
.title-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-weight: bold;
}
.info {
  font-size: 12px;
  color: #666;
}
</style>
