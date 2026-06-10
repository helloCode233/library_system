import request from './request'

export function getSimilarBooks(bookId, topN = 6) {
  return request({ url: `/ai/recommend/book/${bookId}`, method: 'get', params: { topN } })
}

export function getRecommendForUser(topN = 6) {
  return request({ url: '/ai/recommend/user', method: 'get', params: { topN } })
}

export function getHotBooks(topN = 10) {
  return request({ url: '/ai/hot', method: 'get', params: { topN } })
}
