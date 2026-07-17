<template>
  <div class="ai-kb">
    <el-row :gutter="16">
      <el-col :span="7">
        <el-card shadow="never">
          <div slot="header">
            <span>知识库分类</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="openKbForm()">新增分类</el-button>
          </div>
          <el-empty v-if="!kbs.length" description="暂无知识库" :image-size="80"></el-empty>
          <div
            v-for="k in kbs"
            :key="k.id"
            class="kb-item"
            :class="{ active: selectedKb && selectedKb.id === k.id }"
            @click="selectKb(k)"
          >
            <i class="el-icon-collection"></i>
            <div class="kb-info">
              <div class="kb-name">{{ k.name }}</div>
              <div class="kb-desc">{{ k.description || '无描述' }}</div>
            </div>
            <el-dropdown trigger="click" @command="cmd => handleKbCmd(cmd, k)" @click.stop.native>
              <span class="el-dropdown-link"><i class="el-icon-more"></i></span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete" style="color: #f56c6c">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-card>
      </el-col>

      <el-col :span="17">
        <el-card shadow="never" v-if="selectedKb">
          <div slot="header">
            <span>{{ selectedKb.name }} - 文档管理</span>
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
          <el-table :data="documents" v-loading="loadingDoc" stripe>
            <el-table-column prop="filename" label="文件名" min-width="200"></el-table-column>
            <el-table-column label="大小" width="120">
              <template slot-scope="{ row }">{{ formatSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="{ row }">
                <el-tag size="mini" :type="row.status === 1 ? 'success' : row.status === -1 ? 'danger' : 'warning'">
                  {{ row.status === 1 ? '已索引' : row.status === -1 ? '失败' : '处理中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="上传时间" width="170"></el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="{ row }">
                <el-button type="text" icon="el-icon-delete" style="color: #f56c6c" @click="deleteDoc(row)"></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card shadow="never" v-else>
          <el-empty description="请选择或创建一个知识库"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="kbForm.id ? '编辑知识库' : '新增知识库'" :visible.sync="kbVisible" width="500px">
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
.ai-kb { padding: 0; }
.kb-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px; border-radius: 6px; cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; color: #409eff; }
  .kb-info { flex: 1; min-width: 0; }
  .kb-name { font-size: 14px; font-weight: 500; }
  .kb-desc { font-size: 12px; color: #909399; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
}
.kb-uploader { float: right; }
</style>
