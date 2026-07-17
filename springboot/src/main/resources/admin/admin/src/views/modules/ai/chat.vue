<template>
  <div class="ai-chat">
    <div class="session-sidebar">
      <div class="session-header">
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
          <i class="el-icon-chat-dot-square"></i>
          <span class="session-title">{{ s.title }}</span>
          <el-dropdown trigger="click" @command="cmd => handleSessionCmd(cmd, s)" @click.stop.native>
            <span class="el-dropdown-link"><i class="el-icon-more"></i></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="rename">重命名</el-dropdown-item>
              <el-dropdown-item command="delete" style="color: #f56c6c">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="60"></el-empty>
      </div>
    </div>

    <div class="chat-main">
      <div class="chat-messages" ref="messagesBox">
        <div v-if="!messages.length" class="chat-empty">
          <i class="el-icon-cpu" style="font-size: 48px; color: #409eff"></i>
          <p>选择一个知识库，开始智能问答</p>
          <p class="chat-hint">AI 将基于知识库中的文档进行检索与回答</p>
        </div>
        <div
          v-for="(m, idx) in messages"
          :key="idx"
          class="chat-msg"
          :class="'chat-msg-' + m.role"
        >
          <div class="chat-avatar">
            <i :class="m.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
          </div>
          <div class="chat-bubble">
            <div class="chat-text">{{ m.content }}</div>
            <div v-if="m.durationMs" class="chat-meta">{{ m.durationMs }}ms · {{ m.mode }}</div>
          </div>
        </div>
        <div v-if="loading" class="chat-msg chat-msg-bot">
          <div class="chat-avatar"><i class="el-icon-cpu"></i></div>
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
          style="margin-left: 8px"
        >发送</el-button>
      </div>
    </div>
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
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat {
  display: flex; gap: 16px;
  height: calc(100vh - 180px); min-height: 540px;
}
.session-sidebar {
  width: 260px; flex-shrink: 0;
  background: #fff; border-radius: 4px;
  display: flex; flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.session-header {
  display: flex; align-items: center; gap: 8px;
  padding: 12px; border-bottom: 1px solid #ebeef5;
}
.session-list {
  flex: 1; overflow-y: auto; padding: 8px;
}
.session-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px; border-radius: 4px; cursor: pointer; font-size: 13px;
  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; color: #409eff; }
  .session-title { flex: 1; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
}
.chat-main {
  flex: 1; min-width: 0;
  background: #fff; border-radius: 4px;
  display: flex; flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.chat-messages {
  flex: 1; overflow-y: auto; padding: 16px;
  background: #f5f7fa;
}
.chat-empty {
  text-align: center; padding: 80px 20px; color: #909399;
  p { margin: 8px 0; }
  .chat-hint { color: #c0c4cc; font-size: 12px; }
}
.chat-msg {
  display: flex; gap: 10px; margin-bottom: 16px;
}
.chat-msg-user { flex-direction: row-reverse; }
.chat-msg-user .chat-bubble { background: #409eff; color: #fff; }
.chat-msg-bot .chat-bubble { background: #fff; border: 1px solid #ebeef5; }
.chat-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: #f0f2f5; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: #606266;
}
.chat-msg-user .chat-avatar { background: #409eff; color: #fff; }
.chat-bubble {
  max-width: 70%; padding: 10px 14px; border-radius: 8px;
  word-break: break-word; font-size: 14px; line-height: 1.6;
}
.chat-meta { font-size: 11px; opacity: .65; margin-top: 4px; }
.chat-msg-loading { display: flex; gap: 4px; align-items: center; }
.chat-dot {
  width: 6px; height: 6px; border-radius: 50%; background: #c0c4cc;
  animation: chatBlink 1.2s infinite;
  &:nth-child(2) { animation-delay: .2s; }
  &:nth-child(3) { animation-delay: .4s; }
}
@keyframes chatBlink { 0%, 80%, 100% { opacity: .3 } 40% { opacity: 1 } }
.chat-input-bar {
  padding: 12px 16px; border-top: 1px solid #ebeef5;
  display: flex; align-items: flex-start;
}
.chat-input-bar >>> .el-textarea { flex: 1; }
</style>
