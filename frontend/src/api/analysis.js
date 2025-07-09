import request from '@/utils/request'

export function getAnalysisData(params) {
  return request({
    url: '/dashboard/visit-statistics',
    method: 'get',
    params
  })
}

export function getSalesPerformance(params) {
  return request({
    url: '/dashboard/sales-performance',
    method: 'get',
    params
  })
}

export function exportAnalysisReport(params) {
  return request({
    url: '/visit-records/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
