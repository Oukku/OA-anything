<template>
  <div class="ai-kb">
    <el-row :gutter="16">
      <el-col :span="7">
        <div class="kb-panel glass-card">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-collection"></i>
              <span>知识库分类</span>
            </div>
            <el-button type="text" icon="el-icon-plus" @click="openKbForm()">新增</el-button>
          </div>
          <div class="panel-body">
            <el-empty v-if="!kbs.length" description="暂无知识库" :image-size="80"></el-empty>
            <div
              v-for="k in kbs"
              :key="k.id"
              class="kb-item"
              :class="{ active: selectedKb && selectedKb.id === k.id }"
              @click="selectKb(k)"
            >
              <div class="kb-icon"><i class="el-icon-folder-opened"></i></div>
              <div class="kb-info">
                <div class="kb-name">{{ k.name }}</div>
                <div class="kb-desc">{{ k.description || '无描述' }}</div>
              </div>
              <el-dropdown trigger="click" @command="cmd => handleKbCmd(cmd, k)" @click.stop.native>
                <span class="kb-more"><i class="el-icon-more"></i></span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="edit" icon="el-icon-edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" icon="el-icon-delete" style="color: #f56c6c">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="17">
        <div class="doc-panel glass-card" v-if="selectedKb">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-document"></i>
              <span>{{ selectedKb.name }} · 文档管理</span>
            </div>
            <el-upload
              class="kb-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ kbId: selectedKb.id }"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :on-error="onUploadError"
              :before-upload="beforeUpload"
              accept=".pdf,.docx,.txt,.md"
            >
              <el-button size="small" type="primary" icon="el-icon-upload2">上传文档</el-button>
            </el-upload>
          </div>
          <div class="panel-body">
            <el-table :data="documents" v-loading="loadingDoc" stripe :row-style="{ background: 'transparent' }">
              <el-table-column prop="filename" label="文件名" min-width="200">
                <template slot-scope="{ row }">
                  <div class="doc-name">
                    <i :class="fileIcon(row.fileType)"></i>
                    <span>{{ row.filename }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="大小" width="100">
                <template slot-scope="{ row }">
                  <span class="doc-size">{{ formatSize(row.fileSize) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="页/块" width="90">
                <template slot-scope="{ row }">
                  <span class="doc-meta">{{ row.pageCount || '-' }} / {{ row.chunkCount || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template slot-scope="{ row }">
                  <span class="doc-status" :class="'status-' + statusClass(row.status)">
                    <i :class="statusIcon(row.status)"></i>
                    {{ statusText(row.status) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上传时间" width="160"></el-table-column>
              <el-table-column label="操作" width="200">
                <template slot-scope="{ row }">
                  <el-button type="text" icon="el-icon-view" @click="previewDoc(row)">预览</el-button>
                  <el-button type="text" icon="el-icon-refresh" @click="reparseDoc(row)" :disabled="row.status === 0">重新解析</el-button>
                  <el-button type="text" icon="el-icon-delete" style="color: #f56c6c" @click="deleteDoc(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div class="doc-panel glass-card empty-state" v-else>
          <el-empty description="请选择或创建一个知识库">
            <el-button type="primary" icon="el-icon-plus" @click="openKbForm()">新建知识库</el-button>
          </el-empty>
        </div>
      </el-col>
    </el-row>

    <!-- 知识库表单 -->
    <el-dialog :title="kbForm.id ? '编辑知识库' : '新增知识库'" :visible.sync="kbVisible" width="500px" custom-class="kb-dialog">
      <el-form :model="kbForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="kbForm.name" placeholder="如：公司制度"></el-input>
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="kbForm.code" placeholder="唯一编码，如 company_policy"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="kbForm.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="kbVisible = false">取消</el-button>
        <el-button type="primary" @click="saveKb">保存</el-button>
      </span>
    </el-dialog>

    <!-- 文档预览弹窗 -->
    <el-dialog
      :title="previewData.filename || '文档预览'"
      :visible.sync="previewVisible"
      width="80%"
      top="5vh"
      custom-class="preview-dialog"
      :close-on-click-modal="false"
    >
      <div v-loading="previewLoading" class="preview-body">
        <div class="preview-meta" v-if="!previewLoading && previewData.text !== undefined">
          <span class="meta-item"><i class="el-icon-document"></i> {{ previewData.parser || '-' }}</span>
          <span class="meta-item"><i class="el-icon-files"></i> {{ previewData.page_count || 0 }} 页</span>
          <span class="meta-item"><i class="el-icon-s-grid"></i> {{ previewData.chunk_count || 0 }} 分块</span>
          <span class="meta-item"><i class="el-icon-circle-check" :style="{ color: previewData.status === 'indexed' ? '#67c23a' : '#e6a23c' }"></i> {{ previewData.status }}</span>
        </div>
        <el-row :gutter="16" v-if="!previewLoading && previewData.text !== undefined">
          <el-col :span="14">
            <div class="preview-section">
              <div class="preview-section-title">解析后文本</div>
              <div class="preview-text">{{ previewData.text }}</div>
            </div>
          </el-col>
          <el-col :span="10">
            <div class="preview-section">
              <div class="preview-section-title">分块列表（{{ (previewData.chunks || []).length }}）</div>
              <div class="preview-chunks">
                <div v-for="(c, i) in (previewData.chunks || [])" :key="c.id" class="chunk-item">
                  <div class="chunk-head">分块 {{ i + 1 }}</div>
                  <div class="chunk-text">{{ c.text }}</div>
                </div>
                <el-empty v-if="!(previewData.chunks || []).length" description="无分块" :image-size="60"></el-empty>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="!previewLoading && previewData.error" :description="previewData.error" :image-size="80"></el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      kbs: [],
      selectedKb: null,
      documents: [],
      loadingDoc: false,
      kbVisible: false,
      kbForm: { id: null, name: '', code: '', description: '' },
      previewVisible: false,
      previewLoading: false,
      previewData: {},
      pollTimer: null
    }
  },
  computed: {
    uploadUrl() {
      const base = process.env.VUE_APP_BASE_API || '/springboot-oa-v2'
      return (typeof window !== 'undefined' ? window.location.origin : '') + base + '/rag/upload'
    },
    uploadHeaders() {
      return { token: localStorage.getItem('token') || '' }
    }
  },
  mounted() {
    this.loadKbs()
  },
  beforeDestroy() {
    this.stopPolling()
  },
  methods: {
    async loadKbs() {
      const res = await this.$http.get('/ai/kb/list?all=true')
      if (res && res.code === 0) {
        this.kbs = res.data || []
        if (this.kbs.length && !this.selectedKb) {
          this.selectKb(this.kbs[0])
        }
      }
    },
    selectKb(k) {
      this.selectedKb = k
      this.loadDocuments()
    },
    async loadDocuments() {
      if (!this.selectedKb) return
      this.loadingDoc = true
      try {
        const res = await this.$http.get('/rag/documents?kbId=' + this.selectedKb.id)
        this.documents = (res && res.code === 0 ? res.data : []) || []
        this.checkPolling()
      } finally {
        this.loadingDoc = false
      }
    },
    checkPolling() {
      // 有 parsing 状态的文档时启动轮询
      const hasParsing = this.documents.some(d => d.status === 0)
      if (hasParsing) {
        this.startPolling()
      } else {
        this.stopPolling()
      }
    },
    startPolling() {
      if (this.pollTimer) return
      this.pollTimer = setTimeout(() => {
        this.pollTimer = null
        this.loadDocuments()
      }, 2500)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearTimeout(this.pollTimer)
        this.pollTimer = null
      }
    },
    openKbForm(k) {
      this.kbForm = k ? { ...k } : { id: null, name: '', code: '', description: '' }
      this.kbVisible = true
    },
    async saveKb() {
      const url = this.kbForm.id ? '/ai/kb/update' : '/ai/kb/save'
      const res = await this.$http.post(url, this.kbForm)
      if (res && res.code === 0) {
        this.$message.success('保存成功')
        this.kbVisible = false
        this.loadKbs()
      } else {
        this.$message.error(res && res.msg ? res.msg : '保存失败')
      }
    },
    handleKbCmd(cmd, k) {
      if (cmd === 'edit') this.openKbForm(k)
      if (cmd === 'delete') this.deleteKb(k)
    },
    async deleteKb(k) {
      try { await this.$confirm('确认删除该知识库？关联文档将一并清理', '提示', { type: 'warning' }) } catch { return }
      const res = await this.$http.delete('/ai/kb/delete/' + k.id)
      if (res && res.code === 0) {
        this.$message.success('删除成功')
        if (this.selectedKb && this.selectedKb.id === k.id) this.selectedKb = null
        this.loadKbs()
      }
    },
    beforeUpload(file) {
      const allowed = ['.pdf', '.docx', '.txt', '.md']
      const ext = '.' + (file.name.split('.').pop() || '').toLowerCase()
      if (!allowed.includes(ext)) {
        this.$message.error('仅支持 PDF / DOCX / TXT / MD 格式')
        return false
      }
      if (file.size > 100 * 1024 * 1024) {
        this.$message.error('文件不能超过 100MB')
        return false
      }
      return true
    },
    onUploadSuccess() {
      this.$message.success('上传成功，后台解析中')
      this.loadDocuments()
    },
    onUploadError(err) {
      this.$message.error('上传失败：' + (err.message || ''))
    },
    async previewDoc(row) {
      if (!row.ragDocId) {
        this.$message.warning('文档尚未关联到 RAG 服务')
        return
      }
      this.previewVisible = true
      this.previewLoading = true
      this.previewData = { filename: row.filename }
      try {
        const res = await this.$http.get('/rag/preview/' + row.ragDocId)
        if (res && res.code === 0) {
          this.previewData = { filename: row.filename, ...res.data }
        } else {
          this.previewData = { filename: row.filename, error: (res && res.msg) || '预览失败' }
        }
      } catch (e) {
        this.previewData = { filename: row.filename, error: e.message || '预览失败' }
      } finally {
        this.previewLoading = false
      }
    },
    async reparseDoc(row) {
      if (!row.ragDocId) {
        this.$message.warning('文档尚未关联到 RAG 服务')
        return
      }
      try { await this.$confirm('确认重新解析该文档？将清除旧分块并重新索引', '提示', { type: 'warning' }) } catch { return }
      const res = await this.$http.post('/rag/reparse/' + row.ragDocId)
      if (res && res.code === 0) {
        this.$message.success('已开始重新解析')
        this.loadDocuments()
      } else {
        this.$message.error((res && res.msg) || '重新解析失败')
      }
    },
    async deleteDoc(row) {
      try { await this.$confirm('确认删除该文档？', '提示', { type: 'warning' }) } catch { return }
      const res = await this.$http.delete('/rag/documents/' + row.id)
      if (res && res.code === 0) {
        this.$message.success('删除成功')
        this.loadDocuments()
      }
    },
    formatSize(bytes) {
      if (!bytes) return '-'
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    },
    fileIcon(ext) {
      if (!ext) return 'el-icon-document'
      const e = ext.toLowerCase()
      if (e === 'pdf') return 'el-icon-document'
      if (['doc', 'docx'].includes(e)) return 'el-icon-document-copy'
      if (['txt', 'md'].includes(e)) return 'el-icon-tickets'
      return 'el-icon-document'
    },
    statusClass(status) {
      if (status === 1) return 'ok'
      if (status === -1) return 'err'
      return 'wait'
    },
    statusText(status) {
      if (status === 1) return '已索引'
      if (status === -1) return '失败'
      return '解析中'
    },
    statusIcon(status) {
      if (status === 1) return 'el-icon-success'
      if (status === -1) return 'el-icon-error'
      return 'el-icon-loading'
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-kb { padding: 0; }

.glass-card {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: var(--biz-shadow);
  overflow: hidden;
}

.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--biz-border);
  background: #f8fafc;
}
.panel-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 600; color: var(--biz-text-1);
  i { color: var(--biz-primary); font-size: 16px; }
}
.panel-body { padding: 12px; }

/* KB list items */
.kb-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all .2s;
  border: 1px solid transparent;
  &:hover { background: #f8fafc; border-color: var(--biz-border); }
  &.active {
    background: rgba(30, 64, 175, 0.06);
    border-color: rgba(30, 64, 175, 0.2);
    box-shadow: inset 3px 0 0 var(--biz-primary);
  }
  .kb-icon {
    width: 32px; height: 32px;
    border-radius: 4px;
    background: rgba(30, 64, 175, 0.08);
    display: flex; align-items: center; justify-content: center;
    color: var(--biz-primary); font-size: 15px;
    flex-shrink: 0;
  }
  &.active .kb-icon {
    background: var(--biz-primary);
    color: #fff;
  }
  .kb-info { flex: 1; min-width: 0; }
  .kb-name { font-size: 14px; font-weight: 500; color: var(--biz-text-1); }
  .kb-desc {
    font-size: 12px; color: var(--biz-text-4); margin-top: 4px;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
  .kb-more {
    padding: 4px 6px; border-radius: 4px;
    color: var(--biz-text-4); opacity: 0;
    transition: opacity .2s, background .2s;
    &:hover { background: rgba(15,23,42,0.06); }
  }
  &:hover .kb-more { opacity: 1; }
}

.kb-uploader { display: inline-block; }

/* Doc table */
.doc-name {
  display: flex; align-items: center; gap: 6px;
  i { color: var(--biz-primary); font-size: 14px; }
}
.doc-size { color: var(--biz-text-4); font-size: 12px; }
.doc-meta { color: var(--biz-text-3); font-size: 12px; }
.doc-status {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  &.status-ok { background: rgba(22, 163, 74, 0.1); color: #15803d; }
  &.status-err { background: rgba(220, 38, 38, 0.1); color: #b91c1c; }
  &.status-wait { background: rgba(217, 119, 6, 0.1); color: #b45309; }
}

.empty-state {
  height: 100%; min-height: 480px;
  display: flex; align-items: center; justify-content: center;
}
.kb-panel, .doc-panel {
  min-height: 480px;
}

/* Preview dialog */
.preview-body { min-height: 400px; }
.preview-meta {
  display: flex; flex-wrap: wrap; gap: 16px;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: #f8fafc;
  border-radius: 4px;
  border: 1px solid var(--biz-border);
  .meta-item {
    display: inline-flex; align-items: center; gap: 6px;
    font-size: 13px; color: var(--biz-text-3);
    i { color: var(--biz-primary); font-size: 14px; }
  }
}
.preview-section {
  display: flex; flex-direction: column;
  height: 60vh;
}
.preview-section-title {
  font-size: 13px; font-weight: 600; color: var(--biz-text-1);
  padding: 8px 12px;
  border-bottom: 1px solid var(--biz-border);
  background: #f8fafc;
  border-radius: 4px 4px 0 0;
}
.preview-text {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: var(--biz-text-2);
  white-space: pre-wrap;
  word-break: break-word;
  background: #fff;
  border: 1px solid var(--biz-border);
  border-top: none;
  border-radius: 0 0 4px 4px;
}
.preview-chunks {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  background: #fff;
  border: 1px solid var(--biz-border);
  border-top: none;
  border-radius: 0 0 4px 4px;
}
.chunk-item {
  padding: 10px 12px;
  margin-bottom: 8px;
  background: #f8fafc;
  border-left: 3px solid var(--biz-primary);
  border-radius: 4px;
  .chunk-head {
    font-size: 12px; font-weight: 600; color: var(--biz-primary);
    margin-bottom: 4px;
  }
  .chunk-text {
    font-size: 12px; color: var(--biz-text-3);
    line-height: 1.6;
  }
}
</style>
