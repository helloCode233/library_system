import request from './request'

export function aiSearch(params) {
  return request({ url: '/ai/search', method: 'post', data: params })
}

export function rebuildIndex() {
  return request({ url: '/ai/rebuild-index', method: 'post' })
}
