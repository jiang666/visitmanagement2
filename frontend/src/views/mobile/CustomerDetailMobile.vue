<template>
  <MobileLayout title="客户详情">
    <el-card v-if="data">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ data.name }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ data.position }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ data.schoolName }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ data.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ data.phone }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getCustomerDetail } from '@/api/customers'

const route = useRoute()
const data = ref(null)

onMounted(async () => {
  const { data: res } = await getCustomerDetail(route.params.id)
  data.value = res
})
</script>

<style scoped>
</style>
