<template>
    <div class="school-management">
      <el-card>
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :model="searchForm" inline>
            <el-form-item label="关键词">
              <el-input
                v-model="searchForm.keyword"
                placeholder="学校名称、城市"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            
            <el-form-item label="省份">
              <el-select
                v-model="searchForm.province"
                placeholder="全部省份"
                clearable
                class="standard-select"
              >
                <el-option
                  v-for="province in provinceOptions"
                  :key="province"
                  :label="province"
                  :value="province"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="学校类型">
              <el-select
                v-model="searchForm.schoolType"
                placeholder="全部类型"
                clearable
                class="standard-select"
              >
                <el-option
                  v-for="item in schoolTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 操作栏 -->
        <div class="action-bar">
          <div class="action-left">
            <el-button type="primary" @click="handleCreateSchool">
              <el-icon><Plus /></el-icon>
              新增学校
            </el-button>
            <el-button @click="handleImportSchools">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button @click="handleExportSchools">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
        
        <!-- 数据表格 -->
        <el-table
          ref="schoolTableRef"
          v-loading="loading"
          :data="tableData"
          row-key="id"
          lazy
          :load="loadDepartments"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="expand-content">
                <h4>院系列表</h4>
                <div class="department-actions">
                  <el-button size="small" type="primary" @click="handleCreateDepartment(row)">
                    新增院系
                  </el-button>
                </div>
                <el-table :data="row.departments" size="small">
                  <el-table-column prop="name" label="院系名称" />
                  <el-table-column prop="contactPhone" label="联系电话" />
                  <el-table-column prop="address" label="地址" />
                  <el-table-column label="操作" width="200">
                    <template #default="{ row: dept }">
                      <el-button size="small" @click="handleEditDepartment(dept)">编辑</el-button>
                      <el-button size="small" type="danger" @click="handleDeleteDepartment(dept)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column prop="name" label="学校名称" min-width="200" />
          
          <el-table-column prop="schoolType" label="学校类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getSchoolTypeColor(row.schoolType)">
                {{ getSchoolTypeText(row.schoolType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="province" label="省份" width="100" />
          
          <el-table-column prop="city" label="城市" width="100" />
          
          <el-table-column prop="contactPhone" label="联系电话" width="120" />
          
          <el-table-column prop="website" label="官网" width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link v-if="row.website" :href="row.website" target="_blank" type="primary">
                {{ row.website }}
              </el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="departmentCount" label="院系数量" width="100" />
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleEditSchool(row)">编辑</el-button>
              <el-button size="small" type="success" @click="handleCreateDepartment(row)">新增院系</el-button>
              <el-button size="small" type="danger" @click="handleDeleteSchool(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </el-card>
  
      <!-- 学校表单对话框 -->
      <el-dialog
        v-model="schoolDialogVisible"
        :title="isEditSchool ? '编辑学校' : '新增学校'"
        width="600px"
      >
        <el-form ref="schoolFormRef" :model="schoolForm" :rules="schoolRules" label-width="100px">
          <el-form-item label="学校名称" prop="name">
            <el-input v-model="schoolForm.name" placeholder="请输入学校名称" />
          </el-form-item>
          
          <el-form-item label="学校类型" prop="schoolType">
            <el-select
              v-model="schoolForm.schoolType"
              placeholder="请选择学校类型"
              style="width: 100%"
              class="standard-select"
            >
              <el-option
                v-for="item in schoolTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="省份" prop="province">
                <el-select
                  v-model="schoolForm.province"
                  placeholder="请选择省份"
                  style="width: 100%"
                  class="standard-select"
                >
                  <el-option
                    v-for="province in provinceOptions"
                    :key="province"
                    :label="province"
                    :value="province"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="城市" prop="city">
                <el-input v-model="schoolForm.city" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="schoolForm.address" placeholder="请输入详细地址" />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="schoolForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          
          <el-form-item label="官方网站" prop="website">
            <el-input v-model="schoolForm.website" placeholder="请输入官方网站" />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="schoolDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSchool" :loading="schoolSubmitting">
            {{ isEditSchool ? '更新' : '创建' }}
          </el-button>
        </template>
      </el-dialog>
  
      <!-- 院系表单对话框 -->
      <el-dialog
        v-model="departmentDialogVisible"
        :title="isEditDepartment ? '编辑院系' : '新增院系'"
        width="500px"
      >
        <el-form ref="departmentFormRef" :model="departmentForm" :rules="departmentRules" label-width="100px">
          <el-form-item label="院系名称" prop="name">
            <el-input v-model="departmentForm.name" placeholder="请输入院系名称" />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="departmentForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          
          <el-form-item label="地址" prop="address">
            <el-input v-model="departmentForm.address" placeholder="请输入地址" />
          </el-form-item>
          
          <el-form-item label="院系描述" prop="description">
            <el-input
              v-model="departmentForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入院系描述"
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="departmentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitDepartment" :loading="departmentSubmitting">
            {{ isEditDepartment ? '更新' : '创建' }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { Search, Refresh, Plus, Upload, Download } from '@element-plus/icons-vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    getSchoolList, createSchool, updateSchool, deleteSchool,
    getDepartmentsBySchool, createDepartment, updateDepartment, deleteDepartment,
    exportSchools
  } from '@/api/schools'
  
  const loading = ref(false)
  const tableData = ref([])
  const schoolTableRef = ref()
  
  const schoolDialogVisible = ref(false)
  const departmentDialogVisible = ref(false)
  const isEditSchool = ref(false)
  const isEditDepartment = ref(false)
  const schoolSubmitting = ref(false)
  const departmentSubmitting = ref(false)
  
  const schoolFormRef = ref()
  const departmentFormRef = ref()
  
  const searchForm = reactive({
    keyword: '',
    province: null,
    schoolType: null
  })
  
  const pagination = reactive({
    page: 1,
    size: 20,
    total: 0
  })
  
  const schoolForm = reactive({
    name: '',
    schoolType: 'REGULAR',
    province: '',
    city: '',
    address: '',
    contactPhone: '',
    website: ''
  })
  
  const departmentForm = reactive({
    schoolId: '',
    name: '',
    contactPhone: '',
    address: '',
    description: ''
  })

  const refreshDepartments = async (schoolId) => {
    const row = tableData.value.find((s) => s.id === schoolId)
    if (!row) return

    try {
      const res = await getDepartmentsBySchool(schoolId)
      row.departments = res.data

      if (schoolTableRef.value) {
        const map = schoolTableRef.value.store.states.lazyTreeNodeMap.value
        map[schoolId] = undefined
        schoolTableRef.value.toggleRowExpansion(row, false)
        setTimeout(() => {
          schoolTableRef.value.toggleRowExpansion(row, true)
        }, 50)
      }
    } catch (err) {
      console.error('刷新院系失败:', err)
    }

}


  
  const provinceOptions = [
    '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
    '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
    '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
    '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆', '香港', '澳门', '台湾'
  ]

  const schoolTypeOptions = [
    { label: '985工程', value: 'PROJECT_985' },
    { label: '211工程', value: 'PROJECT_211' },
    { label: '双一流', value: 'DOUBLE_FIRST_CLASS' },
    { label: '普通高校', value: 'REGULAR' }
  ]
  
  const schoolRules = {
    name: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
    schoolType: [{ required: true, message: '请选择学校类型', trigger: 'change' }],
    province: [{ required: true, message: '请选择省份', trigger: 'change' }],
    city: [{ required: true, message: '请输入城市', trigger: 'blur' }]
  }
  
  const departmentRules = {
    name: [{ required: true, message: '请输入院系名称', trigger: 'blur' }]
  }
  
  const loadData = async () => {
    loading.value = true
    try {
      const params = {
        page: pagination.page - 1,
        size: pagination.size,
        keyword: searchForm.keyword,
        province: searchForm.province,
        schoolType: searchForm.schoolType
      }
      
      const response = await getSchoolList(params)
      const { content, totalElements } = response.data

      tableData.value = content.map(item => ({
        ...item,
        hasChildren: true
      }))
      pagination.total = totalElements
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      loading.value = false
    }
  }
  
  const loadDepartments = async (tree, treeNode, resolve) => {
    console.log('\uD83D\uDC4D \u6B63\u5728\u52A0\u8F7D\u9662\u7CFB\u4FE1\u606F:', tree.id)
    try {
      const response = await getDepartmentsBySchool(tree.id)
      tree.departments = response.data
      resolve(response.data)
    } catch (error) {
      console.error('加载院系数据失败:', error)
      resolve([])
    }
  }
  
  const handleSearch = () => {
    pagination.page = 1
    loadData()
  }
  
  const handleReset = () => {
    Object.assign(searchForm, {
      keyword: '',
      province: null,
      schoolType: null
    })
    pagination.page = 1
    loadData()
  }
  
  // 学校相关操作
  const handleCreateSchool = () => {
    isEditSchool.value = false
    Object.assign(schoolForm, {
      name: '',
      schoolType: 'REGULAR',
      province: '',
      city: '',
      address: '',
      contactPhone: '',
      website: ''
    })
    schoolDialogVisible.value = true
  }
  
  const handleEditSchool = (row) => {
    isEditSchool.value = true
    Object.assign(schoolForm, row)
    schoolDialogVisible.value = true
  }
  
  const handleSubmitSchool = async () => {
    if (!schoolFormRef.value) return
    
    await schoolFormRef.value.validate(async (valid) => {
      if (!valid) return
      
      schoolSubmitting.value = true
      if (schoolForm.website && !/^https?:\/\//i.test(schoolForm.website)) {
        schoolForm.website = `https://${schoolForm.website}`
      }
      try {
        if (isEditSchool.value) {
          await updateSchool(schoolForm.id, schoolForm)
          ElMessage.success('更新成功')
        } else {
          await createSchool(schoolForm)
          ElMessage.success('创建成功')
        }
        schoolDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        schoolSubmitting.value = false
      }
    })
  }
  
  const handleDeleteSchool = async (row) => {
    try {
      await ElMessageBox.confirm('确定要删除这个学校吗？删除后相关院系和客户数据也会被删除！', '确认删除', {
        type: 'warning'
      })
      
      await deleteSchool(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
  
  // 院系相关操作
  const handleCreateDepartment = (school) => {
    isEditDepartment.value = false
    Object.assign(departmentForm, {
      schoolId: school.id,
      name: '',
      contactPhone: '',
      address: '',
      description: ''
    })
    departmentDialogVisible.value = true
  }
  
  const handleEditDepartment = (dept) => {
    isEditDepartment.value = true
    Object.assign(departmentForm, dept)
    departmentDialogVisible.value = true
  }
  
  const handleSubmitDepartment = async () => {
    if (!departmentFormRef.value) return
    
    await departmentFormRef.value.validate(async (valid) => {
      if (!valid) return
      
      departmentSubmitting.value = true
      try {
        if (isEditDepartment.value) {
          await updateDepartment(departmentForm.id, departmentForm)
          ElMessage.success('更新成功')
        } else {
          await createDepartment(departmentForm)
          ElMessage.success('创建成功')
        }
        departmentDialogVisible.value = false
        await refreshDepartments(departmentForm.schoolId)
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        departmentSubmitting.value = false
      }
    })
  }
  
  const handleDeleteDepartment = async (dept) => {
    try {
      await ElMessageBox.confirm('确定要删除这个院系吗？', '确认删除', {
        type: 'warning'
      })
      
      await deleteDepartment(dept.id)
      ElMessage.success('删除成功')
      await refreshDepartments(dept.schoolId)
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
  
  const handleImportSchools = () => {
    ElMessage.info('批量导入功能开发中...')
  }
  
  const handleExportSchools = async () => {
    try {
      const params = {
        keyword: searchForm.keyword,
        province: searchForm.province,
        schoolType: searchForm.schoolType
      }
      
      const response = await exportSchools(params)
      
      // 创建下载链接
      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `学校数据_${new Date().getTime()}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
    } catch (error) {
      ElMessage.error('导出失败')
    }
  }
  
  const getSchoolTypeColor = (type) => {
    const colorMap = {
      'PROJECT_985': 'danger',
      'PROJECT_211': 'warning',
      'DOUBLE_FIRST_CLASS': 'success',
      'REGULAR': 'info'
    }
    return colorMap[type] || 'info'
  }
  
  const getSchoolTypeText = (type) => {
    const map = Object.fromEntries(schoolTypeOptions.map(item => [item.value, item.label]))
    return map[type] || type
  }
  
  onMounted(() => {
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .school-management {
    .search-bar {
      margin-bottom: 20px;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 4px;
    }
    
    .action-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .expand-content {
      padding: 20px;
      background: #fafafa;
      
      h4 {
        margin: 0 0 16px 0;
        color: #333;
      }
      
      .department-actions {
        margin-bottom: 16px;
      }
    }
    
    .pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
  </style>