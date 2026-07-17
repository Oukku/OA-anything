<template>
  <el-card>
    <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
      <span>公告管理</span>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="onAdd">发布公告</el-button>
    </div>
    <el-form :inline="true" :model="query" size="small" style="margin-bottom: 12px">
      <el-form-item label="标题"><el-input v-model="query.title" clearable /></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="author" label="发布人" width="120" />
      <el-table-column prop="addtime" label="发布时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="mini" @click="onView(row)">查看</el-button>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialog" width="700px">
      <el-form v-if="!readonly" :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input type="textarea" :rows="8" v-model="form.content" /></el-form-item>
        <el-form-item label="发布人"><el-input v-model="form.author" /></el-form-item>
      </el-form>
      <div v-else>
        <h2>{{ form.title }}</h2>
        <p style="color: #999; font-size: 12px">发布人：{{ form.author }} · {{ form.addtime }}</p>
        <div style="margin-top: 16px; white-space: pre-wrap">{{ form.content }}</div>
      </div>
      <template #footer>
        <el-button v-if="!readonly" @click="dialog = false">取消</el-button>
        <el-button v-if="!readonly" type="primary" @click="onSave">发布</el-button>
        <el-button v-else type="primary" @click="dialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script>
import http from '@/utils/http'
export default {
  data() { return { loading: false, list: [], total: 0, query: { page: 1, limit: 10, title: '' }, dialog: false, form: {}, readonly: false, dialogTitle: '' } },
  mounted() { this.loadList() },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const r = await http.get('/gonggaoxinxi/page', { params: this.query })
        if (r.code === 0) { this.list = r.data.list || []; this.total = r.data.total }
      } finally { this.loading = false }
    },
    onAdd() { this.form = {}; this.readonly = false; this.dialogTitle = '发布公告'; this.dialog = true },
    onView(row) { this.form = { ...row }; this.readonly = true; this.dialogTitle = '查看公告'; this.dialog = true },
    async onSave() {
      const r = await http.post('/gonggaoxinxi/save', this.form)
      if (r.code === 0) { this.$message.success('发布成功'); this.dialog = false; this.loadList() }
      else this.$message.error(r.msg || '发布失败')
    },
    onDelete(row) {
      this.$confirm('确认删除？', '提示', { type: 'warning' }).then(async () => {
        const r = await http.get('/gonggaoxinxi/delete', { params: { ids: row.id } })
        if (r.code === 0) { this.$message.success('已删除'); this.loadList() }
      }).catch(() => {})
    }
  }
}
</script>
