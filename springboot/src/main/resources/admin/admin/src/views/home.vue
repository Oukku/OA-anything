<template>
  <div class="home-page">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <div class="stat-card glass-card">
          <div class="stat-icon" :style="{ background: card.bg }">
            <i :class="card.icon"></i>
          </div>
          <div class="stat-info">
            <div class="stat-title">{{ card.title }}</div>
            <div class="stat-value">{{ card.value }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="welcome-card glass-card">
      <div class="welcome-header">
        <i class="el-icon-magic-stick"></i>
        <span>欢迎使用 OA 办公自动化系统</span>
      </div>
      <p class="welcome-text">
        本系统集成 RAG-Anything 智能问答能力，支持企业知识库、公告、文件、员工等模块的智能检索。
        基于多模型协同的 RAG 流水线：文档解析 → 知识图谱提取 → 关键词检索 → 向量匹配 → LLM 总结回答。
      </p>
      <div class="welcome-actions">
        <el-button type="primary" icon="el-icon-chat-dot-square" @click="$router.push('/ai/chat')">AI 对话</el-button>
        <el-button icon="el-icon-collection" @click="$router.push('/ai/kb')">知识库管理</el-button>
        <el-button icon="el-icon-setting" @click="$router.push('/ai/config')">AI 配置</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import http from '@/utils/http'
export default {
  data() {
    return {
      cards: [
        { title: '用户总数', value: 0, icon: 'el-icon-user', bg: 'linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%)' },
        { title: '员工总数', value: 0, icon: 'el-icon-user-solid', bg: 'linear-gradient(135deg, #67c23a 0%, #4e9c2c 100%)' },
        { title: '公告数', value: 0, icon: 'el-icon-bell', bg: 'linear-gradient(135deg, #e6a23c 0%, #c4821c 100%)' },
        { title: '文件数', value: 0, icon: 'el-icon-folder', bg: 'linear-gradient(135deg, #f56c6c 0%, #d04545 100%)' }
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

<style scoped>
.home-page { padding: 0; }

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 21, 41, 0.06);
}

.stat-card {
  padding: 20px;
  display: flex; align-items: center; gap: 16px;
  transition: transform .2s, box-shadow .2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 21, 41, 0.1);
}
.stat-icon {
  width: 50px; height: 50px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 21, 41, 0.12);
}
.stat-info { flex: 1; }
.stat-title { color: #909399; font-size: 13px; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 600; color: #1f2d3d; }

.welcome-card {
  margin-top: 16px;
  padding: 24px;
}
.welcome-header {
  display: flex; align-items: center; gap: 8px;
  font-size: 16px; font-weight: 600; color: #1f2d3d;
  margin-bottom: 12px;
}
.welcome-header i { color: #4a90e2; font-size: 20px; }
.welcome-text {
  font-size: 14px; line-height: 1.8; color: #606266;
  margin: 0 0 18px;
}
.welcome-actions {
  display: flex; gap: 10px;
}
</style>
