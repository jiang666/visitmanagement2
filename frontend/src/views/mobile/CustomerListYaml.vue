<template>
  <MobileLayout title="客户列表">
    <template #header-right>
      <el-button text @click="add">
        <el-icon><Plus /></el-icon>
      </el-button>
    </template>
    <div class="search-area">
      <el-input
        v-model="keyword"
        placeholder="输入姓名、职位、学校等"
        clearable
        class="search-input"
        @keyup.enter="search"
      />
      <div class="actions">
        <el-button type="primary" class="btn" @click="search">搜索</el-button>
        <el-button type="danger" class="btn" @click="deleteSelected">删除客户</el-button>
      </div>
    </div>
    <el-card v-for="item in list" :key="item.id" class="customer-card">
      <div class="customer-title">
        <el-checkbox v-model="selectedIds" :label="item.id" class="check" />
        <span class="name" @click="view(item)">{{ item.name }}</span>
        <span
          class="badge"
          :class="badgeClass(item.influenceLevel)"
          v-if="item.influenceLevel"
        >{{ item.influenceLevelDescription }}</span>
      </div>
      <div class="customer-info" @click="view(item)">
        {{ item.position }} | {{ item.schoolName }}
      </div>
    </el-card>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getCustomerList, deleteCustomer } from '@/api/customers'
import { ElMessageBox, ElMessage } from 'element-plus'

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

const badgeClass = (level) => {
  if (level === 'HIGH') return 'badge-high'
  if (level === 'MEDIUM') return 'badge-mid'
  return ''
}

onMounted(load)
</script>

<style scoped>
.search-area {
  margin-top: 16px;
}

.search-input {
  width: 100%;
  height: 40px;
  font-size: 14px;
  padding: 0 12px;
  border: 1px solid #E4E7ED;
  border-radius: 8px;
  background: #fff;
}

.actions {
  display: flex;
  gap: 8px;
  margin: 8px 0;
}

.btn {
  flex: 1;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.customer-card {
  margin: 8px 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #E4E7ED;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.customer-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.check {
  margin-right: 6px;
}

.name {
  flex: 1;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.customer-info {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.badge {
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 4px;
  color: #fff;
}

.badge-high {
  background: #F56C6C;
}

.badge-mid {
  background: #E6A23C;
}
</style>
