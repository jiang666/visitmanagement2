<template>
  <MobileLayout title="客户列表">
    <template #header-right>
      <el-button text @click="add"><el-icon><Plus /></el-icon></el-button>
    </template>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索姓名/职称/学校" clearable class="search-input" />
      <el-button-group class="action-group">
        <el-button size="large" type="primary" @click="search">搜索</el-button>
        <el-button size="large" type="danger" @click="deleteSelected">删除客户</el-button>
      </el-button-group>
    </div>
    <el-card v-for="item in list" :key="item.id" class="customer-card">
      <div class="title-row">
        <el-checkbox v-model="selectedIds" :label="item.id" class="card-check" />
        <span @click="view(item)">{{ item.name }}</span>
        <el-tag size="small" :type="levelType(item.influenceLevel)">{{ item.influenceLevelDescription }}</el-tag>
      </div>
      <div class="info" @click="view(item)">{{ item.position }} | {{ item.schoolName }}</div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getCustomerList, deleteCustomer } from '@/api/customers'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const list = ref([])
const keyword = ref('')
const selectedIds = ref([])

const load = async () => {
  const { data } = await getCustomerList({ page: 0, size: 20, keyword: keyword.value })
  list.value = data.content || []
}

const view = (item) => {
  router.push(`/m/customers/detail/${item.id}`)
}

const add = () => {
  router.push('/m/customers/create')
}

const search = () => {
  load()
}

const deleteSelected = async () => {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm('确定删除选中的客户吗？', '提示', { type: 'warning' })
    for (const id of selectedIds.value) {
      await deleteCustomer(id)
    }
    ElMessage.success('删除成功')
    selectedIds.value = []
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const levelType = (level) => {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  return 'info'
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.search-input {
  margin-bottom: 8px;
}
.action-group {
  width: 100%;
  margin-bottom: 12px;
}
.customer-card {
  margin-bottom: 10px;
  box-shadow: var(--el-box-shadow-lighter);
}
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
  margin-bottom: 4px;
}
.card-check {
  margin-right: 6px;
}
.info {
  font-size: 12px;
  color: #666;
}
</style>
