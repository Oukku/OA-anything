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
              accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.md,.jpg,.jpeg,.png"
            >
              <el-button size="small" type="primary" icon="el-icon-upload2">上传文档</el-button>
            </el-upload>
          </div>
          <div class="panel-body">
            <el-table :data="documents" v-loading="loadingDoc" stripe :row-style="{ background: 'transparent' }">
              <el-table-column prop="filename" label="文件名" min-width="200">
                <template slot-scope="{ row }">
                  <div class="doc-name">
                    <i class="el-icon-document-copy"></i>
                    <span>{{ row.filename }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="大小" width="120">
                <template slot-scope="{ row }">
                  <span class="doc-size">{{ formatSize(row.fileSize) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template slot-scope="{ row }">
                  <span class="doc-status" :class="'status-' + (row.status === 1 ? 'ok' : row.status === -1 ? 'err' : 'wait')">
                    <i :class="row.status === 1 ? 'el-icon-success' : row.status === -1 ? 'el-icon-error' : 'el-icon-loading'"></i>
                    {{ row.status === 1 ? '已索引' : row.status === -1 ? '失败' : '处理中' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上传时间" width="170"></el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="{ row }">
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
      kbForm: { id: null, name: '', code: '', description: '' }
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
      } finally {
        this.loadingDoc = false
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
      if (file.size > 100 * 1024 * 1024) {
        this.$message.error('文件不能超过 100MB')
        return false
      }
      return true
    },
    onUploadSuccess() {
      this.$message.success('上传成功')
      this.loadDocuments()
    },
    onUploadError(err) {
      this.$message.error('上传失败：' + (err.message || ''))
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
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #4a90e2;

.ai-kb { padding: 0; }

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 21, 41, 0.06);
  overflow: hidden;
}

.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(0, 21, 41, 0.05);
  background: rgba(255, 255, 255, 0.4);
}
.panel-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 600; color: #1f2d3d;
  i { color: $primary; font-size: 16px; }
}
.panel-body { padding: 12px; }

/* KB list items */
.kb-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all .2s;
  border: 1px solid transparent;
  &:hover { background: rgba(74, 144, 226, 0.06); border-color: rgba(74, 144, 226, 0.12); }
  &.active {
    background: linear-gradient(90deg, rgba(74,144,226,0.18) 0%, rgba(74,144,226,0.04) 100%);
    border-color: rgba(74, 144, 226, 0.3);
    box-shadow: inset 3px 0 0 $primary;
  }
  .kb-icon {
    width: 34px; height: 34px;
    border-radius: 8px;
    background: linear-gradient(135deg, rgba(74,144,226,0.18) 0%, rgba(74,144,226,0.04) 100%);
    display: flex; align-items: center; justify-content: center;
    color: $primary; font-size: 16px;
    flex-shrink: 0;
  }
  &.active .kb-icon {
    background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
    color: #fff;
    box-shadow: 0 2px 6px rgba(74,144,226,0.32);
  }
  .kb-info { flex: 1; min-width: 0; }
  .kb-name { font-size: 14px; font-weight: 500; color: #1f2d3d; }
  .kb-desc {
    font-size: 12px; color: #909399; margin-top: 4px;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
  .kb-more {
    padding: 4px 6px; border-radius: 6px;
    color: #909399; opacity: 0;
    transition: opacity .2s, background .2s;
    &:hover { background: rgba(0,0,0,0.06); }
  }
  &:hover .kb-more { opacity: 1; }
}

.kb-uploader { display: inline-block; }

/* Doc table */
.doc-name {
  display: flex; align-items: center; gap: 6px;
  i { color: $primary; font-size: 14px; }
}
.doc-size { color: #909399; font-size: 12px; }
.doc-status {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  &.status-ok { background: rgba(103, 194, 58, 0.12); color: #67c23a; }
  &.status-err { background: rgba(245, 108, 108, 0.12); color: #f56c6c; }
  &.status-wait { background: rgba(230, 162, 60, 0.12); color: #e6a23c; }
}

.empty-state {
  height: 100%; min-height: 480px;
  display: flex; align-items: center; justify-content: center;
}
.kb-panel, .doc-panel {
  min-height: 480px;
}
</style>
