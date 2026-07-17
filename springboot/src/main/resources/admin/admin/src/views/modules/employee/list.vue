<template>
  <el-card>
    <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
      <span>员工管理</span>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="onAdd">新增员工</el-button>
    </div>
    <el-form :inline="true" :model="query" size="small" style="margin-bottom: 12px">
      <el-form-item label="姓名"><el-input v-model="query.name" clearable /></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="deptName" label="部门" />
      <el-table-column prop="position" label="职位" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="mini" @click="onEdit(row)">编辑</el-button>
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
    <el-dialog :title="form.id ? '编辑员工' : '新增员工'" :visible.sync="dialog" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender"><el-radio label="男" />女<el-radio label="女" /></el-radio-group>
        </el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="form.position" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script>
import http from '@/utils/http'
export default {
  data() { return { loading: false, list: [], total: 0, query: { page: 1, limit: 10, name: '' }, dialog: false, form: {} } },
  mounted() { this.loadList() },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const r = await http.get('/employee/page', { params: this.query })
        if (r.code === 0) { this.list = r.data.list || []; this.total = r.data.total }
      } finally { this.loading = false }
    },
    onAdd() { this.form = { gender: '男' }; this.dialog = true },
    onEdit(row) { this.form = { ...row }; this.dialog = true },
    async onSave() {
      const r = this.form.id
        ? await http.post('/employee/update', this.form)
        : await http.post('/employee/save', this.form)
      if (r.code === 0) { this.$message.success('保存成功'); this.dialog = false; this.loadList() }
      else this.$message.error(r.msg || '保存失败')
    },
    onDelete(row) {
      this.$confirm('确认删除？', '提示', { type: 'warning' }).then(async () => {
        const r = await http.get('/employee/delete', { params: { ids: row.id } })
        if (r.code === 0) { this.$message.success('已删除'); this.loadList() }
        else this.$message.error(r.msg)
      }).catch(() => {})
    }
  }
}
</script>
