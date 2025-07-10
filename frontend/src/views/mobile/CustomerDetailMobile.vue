<template>
  <MobileLayout title="客户详情">
    <template #header-right>
      <el-button text @click="edit"><el-icon><Edit /></el-icon></el-button>
      <el-button text @click="remove"><el-icon><Delete /></el-icon></el-button>
    </template>
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
import { useRoute, useRouter } from 'vue-router'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getCustomerDetail, deleteCustomer } from '@/api/customers'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const data = ref(null)

onMounted(async () => {
  const { data: res } = await getCustomerDetail(route.params.id)
  data.value = res
})

const edit = () => {
  router.push(`/m/customers/edit/${route.params.id}`)
}

const remove = async () => {
  try {
    await ElMessageBox.confirm('确定删除该客户吗？', '提示', { type: 'warning' })
    await deleteCustomer(route.params.id)
    ElMessage.success('删除成功')
    router.push('/m/customers/list')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
</style>
