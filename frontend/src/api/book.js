import request from './request'

export function getBookList(params) {
  return request({ url: '/book/list', method: 'get', params })
}

export function getBook(id) {
  return request({ url: `/book/${id}`, method: 'get' })
}

export function createBook(data) {
  return request({ url: '/book', method: 'post', data })
}

export function updateBook(data) {
  return request({ url: '/book', method: 'put', data })
}

export function deleteBook(id) {
  return request({ url: `/book/${id}`, method: 'delete' })
}
