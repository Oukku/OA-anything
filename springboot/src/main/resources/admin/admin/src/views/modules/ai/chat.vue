<template>
  <div class="ai-chat">
    <aside class="chat-sidebar glass-card">
      <div class="sidebar-header">
        <i class="el-icon-collection"></i>
        <span>会话列表</span>
      </div>
      <div class="sidebar-toolbar">
        <el-select v-model="currentKbId" size="small" placeholder="选择知识库" style="flex:1">
          <el-option v-for="k in kbs" :key="k.id" :label="k.name" :value="k.id"></el-option>
        </el-select>
        <el-button size="small" type="primary" icon="el-icon-plus" circle @click="createSession"></el-button>
      </div>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: currentSession && currentSession.id === s.id }"
          @click="selectSession(s)"
        >
          <i class="el-icon-chat-dot-square session-item-icon"></i>
          <span class="session-title">{{ s.title }}</span>
          <el-dropdown trigger="click" @command="cmd => handleSessionCmd(cmd, s)" @click.stop.native>
            <span class="session-more"><i class="el-icon-more"></i></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="rename" icon="el-icon-edit">重命名</el-dropdown-item>
              <el-dropdown-item command="delete" icon="el-icon-delete" style="color: #f56c6c">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="60"></el-empty>
      </div>
    </aside>

    <section class="chat-main glass-card">
      <div class="chat-messages" ref="messagesBox">
        <div v-if="!messages.length" class="chat-empty">
          <div class="chat-empty-icon"><i class="el-icon-cpu"></i></div>
          <p class="chat-empty-title">选择一个知识库，开始智能问答</p>
          <p class="chat-empty-hint">AI 将基于知识库中的文档进行检索与回答</p>
        </div>

        <div
          v-for="(m, idx) in messages"
          :key="idx"
          class="chat-msg"
          :class="'chat-msg-' + m.role"
        >
          <div class="chat-avatar" :class="'avatar-' + m.role">
            <i :class="m.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
          </div>
          <div class="chat-bubble">
            <div
              class="chat-text"
              :class="{ 'chat-text-md': m.role === 'bot' }"
              v-html="m.role === 'bot' ? renderMd(m.content) : ''"
              v-if="m.role === 'bot'"
            ></div>
            <div class="chat-text" v-else>{{ m.content }}</div>
            <div v-if="m.durationMs" class="chat-meta">
              <i class="el-icon-time"></i> {{ m.durationMs }}ms · {{ m.mode }}
            </div>
          </div>
        </div>

        <div v-if="loading" class="chat-msg chat-msg-bot">
          <div class="chat-avatar avatar-bot"><i class="el-icon-cpu"></i></div>
          <div class="chat-bubble chat-stage-panel">
            <div class="stage-panel-title">
              <i class="el-icon-loading"></i>
              <span>RAG 流水线</span>
              <span v-if="streamTotalMs" class="stage-panel-total">{{ streamTotalMs }}ms</span>
            </div>
            <div class="stage-list">
              <div
                v-for="s in stages"
                :key="s.key"
                class="stage-item"
                :class="'stage-' + s.status"
              >
                <div class="stage-icon">
                  <i v-if="s.status === 'done'" class="el-icon-check"></i>
                  <i v-else-if="s.status === 'running'" class="el-icon-loading"></i>
                  <i v-else :class="s.icon"></i>
                </div>
                <div class="stage-info">
                  <span class="stage-label">{{ s.label }}</span>
                  <span v-if="s.status === 'done' && s.duration" class="stage-duration">{{ s.duration }}ms</span>
                  <span v-else-if="s.status === 'running'" class="stage-running">{{ s.message || '处理中...' }}</span>
                  <span v-else class="stage-pending">待处理</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-bar">
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
          class="chat-send-btn"
        >发送</el-button>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  data() {
    return {
      kbs: [],
      currentKbId: null,
      sessions: [],
      currentSession: null,
      messages: [],
      question: '',
      loading: false,
      mode: 'naive',
      // SSE 流式相关
      streamTotalMs: 0,
      stages: [
        { key: 'receive',          label: '接收问题',   icon: 'el-icon-message',  status: 'pending', duration: 0, message: '' },
        { key: 'vector_search',    label: '向量检索',   icon: 'el-icon-search',   status: 'pending', duration: 0, message: '' },
        { key: 'keyword_search',   label: '关键词检索', icon: 'el-icon-key',      status: 'pending', duration: 0, message: '' },
        { key: 'rerank',           label: '重排序',     icon: 'el-icon-sort',     status: 'pending', duration: 0, message: '' },
        { key: 'context_assemble', label: '上下文组装', icon: 'el-icon-files',    status: 'pending', duration: 0, message: '' },
        { key: 'llm_generate',     label: 'LLM 生成',   icon: 'el-icon-cpu',      status: 'pending', duration: 0, message: '' }
      ]
    }
  },
  watch: {
    currentKbId(val) {
      if (this.currentSession && this.currentSession.kbId !== val) {
        this.currentSession = null
        this.messages = []
      }
      this.loadSessions()
    }
  },
  mounted() {
    this.loadKbs()
  },
  methods: {
    async loadKbs() {
      const res = await this.$http.get('/ai/kb/list')
      if (res && res.code === 0) {
        this.kbs = res.data || []
        if (this.kbs.length) this.currentKbId = this.kbs[0].id
      }
    },
    async loadSessions() {
      if (!this.currentKbId) return
      const res = await this.$http.get('/ai/session/list')
      if (res && res.code === 0) {
        this.sessions = (res.data || []).filter(s => s.kbId === this.currentKbId)
      }
    },
    async selectSession(s) {
      this.currentSession = s
      const res = await this.$http.get('/ai/session/messages/' + s.id)
      if (res && res.code === 0) {
        this.messages = (res.data || []).map(m => ({
          role: m.role,
          content: m.content,
          mode: m.mode,
          durationMs: m.durationMs
        }))
        this.scrollBottom()
      }
    },
    async createSession() {
      if (!this.currentKbId) {
        this.$message.warning('请先选择知识库')
        return
      }
      const res = await this.$http.post('/ai/session/save', {
        kbId: this.currentKbId,
        title: '新会话'
      })
      if (res && res.code === 0) {
        await this.loadSessions()
        const created = this.sessions.find(s => s.kbId === this.currentKbId)
        if (created) this.selectSession(created)
      }
    },
    handleSessionCmd(cmd, s) {
      if (cmd === 'rename') this.renameSession(s)
      if (cmd === 'delete') this.deleteSession(s)
    },
    async renameSession(s) {
      try {
        const { value } = await this.$prompt('请输入新名称', '重命名', {
          inputValue: s.title,
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })
        const res = await this.$http.post('/ai/session/updateTitle', { id: s.id, title: value })
        if (res && res.code === 0) this.loadSessions()
      } catch { }
    },
    async deleteSession(s) {
      try { await this.$confirm('确认删除该会话？', '提示', { type: 'warning' }) } catch { return }
      const res = await this.$http.delete('/ai/session/delete/' + s.id)
      if (res && res.code === 0) {
        if (this.currentSession && this.currentSession.id === s.id) {
          this.currentSession = null
          this.messages = []
        }
        this.loadSessions()
      }
    },
    async send() {
      const q = this.question.trim()
      if (!q || this.loading) return
      if (!this.currentSession) {
        await this.createSession()
      }
      if (!this.currentSession) return

      this.messages.push({ role: 'user', content: q })
      this.question = ''
      this.loading = true
      this.streamTotalMs = 0
      this.resetStages()
      this.scrollBottom()

      let answer = ''
      let totalMs = 0
      let answerMode = this.mode
      try {
        // SSE 流式查询：直接调 rag-service（通过 Vue devServer 代理）
        const resp = await fetch('/rag-service/api/v1/query/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'token': localStorage.getItem('token') || ''
          },
          body: JSON.stringify({
            question: q,
            mode: this.mode,
            kb_id: String(this.currentKbId),
            top_k: 5
          })
        })
        if (!resp.ok) throw new Error('SSE 接口返回 ' + resp.status)
        await this.consumeSSE(resp, (event) => {
          if (event.type === 'stage') {
            this.updateStage(event.step, event.status, event.duration_ms || 0, event.message || '')
          } else if (event.type === 'answer') {
            answer = event.answer || ''
            totalMs = event.total_ms || 0
            answerMode = event.mode || this.mode
          } else if (event.type === 'error') {
            throw new Error(event.error || 'RAG 服务错误')
          }
        })
        this.streamTotalMs = totalMs
        this.messages.push({
          role: 'bot',
          content: answer || '（无回复）',
          mode: answerMode,
          durationMs: totalMs
        })
        // 保存到会话消息（SpringBoot）
        try {
          await this.$http.post('/rag/message/save', {
            sessionId: this.currentSession.id,
            question: q,
            answer: answer,
            mode: answerMode,
            durationMs: totalMs
          })
        } catch (saveErr) {
          console.warn('保存会话消息失败（不影响显示）:', saveErr)
        }
        this.scrollBottom()
      } catch (e) {
        this.messages.push({ role: 'bot', content: '请求失败：' + (e.message || '') })
      } finally {
        this.loading = false
      }
    },
    resetStages() {
      this.stages.forEach(s => { s.status = 'pending'; s.duration = 0; s.message = '' })
    },
    updateStage(step, status, duration, message) {
      const stage = this.stages.find(s => s.key === step)
      if (!stage) return
      stage.status = status
      if (status === 'done') {
        stage.duration = duration
        stage.message = message
      } else if (status === 'start') {
        stage.message = message
      }
      // 当前阶段开始时，把前面未完成的阶段标记为 done（容错）
      if (status === 'start') {
        const idx = this.stages.findIndex(s => s.key === step)
        for (let i = 0; i < idx; i++) {
          if (this.stages[i].status === 'pending' || this.stages[i].status === 'running') {
            this.stages[i].status = 'done'
            this.stages[i].duration = this.stages[i].duration || 1
          }
        }
      }
      this.scrollBottom()
    },
    /** 解析 SSE 响应流 */
    async consumeSSE(resp, onEvent) {
      const reader = resp.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        // SSE 消息以 \n\n 分隔
        const parts = buffer.split('\n\n')
        buffer = parts.pop() // 保留最后未完整的部分
        for (const part of parts) {
          const line = part.trim()
          if (!line.startsWith('data:')) continue
          const payload = line.slice(5).trim()
          if (payload === '[DONE]') return
          try {
            const event = JSON.parse(payload)
            onEvent(event)
          } catch (e) {
            console.warn('SSE 解析失败:', e, payload)
          }
        }
      }
    },
    scrollBottom() {
      this.$nextTick(() => {
        const box = this.$refs.messagesBox
        if (box) box.scrollTop = box.scrollHeight
      })
    },
    /**
     * 轻量 Markdown 渲染（无外部依赖）。
     * 支持：标题 / 粗体 / 斜体 / 行内代码 / 无序有序列表 / 段落 / 换行。
     * 输入先转义 HTML，再做 markdown 替换，避免 XSS。
     */
    renderMd(text) {
      if (!text) return ''
      let s = String(text)
      // 1. HTML 转义
      s = s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      // 2. 代码块（```...```）
      s = s.replace(/```([\s\S]*?)```/g, (m, c) => '<pre><code>' + c.replace(/^\n/, '') + '</code></pre>')
      // 3. 按行分割处理标题/列表/段落
      const lines = s.split('\n')
      const out = []
      let inUl = false, inOl = false
      const closeLists = () => { if (inUl) { out.push('</ul>'); inUl = false } if (inOl) { out.push('</ol>'); inOl = false } }
      for (let raw of lines) {
        const line = raw
        // 代码块中行已包含 <pre>，跳过处理
        if (line.indexOf('<pre>') === 0 || line.indexOf('</pre>') === 0) { closeLists(); out.push(line); continue }
        // 标题
        const h = line.match(/^(#{1,6})\s+(.*)$/)
        if (h) { closeLists(); const lvl = h[1].length; out.push('<h' + lvl + '>' + this._mdInline(h[2]) + '</h' + lvl + '>'); continue }
        // 空行
        if (!line.trim()) { closeLists(); continue }
        // 无序列表
        const ul = line.match(/^[\-\*\+]\s+(.*)$/)
        if (ul) { if (!inUl) { closeLists(); out.push('<ul>'); inUl = true } out.push('<li>' + this._mdInline(ul[1]) + '</li>'); continue }
        // 有序列表
        const ol = line.match(/^\d+\.\s+(.*)$/)
        if (ol) { if (!inOl) { closeLists(); out.push('<ol>'); inOl = true } out.push('<li>' + this._mdInline(ol[1]) + '</li>'); continue }
        // 引用
        const bq = line.match(/^&gt;\s?(.*)$/)
        if (bq) { closeLists(); out.push('<blockquote>' + this._mdInline(bq[1]) + '</blockquote>'); continue }
        // 普通段落
        closeLists()
        out.push('<p>' + this._mdInline(line) + '</p>')
      }
      closeLists()
      return out.join('\n')
    },
    /** Markdown 行内元素：粗体 / 斜体 / 行内代码 / 链接 */
    _mdInline(s) {
      if (!s) return ''
      // 行内代码
      s = s.replace(/`([^`]+)`/g, '<code>$1</code>')
      // 粗体
      s = s.replace(/\*\*([^\*]+)\*\*/g, '<strong>$1</strong>')
      // 斜体
      s = s.replace(/\*([^\*]+)\*/g, '<em>$1</em>')
      // 链接 [text](url)
      s = s.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
      return s
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat {
  display: flex; gap: 16px;
  height: calc(100vh - 96px);
  min-height: 540px;
}

.glass-card {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: var(--biz-shadow);
}

/* ===== Sidebar ===== */
.chat-sidebar {
  width: 264px; flex-shrink: 0;
  display: flex; flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  display: flex; align-items: center; gap: 8px;
  padding: 14px 16px;
  font-size: 14px; font-weight: 600;
  color: var(--biz-text-1);
  border-bottom: 1px solid var(--biz-border);
  i { color: var(--biz-primary); font-size: 16px; }
}
.sidebar-toolbar {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--biz-border);
}
.session-list {
  flex: 1; overflow-y: auto; padding: 8px;
}
.session-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: var(--biz-text-2);
  margin-bottom: 2px;
  border: 1px solid transparent;
  transition: all .2s;
  &:hover { background: #f8fafc; border-color: var(--biz-border); }
  &.active {
    background: rgba(30, 64, 175, 0.06);
    color: var(--biz-primary);
    border-color: rgba(30, 64, 175, 0.2);
    box-shadow: inset 3px 0 0 var(--biz-primary);
  }
  .session-item-icon { font-size: 14px; flex-shrink: 0; }
  .session-title {
    flex: 1; min-width: 0;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
  .session-more {
    padding: 2px 4px; border-radius: 4px;
    color: var(--biz-text-4); opacity: 0;
    transition: opacity .2s, background .2s;
    &:hover { background: rgba(15,23,42,0.06); }
  }
  &:hover .session-more { opacity: 1; }
}

/* ===== Main ===== */
.chat-main {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column;
  overflow: hidden;
}
.chat-messages {
  flex: 1; overflow-y: auto; padding: 20px;
  background: var(--biz-bg);
}
.chat-empty {
  text-align: center; padding: 80px 20px;
  .chat-empty-icon {
    width: 56px; height: 56px; margin: 0 auto 14px;
    border-radius: 6px;
    background: rgba(30, 64, 175, 0.08);
    display: flex; align-items: center; justify-content: center;
    i { font-size: 26px; color: var(--biz-primary); }
  }
  .chat-empty-title { font-size: 14px; color: var(--biz-text-2); margin: 0 0 6px; }
  .chat-empty-hint { font-size: 12px; color: var(--biz-text-4); margin: 0; }
}
.chat-msg {
  display: flex; gap: 10px; margin-bottom: 18px;
}
.chat-msg-user { flex-direction: row-reverse; }
.chat-avatar {
  width: 32px; height: 32px; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; font-size: 15px;
}
.avatar-user {
  background: var(--biz-primary);
  color: #fff;
}
.avatar-bot {
  background: #fff;
  color: var(--biz-primary);
  border: 1px solid var(--biz-border);
}
.chat-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 6px;
  word-break: break-word;
  font-size: 14px; line-height: 1.6;
}
.chat-msg-user .chat-bubble {
  background: var(--biz-primary);
  color: #fff;
  border-top-right-radius: 2px;
}
.chat-msg-bot .chat-bubble {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-top-left-radius: 2px;
  color: var(--biz-text-1);
}

/* ===== Markdown 渲染 ===== */
.chat-text-md {
  line-height: 1.7;
  ::v-deep {
    h1, h2, h3, h4, h5, h6 {
      margin: 12px 0 6px;
      font-weight: 600;
      color: var(--biz-text-1);
      line-height: 1.4;
    }
    h1 { font-size: 18px; padding-bottom: 6px; border-bottom: 1px solid var(--biz-border); }
    h2 { font-size: 16px; }
    h3 { font-size: 15px; }
    h4 { font-size: 14px; }
    p { margin: 6px 0; }
    ul, ol { margin: 6px 0; padding-left: 22px; }
    ul li { list-style: disc; }
    ol li { list-style: decimal; }
    li { margin: 3px 0; }
    code {
      background: rgba(30,64,175,0.08);
      color: #be123c;
      padding: 1px 5px;
      border-radius: 3px;
      font-family: 'Consolas','Monaco',monospace;
      font-size: 13px;
    }
    pre {
      background: #0f172a;
      color: #e2e8f0;
      padding: 10px 12px;
      border-radius: 4px;
      overflow-x: auto;
      margin: 8px 0;
      code { background: transparent; color: inherit; padding: 0; font-size: 13px; }
    }
    blockquote {
      margin: 6px 0;
      padding: 4px 12px;
      border-left: 3px solid var(--biz-primary);
      background: rgba(30,64,175,0.04);
      color: var(--biz-text-3);
    }
    strong { color: var(--biz-text-1); font-weight: 600; }
    a { color: var(--biz-primary); text-decoration: none; &:hover { text-decoration: underline; } }
    table {
      border-collapse: collapse;
      margin: 8px 0;
      th, td {
        border: 1px solid var(--biz-border);
        padding: 6px 10px;
        font-size: 13px;
      }
      th { background: #f8fafc; font-weight: 600; }
    }
  }
}
.chat-meta {
  font-size: 11px;
  opacity: .65;
  margin-top: 6px;
  display: flex; align-items: center; gap: 4px;
}
.chat-msg-loading { display: flex; gap: 4px; align-items: center; padding: 14px; }
.chat-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--biz-primary);
  animation: chatBlink 1.2s infinite;
  &:nth-child(2) { animation-delay: .2s; }
  &:nth-child(3) { animation-delay: .4s; }
}
@keyframes chatBlink { 0%, 80%, 100% { opacity: .3 } 40% { opacity: 1 } }

/* ===== 6 步骤进度面板 ===== */
.chat-stage-panel {
  max-width: 360px;
  min-width: 280px;
  padding: 12px 14px;

  .stage-panel-title {
    display: flex; align-items: center; gap: 6px;
    font-size: 13px; font-weight: 600;
    color: var(--biz-primary);
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 1px solid var(--biz-border);

    .stage-panel-total {
      margin-left: auto;
      font-size: 11px;
      color: var(--biz-text-3);
      font-weight: normal;
    }
  }

  .stage-list {
    display: flex; flex-direction: column;
    gap: 6px;
  }

  .stage-item {
    display: flex; align-items: center; gap: 10px;
    padding: 4px 0;
    font-size: 12px;
    transition: all .2s;

    .stage-icon {
      width: 22px; height: 22px;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
      font-size: 12px;
      transition: all .3s;
    }
    .stage-info {
      flex: 1;
      display: flex; align-items: center; justify-content: space-between;
      gap: 8px;
    }
    .stage-label { color: var(--biz-text-2); }
    .stage-duration {
      font-size: 11px; color: var(--biz-primary);
      font-family: 'Consolas', 'Monaco', monospace;
      background: rgba(30,64,175,0.08);
      padding: 1px 6px; border-radius: 3px;
    }
    .stage-running { font-size: 11px; color: var(--biz-text-3); }
    .stage-pending { font-size: 11px; color: var(--biz-text-4); }
  }

  /* 待处理：灰色 */
  .stage-pending .stage-icon {
    background: #f1f5f9;
    color: var(--biz-text-4);
    border: 1px solid var(--biz-border);
  }
  /* 进行中：蓝色 + 旋转 */
  .stage-running .stage-icon {
    background: rgba(30,64,175,0.1);
    color: var(--biz-primary);
    border: 1px solid var(--biz-primary);
  }
  .stage-running .stage-label { color: var(--biz-primary); font-weight: 500; }
  /* 已完成：绿色 */
  .stage-done .stage-icon {
    background: #dcfce7;
    color: #15803d;
    border: 1px solid #86efac;
  }
  .stage-done .stage-label { color: var(--biz-text-2); }
}

.chat-input-bar {
  padding: 12px 16px;
  border-top: 1px solid var(--biz-border);
  display: flex; align-items: flex-end; gap: 8px;
  background: #fff;
}
.chat-input-bar >>> .el-textarea { flex: 1; }
.chat-input-bar >>> .el-textarea__inner {
  border-radius: 4px;
  background: #fff;
}
.chat-send-btn {
  height: 56px;
  border-radius: 4px;
  background: var(--biz-primary) !important;
  border: none;
  &:hover { background: var(--biz-primary-light) !important; }
}

.chat-messages::-webkit-scrollbar { width: 6px; }
.chat-messages::-webkit-scrollbar-thumb { background: rgba(30,64,175,0.18); border-radius: 6px; }
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-thumb { background: rgba(30,64,175,0.18); border-radius: 4px; }
</style>
