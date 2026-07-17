import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

const http = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/springboot-oa-v2',
  timeout: 30000
})

http.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers['token'] = token
  return cfg
})

http.interceptors.response.use(
  res => {
    const data = res.data
    if (data && data.code === 401) {
      Message.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    }
    return data
  },
  err => {
    Message.error(err.message || '请求失败')
    return Promise.reject(err)
  }
)

export default http
