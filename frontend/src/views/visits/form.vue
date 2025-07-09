<template>
    <div class="visit-form">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>{{ isEdit ? '编辑拜访记录' : '新增拜访记录' }}</span>
            <div>
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ isEdit ? '更新' : '保存' }}
              </el-button>
            </div>
          </div>
        </template>
  
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          style="max-width: 800px"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="客户" prop="customerId">
                <el-select
                  v-model="form.customerId"
                  placeholder="请选择客户"
                  filterable
                  remote
                  :remote-method="searchCustomers"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option
                    v-for="customer in customerOptions"
                    :key="customer.id"
                    :label="`${customer.name} (${customer.schoolName})`"
                    :value="customer.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="拜访日期" prop="visitDate">
                <el-date-picker
                  v-model="form.visitDate"
                  type="date"
                  placeholder="选择日期"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="拜访时间" prop="visitTime">
                <el-time-picker
                  v-model="form.visitTime"
                  placeholder="选择时间"
                  style="width: 100%"
                  format="HH:mm"
                  value-format="HH:mm"
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="拜访时长" prop="durationMinutes">
                <el-input-number
                  v-model="form.durationMinutes"
                  :min="1"
                  :max="1440"
                  placeholder="分钟"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="拜访类型" prop="visitType">
                <el-select
                  v-model="form.visitType"
                  placeholder="请选择"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option label="首次拜访" value="FIRST_VISIT" />
                  <el-option label="跟进拜访" value="FOLLOW_UP" />
                  <el-option label="技术交流" value="TECHNICAL" />
                  <el-option label="商务谈判" value="BUSINESS" />
                  <el-option label="售后服务" value="AFTER_SALES" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="拜访状态" prop="status">
                <el-select
                  v-model="form.status"
                  placeholder="请选择"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option label="已安排" value="SCHEDULED" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
                  <el-option label="已延期" value="POSTPONED" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="意向等级" prop="intentLevel">
                <el-select
                  v-model="form.intentLevel"
                  placeholder="请选择"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option label="A类（高意向）" value="A" />
                  <el-option label="B类（中意向）" value="B" />
                  <el-option label="C类（低意向）" value="C" />
                  <el-option label="D类（无意向）" value="D" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="拜访评分" prop="rating">
                <el-rate v-model="form.rating" :max="5" show-score />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="拜访地点" prop="location">
            <el-input v-model="form.location" placeholder="请输入拜访地点" />
          </el-form-item>
  
          <el-form-item label="可办事项" prop="businessItems">
            <el-input
              v-model="form.businessItems"
              type="textarea"
              :rows="3"
              placeholder="请输入可办事项"
            />
          </el-form-item>
  
          <el-form-item label="需求痛点" prop="painPoints">
            <el-input
              v-model="form.painPoints"
              type="textarea"
              :rows="3"
              placeholder="请输入需求痛点"
            />
          </el-form-item>
  
          <el-form-item label="竞品信息" prop="competitors">
            <el-input
              v-model="form.competitors"
              type="textarea"
              :rows="3"
              placeholder="请输入竞品信息"
            />
          </el-form-item>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预算范围" prop="budgetRange">
                <el-input v-model="form.budgetRange" placeholder="如：50-100万" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="决策时间线" prop="decisionTimeline">
                <el-input v-model="form.decisionTimeline" placeholder="如：3个月内" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="下一步计划" prop="nextStep">
            <el-input
              v-model="form.nextStep"
              type="textarea"
              :rows="3"
              placeholder="请输入下一步计划"
            />
          </el-form-item>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="跟进日期" prop="followUpDate">
                <el-date-picker
                  v-model="form.followUpDate"
                  type="date"
                  placeholder="选择跟进日期"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="天气情况" prop="weather">
                <el-input v-model="form.weather" placeholder="如：晴天" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="留下资料" prop="materialsLeft">
            <el-input v-model="form.materialsLeft" placeholder="请输入留下的资料" />
          </el-form-item>
  
          <el-form-item label="拜访备注" prop="notes">
            <el-input
              v-model="form.notes"
              type="textarea"
              :rows="4"
              placeholder="请输入拜访备注"
            />
          </el-form-item>
  
          <el-form-item label="其他">
            <el-checkbox v-model="form.wechatAdded">已添加微信</el-checkbox>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { getVisitDetail, createVisit, updateVisit } from '@/api/visits'
  import { searchCustomers as searchCustomersApi } from '@/api/customers'
  
  const route = useRoute()
  const router = useRouter()
  
  const formRef = ref()
  const submitting = ref(false)
  const customerOptions = ref([])
  
  const isEdit = computed(() => !!route.params.id)
  
  const form = reactive({
    customerId: '',
    visitDate: '',
    visitTime: '',
    durationMinutes: 60,
    visitType: 'FIRST_VISIT',
    status: 'SCHEDULED',
    intentLevel: 'C',
    businessItems: '',
    painPoints: '',
    competitors: '',
    budgetRange: '',
    decisionTimeline: '',
    nextStep: '',
    followUpDate: '',
    notes: '',
    materialsLeft: '',
    wechatAdded: false,
    rating: 0,
    location: '',
    weather: ''
  })
  
  const rules = {
    customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
    visitDate: [{ required: true, message: '请选择拜访日期', trigger: 'change' }],
    visitType: [{ required: true, message: '请选择拜访类型', trigger: 'change' }],
    status: [{ required: true, message: '请选择拜访状态', trigger: 'change' }]
  }
  
  let searchTimeout
  const searchCustomers = async (query) => {
    if (!query || !query.trim()) {
      customerOptions.value = []
      return
    }
    clearTimeout(searchTimeout)
    searchTimeout = setTimeout(async () => {
      try {
        const response = await searchCustomersApi({ keyword: query.trim() })
        customerOptions.value = response.data.content || []
      } catch (error) {
        console.error('搜索客户失败:', error)
      }
    }, 300)
  }
  
  const loadData = async () => {
    if (isEdit.value) {
      try {
        const response = await getVisitDetail(route.params.id)
        Object.assign(form, response.data)
      } catch (error) {
        ElMessage.error('加载数据失败')
        router.back()
      }
    }
  }
  
  const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateVisit(route.params.id, form)
          ElMessage.success('更新成功')
        } else {
          await createVisit(form)
          ElMessage.success('创建成功')
        }
        router.push('/visits')
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    })
  }
  
  const handleCancel = () => {
    router.back()
  }
  
  onMounted(() => {
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .visit-form {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  </style>