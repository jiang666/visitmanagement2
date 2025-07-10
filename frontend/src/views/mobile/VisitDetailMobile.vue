<template>
  <MobileLayout title="拜访详情">
    <template #header-right>
      <el-button text @click="edit"><el-icon><Edit /></el-icon></el-button>
      <el-button text @click="remove"><el-icon><Delete /></el-icon></el-button>
    </template>
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
import { useRoute, useRouter } from 'vue-router'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getVisitDetail, deleteVisit } from '@/api/visits'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
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

const edit = () => {
  router.push(`/m/visits/edit/${route.params.id}`)
}

const remove = async () => {
  try {
    await ElMessageBox.confirm('确定删除该拜访记录吗？', '提示', { type: 'warning' })
    await deleteVisit(route.params.id)
    ElMessage.success('删除成功')
    router.push('/m/visits/list')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.content {
  margin-top: 12px;
  line-height: 1.4;
}
</style>
