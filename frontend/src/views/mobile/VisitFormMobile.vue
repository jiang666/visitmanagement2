<template>
  <MobileLayout title="拜访表单">
    <el-form ref="formRef" :model="form" label-width="80px">
      <el-form-item label="客户" prop="customerId">
        <el-input v-model="form.customerId" placeholder="客户ID" />
      </el-form-item>
      <el-form-item label="日期" prop="visitDate">
        <el-date-picker v-model="form.visitDate" style="width:100%" />
      </el-form-item>
      <el-form-item label="类型" prop="visitType">
        <el-select v-model="form.visitType" style="width:100%">
          <el-option label="电话" value="PHONE" />
          <el-option label="上门拜访" value="VISIT" />
        </el-select>
      </el-form-item>
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="form.rating" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input type="textarea" v-model="form.content" rows="3" />
      </el-form-item>
      <el-form-item label="反馈" prop="feedback">
        <el-input type="textarea" v-model="form.feedback" rows="3" />
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
import { createVisit, updateVisit, getVisitDetail } from '@/api/visits'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const form = reactive({
  customerId: '',
  visitDate: '',
  visitType: 'PHONE',
  rating: 0,
  content: '',
  feedback: ''
})

if (route.params.id) {
  getVisitDetail(route.params.id).then(({ data }) => Object.assign(form, data))
}

const submit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (route.params.id) {
    await updateVisit(route.params.id, form)
  } else {
    await createVisit(form)
  }
  router.push('/m/visits/list')
}
</script>

<style scoped>
</style>
