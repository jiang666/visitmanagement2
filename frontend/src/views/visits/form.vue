<template>
  <div class="visit-form">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑拜访记录' : '新增拜访记录' }}</span>
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
        <!-- 客户信息 -->
        <div class="form-section">
          <h3 class="section-title">客户信息</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="客户姓名" prop="customerName" :required="true">
                <el-input
                    v-model="form.customerName"
                    placeholder="请输入客户姓名"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="客户职位" prop="customerPosition">
                <el-input
                    v-model="form.customerPosition"
                    placeholder="请输入客户职位"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="联系电话" prop="customerPhone">
                <el-input
                    v-model="form.customerPhone"
                    placeholder="请输入联系电话"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="所在学校" prop="schoolId" :required="true">
                <el-select
                    v-model="form.schoolId"
                    placeholder="请选择学校"
                    filterable
                    style="width: 100%"
                    @change="handleSchoolChange"
                >
                  <el-option
                      v-for="school in schoolOptions"
                      :key="school.id"
                      :label="school.name"
                      :value="school.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="所在院系" prop="departmentName">
                <el-input
                    v-model="form.departmentName"
                    placeholder="请输入所在院系"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="客户邮箱" prop="customerEmail">
                <el-input
                    v-model="form.customerEmail"
                    placeholder="请输入客户邮箱"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 拜访信息 -->
        <div class="form-section">
          <h3 class="section-title">拜访信息</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="拜访日期" prop="visitDate" :required="true">
                <el-date-picker
                    v-model="form.visitDate"
                    type="date"
                    placeholder="选择拜访日期"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="拜访时间" prop="visitTime">
                <el-time-picker
                    v-model="form.visitTime"
                    placeholder="选择拜访时间"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="拜访类型" prop="visitType" :required="true">
                <el-select
                    v-model="form.visitType"
                    placeholder="请选择拜访类型"
                    style="width: 100%"
                >
                  <el-option label="面对面拜访" value="FACE_TO_FACE" />
                  <el-option label="电话拜访" value="PHONE_CALL" />
                  <el-option label="视频拜访" value="VIDEO_CALL" />
                  <el-option label="邮件沟通" value="EMAIL" />
                  <el-option label="微信沟通" value="WECHAT" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="拜访时长" prop="durationMinutes">
                <el-input
                    v-model="form.durationMinutes"
                    placeholder="请输入拜访时长（分钟）"
                    type="number"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="状态" prop="status" :required="true">
                <el-select
                    v-model="form.status"
                    placeholder="请选择状态"
                    style="width: 100%"
                >
                  <el-option label="已安排" value="SCHEDULED" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
                  <el-option label="已延期" value="POSTPONED" />
                  <el-option label="未出现" value="NO_SHOW" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="意向等级" prop="intentLevel" :required="true">
                <el-select
                    v-model="form.intentLevel"
                    placeholder="请选择意向等级"
                    style="width: 100%"
                >
                  <el-option label="非常高" value="VERY_HIGH" />
                  <el-option label="高" value="HIGH" />
                  <el-option label="中等" value="MEDIUM" />
                  <el-option label="低" value="LOW" />
                  <el-option label="非常低" value="VERY_LOW" />
                  <el-option label="无意向" value="NO_INTENT" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="销售人员" prop="salesId">
                <el-select
                    v-model="form.salesId"
                    placeholder="请选择销售人员"
                    style="width: 100%"
                >
                  <el-option
                      v-for="sales in salesOptions"
                      :key="sales.id"
                      :label="sales.realName"
                      :value="sales.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="预期收益" prop="expectedRevenue">
                <el-input
                    v-model="form.expectedRevenue"
                    placeholder="请输入预期收益（万元）"
                    type="number"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 拜访内容 -->
        <div class="form-section">
          <h3 class="section-title">拜访内容</h3>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="拜访目的" prop="purpose">
                <el-input
                    v-model="form.purpose"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入拜访目的"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="交流内容" prop="content">
                <el-input
                    v-model="form.content"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入交流内容"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="客户反馈" prop="feedback">
                <el-input
                    v-model="form.feedback"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入客户反馈"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="下次计划" prop="nextPlan">
                <el-input
                    v-model="form.nextPlan"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入下次拜访计划"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="备注" prop="notes">
                <el-input
                    v-model="form.notes"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入备注信息"
                />
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
import { getSchoolList } from '@/api/schools'
import { getVisitDetail, createVisit, updateVisit } from '@/api/visits'
import { getUserList } from '@/api/users'

const route = useRoute()
const router = useRouter()

// 响应式状态
const isMobile = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
}

// 计算属性
const isEdit = computed(() => !!route.params.id)

// 表单数据
const form = reactive({
  customerId: null,
  customerName: '',
  customerPosition: '',
  customerPhone: '',
  customerEmail: '',
  schoolId: null,
  schoolName: '',
  departmentName: '',
  visitDate: '',
  visitTime: '',
  visitType: '',
  durationMinutes: '',
  status: 'SCHEDULED',
  intentLevel: '',
  salesId: null,
  purpose: '',
  content: '',
  feedback: '',
  nextPlan: '',
  notes: '',
  expectedRevenue: ''
})

