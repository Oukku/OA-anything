<template>
  <div class="ai-graph">
    <el-card shadow="never">
      <div slot="header">
        <span>知识图谱</span>
        <el-select v-model="kbId" size="small" placeholder="选择知识库" style="width: 180px; margin-left: 12px" @change="loadDocuments">
          <el-option v-for="k in kbs" :key="k.id" :label="k.name" :value="k.id"></el-option>
        </el-select>
        <el-select v-model="docId" size="small" placeholder="选择文档" style="width: 220px; margin-left: 12px" @change="loadGraph" :disabled="!documents.length">
          <el-option v-for="d in documents" :key="d.id" :label="d.filename" :value="d.ragDocId"></el-option>
        </el-select>
      </div>
      <div ref="cy" class="graph-canvas" v-loading="loading"></div>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      kbs: [],
      kbId: null,
      documents: [],
      docId: null,
      loading: false,
      cy: null
    }
  },
  mounted() {
    this.loadKbs()
  },
  beforeDestroy() {
    if (this.cy) this.cy.destroy()
  },
  methods: {
    async loadKbs() {
      const res = await this.$http.get('/ai/kb/list')
      if (res && res.code === 0) {
        this.kbs = res.data || []
        if (this.kbs.length) {
          this.kbId = this.kbs[0].id
          this.loadDocuments()
        }
      }
    },
    async loadDocuments() {
      if (!this.kbId) return
      this.docId = null
      const res = await this.$http.get('/rag/documents?kbId=' + this.kbId)
      this.documents = (res && res.code === 0 ? res.data : []).filter(d => d.ragDocId)
      if (this.documents.length) {
        this.docId = this.documents[0].ragDocId
        this.loadGraph()
      } else {
        this.renderGraph({ nodes: [], edges: [] })
      }
    },
    async loadGraph() {
      if (!this.docId) return
      this.loading = true
      try {
        const res = await this.$http.get('/rag/graph/' + this.docId)
        const data = (res && res.code === 0) ? res.data : { nodes: [], edges: [] }
        this.renderGraph(data)
      } finally {
        this.loading = false
      }
    },
    renderGraph(data) {
      if (this.cy) { this.cy.destroy(); this.cy = null }
      if (!window.cytoscape) {
        this.$message.warning('Cytoscape.js 未加载，请检查依赖')
        return
      }
      const elements = []
      ;(data.nodes || []).forEach(n => {
        elements.push({
          data: {
            id: n.id,
            label: n.label,
            type: n.type,
            size: n.size || (n.type === 'document' ? 30 : 20)
          }
        })
      })
      ;(data.edges || []).forEach(e => {
        elements.push({ data: { source: e.source, target: e.target, label: e.label } })
      })
      this.cy = window.cytoscape({
        container: this.$refs.cy,
        elements,
        style: [
          {
            selector: 'node',
            style: {
              'background-color': ele => ele.data('type') === 'document' ? '#409eff' : '#67c23a',
              'label': 'data(label)',
              'width': 'data(size)',
              'height': 'data(size)',
              'color': '#303133',
              'font-size': '12px',
              'text-valign': 'bottom',
              'text-halign': 'center',
              'text-margin-y': 6
            }
          },
          {
            selector: 'edge',
            style: {
              'width': 1.5,
              'line-color': '#c0c4cc',
              'target-arrow-color': '#c0c4cc',
              'target-arrow-shape': 'triangle',
              'curve-style': 'bezier',
              'label': 'data(label)',
              'font-size': '10px',
              'color': '#606266'
            }
          }
        ],
        layout: { name: 'cose', padding: 20, animate: true }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-graph { height: calc(100vh - 180px); }
.graph-canvas {
  width: 100%; height: calc(100vh - 280px);
  background: #fafbfc; border-radius: 4px;
}
</style>
