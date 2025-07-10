<template>
  <MobileLayout title="客户表单">
    <el-form ref="formRef" :model="form" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="职位" prop="position">
        <el-input v-model="form.position" />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="学校" prop="schoolId">
        <el-select v-model="form.schoolId" style="width:100%">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="院系" prop="departmentName">
        <el-input v-model="form.departmentName" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width:100%" @click="submit">保存</el-button>
      </el-form-item>
    </el-form>
  </MobileLayout>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { createCustomer, updateCustomer, getCustomerDetail } from '@/api/customers'
import { getSchoolList } from '@/api/schools'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const form = reactive({
  name: '',
  position: '',
  phone: '',
  schoolId: null,
  departmentName: '',
  email: ''
})
const schools = ref([])

const loadSchools = async () => {
  const { data } = await getSchoolList({ page: 0, size: 100 })
  schools.value = data.content || []
}

if (route.params.id) {
  getCustomerDetail(route.params.id).then(({ data }) => Object.assign(form, data))
}

onMounted(loadSchools)

const submit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (route.params.id) {
    await updateCustomer(route.params.id, form)
  } else {
    await createCustomer(form)
  }
  router.back()
}
</script>

<style scoped>
</style>
