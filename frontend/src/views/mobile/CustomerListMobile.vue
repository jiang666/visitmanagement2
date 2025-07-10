<template>
  <MobileLayout title="客户列表">
    <el-card v-for="item in list" :key="item.id" class="mb-2" @click="view(item)">
      <div class="title-row">{{ item.name }}</div>
      <div class="info">{{ item.schoolName }} {{ item.departmentName }}</div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getCustomerList } from '@/api/customers'
import { useRouter } from 'vue-router'

const router = useRouter()
const list = ref([])

const load = async () => {
  const { data } = await getCustomerList({ page: 0, size: 20 })
  list.value = data.content || []
}

const view = (item) => {
  router.push(`/m/customers/detail/${item.id}`)
}

onMounted(load)
</script>

<style scoped>
.mb-2 {
  margin-bottom: 12px;
}
.title-row {
  font-weight: bold;
  margin-bottom: 4px;
}
.info {
  font-size: 12px;
  color: #666;
}
</style>
