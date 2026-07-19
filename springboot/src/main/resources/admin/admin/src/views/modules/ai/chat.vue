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
          <div class="chat-bubble chat-msg-loading">
            <span class="chat-dot"></span><span class="chat-dot"></span><span class="chat-dot"></span>
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
      mode: 'hybrid'
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
      this.scrollBottom()

      try {
        const res = await this.$http.post('/rag/query', {
          question: q,
          mode: this.mode,
          kbId: String(this.currentKbId),
          sessionId: this.currentSession.id
        })
        const data = (res && res.code === 0) ? res.data : {}
        this.messages.push({
          role: 'bot',
          content: data.answer || '（无回复）',
          mode: data.mode || this.mode,
          durationMs: data.durationMs
        })
        this.scrollBottom()
      } catch (e) {
        this.messages.push({ role: 'bot', content: '请求失败：' + (e.message || '') })
      } finally {
        this.loading = false
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
$primary: #4a90e2;
$primary-light: #6ec5ff;

.ai-chat {
  display: flex; gap: 16px;
  height: calc(100vh - 96px);
  min-height: 540px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 21, 41, 0.06);
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
  color: #1f2d3d;
  border-bottom: 1px solid rgba(0, 21, 41, 0.05);
  i { color: $primary; font-size: 16px; }
}
.sidebar-toolbar {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid rgba(0, 21, 41, 0.05);
}
.session-list {
  flex: 1; overflow-y: auto; padding: 8px;
}
.session-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
  transition: all .2s;
  &:hover { background: rgba(74, 144, 226, 0.08); }
  &.active {
    background: linear-gradient(90deg, rgba(74,144,226,0.18) 0%, rgba(74,144,226,0.04) 100%);
    color: $primary;
    box-shadow: inset 3px 0 0 $primary;
  }
  .session-item-icon { font-size: 14px; flex-shrink: 0; }
  .session-title {
    flex: 1; min-width: 0;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
  .session-more {
    padding: 2px 4px; border-radius: 4px;
    color: #909399; opacity: 0;
    transition: opacity .2s, background .2s;
    &:hover { background: rgba(0,0,0,0.06); }
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
  background: linear-gradient(180deg, rgba(245, 247, 250, 0.4) 0%, rgba(238, 243, 248, 0.6) 100%);
}
.chat-empty {
  text-align: center; padding: 80px 20px;
  .chat-empty-icon {
    width: 64px; height: 64px; margin: 0 auto 16px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(74,144,226,0.18) 0%, rgba(74,144,226,0.06) 100%);
    display: flex; align-items: center; justify-content: center;
    i { font-size: 30px; color: $primary; }
  }
  .chat-empty-title { font-size: 14px; color: #606266; margin: 0 0 6px; }
  .chat-empty-hint { font-size: 12px; color: #909399; margin: 0; }
}
.chat-msg {
  display: flex; gap: 10px; margin-bottom: 18px;
}
.chat-msg-user { flex-direction: row-reverse; }
.chat-avatar {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; font-size: 16px;
}
.avatar-user {
  background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.32);
}
.avatar-bot {
  background: rgba(255, 255, 255, 0.85);
  color: $primary;
  border: 1px solid rgba(74, 144, 226, 0.22);
}
.chat-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
  font-size: 14px; line-height: 1.6;
}
.chat-msg-user .chat-bubble {
  background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.25);
}
.chat-msg-bot .chat-bubble {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-top-left-radius: 4px;
  color: #1f2d3d;
}

/* ===== Markdown 渲染 ===== */
.chat-text-md {
  line-height: 1.7;
  ::v-deep {
    h1, h2, h3, h4, h5, h6 {
      margin: 12px 0 6px;
      font-weight: 600;
      color: #1f2d3d;
      line-height: 1.4;
    }
    h1 { font-size: 18px; padding-bottom: 6px; border-bottom: 1px solid rgba(0,21,41,0.08); }
    h2 { font-size: 16px; }
    h3 { font-size: 15px; }
    h4 { font-size: 14px; }
    p { margin: 6px 0; }
    ul, ol { margin: 6px 0; padding-left: 22px; }
    ul li { list-style: disc; }
    ol li { list-style: decimal; }
    li { margin: 3px 0; }
    code {
      background: rgba(74,144,226,0.1);
      color: #c7254e;
      padding: 1px 5px;
      border-radius: 3px;
      font-family: 'Consolas','Monaco',monospace;
      font-size: 13px;
    }
    pre {
      background: rgba(30,41,59,0.92);
      color: #e2e8f0;
      padding: 10px 12px;
      border-radius: 6px;
      overflow-x: auto;
      margin: 8px 0;
      code { background: transparent; color: inherit; padding: 0; font-size: 13px; }
    }
    blockquote {
      margin: 6px 0;
      padding: 4px 12px;
      border-left: 3px solid $primary;
      background: rgba(74,144,226,0.06);
      color: #606266;
    }
    strong { color: #1f2d3d; font-weight: 600; }
    a { color: $primary; text-decoration: none; &:hover { text-decoration: underline; } }
    table {
      border-collapse: collapse;
      margin: 8px 0;
      th, td {
        border: 1px solid rgba(0,21,41,0.1);
        padding: 6px 10px;
        font-size: 13px;
      }
      th { background: rgba(74,144,226,0.08); font-weight: 600; }
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
  background: $primary;
  animation: chatBlink 1.2s infinite;
  &:nth-child(2) { animation-delay: .2s; }
  &:nth-child(3) { animation-delay: .4s; }
}
@keyframes chatBlink { 0%, 80%, 100% { opacity: .3 } 40% { opacity: 1 } }

.chat-input-bar {
  padding: 12px 16px;
  border-top: 1px solid rgba(0, 21, 41, 0.05);
  display: flex; align-items: flex-end; gap: 8px;
  background: rgba(255, 255, 255, 0.6);
}
.chat-input-bar >>> .el-textarea { flex: 1; }
.chat-input-bar >>> .el-textarea__inner {
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.85);
}
.chat-send-btn {
  height: 56px;
  border-radius: 8px;
  background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.25);
}

.chat-messages::-webkit-scrollbar { width: 6px; }
.chat-messages::-webkit-scrollbar-thumb { background: rgba(74,144,226,0.2); border-radius: 6px; }
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-thumb { background: rgba(74,144,226,0.2); border-radius: 4px; }
</style>
