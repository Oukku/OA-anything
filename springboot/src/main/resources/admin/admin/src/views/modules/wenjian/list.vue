<template>
  <div class="wenjian-page">
    <div class="glass-card filter-bar">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="文件名">
          <el-input v-model="query.name" clearable placeholder="按文件名搜索" style="width: 200px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.sfsh" clearable placeholder="全部" style="width: 140px">
            <el-option label="待审核" value="否" />
            <el-option label="已通过" value="是" />
            <el-option label="已驳回" value="驳回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="onAdd">上传文件</el-button>
    </div>

    <div class="glass-card table-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="fabuTime" label="上传时间" width="170">
          <template #default="{ row }">{{ row.fabuTime || row.addtime || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="mini" :type="sfshTagType(row.sfsh)" effect="plain">
              {{ sfshLabel(row.sfsh) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="mini" @click="onDownload(row)">下载</el-button>
            <el-button size="mini" type="warning" @click="onApprove(row)">审核</el-button>
            <el-button size="mini" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 16px; text-align: right"
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page.sync="query.page"
        :page-size.sync="query.limit"
        :page-sizes="[10, 20, 50]"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>

    <!-- 上传文件对话框 -->
    <el-dialog title="上传文件" :visible.sync="dialog" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="文件">
          <el-upload :auto-upload="false" :on-change="onFileChange" :file-list="fileList" :limit="1">
            <el-button icon="el-icon-upload2">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">支持 PDF / Word / Markdown / TXT 等格式</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.name" placeholder="文件标题" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="onSave" :loading="saving">上传</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog title="文件审核" :visible.sync="approveDialog" width="520px" :close-on-click-modal="false">
      <div v-if="currentRow" class="approve-info">
        <div class="approve-info-row"><span class="label">文件名：</span>{{ currentRow.name }}</div>
        <div class="approve-info-row"><span class="label">备注：</span>{{ currentRow.description || '-' }}</div>
        <div class="approve-info-row"><span class="label">当前状态：</span>
          <el-tag size="mini" :type="sfshTagType(currentRow.sfsh)" effect="plain">{{ sfshLabel(currentRow.sfsh) }}</el-tag>
        </div>
        <div v-if="currentRow.shhf" class="approve-info-row"><span class="label">历史回复：</span>{{ currentRow.shhf }}</div>
      </div>
      <el-form :model="approveForm" label-width="80px" style="margin-top: 16px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="approveForm.sfsh">
            <el-radio label="是">通过</el-radio>
            <el-radio label="驳回">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核回复">
          <el-input v-model="approveForm.shhf" type="textarea" :rows="3" placeholder="审核意见（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialog = false">取消</el-button>
        <el-button type="primary" @click="onApproveSubmit" :loading="approving">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import http from '@/utils/http'
export default {
  data() {
    return {
      loading: false,
      saving: false,
      approving: false,
      list: [],
      total: 0,
      query: { page: 1, limit: 10, name: '', sfsh: '' },
      dialog: false,
      approveDialog: false,
      form: {},
      fileList: [],
      currentRow: null,
      approveForm: { id: null, sfsh: '是', shhf: '' }
    }
  },
  mounted() { this.loadList() },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const r = await http.get('/wenjianxinxi/page', { params: this.query })
        if (r && r.code === 0) { this.list = r.data.list || []; this.total = r.data.total || 0 }
      } finally { this.loading = false }
    },
    resetQuery() { this.query = { page: 1, limit: 10, name: '', sfsh: '' }; this.loadList() },
    sfshLabel(sfsh) {
      if (sfsh === '是') return '已通过'
      if (sfsh === '驳回') return '已驳回'
      return '待审核'
    },
    sfshTagType(sfsh) {
      if (sfsh === '是') return 'success'
      if (sfsh === '驳回') return 'danger'
      return 'warning'
    },
    onAdd() {
      this.form = { name: '', description: '' }
      this.fileList = []
      this.dialog = true
    },
    onFileChange(file) {
      this.fileList = [file]
      if (!this.form.name) this.form.name = file.name
    },
    onDownload(row) {
      if (row.url) window.open(row.url, '_blank')
      else this.$message.info('该文件无下载链接')
    },
    async onSave() {
      if (!this.fileList.length) { this.$message.warning('请先选择文件'); return }
      this.saving = true
      try {
        const fd = new FormData()
        fd.append('file', this.fileList[0].raw)
        fd.append('name', this.form.name || this.fileList[0].name)
        fd.append('description', this.form.description || '')
        const r = await http.post('/wenjianxinxi/save', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
        if (r && r.code === 0) { this.$message.success('上传成功，待审核'); this.dialog = false; this.loadList() }
        else this.$message.error((r && r.msg) || '上传失败')
      } finally { this.saving = false }
    },
    onApprove(row) {
      this.currentRow = row
      this.approveForm = { id: row.id, sfsh: '是', shhf: row.shhf || '' }
      this.approveDialog = true
    },
    async onApproveSubmit() {
      this.approving = true
      try {
        const r = await http.post('/wenjianxinxi/approve', this.approveForm)
        if (r && r.code === 0) {
          this.$message.success('审核已提交')
          this.approveDialog = false
          this.loadList()
        } else {
          this.$message.error((r && r.msg) || '审核失败')
        }
      } finally { this.approving = false }
    },
    onDelete(row) {
      this.$confirm(`确认删除文件「${row.name}」？`, '提示', { type: 'warning' }).then(async () => {
        const r = await http.delete(`/wenjianxinxi/delete/${row.id}`)
        if (r && r.code === 0) { this.$message.success('已删除'); this.loadList() }
        else this.$message.error((r && r.msg) || '删除失败')
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.wenjian-page { padding: 0; }

.glass-card {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: var(--biz-shadow);
}

.filter-bar {
  padding: 14px 20px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;

  ::v-deep .el-form--inline .el-form-item { margin-bottom: 0; }
}

.table-card { padding: 16px 20px; }

.approve-info {
  background: #f8fafc;
  border: 1px solid var(--biz-border);
  border-radius: 4px;
  padding: 12px 16px;

  .approve-info-row {
    font-size: 13px;
    color: var(--biz-text-2);
    line-height: 1.9;

    .label {
      color: var(--biz-text-3);
      display: inline-block;
      width: 80px;
    }
  }
}
</style>
