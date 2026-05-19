import axios from 'axios'
import { ElMessage } from 'element-plus'

const authRequest = axios.create({
  baseURL: '/auth',
  timeout: 10000
})

authRequest.interceptors.response.use(
  response => response.data,
  error => {
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export function login(data) {
  return authRequest({
    url: '/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return authRequest({
    url: '/register',
    method: 'post',
    data
  })
}
