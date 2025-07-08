import request from '@/utils/request'

export function getAnalysisData(params) {
  return request({
    url: '/analysis/data',
    method: 'get',
    params
  })
}

export function getSalesPerformance(params) {
  return request({
    url: '/analysis/performance',
    method: 'get',
    params
  })
}

export function exportAnalysisReport(params) {
  return request({
    url: '/analysis/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
