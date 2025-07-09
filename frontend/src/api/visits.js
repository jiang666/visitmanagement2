// src/api/visits.js
import request from '@/utils/request'

/**
 * 获取拜访记录列表
 */
export function getVisitList(params) {
  return request({
    url: '/visit-records',
    method: 'get',
    params
  })
}

/**
 * 获取拜访记录详情
 */
export function getVisitDetail(id) {
  return request({
    url: `/visit-records/${id}`,
    method: 'get'
  })
}

/**
 * 创建拜访记录
 */
export function createVisit(data) {
  return request({
    url: '/visit-records',
    method: 'post',
    data
  })
}

/**
 * 更新拜访记录
 */
export function updateVisit(id, data) {
  return request({
    url: `/visit-records/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除拜访记录
 */
export function deleteVisit(id) {
  return request({
    url: `/visit-records/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除拜访记录
 */
export function batchDeleteVisits(ids) {
  return request({
    url: '/visit-records/batch-delete',
    method: 'post',
    data: { ids }
  })
}
/**
 * 导出拜访记录
 */
export function exportVisits(params) {
    return request({
      url: '/visit-records/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
  export function getCustomerVisits(customerId) {
    return request({
      url: `/visit-records/customer/${customerId}`,
      method: 'get'
    })  }