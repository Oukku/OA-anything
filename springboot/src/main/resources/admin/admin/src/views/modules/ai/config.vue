<template>
  <div class="ai-config">
    <div class="config-header glass-card">
      <div class="config-header-title">
        <i class="el-icon-setting"></i>
        <span>AI 模型配置</span>
      </div>
      <div class="config-header-desc">配置 RAG 多阶段流水线：向量检索 / 重排序 / LLM 总结</div>
      <div class="config-header-switch">
        <span>MOCK 模式</span>
        <el-switch v-model="form.mockMode" :active-value="1" :inactive-value="0"></el-switch>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="8">
        <div class="glass-card model-card">
          <div class="model-card-header">
            <i class="el-icon-coin"></i>
            <span>Embedding 向量模型</span>
            <el-tag size="mini" type="success" effect="plain">检索</el-tag>
          </div>
          <el-form :model="form" label-width="90px" size="small">
            <el-form-item label="Provider">
              <el-select v-model="form.embeddingProvider" style="width: 100%">
                <el-option label="硅基流动" value="siliconflow"></el-option>
                <el-option label="OpenAI" value="openai"></el-option>
                <el-option label="Ollama" value="ollama"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="Base URL">
              <el-input v-model="form.embeddingBaseUrl" placeholder="https://api.siliconflow.cn/v1"></el-input>
            </el-form-item>
            <el-form-item label="模型">
              <el-input v-model="form.embeddingModel" placeholder="BAAI/bge-m3"></el-input>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="form.embeddingApiKey" type="password" show-password placeholder="留空保留旧值"></el-input>
            </el-form-item>
            <el-form-item label="维度">
              <el-input-number v-model="form.embeddingDim" :min="256" :max="4096" :step="256" style="width: 100%"></el-input-number>
            </el-form-item>
            <el-button size="small" @click="testEmbedding" :loading="testingEmbedding" style="width: 100%">测试连通</el-button>
          </el-form>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="glass-card model-card">
          <div class="model-card-header">
            <i class="el-icon-cpu"></i>
            <span>LLM 大语言模型</span>
            <el-tag size="mini" type="primary" effect="plain">总结</el-tag>
          </div>
          <el-form :model="form" label-width="90px" size="small">
            <el-form-item label="Provider">
              <el-select v-model="form.llmProvider" style="width: 100%">
                <el-option label="硅基流动" value="siliconflow"></el-option>
                <el-option label="OpenAI" value="openai"></el-option>
                <el-option label="Ollama" value="ollama"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="Base URL">
              <el-input v-model="form.llmBaseUrl" placeholder="https://api.siliconflow.cn/v1"></el-input>
            </el-form-item>
            <el-form-item label="模型">
              <el-input v-model="form.llmModel" placeholder="Qwen/Qwen2.5-7B-Instruct"></el-input>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="form.llmApiKey" type="password" show-password placeholder="留空保留旧值"></el-input>
            </el-form-item>
            <el-form-item label="Temperature">
              <el-slider v-model="form.llmTemperature" :min="0" :max="2" :step="0.1" show-input :show-input-controls="false"></el-slider>
            </el-form-item>
            <el-form-item label="Max Tokens">
              <el-input-number v-model="form.llmMaxTokens" :min="256" :max="8192" :step="256" style="width: 100%"></el-input-number>
            </el-form-item>
            <el-button size="small" @click="testLlm" :loading="testingLlm" style="width: 100%">测试连通</el-button>
          </el-form>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="glass-card model-card">
          <div class="model-card-header">
            <i class="el-icon-sort"></i>
            <span>Reranker 重排序</span>
            <el-tag size="mini" type="warning" effect="plain">精排</el-tag>
          </div>
          <el-form :model="form" label-width="90px" size="small">
            <el-form-item label="Provider">
              <el-select v-model="form.rerankerProvider" style="width: 100%">
                <el-option label="硅基流动" value="siliconflow"></el-option>
                <el-option label="OpenAI" value="openai"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="Base URL">
              <el-input v-model="form.rerankerBaseUrl" placeholder="https://api.siliconflow.cn/v1"></el-input>
            </el-form-item>
            <el-form-item label="模型">
              <el-input v-model="form.rerankerModel" placeholder="BAAI/bge-reranker-v2-m3"></el-input>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="form.rerankerApiKey" type="password" show-password placeholder="留空保留旧值"></el-input>
            </el-form-item>
            <el-form-item label="RAG Top-K">
              <el-input-number v-model="form.topK" :min="1" :max="20" style="width: 100%"></el-input-number>
            </el-form-item>
            <el-button size="small" @click="testReranker" :loading="testingReranker" style="width: 100%">测试连通</el-button>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <div class="glass-card prompt-card">
      <div class="model-card-header">
        <i class="el-icon-edit-outline"></i>
        <span>系统提示词（约束 AI 输出格式）</span>
        <el-tag size="mini" type="info" effect="plain">可选</el-tag>
        <el-button size="mini" type="text" @click="resetPrompt" style="margin-left: auto">重置为默认</el-button>
      </div>
      <div class="prompt-desc">
        自定义 LLM 系统提示词，用于约束 AI 回答的语言、格式、数字规范等。
        必须包含 <code>&#123;&#123;context_data&#125;&#125;</code> 占位符（LightRAG 会自动替换为检索到的文档内容）。
        留空则使用内置默认模板。
      </div>
      <el-input
        v-model="form.systemPrompt"
        type="textarea"
        :rows="10"
        placeholder="留空使用默认提示词模板（中文 + Markdown + 禁止乱码）"
      ></el-input>
    </div>

    <div class="glass-card config-actions">
      <el-button type="primary" @click="save" :loading="saving">保存配置</el-button>
      <div class="config-tips">
        <span>1. 三组模型共用硅基流动一个 Key 即可</span>
        <span>2. API Key 经 AES 加密存储</span>
        <span>3. 保存后自动推送到 RAG 引擎并重新初始化</span>
        <span>4. 系统提示词变更不需要重新初始化 RAG 引擎</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        id: null,
        mockMode: 1,
        topK: 5,
        embeddingProvider: 'siliconflow',
        embeddingBaseUrl: 'https://api.siliconflow.cn/v1',
        embeddingModel: 'BAAI/bge-m3',
        embeddingApiKey: '',
        embeddingDim: 1024,
        llmProvider: 'siliconflow',
        llmBaseUrl: 'https://api.siliconflow.cn/v1',
        llmModel: 'Qwen/Qwen2.5-7B-Instruct',
        llmApiKey: '',
        llmTemperature: 0.7,
        llmMaxTokens: 2048,
        systemPrompt: '',
        rerankerProvider: 'siliconflow',
        rerankerBaseUrl: 'https://api.siliconflow.cn/v1',
        rerankerModel: 'BAAI/bge-reranker-v2-m3',
        rerankerApiKey: ''
      },
      // 默认系统提示词模板（与后端 AiConfigService.DEFAULT_SYSTEM_PROMPT 一致）
      defaultSystemPrompt: [
        '你是 OA 系统的知识库问答助手。请严格遵循以下规则：',
        '',
        '1. 输出语言：始终使用简体中文回答。',
        '2. 输出格式：使用规范的 Markdown，可用标题（#/##/###）、有序列表（1. 2.）、无序列表（-）、表格等。',
        '3. 数字与金额：所有数字、年限、金额、天数必须使用阿拉伯数字（如 5 天、500 元、3 年）。禁止用星号（*）、字母（a/o/x）或其他符号代替数字。',
        '4. 章节编号：保持原文档章节顺序，如"第一章"、"1.1"。禁止写成"第章第二"这种错乱格式。',
        '5. 引用来源：不要在末尾添加 References 列表，除非用户明确要求。',
        '6. 内容准确性：严格基于检索到的 Context 回答，不要编造信息。Context 不完整时明确说明"知识库内容不完整"。',
        '7. 简洁性：回答简明扼要，避免重复，禁止使用"夃、夊、夤、夥"等异常汉字。',
        '',
        '---Context---',
        '{context_data}'
      ].join('\n'),
      saving: false,
      testingEmbedding: false,
      testingLlm: false,
      testingReranker: false
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
        Object.assign(this.form, {
          id: d.id,
          mockMode: d.mockMode || 0,
          topK: d.topK || 5,
          embeddingProvider: d.embeddingProvider || 'siliconflow',
          embeddingBaseUrl: d.embeddingBaseUrl || 'https://api.siliconflow.cn/v1',
          embeddingModel: d.embeddingModel || 'BAAI/bge-m3',
          embeddingApiKey: '',
          embeddingDim: d.embeddingDim || 1024,
          llmProvider: d.llmProvider || 'siliconflow',
          llmBaseUrl: d.llmBaseUrl || 'https://api.siliconflow.cn/v1',
          llmModel: d.llmModel || 'Qwen/Qwen2.5-7B-Instruct',
          llmApiKey: '',
          llmTemperature: d.llmTemperature != null ? Number(d.llmTemperature) : 0.7,
          llmMaxTokens: d.llmMaxTokens || 2048,
          systemPrompt: d.systemPrompt || '',
          rerankerProvider: d.rerankerProvider || 'siliconflow',
          rerankerBaseUrl: d.rerankerBaseUrl || 'https://api.siliconflow.cn/v1',
          rerankerModel: d.rerankerModel || 'BAAI/bge-reranker-v2-m3',
          rerankerApiKey: ''
        })
      }
    },
    async save() {
      this.saving = true
      try {
        const res = await this.$http.post('/ai/config/save', this.form)
        if (res && res.code === 0) {
          this.$message.success('保存成功，已推送到 RAG 引擎')
          this.load()
        } else {
          this.$message.error(res && res.msg ? res.msg : '保存失败')
        }
      } finally {
        this.saving = false
      }
    },
    async testEmbedding() {
      this.testingEmbedding = true
      try {
        const res = await this.$http.post('/ai/config/test/embedding', this.form)
        this.handleTestResult(res)
      } finally {
        this.testingEmbedding = false
      }
    },
    async testLlm() {
      this.testingLlm = true
      try {
        const res = await this.$http.post('/ai/config/test/llm', this.form)
        this.handleTestResult(res)
      } finally {
        this.testingLlm = false
      }
    },
    async testReranker() {
      this.testingReranker = true
      try {
        const res = await this.$http.post('/ai/config/test/reranker', this.form)
        this.handleTestResult(res)
      } finally {
        this.testingReranker = false
      }
    },
    handleTestResult(res) {
      if (res && res.code === 0) {
        const data = res.data || {}
        if (data.ok) {
          this.$message.success(data.msg || '连通性测试通过')
        } else {
          this.$message.error(data.msg || '测试失败')
        }
      } else {
        this.$message.error(res && res.msg ? res.msg : '测试失败')
      }
    },
    resetPrompt() {
      this.form.systemPrompt = this.defaultSystemPrompt
      this.$message.success('已重置为默认提示词，记得点击保存')
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-config { padding: 0; }

.glass-card {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: var(--biz-shadow);
}

.config-header {
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .config-header-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--biz-text-1);
    i { margin-right: 8px; color: var(--biz-primary); }
  }
  .config-header-desc {
    flex: 1;
    margin-left: 24px;
    font-size: 13px;
    color: var(--biz-text-3);
  }
  .config-header-switch {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--biz-text-2);
  }
}

.model-card {
  padding: 16px 20px;

  .model-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--biz-border);

    i { font-size: 18px; color: var(--biz-primary); }
    span { font-size: 14px; font-weight: 600; color: var(--biz-text-1); flex: 1; }
  }
}

.config-actions {
  margin-top: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 20px;

  .config-tips {
    flex: 1;
    display: flex;
    flex-direction: column;
    font-size: 12px;
    color: var(--biz-text-3);
    line-height: 1.8;
  }
}

.prompt-card {
  margin-top: 16px;
  padding: 16px 20px;

  .prompt-desc {
    font-size: 12px;
    color: var(--biz-text-3);
    line-height: 1.7;
    margin-bottom: 10px;
    code {
      background: rgba(30, 64, 175, 0.08);
      color: var(--biz-primary);
      padding: 1px 6px;
      border-radius: 3px;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 12px;
    }
  }
}
</style>
