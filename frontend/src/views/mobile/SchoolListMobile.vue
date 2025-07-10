<template>
  <MobileLayout title="学校管理">
    <el-card v-for="s in list" :key="s.id" class="mb-2">
      <div class="title-row">{{ s.name }}</div>
      <div class="info">{{ s.city }}</div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getSchoolList } from '@/api/schools'

const list = ref([])

onMounted(async () => {
  const { data } = await getSchoolList({ page: 0, size: 20 })
  list.value = data.content || []
})
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
.title-row { font-weight: bold; margin-bottom: 4px; }
.info { font-size: 12px; color: #666; }
</style>
