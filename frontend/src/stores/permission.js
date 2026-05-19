import { defineStore } from 'pinia'
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [
      { path: '/dashboard', name: 'Dashboard', meta: { title: '首页' } },
      { path: '/system', name: 'System', meta: { title: '系统管理' }, children: [
        { path: '/system/user', name: 'UserManage', meta: { title: '用户管理', perms: 'user:list' } },
        { path: '/system/role', name: 'RoleManage', meta: { title: '角色管理', perms: 'role:list' } },
        { path: '/system/menu', name: 'MenuManage', meta: { title: '菜单管理', perms: 'menu:list' } }
      ]}
    ]
  }),
  actions: {}
})