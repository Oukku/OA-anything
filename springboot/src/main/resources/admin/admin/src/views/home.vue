<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card>
          <div style="display: flex; align-items: center">
            <i :class="card.icon" :style="{ fontSize: '40px', color: card.color, marginRight: '16px' }"></i>
            <div>
              <div style="color: #999; font-size: 13px">{{ card.title }}</div>
              <div style="font-size: 24px; font-weight: bold">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 16px">
      <div slot="header"><b>欢迎使用 OA 办公自动化系统 V2</b></div>
      <p>本系统集成 RAG-Anything 智能问答能力，支持企业知识库、公告、文件、员工等模块的智能检索。</p>
      <p>快捷入口：<el-button type="text" @click="$router.push('/rag/chat')">AI 知识库问答</el-button></p>
    </el-card>
  </div>
</template>

<script>
import http from '@/utils/http'
export default {
  data() {
    return {
      cards: [
        { title: '用户总数', value: 0, icon: 'el-icon-user', color: '#1890ff' },
        { title: '员工总数', value: 0, icon: 'el-icon-user-solid', color: '#52c41a' },
        { title: '公告数', value: 0, icon: 'el-icon-bell', color: '#faad14' },
        { title: '文件数', value: 0, icon: 'el-icon-folder', color: '#f5222d' }
      ]
    }
  },
  mounted() {
    this.loadCounts()
  },
  methods: {
    async loadCounts() {
      try {
        const users = await http.get('/users/page?page=1&limit=1'); if (users.code === 0) this.cards[0].value = users.data.total
        const emp = await http.get('/employee/page?page=1&limit=1'); if (emp.code === 0) this.cards[1].value = emp.data.total
        const gg = await http.get('/gonggaoxinxi/page?page=1&limit=1'); if (gg.code === 0) this.cards[2].value = gg.data.total
        const wj = await http.get('/wenjianxinxi/page?page=1&limit=1'); if (wj.code === 0) this.cards[3].value = wj.data.total
      } catch (e) { /* ignore */ }
    }
  }
}
</script>
