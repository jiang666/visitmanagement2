<template>
  <MobileLayout title="客户表单">
    <el-form ref="formRef" :model="form" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="学校" prop="schoolName">
        <el-input v-model="form.schoolName" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width:100%" @click="submit">保存</el-button>
      </el-form-item>
    </el-form>
  </MobileLayout>
</template>

<script setup>
import { reactive, ref } from 'vue'
import MobileLayout from '@/layout/MobileLayout.vue'
import { createCustomer, updateCustomer, getCustomerDetail } from '@/api/customers'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const form = reactive({ name: '', schoolName: '' })

if (route.params.id) {
  getCustomerDetail(route.params.id).then(({ data }) => Object.assign(form, data))
}

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
