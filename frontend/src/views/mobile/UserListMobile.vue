<template>
  <MobileLayout title="用户管理">
    <el-card v-for="u in list" :key="u.id" class="mb-2">
      <div class="title-row">{{ u.username }}</div>
      <div class="info">{{ u.role }}</div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getUserList } from '@/api/users'

const list = ref([])

onMounted(async () => {
  const { data } = await getUserList({ page: 0, size: 20 })
  list.value = data.content || []
})
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
.title-row { font-weight: bold; margin-bottom: 4px; }
.info { font-size: 12px; color: #666; }
</style>
