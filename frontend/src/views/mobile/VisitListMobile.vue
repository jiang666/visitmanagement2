<template>
  <MobileLayout title="拜访记录">
    <template #header-right>
      <el-button text @click="add"><el-icon><Plus /></el-icon></el-button>
    </template>
    <div class="filter">
      <el-input v-model="query.customerName" placeholder="客户名" clearable class="filter-item" />
      <el-select v-model="query.visitType" placeholder="拜访方式" clearable class="filter-item">
        <el-option label="电话" value="PHONE" />
        <el-option label="上门拜访" value="VISIT" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable class="filter-item">
        <el-option label="已安排" value="PLANNED" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-select v-model="query.intentLevel" placeholder="意向等级" clearable class="filter-item">
        <el-option label="A" value="A" />
        <el-option label="B" value="B" />
        <el-option label="C" value="C" />
      </el-select>
      <el-button size="large" type="primary" class="search-btn" @click="search">筛选</el-button>
    </div>
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
import { ref, reactive, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { getVisitList } from '@/api/visits'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const list = ref([])
const query = reactive({
  customerName: '',
  visitType: '',
  status: '',
  intentLevel: ''
})

const load = async () => {
  const { data } = await getVisitList({
    page: 0,
    size: 20,
    customerName: query.customerName,
    visitType: query.visitType,
    status: query.status,
    intentLevel: query.intentLevel
  })
  list.value = data.content || []
}

const view = (item) => {
  router.push(`/m/visits/detail/${item.id}`)
}

const add = () => {
  router.push('/m/visits/create')
}

const search = () => {
  load()
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
.filter {
  margin-bottom: 12px;
}
.filter-item {
  margin-bottom: 8px;
  width: 100%;
}
.search-btn {
  width: 100%;
  border-radius: 12px;
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
