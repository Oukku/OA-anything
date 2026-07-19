<template>
  <div class="dept-page">
    <div class="glass-card toolbar">
      <div class="toolbar-title">
        <i class="el-icon-s-operation"></i>
        <span>部门架构管理</span>
      </div>
      <div class="toolbar-actions">
        <el-input v-model="filterText" placeholder="搜索部门名称" prefix-icon="el-icon-search" size="small" clearable style="width: 200px" />
        <el-button size="small" @click="expandAll">展开全部</el-button>
        <el-button size="small" @click="collapseAll">折叠全部</el-button>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="onAdd(null)">新增顶级部门</el-button>
      </div>
    </div>

    <div class="glass-card tree-card" v-loading="loading">
      <el-empty v-if="!loading && treeData.length === 0" description="暂无部门数据，请点击右上角新增顶级部门"></el-empty>
      <el-tree
        v-else
        ref="tree"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        default-expand-all
        @node-click="onNodeClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="tree-node-label">
              <i class="el-icon-office-building"></i>
              <span class="tree-node-name">{{ data.deptName }}</span>
              <el-tag size="mini" :type="deptTypeTag(data.deptType)" effect="plain">{{ deptTypeLabel(data.deptType) }}</el-tag>
              <el-tag v-if="data.status === 0" size="mini" type="info" effect="plain">已禁用</el-tag>
              <span v-if="data.leaderName" class="tree-node-leader">负责人：{{ data.leaderName }}</span>
            </div>
            <div class="tree-node-actions">
              <el-button size="mini" type="text" icon="el-icon-plus" @click.stop="onAdd(data)">添加子部门</el-button>
              <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="onEdit(data)">编辑</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="onDelete(data)" style="color: #f56c6c">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 部门编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialog" width="560px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="deptForm" label-width="100px" size="small">
        <el-form-item label="上级部门">
          <el-cascader
            v-model="form.parentPath"
            :options="cascaderOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'deptName', children: 'children' }"
            placeholder="顶级部门（不选）"
            clearable
            style="width: 100%"
            @change="onParentChange"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="如：技术研发部" />
        </el-form-item>
        <el-form-item label="部门类型" prop="deptType">
          <el-select v-model="form.deptType" style="width: 100%">
            <el-option label="公司" value="company" />
            <el-option label="事业部" value="division" />
            <el-option label="部门" value="department" />
            <el-option label="小组" value="group" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.leaderName" placeholder="负责人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人ID">
              <el-input v-model="form.leaderId" placeholder="留空或填员工ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="联系邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="onSave" :loading="saving">保存</el-button>
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
      list: [],
      treeData: [],
      filterText: '',
      dialog: false,
      form: this.defaultForm(),
      rules: {
        deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
        deptType: [{ required: true, message: '请选择部门类型', trigger: 'change' }]
      },
      treeProps: { label: 'deptName', children: 'children' }
    }
  },
  computed: {
    dialogTitle() {
      return this.form.id ? '编辑部门' : '新增部门'
    },
    cascaderOptions() {
      // 构造级联选择器选项（带 children）
      const build = (nodes) => {
        return nodes.map(n => ({
          id: n.id,
          deptName: n.deptName,
          children: n.children && n.children.length ? build(n.children) : undefined
        }))
      }
      return build(this.treeData)
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree && this.$refs.tree.filter(val)
    }
  },
  mounted() { this.loadList() },
  methods: {
    defaultForm() {
      return {
        id: null,
        parentId: null,
        parentPath: [],
        deptName: '',
        deptType: 'department',
        sortOrder: 0,
        status: 1,
        leaderId: null,
        leaderName: '',
        phone: '',
        email: ''
      }
    },
    async loadList() {
      this.loading = true
      try {
        const r = await http.get('/dept/list')
        if (r && r.code === 0) {
          this.list = r.data || []
          this.treeData = this.buildTree(this.list)
        }
      } finally { this.loading = false }
    },
    /** 扁平列表转树结构 */
    buildTree(list) {
      const map = {}
      const roots = []
      // 复制并初始化 children
      list.forEach(item => {
        map[item.id] = { ...item, children: [] }
      })
      // 组装父子关系
      list.forEach(item => {
        const node = map[item.id]
        if (item.parentId && map[item.parentId]) {
          map[item.parentId].children.push(node)
        } else {
          roots.push(node)
        }
      })
      // 排序
      const sortNodes = (nodes) => {
        nodes.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
        nodes.forEach(n => { if (n.children && n.children.length) sortNodes(n.children) })
      }
      sortNodes(roots)
      // 移除空 children 数组以避免 el-tree 显示展开箭头
      const cleanNodes = (nodes) => {
        nodes.forEach(n => {
          if (n.children && n.children.length === 0) delete n.children
          else if (n.children) cleanNodes(n.children)
        })
      }
      cleanNodes(roots)
      return roots
    },
    filterNode(value, data) {
      if (!value) return true
      return (data.deptName || '').includes(value)
    },
    expandAll() {
      const tree = this.$refs.tree
      if (!tree) return
      const expand = (nodes) => {
        nodes.forEach(n => { tree.store.nodesMap[n.id] && (tree.store.nodesMap[n.id].expanded = true) })
      }
      // el-tree default-expand-all 已展开，这里手动控制
      Object.values(tree.store.nodesMap).forEach(n => { n.expanded = true })
    },
    collapseAll() {
      const tree = this.$refs.tree
      if (!tree) return
      Object.values(tree.store.nodesMap).forEach(n => {
        if (n.level > 1) n.expanded = false
      })
    },
    deptTypeLabel(t) {
      return { company: '公司', division: '事业部', department: '部门', group: '小组' }[t] || '部门'
    },
    deptTypeTag(t) {
      return { company: 'danger', division: 'warning', department: 'primary', group: 'success' }[t] || 'info'
    },
    onNodeClick(data) {
      // 占位：可扩展右侧详情面板
    },
    onAdd(parent) {
      this.form = this.defaultForm()
      if (parent) {
        this.form.parentId = parent.id
        this.form.parentPath = this.findPath(this.treeData, parent.id)
        // 子部门类型默认比父级低一级
        const typeMap = { company: 'division', division: 'department', department: 'group', group: 'group' }
        this.form.deptType = typeMap[parent.deptType] || 'department'
      }
      this.dialog = true
      this.$nextTick(() => this.$refs.deptForm && this.$refs.deptForm.clearValidate())
    },
    onEdit(data) {
      this.form = {
        id: data.id,
        parentId: data.parentId,
        parentPath: data.parentId ? this.findPath(this.treeData, data.parentId) : [],
        deptName: data.deptName,
        deptType: data.deptType || 'department',
        sortOrder: data.sortOrder || 0,
        status: data.status != null ? data.status : 1,
        leaderId: data.leaderId,
        leaderName: data.leaderName || '',
        phone: data.phone || '',
        email: data.email || ''
      }
      this.dialog = true
      this.$nextTick(() => this.$refs.deptForm && this.$refs.deptForm.clearValidate())
    },
    /** 在 treeData 中查找从根到目标 id 的路径（返回 id 数组） */
    findPath(nodes, targetId, path = []) {
      for (const n of nodes) {
        const cur = [...path, n.id]
        if (n.id === targetId) return cur
        if (n.children) {
          const r = this.findPath(n.children, targetId, cur)
          if (r) return r
        }
      }
      return null
    },
    onParentChange(pathArr) {
      if (pathArr && pathArr.length) {
        this.form.parentId = pathArr[pathArr.length - 1]
      } else {
        this.form.parentId = null
      }
    },
    async onSave() {
      this.$refs.deptForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          const payload = { ...this.form }
          delete payload.parentPath
          // leaderId 空字符串处理
          if (!payload.leaderId) payload.leaderId = null
          const r = await http.post('/dept/save', payload)
          if (r && r.code === 0) {
            this.$message.success(this.form.id ? '已更新' : '已新增')
            this.dialog = false
            this.loadList()
          } else {
            this.$message.error((r && r.msg) || '保存失败')
          }
        } finally { this.saving = false }
      })
    },
    onDelete(data) {
      const hasChildren = data.children && data.children.length > 0
      const tip = hasChildren
        ? `部门「${data.deptName}」包含子部门，删除后子部门将变为顶级部门。确认删除？`
        : `确认删除部门「${data.deptName}」？`
      this.$confirm(tip, '提示', { type: 'warning' }).then(async () => {
        const r = await http.delete(`/dept/delete/${data.id}`)
        if (r && r.code === 0) {
          this.$message.success('已删除')
          this.loadList()
        } else {
          this.$message.error((r && r.msg) || '删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.dept-page { padding: 0; }

.glass-card {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: var(--biz-shadow);
}

.toolbar {
  padding: 14px 20px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;

  .toolbar-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--biz-text-1);

    i { color: var(--biz-primary); font-size: 18px; }
  }

  .toolbar-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.tree-card {
  padding: 16px 20px;
  min-height: 300px;

  ::v-deep .el-tree {
    background: transparent;

    .el-tree-node__content {
      height: 38px;
      border-radius: 4px;
      transition: background .2s;

      &:hover { background: #f1f5f9; }
    }
  }
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 12px;

  .tree-node-label {
    display: flex;
    align-items: center;
    gap: 8px;

    i { color: var(--biz-primary); font-size: 14px; }
    .tree-node-name { font-size: 14px; color: var(--biz-text-1); font-weight: 500; }
    .tree-node-leader { font-size: 12px; color: var(--biz-text-3); margin-left: 8px; }
  }

  .tree-node-actions {
    display: none;
    gap: 4px;
  }

  &:hover .tree-node-actions { display: flex; }
}
</style>
