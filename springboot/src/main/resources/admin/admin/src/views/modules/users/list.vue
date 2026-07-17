<template>
  <el-card>
    <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
      <span>用户管理</span>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="onAdd">新增用户</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="addtime" label="创建时间" width="180" />
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
    <el-dialog :title="form.id ? '编辑用户' : '新增用户'" :visible.sync="dialog" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" show-password /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="选择角色" style="width:100%">
            <el-option label="管理员" value="管理员" />
            <el-option label="普通用户" value="用户" />
          </el-select>
        </el-form-item>
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
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { page: 1, limit: 10 },
      dialog: false,
      form: {}
    }
  },
  mounted() { this.loadList() },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const r = await http.get('/users/page', { params: this.query })
        if (r.code === 0) { this.list = r.data.list || []; this.total = r.data.total }
      } finally { this.loading = false }
    },
    onAdd() { this.form = {}; this.dialog = true },
    onEdit(row) { this.form = { ...row }; this.dialog = true },
    async onSave() {
      const r = this.form.id
        ? await http.post('/users/update', this.form)
        : await http.post('/users/save', this.form)
      if (r.code === 0) { this.$message.success('保存成功'); this.dialog = false; this.loadList() }
      else this.$message.error(r.msg || '保存失败')
    },
    onDelete(row) {
      this.$confirm('确认删除该用户？', '提示', { type: 'warning' }).then(async () => {
        const r = await http.get('/users/delete', { params: { ids: row.id } })
        if (r.code === 0) { this.$message.success('已删除'); this.loadList() }
        else this.$message.error(r.msg)
      }).catch(() => {})
    }
  }
}
</script>
