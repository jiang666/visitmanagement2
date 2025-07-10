<template>
  <MobileLayout title="拜访记录">
    <el-card v-for="item in list" :key="item.id" class="mb-2" @click="view(item)">
      <div class="title-row">
        <span>{{ item.customerName }}</span>
        <el-tag :type="item.status === 'COMPLETED' ? 'success' : 'info'" size="small">{{ item.status }}</el-tag>
      </div>
      <div class="info">{{ item.visitDate }} | {{ item.visitType }}</div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getVisitList } from '@/api/visits'
import { useRouter } from 'vue-router'

const router = useRouter()
const list = ref([])

const load = async () => {
  const { data } = await getVisitList({ page: 0, size: 20 })
  list.value = data.content || []
}

const view = (item) => {
  router.push(`/m/visits/detail/${item.id}`)
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
