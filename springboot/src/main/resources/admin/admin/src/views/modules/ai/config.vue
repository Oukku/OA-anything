<template>
  <div class="ai-config">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <div slot="header"><span>AI 模型配置</span></div>
          <el-form :model="form" label-width="120px" style="max-width: 600px">
            <el-form-item label="Provider">
              <el-select v-model="form.provider" style="width: 100%">
                <el-option label="MOCK（离线模式）" value="mock"></el-option>
                <el-option label="OpenAI" value="openai"></el-option>
                <el-option label="OpenAI 兼容" value="custom"></el-option>
                <el-option label="MiniMax" value="minimax"></el-option>
                <el-option label="Ollama" value="ollama"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="Base URL">
              <el-input v-model="form.baseUrl" placeholder="https://api.openai.com/v1"></el-input>
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="form.model" placeholder="gpt-3.5-turbo"></el-input>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="form.apiKey" type="password" show-password placeholder="保存后将 AES 加密存储"></el-input>
            </el-form-item>
            <el-form-item label="Temperature">
              <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input></el-slider>
            </el-form-item>
            <el-form-item label="Max Tokens">
              <el-input-number v-model="form.maxTokens" :min="256" :max="8192" :step="256"></el-input-number>
            </el-form-item>
            <el-form-item label="RAG Top-K">
              <el-input-number v-model="form.topK" :min="1" :max="20"></el-input-number>
            </el-form-item>
            <el-form-item label="MOCK 模式">
              <el-switch v-model="form.mockMode" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="save" :loading="saving">保存配置</el-button>
              <el-button @click="test">测试连通</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header"><span>说明</span></div>
          <p class="config-tip">1. API Key 会经 AES 加密后存入数据库。</p>
          <p class="config-tip">2. MOCK 模式无需 Key，适合离线演示。</p>
          <p class="config-tip">3. 修改后即时生效，新对话将使用新配置。</p>
          <p class="config-tip">4. RAG-Anything 原生能力负责文档解析、索引与检索。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        provider: 'mock',
        baseUrl: '',
        model: '',
        apiKey: '',
        temperature: 0.7,
        maxTokens: 2048,
        topK: 5,
        mockMode: 1
      },
      saving: false
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    async load() {
      const res = await this.$http.get('/ai/config/info')
      if (res && res.code === 0 && res.data) {
        const d = res.data
        this.form = {
          id: d.id,
          provider: d.provider || 'mock',
          baseUrl: d.baseUrl || '',
          model: d.model || '',
          apiKey: '',
          temperature: d.temperature != null ? Number(d.temperature) : 0.7,
          maxTokens: d.maxTokens || 2048,
          topK: d.topK || 5,
          mockMode: d.mockMode || 0
        }
      }
    },
    async save() {
      this.saving = true
      try {
        const payload = { ...this.form }
        if (!payload.apiKey) delete payload.apiKey
        const res = await this.$http.post('/ai/config/save', payload)
        if (res && res.code === 0) {
          this.$message.success('保存成功')
          this.load()
        } else {
          this.$message.error(res && res.msg ? res.msg : '保存失败')
        }
      } finally {
        this.saving = false
      }
    },
    async test() {
      const res = await this.$http.post('/ai/config/test', this.form)
      if (res && res.code === 0) {
        this.$message.success('连通性测试通过')
      } else {
        this.$message.error(res && res.msg ? res.msg : '测试失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-config { padding: 0; }
.config-tip { font-size: 13px; color: #606266; line-height: 1.8; margin: 0 0 8px; }
</style>
