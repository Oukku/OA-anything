import Vue from 'vue'
import VueRouter from 'vue-router'
import http from '@/utils/http'

Vue.use(VueRouter)

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login.vue') },
  {
    path: '/',
    component: () => import('@/views/index.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home.vue'), meta: { title: '首页' } },
      { path: 'users', name: 'Users', component: () => import('@/views/modules/users/list.vue'), meta: { title: '用户管理' } },
      { path: 'employee', name: 'Employee', component: () => import('@/views/modules/employee/list.vue'), meta: { title: '员工管理' } },
      { path: 'dept', name: 'Dept', component: () => import('@/views/modules/dept/list.vue'), meta: { title: '部门管理' } },
      { path: 'gonggao', name: 'Gonggao', component: () => import('@/views/modules/gonggao/list.vue'), meta: { title: '公告管理' } },
      { path: 'wenjian', name: 'Wenjian', component: () => import('@/views/modules/wenjian/list.vue'), meta: { title: '文件管理' } },
      { path: 'gongzuorizhi', name: 'Gongzuorizhi', component: () => import('@/views/modules/gongzuorizhi/list.vue'), meta: { title: '工作日志' } },
      {
        path: 'ai',
        component: () => import('@/views/modules/ai/layout.vue'),
        redirect: '/ai/chat',
        meta: { title: 'AI 中心' },
        children: [
          { path: 'chat', name: 'AiChat', component: () => import('@/views/modules/ai/chat.vue'), meta: { title: 'AI 对话' } },
          { path: 'kb', name: 'AiKb', component: () => import('@/views/modules/ai/kb.vue'), meta: { title: '知识库管理' } },
          { path: 'graph', name: 'AiGraph', component: () => import('@/views/modules/ai/graph.vue'), meta: { title: '知识图谱' } },
          { path: 'config', name: 'AiConfig', component: () => import('@/views/modules/ai/config.vue'), meta: { title: 'AI 配置' } }
        ]
      }
    ]
  },
  { path: '/404', name: 'NotFound', component: () => import('@/views/404.vue') },
  { path: '*', redirect: '/404' }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.NODE_ENV === 'production' ? '/springboot-oa-v2/' : '/',
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') return next()
  if (!token) return next('/login')
  next()
})

export default router
