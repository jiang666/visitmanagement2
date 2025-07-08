// src/api/visits.js
import request from '@/utils/request'

/**
 * 获取拜访记录列表
 */
export function getVisitList(params) {
  return request({
    url: '/visits',
    method: 'get',
    params
  })
}

/**
 * 获取拜访记录详情
 */
export function getVisitDetail(id) {
  return request({
    url: `/visits/${id}`,
    method: 'get'
  })
}

/**
 * 创建拜访记录
 */
export function createVisit(data) {
  return request({
    url: '/visits',
    method: 'post',
    data
  })
}

/**
 * 更新拜访记录
 */
export function updateVisit(id, data) {
  return request({
    url: `/visits/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除拜访记录
 */
export function deleteVisit(id) {
  return request({
    url: `/visits/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除拜访记录
 */
export function batchDeleteVisits(ids) {
  return request({
    url: '/visits/batch-delete',
    method: 'post',
    data: { ids }
  })
}
/**
 * 导出拜访记录
 */
export function exportVisits(params) {
    return request({
      url: '/visits/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
  export function getCustomerVisits(customerId) {
    return request({
      url: `/visits/customer/${customerId}`,
      method: 'get'
    })
  }