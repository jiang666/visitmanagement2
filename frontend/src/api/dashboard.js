import request from '@/utils/request'

/**
 * 获取仪表盘数据
 */
export function getDashboardData() {
  return request({
    url: '/dashboard/overview',
    method: 'get'
  })
}

/**
 * 获取统计数据
 */
export function getStatistics(params) {
  return request({
    url: '/dashboard/visit-statistics',
    method: 'get',
    params
  })
}
