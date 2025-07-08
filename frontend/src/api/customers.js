import request from '@/utils/request'

export function getCustomerList(params) {
  return request({
    url: '/customers',
    method: 'get',
    params
  })
}

export function getCustomerDetail(id) {
  return request({
    url: `/customers/${id}`,
    method: 'get'
  })
}

export function createCustomer(data) {
  return request({
    url: '/customers',
    method: 'post',
    data
  })
}

export function updateCustomer(id, data) {
  return request({
    url: `/customers/${id}`,
    method: 'put',
    data
  })
}

export function deleteCustomer(id) {
  return request({
    url: `/customers/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCustomers(ids) {
  return request({
    url: '/customers/batch-delete',
    method: 'post',
    data: ids
  })
}

export function exportCustomers(params) {
  return request({
    url: '/customers/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function searchCustomers(params) {
  return request({
    url: '/customers/search',
    method: 'get',
    params
  })
}