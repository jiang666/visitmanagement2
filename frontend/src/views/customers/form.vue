<template>
  <div class="customer-form">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑客户' : '新增客户' }}</span>
          <el-button @click="handleBack" v-if="!isMobile">返回</el-button>
        </div>
      </template>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-width="isMobile ? '0' : '100px'"
          class="responsive-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="客户姓名" prop="name" :required="true">
                <el-input v-model="form.name" placeholder="请输入客户姓名" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="客户职位" prop="position">
                <el-input v-model="form.position" placeholder="请输入客户职位" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="邮箱地址" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱地址" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="影响力等级" prop="influenceLevel">
                <el-select v-model="form.influenceLevel" placeholder="请选择影响力等级" style="width: 100%">
                  <el-option label="高影响力" value="HIGH" />
                  <el-option label="中影响力" value="MEDIUM" />
                  <el-option label="低影响力" value="LOW" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="决策权力" prop="decisionPower">
                <el-select v-model="form.decisionPower" placeholder="请选择决策权力" style="width: 100%">
                  <el-option label="决策者" value="DECISION_MAKER" />
                  <el-option label="影响者" value="INFLUENCER" />
                  <el-option label="使用者" value="USER" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 学校信息 -->
        <div class="form-section">
          <h3 class="section-title">学校信息</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="所在学校" prop="schoolId" :required="true">
                <el-select 
                  v-model="form.schoolId" 
                  placeholder="请输入学校名称搜索" 
                  filterable 
                  remote
                  reserve-keyword
                  :remote-method="searchSchools"
                  :loading="schoolLoading"
                  style="width: 100%"
                  clearable
                  @change="handleSchoolChange"
                >
                  <el-option v-for="school in schoolOptions" :key="school.id" :label="school.name" :value="school.id">
                    <span style="float: left">{{ school.name }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ school.city }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="所在院系" prop="departmentId">
                <el-select 
                  v-model="form.departmentId" 
                  placeholder="请选择院系"
                  style="width: 100%"
                  clearable
                  :disabled="!form.schoolId"
                  :loading="departmentLoading"
                >
                  <el-option v-for="dept in departmentOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="办公地址" prop="officeLocation">
                <el-input v-model="form.officeLocation" placeholder="请输入办公地址" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions" :class="{ 'mobile-actions': isMobile }">
          <el-button @click="handleBack">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '保存' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSchoolList, getDepartmentsBySchool } from '@/api/schools'
import { createCustomer, updateCustomer, getCustomerDetail } from '@/api/customers'

const route = useRoute()
const router = useRouter()

const isMobile = ref(false)
const loading = ref(false)
const submitting = ref(false)
const schoolLoading = ref(false)
const departmentLoading = ref(false)
const formRef = ref()

const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
}

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  position: '',
  phone: '',
  email: '',
  influenceLevel: 'MEDIUM',
  decisionPower: 'OTHER',
  schoolId: null,
  departmentId: null,
  departmentName: '',
  officeLocation: ''
})

const rules = {
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  schoolId: [{ required: true, message: '请选择学校', trigger: 'change' }]
}

const schoolOptions = ref([])
const departmentOptions = ref([])

const handleBack = () => router.back()

const loadSchools = async (keyword = '') => {
  try {
    schoolLoading.value = true
    const params = { size: 50 }
    if (keyword) {
      params.keyword = keyword
    }
    const response = await getSchoolList(params)
    schoolOptions.value = response.data?.content || []
  } catch (error) {
    console.error('Failed to load schools:', error)
    ElMessage.error('加载学校数据失败')
    schoolOptions.value = []
  } finally {
    schoolLoading.value = false
  }
}

const searchSchools = (keyword) => {
  if (keyword) {
    loadSchools(keyword)
  } else {
    loadSchools()
  }
}

const loadDepartmentsBySchool = async (schoolId) => {
  if (!schoolId) {
    departmentOptions.value = []
    return
  }
  
  try {
    departmentLoading.value = true
    const response = await getDepartmentsBySchool(schoolId)
    departmentOptions.value = response.data || []
  } catch (error) {
    console.error('Failed to load departments:', error)
    ElMessage.error('加载院系数据失败')
    departmentOptions.value = []
  } finally {
    departmentLoading.value = false
  }
}

const handleSchoolChange = (schoolId) => {
  form.departmentId = null // 清空院系选择
  loadDepartmentsBySchool(schoolId)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const customerData = {
      name: form.name,
      position: form.position,
      phone: form.phone,
      email: form.email,
      influenceLevel: form.influenceLevel,
      decisionPower: form.decisionPower,
      officeLocation: form.officeLocation
    }
    
    // 添加学校ID（必选）
    if (form.schoolId) {
      customerData.schoolId = form.schoolId
    }
    
    // 只有选择了院系才传递 departmentId
    if (form.departmentId) {
      customerData.departmentId = form.departmentId
    }

    if (isEdit.value) {
      await updateCustomer(route.params.id, customerData)
      ElMessage.success('更新成功')
    } else {
      await createCustomer(customerData)
      ElMessage.success('创建成功')
    }
    
    router.push('/customers/list')
  } catch (error) {
    if (error !== false) {
      console.error('Save customer error:', error)
      ElMessage.error('保存失败：' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

const loadCustomerDetail = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const response = await getCustomerDetail(route.params.id)
    const customer = response.data
    
    Object.assign(form, {
      name: customer.name || '',
      position: customer.position || '',
      phone: customer.phone || '',
      email: customer.email || '',
      influenceLevel: customer.influenceLevel || 'MEDIUM',
      decisionPower: customer.decisionPower || 'OTHER',
      schoolId: customer.schoolId || null,
      departmentId: customer.departmentId || null,
      officeLocation: customer.officeLocation || ''
    })
    
    // 如果有学校ID，加载对应的院系列表
    if (customer.schoolId) {
      await loadDepartmentsBySchool(customer.schoolId)
      
      // 为了编辑时能正确显示已选择的学校，需要确保学校在选项列表中
      if (schoolOptions.value.length === 0 || !schoolOptions.value.find(s => s.id === customer.schoolId)) {
        await loadSchools()
      }
    }
  } catch (error) {
    console.error('Failed to load customer:', error)
    ElMessage.error('加载客户信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
  loadSchools()
  loadCustomerDetail()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style lang="scss" scoped>
.customer-form {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .responsive-form {
    @media (max-width: 768px) {
      .el-form-item__label {
        display: block;
        text-align: left;
        padding: 0 0 8px 0;
      }
    }
  }

  .form-section {
    margin-bottom: 30px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      padding-bottom: 8px;
      border-bottom: 2px solid #409eff;
    }
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    &.mobile-actions {
      flex-direction: column;
      gap: 12px;

      .el-button {
        width: 100%;
        height: 44px;
      }
    }
  }
}
</style>