// 表单验证规则
const rules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  schoolId: [
    { required: true, message: '请选择学校', trigger: 'change' }
  ],
  visitDate: [
    { required: true, message: '请选择拜访日期', trigger: 'change' }
  ],
  visitType: [
    { required: true, message: '请选择拜访类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  intentLevel: [
    { required: true, message: '请选择意向等级', trigger: 'change' }
  ]
}

// 选项数据
const schoolOptions = ref([])
const salesOptions = ref([])

// 事件处理
const handleBack = () => {
  router.back()
}

const handleSchoolChange = (schoolId) => {
  const school = schoolOptions.value.find(s => s.id === schoolId)
  if (school) {
    form.schoolName = school.name
  }
}

// 日期格式化函数
const formatDate = (date) => {
  if (!date) return null
  if (typeof date === 'string') return date
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 时间格式化函数
const formatTime = (time) => {
  if (!time) return null
  if (typeof time === 'string') return time
  const t = new Date(time)
  const hours = String(t.getHours()).padStart(2, '0')
  const minutes = String(t.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 时间字符串解析函数
const parseTimeString = (timeStr) => {
  if (!timeStr) return null
  const [hours, minutes] = timeStr.split(':')
  const date = new Date()
  date.setHours(parseInt(hours, 10))
  date.setMinutes(parseInt(minutes, 10))
  date.setSeconds(0)
  date.setMilliseconds(0)
  return date
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const visitData = {
      customerId: form.customerId,
      customerName: form.customerName,
      customerPosition: form.customerPosition,
      customerPhone: form.customerPhone,
      customerEmail: form.customerEmail,
      schoolId: form.schoolId,
      departmentName: form.departmentName,
      visitDate: form.visitDate ? formatDate(form.visitDate) : null,
      visitTime: form.visitTime ? formatTime(form.visitTime) : null,
      visitType: form.visitType,
      durationMinutes: form.durationMinutes ? parseInt(form.durationMinutes) : null,
      status: form.status,
      intentLevel: form.intentLevel,
      salesId: form.salesId,
      businessItems: form.purpose,
      painPoints: form.content,
      competitors: form.feedback,
      nextStep: form.nextPlan,
      notes: form.notes,
      budgetRange: form.expectedRevenue
    }

    if (isEdit.value) {
      await updateVisit(route.params.id, visitData)
      ElMessage.success('更新成功')
    } else {
      await createVisit(visitData)
      ElMessage.success('创建成功')
    }
    
    router.push('/visits/list')
  } catch (error) {
    if (error !== false) {
      console.error('Save visit error:', error)
      ElMessage.error('保存失败：' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载学校选项
    const schoolResponse = await getSchoolList({ size: 100 })
    schoolOptions.value = schoolResponse.data?.content || []

    // 加载销售人员选项
    const userResponse = await getUserList({ size: 100 })
    salesOptions.value = userResponse.data?.content || []

    // 如果是编辑模式，加载表单数据
    if (isEdit.value) {
      await loadVisitDetail()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadVisitDetail = async () => {
  try {
    const response = await getVisitDetail(route.params.id)
    const visit = response.data
    
    Object.assign(form, {
      customerId: visit.customerId,
      customerName: visit.customerName || '',
      customerPosition: visit.customerPosition || '',
      customerPhone: visit.customerPhone || '',
      customerEmail: visit.customerEmail || '',
      schoolId: visit.schoolId || null,
      departmentName: visit.departmentName || '',
      visitDate: visit.visitDate ? new Date(visit.visitDate) : '',
      visitTime: visit.visitTime ? parseTimeString(visit.visitTime) : '',
      visitType: visit.visitType || '',
      durationMinutes: visit.durationMinutes ? visit.durationMinutes.toString() : '',
      status: visit.status || 'SCHEDULED',
      intentLevel: visit.intentLevel || '',
      salesId: visit.salesId || null,
      purpose: visit.businessItems || '',
      content: visit.painPoints || '',
      feedback: visit.competitors || '',
      nextPlan: visit.nextStep || '',
      notes: visit.notes || '',
      expectedRevenue: visit.budgetRange || ''
    })
  } catch (error) {
    console.error('Failed to load visit detail:', error)
    ElMessage.error('加载拜访记录详情失败')
    router.back()
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style lang="scss" scoped>
.visit-form {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    @media (max-width: 768px) {
      span {
        font-size: 16px;
      }
    }
  }

  .responsive-form {
    @media (max-width: 768px) {
      .el-form-item__label {
        display: block;
        text-align: left;
        padding: 0 0 8px 0;
        line-height: 1.5;
      }

      .el-form-item {
        margin-bottom: 20px;
      }
    }
  }

  .form-section {
    margin-bottom: 30px;

    &:last-of-type {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      padding-bottom: 8px;
      border-bottom: 2px solid #409eff;

      @media (max-width: 768px) {
        font-size: 14px;
        margin-bottom: 16px;
      }
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
        font-size: 16px;
      }
    }

    @media (min-width: 769px) {
      .el-button {
        min-width: 100px;
      }
    }
  }
}
</style>