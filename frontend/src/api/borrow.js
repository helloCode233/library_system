import request from './request'

export function borrowBook(data) {
  return request({ url: '/borrow', method: 'post', data })
}

export function returnBook(id) {
  return request({ url: `/borrow/${id}/return`, method: 'put' })
}

export function getMyBorrows() {
  return request({ url: '/borrow/my', method: 'get' })
}

export function getAllBorrows() {
  return request({ url: '/borrow/list', method: 'get' })
}