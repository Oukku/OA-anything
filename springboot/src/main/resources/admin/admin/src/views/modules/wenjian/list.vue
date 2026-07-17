<template>
  <el-card>
    <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
      <span>文件管理</span>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="onAdd">上传文件</el-button>
    </div>
    <el-form :inline="true" :model="query" size="small" style="margin-bottom: 12px">
      <el-form-item label="文件名"><el-input v-model="query.name" clearable /></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="文件名" />
      <el-table-column prop="size" label="大小" width="120" />
      <el-table-column prop="uploader" label="上传人" width="120" />
      <el-table-column prop="addtime" label="上传时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="mini" @click="onDownload(row)">下载</el-button>
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
    <el-dialog title="上传文件" :visible.sync="dialog" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="文件">
          <el-upload :auto-upload="false" :on-change="onFileChange" :file-list="fileList">
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="onSave">上传</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script>
import http from '@/utils/http'
export default {
  data() { return { loading: false, list: [], total: 0, query: { page: 1, limit: 10, name: '' }, dialog: false, form: {}, fileList: [] } },
  mounted() { this.loadList() },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const r = await http.get('/wenjianxinxi/page', { params: this.query })
        if (r.code === 0) { this.list = r.data.list || []; this.total = r.data.total }
      } finally { this.loading = false }
    },
    onAdd() { this.form = {}; this.fileList = []; this.dialog = true },
    onFileChange(file) { this.fileList = [file]; this.form.name = file.name; this.form.size = file.size },
    onDownload(row) {
      window.open(row.url || '#', '_blank')
    },
    async onSave() {
      if (!this.fileList.length) { this.$message.warning('请先选择文件'); return }
      const fd = new FormData()
      fd.append('file', this.fileList[0].raw)
      fd.append('name', this.form.name || '')
      fd.append('remark', this.form.remark || '')
      const r = await http.post('/wenjianxinxi/save', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      if (r.code === 0) { this.$message.success('上传成功'); this.dialog = false; this.loadList() }
      else this.$message.error(r.msg || '上传失败')
    },
    onDelete(row) {
      this.$confirm('确认删除？', '提示', { type: 'warning' }).then(async () => {
        const r = await http.get('/wenjianxinxi/delete', { params: { ids: row.id } })
        if (r.code === 0) { this.$message.success('已删除'); this.loadList() }
      }).catch(() => {})
    }
  }
}
</script>
