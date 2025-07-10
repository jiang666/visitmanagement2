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
              <el-form-item label="所在学校" prop="schoolName" :required="true">
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
                  <el-option label="电话拜访" value="PHONE" />
                  <el-option label="上门拜访" value="VISIT" />
                  <el-option label="会议交流" value="MEETING" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="拜访时长" prop="visitDuration">
                <el-input
                    v-model="form.visitDuration"
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
                  <el-option label="计划中" value="PLANNED" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
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
                  <el-option label="A类" value="A" />
                  <el-option label="B类" value="B" />
                  <el-option label="C类" value="C" />
                  <el-option label="D类" value="D" />
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
  visitDuration: '',
  status: 'PLANNED',
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

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    router.push('/visits/list')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
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
    schoolOptions.value = [
      { id: 1, name: '北京大学' },
      { id: 2, name: '清华大学' },
      { id: 3, name: '复旦大学' }
    ]

    // 加载销售人员选项
    salesOptions.value = [
      { id: 1, realName: '张销售' },
      { id: 2, realName: '李销售' },
      { id: 3, realName: '王销售' }
    ]

    // 如果是编辑模式，加载表单数据
    if (isEdit.value) {
      // 模拟加载编辑数据
      Object.assign(form, {
        customerName: '张三',
        customerPosition: '教授',
        schoolId: 1,
        schoolName: '北京大学',
        visitType: 'VISIT',
        status: 'COMPLETED',
        intentLevel: 'A'
      })
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
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