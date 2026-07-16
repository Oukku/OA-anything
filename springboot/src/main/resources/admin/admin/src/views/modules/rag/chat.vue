<template>
  <div class="rag-chat">
    <el-card class="rag-sidebar" shadow="never">
      <div slot="header" class="clearfix">
        <span>知识库文档</span>
        <el-upload
          class="rag-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
          :before-upload="beforeUpload"
          accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.md,.jpg,.jpeg,.png"
        >
          <el-button size="mini" type="primary" icon="el-icon-upload2">上传文档</el-button>
        </el-upload>
      </div>
      <el-empty v-if="!documents.length" description="暂无文档" :image-size="80"></el-empty>
      <el-scrollbar v-else wrap-class="rag-doc-scroll">
        <div
          v-for="d in documents"
          :key="d.id"
          class="rag-doc-item"
          :class="{ active: currentDocId === d.id }"
          @click="selectDoc(d)"
        >
          <i class="el-icon-document"></i>
          <span class="rag-doc-name" :title="d.filename">{{ d.filename }}</span>
          <el-button
            type="text"
            icon="el-icon-delete"
            class="rag-doc-del"
            @click.stop="deleteDoc(d)"
          ></el-button>
        </div>
      </el-scrollbar>
    </el-card>

    <div class="rag-main">
      <el-card shadow="never" class="rag-chat-card">
        <div slot="header">
          <i class="el-icon-magic-stick"></i>
          <span style="margin-left: 8px">AI 知识库问答</span>
          <el-select
            v-model="mode"
            size="mini"
            style="float: right; width: 110px"
          >
            <el-option label="混合" value="hybrid"></el-option>
            <el-option label="本地" value="local"></el-option>
            <el-option label="全局" value="global"></el-option>
          </el-select>
        </div>

        <div class="rag-messages" ref="messagesBox">
          <div v-if="!messages.length" class="rag-empty">
            <i class="el-icon-magic-stick" style="font-size: 48px; color: #409eff"></i>
            <p>上传企业文档，开启智能问答</p>
            <p class="rag-hint">例如：年假怎么请？ / 合同违约金条款 / 上季度营收</p>
          </div>
          <div
            v-for="(m, idx) in messages"
            :key="idx"
            class="rag-msg"
            :class="'rag-msg-' + m.role"
          >
            <div class="rag-msg-avatar">
              <i :class="m.role === 'user' ? 'el-icon-user' : 'el-icon-magic-stick'"></i>
            </div>
            <div class="rag-msg-bubble">
              <div class="rag-msg-text">{{ m.content }}</div>
              <div v-if="m.durationMs" class="rag-msg-meta">{{ m.durationMs }}ms · {{ m.mode }}</div>
            </div>
          </div>
          <div v-if="loading" class="rag-msg rag-msg-bot">
            <div class="rag-msg-avatar"><i class="el-icon-magic-stick"></i></div>
            <div class="rag-msg-bubble rag-msg-loading">
              <span class="rag-dot"></span><span class="rag-dot"></span><span class="rag-dot"></span>
            </div>
          </div>
        </div>

        <div class="rag-input-bar">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="请输入你的问题，回车发送"
            @keydown.enter.native.exact.prevent="send"
          ></el-input>
          <el-button
            type="primary"
            icon="el-icon-position"
            :loading="loading"
            @click="send"
            style="margin-left: 8px"
          >发送</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      question: '',
      mode: 'hybrid',
      loading: false,
      currentDocId: null,
      documents: [],
      messages: [],
      uploadUrl: this.$http.adornUrl('/springboot-oa-v2/rag/upload'),
      uploadHeaders: { token: this.$store.state.user.token }
    }
  },
  mounted() {
    this.loadDocuments()
  },
  methods: {
    selectDoc(d) { this.currentDocId = d.id },
    beforeUpload(file) {
      if (file.size > 100 * 1024 * 1024) {
        this.$message.error('文件不能超过 100MB')
        return false
      }
      return true
    },
    onUploadSuccess() {
      this.$message.success('上传并索引成功')
      this.loadDocuments()
    },
    onUploadError(err) {
      this.$message.error('上传失败：' + (err.message || ''))
    },
    async loadDocuments() {
      const { data } = await this.$http.get('/springboot-oa-v2/rag/documents')
      if (data && data.code === 0) {
        this.documents = data.data || []
      } else if (Array.isArray(data)) {
        this.documents = data
      }
    },
    async deleteDoc(d) {
      try { await this.$confirm(`确认删除「${d.filename}」？`, '提示', { type: 'warning' }) } catch { return }
      await this.$http.delete(`/springboot-oa-v2/rag/documents/${d.id}`)
      this.$message.success('已删除')
      this.loadDocuments()
    },
    async send() {
      const q = this.question.trim()
      if (!q || this.loading) return
      this.messages.push({ role: 'user', content: q })
      this.question = ''
      this.loading = true
      try {
        const { data } = await this.$http.post('/springboot-oa-v2/rag/query', {
          question: q,
          mode: this.mode,
          kbId: 'default'
        })
        this.messages.push({
          role: 'bot',
          content: (data && data.answer) || (typeof data === 'string' ? data : '（无回复）'),
          mode: this.mode,
          durationMs: data && data.durationMs
        })
      } catch (e) {
        this.messages.push({ role: 'bot', content: '请求失败：' + (e.message || '') })
      } finally {
        this.loading = false
        this.$nextTick(() => {
          const box = this.$refs.messagesBox
          if (box) box.scrollTop = box.scrollHeight
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.rag-chat {
  display: flex;
  gap: 12px;
  height: calc(100vh - 180px);
  min-height: 540px;
}
.rag-sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.rag-uploader { float: right; }
.rag-doc-scroll { height: calc(100vh - 280px); }
.rag-doc-item {
  display: flex;
  align-items: center;
  padding: 8px 4px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; color: #409eff; }
}
.rag-doc-name { flex: 1; margin-left: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rag-doc-del { padding: 2px 4px; }

.rag-main { flex: 1; min-width: 0; }
.rag-chat-card { display: flex; flex-direction: column; height: 100%; }
.rag-chat-card >>> .el-card__body { display: flex; flex-direction: column; flex: 1; min-height: 0; }

.rag-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #fafbfc;
  border-radius: 6px;
  margin-bottom: 12px;
}
.rag-empty {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
  p { margin: 8px 0; }
  .rag-hint { color: #c0c4cc; font-size: 12px; }
}
.rag-msg { display: flex; gap: 8px; margin-bottom: 14px; }
.rag-msg-user { flex-direction: row-reverse; }
.rag-msg-user .rag-msg-bubble { background: #409eff; color: #fff; }
.rag-msg-bot .rag-msg-bubble { background: #fff; border: 1px solid #ebeef5; }
.rag-msg-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  color: #606266;
}
.rag-msg-user .rag-msg-avatar { background: #409eff; color: #fff; }
.rag-msg-bubble {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 6px;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.6;
}
.rag-msg-meta { font-size: 11px; opacity: .6; margin-top: 4px; }
.rag-msg-loading { display: flex; gap: 4px; align-items: center; }
.rag-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #c0c4cc;
  animation: ragBlink 1.2s infinite;
  &:nth-child(2) { animation-delay: .2s; }
  &:nth-child(3) { animation-delay: .4s; }
}
@keyframes ragBlink { 0%, 80%, 100% { opacity: .3 } 40% { opacity: 1 } }

.rag-input-bar { display: flex; align-items: flex-start; }
.rag-input-bar >>> .el-textarea { flex: 1; }
</style>
