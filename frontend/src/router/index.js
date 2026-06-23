import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/books', name: 'PublicBooks', component: () => import('../views/public/PublicBooks.vue') },
  { path: '/books/:id', name: 'PublicBookDetail', component: () => import('../views/public/PublicBookDetail.vue') },
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
      // AI智能
      { path: '/ai/search', name: 'SmartSearch', component: () => import('../views/ai/SmartSearch.vue') },
      { path: '/ai/recommend', name: 'Recommend', component: () => import('../views/ai/Recommend.vue') },
      { path: '/ai/hotbooks', name: 'HotBooks', component: () => import('../views/ai/HotBooks.vue') },
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

const PUBLIC_PATHS = ['/login', '/register', '/books']

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  // Allow public routes without token
  const isPublic = PUBLIC_PATHS.some(p => to.path === p || to.path.startsWith('/books/'))
  if (isPublic && !token) {
    return next()
  }

  // No token on protected route → login
  if (!token) {
    return next('/login')
  }

  // Logged-in user on login/register → role-based redirect
  if ((to.path === '/login' || to.path === '/register') && token) {
    const isAdmin = userInfo?.roleName === '管理员'
    return next(isAdmin ? '/dashboard' : '/books')
  }

  next()
})

export default router