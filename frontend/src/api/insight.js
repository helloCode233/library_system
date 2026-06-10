import request from './request'

export function getHotBooksRank(topN = 10) {
  return request({ url: '/ai/insight/hot', method: 'get', params: { topN } })
}

export function getMonthlyTrend(months = 12) {
  return request({ url: '/ai/insight/trend', method: 'get', params: { months } })
}

export function getCategoryDistribution() {
  return request({ url: '/ai/insight/category', method: 'get' })
}

export function getOverview() {
  return request({ url: '/ai/insight/overview', method: 'get' })
}
