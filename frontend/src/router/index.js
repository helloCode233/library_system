import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('../views/dashboard/Index.vue') },
      // 系统管理
      { path: '/system/user', name: 'UserManage', component: () => import('../views/system/UserManage.vue') },
      { path: '/system/role', name: 'RoleManage', component: () => import('../views/system/RoleManage.vue') },
      { path: '/system/menu', name: 'MenuManage', component: () => import('../views/system/MenuManage.vue') },
      // 图书馆业务
      { path: '/category', name: 'Category', component: () => import('../views/category/Index.vue') },
      { path: '/book', name: 'Book', component: () => import('../views/book/Index.vue') },
      { path: '/book/:id', name: 'BookDetail', component: () => import('../views/book/Detail.vue') },
      { path: '/borrow/my', name: 'MyBorrows', component: () => import('../views/borrow/MyRecords.vue') },
      { path: '/borrow/manage', name: 'BorrowManage', component: () => import('../views/borrow/Manage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router