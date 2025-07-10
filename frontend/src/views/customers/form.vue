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
              <el-form-item label="客户级别" prop="level">
                <el-select v-model="form.level" placeholder="请选择客户级别" style="width: 100%">
                  <el-option label="重要客户" value="IMPORTANT" />
                  <el-option label="普通客户" value="NORMAL" />
                  <el-option label="潜在客户" value="POTENTIAL" />
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
                <el-select v-model="form.schoolId" placeholder="请选择学校" filterable style="width: 100%">
                  <el-option v-for="school in schoolOptions" :key="school.id" :label="school.name" :value="school.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="所在院系" prop="departmentName">
                <el-input v-model="form.departmentName" placeholder="请输入所在院系" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="办公地址" prop="address">
                <el-input v-model="form.address" placeholder="请输入办公地址" />
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

const isMobile = ref(false)
const loading = ref(false)
const submitting = ref(false)
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
  level: 'NORMAL',
  schoolId: null,
  departmentName: '',
  address: ''
})

const rules = {
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  schoolId: [{ required: true, message: '请选择学校', trigger: 'change' }]
}

const schoolOptions = ref([])

const handleBack = () => router.back()

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    router.push('/customers/list')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
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