import request from '@/utils/request'

export function getSchoolList(params) {
  return request({
    url: '/schools',
    method: 'get',
    params
  })
}

export function getSchoolDetail(id) {
  return request({
    url: `/schools/${id}`,
    method: 'get'
  })
}

export function createSchool(data) {
  return request({
    url: '/schools',
    method: 'post',
    data
  })
}

export function updateSchool(id, data) {
  return request({
    url: `/schools/${id}`,
    method: 'put',
    data
  })
}

export function deleteSchool(id) {
  return request({
    url: `/schools/${id}`,
    method: 'delete'
  })
}

export function getDepartmentsBySchool(schoolId) {
  return request({
    url: `/schools/${schoolId}/departments`,
    method: 'get'
  })
}

export function createDepartment(data) {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

export function updateDepartment(id, data) {
  return request({
    url: `/departments/${id}`,
    method: 'put',
    data
  })
}

export function deleteDepartment(id) {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
}

export function getSchoolDepartmentTree() {
  return request({
    url: '/schools/tree',
    method: 'get'
  })
}

export function exportSchools(params) {
  return request({
    url: '/schools/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
