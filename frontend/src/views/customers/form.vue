<template>
    <div class="customer-form">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>{{ isEdit ? '编辑客户' : '新增客户' }}</span>
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
              <el-form-item label="客户姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入客户姓名" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="职位" prop="position">
                <el-input v-model="form.position" placeholder="请输入职位" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="职称" prop="title">
                <el-input v-model="form.title" placeholder="请输入职称" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="所属院系" prop="departmentId">
                <el-cascader
                  v-model="departmentPath"
                  :options="departmentOptions"
                  :props="cascaderProps"
                  placeholder="请选择学校和院系"
                  style="width: 100%"
                  @change="handleDepartmentChange"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="微信号" prop="wechat">
                <el-input v-model="form.wechat" placeholder="请输入微信号" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="生日" prop="birthday">
                <el-date-picker
                  v-model="form.birthday"
                  type="date"
                  placeholder="选择生日"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="办公地点" prop="officeLocation">
                <el-input v-model="form.officeLocation" placeholder="请输入办公地点" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="楼层房间" prop="floorRoom">
                <el-input v-model="form.floorRoom" placeholder="请输入楼层房间" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="影响力等级" prop="influenceLevel">
                <el-select
                  v-model="form.influenceLevel"
                  placeholder="请选择"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option label="高影响力" value="HIGH" />
                  <el-option label="中等影响力" value="MEDIUM" />
                  <el-option label="低影响力" value="LOW" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="决策权力" prop="decisionPower">
                <el-select
                  v-model="form.decisionPower"
                  placeholder="请选择"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option label="决策者" value="DECISION_MAKER" />
                  <el-option label="影响者" value="INFLUENCER" />
                  <el-option label="使用者" value="USER" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="研究方向" prop="researchDirection">
            <el-input
              v-model="form.researchDirection"
              type="textarea"
              :rows="3"
              placeholder="请输入研究方向"
            />
          </el-form-item>
  
          <el-form-item label="备注" prop="notes">
            <el-input
              v-model="form.notes"
              type="textarea"
              :rows="4"
              placeholder="请输入备注信息"
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { getCustomerDetail, createCustomer, updateCustomer } from '@/api/customers'
  import { getSchoolDepartmentTree } from '@/api/schools'
  
  const route = useRoute()
  const router = useRouter()
  
  const formRef = ref()
  const submitting = ref(false)
  const departmentOptions = ref([])
  const departmentPath = ref([])
  
  const isEdit = computed(() => !!route.params.id)
  
  const form = reactive({
    name: '',
    position: '',
    title: '',
    departmentId: '',
    phone: '',
    email: '',
    wechat: '',
    birthday: '',
    officeLocation: '',
    floorRoom: '',
    influenceLevel: 'MEDIUM',
    decisionPower: 'OTHER',
    researchDirection: '',
    notes: ''
  })
  
  const rules = {
    name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  }
  
  const cascaderProps = {
    value: 'id',
    label: 'name',
    children: 'departments',
    emitPath: true,
    checkStrictly: true
  }
  
  const loadDepartmentOptions = async () => {
    try {
      const response = await getSchoolDepartmentTree()
      departmentOptions.value = response.data
    } catch (error) {
      console.error('加载院系数据失败:', error)
    }
  }
  
  const handleDepartmentChange = (value) => {
    form.departmentId = value[value.length - 1] // 取最后一级的ID
  }
  
  const loadData = async () => {
    if (isEdit.value) {
      try {
        const response = await getCustomerDetail(route.params.id)
        Object.assign(form, response.data)
        
        // 设置级联选择器的值
        if (response.data.schoolId && response.data.departmentId) {
          departmentPath.value = [response.data.schoolId, response.data.departmentId]
        }
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
          await updateCustomer(route.params.id, form)
          ElMessage.success('更新成功')
        } else {
          await createCustomer(form)
          ElMessage.success('创建成功')
        }
        router.push('/customers')
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
    loadDepartmentOptions()
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .customer-form {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  </style>