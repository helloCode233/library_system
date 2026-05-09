import request from './request'

export function getRoleList(params) {
  return request({ url: '/role/list', method: 'get', params })
}

export function getAllRoles() {
  return request({ url: '/role/all', method: 'get' })
}

export function getRole(id) {
  return request({ url: `/role/${id}`, method: 'get' })
}

export function getRoleMenus(id) {
  return request({ url: `/role/${id}/menus`, method: 'get' })
}

export function createRole(data) {
  return request({ url: '/role', method: 'post', data })
}

export function updateRole(data) {
  return request({ url: '/role', method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/role/${id}`, method: 'delete' })
}

export function assignMenus(id, menuIds) {
  return request({ url: `/role/${id}/menus`, method: 'put', data: menuIds })
}