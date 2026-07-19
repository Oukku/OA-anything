<template>
  <div class="ai-graph">
    <div class="graph-panel glass-card">
      <div class="panel-header">
        <div class="panel-title">
          <i class="el-icon-share"></i>
          <span>知识图谱</span>
        </div>
        <div class="panel-toolbar">
          <span class="toolbar-label">知识库</span>
          <el-select v-model="kbId" size="small" placeholder="选择知识库" style="width: 180px" @change="loadDocuments"></el-select>
          <span class="toolbar-label">文档</span>
          <el-select v-model="docId" size="small" placeholder="选择文档" style="width: 220px" @change="loadGraph" :disabled="!documents.length"></el-select>
        </div>
      </div>

      <div class="panel-body">
        <div ref="cy" class="graph-canvas" v-loading="loading"></div>
        <div v-if="!hasGraph && !loading" class="graph-empty">
          <div class="graph-empty-icon"><i class="el-icon-share"></i></div>
          <p class="graph-empty-title">暂无图谱数据</p>
          <p class="graph-empty-hint">选择文档后将自动渲染实体-关系图</p>
        </div>
      </div>

      <div class="panel-legend" v-if="hasGraph">
        <div class="legend-item">
          <span class="legend-dot legend-doc"></span>
          <span>文档节点</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot legend-entity"></span>
          <span>实体节点</span>
        </div>
      </div>
    </div>
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
      cy: null,
      hasGraph: false
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
        this.hasGraph = false
        if (this.cy) { this.cy.destroy(); this.cy = null }
      }
    },
    async loadGraph() {
      if (!this.docId) return
      this.loading = true
      try {
        const res = await this.$http.get('/rag/graph/' + this.docId)
        const data = (res && res.code === 0) ? res.data : { nodes: [], edges: [] }
        this.hasGraph = (data.nodes || []).length > 0
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
            size: n.size || (n.type === 'document' ? 36 : 24)
          }
        })
      })
      ;(data.edges || []).forEach(e => {
        elements.push({ data: { source: e.source, target: e.target, label: e.label } })
      })
      this.$nextTick(() => {
        if (!this.$refs.cy) {
          console.warn('cy container not found')
          return
        }
        // 确保容器有显式尺寸再初始化
        const container = this.$refs.cy
        if (container.offsetWidth === 0 || container.offsetHeight === 0) {
          console.warn('cy container has zero size, retrying...', container.offsetWidth, container.offsetHeight)
          setTimeout(() => this.renderGraph(data), 60)
          return
        }
        try {
          this.cy = window.cytoscape({
            container: container,
            elements,
            style: [
              {
                selector: 'node',
                style: {
                  'background-color': ele => ele.data('type') === 'document' ? '#4a90e2' : '#67c23a',
                  'background-opacity': 0.85,
                  'label': 'data(label)',
                  'width': 'data(size)',
                  'height': 'data(size)',
                  'color': '#1f2d3d',
                  'font-size': '12px',
                  'font-weight': 500,
                  'text-valign': 'bottom',
                  'text-halign': 'center',
                  'text-margin-y': 8,
                  'border-width': 2,
                  'border-color': ele => ele.data('type') === 'document' ? '#6ec5ff' : '#95d475',
                  'border-opacity': 0.6
                }
              },
              {
                selector: 'edge',
                style: {
                  'width': 1.5,
                  'line-color': 'rgba(74, 144, 226, 0.45)',
                  'target-arrow-color': 'rgba(74, 144, 226, 0.55)',
                  'target-arrow-shape': 'triangle',
                  'curve-style': 'bezier',
                  'label': 'data(label)',
                  'font-size': '10px',
                  'color': '#909399',
                  'text-background-color': 'rgba(255,255,255,0.85)',
                  'text-background-padding': 2,
                  'text-background-shape': 'roundrectangle'
                }
              }
            ],
            layout: { name: 'cose', padding: 30, animate: true, nodeRepulsion: 8000, idealEdgeLength: 100 }
          })
          this.cy.resize()
        } catch (e) {
          console.error('cytoscape init failed:', e)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #4a90e2;

.ai-graph { height: calc(100vh - 96px); }

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 21, 41, 0.06);
  overflow: hidden;
  height: 100%;
  display: flex; flex-direction: column;
}

.graph-panel { display: flex; flex-direction: column; height: 100%; }

.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(0, 21, 41, 0.05);
  background: rgba(255, 255, 255, 0.4);
  flex-wrap: wrap; gap: 8px;
}
.panel-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 600; color: #1f2d3d;
  i { color: $primary; font-size: 16px; }
}
.panel-toolbar {
  display: flex; align-items: center; gap: 8px;
  .toolbar-label {
    font-size: 12px; color: #909399;
    margin-left: 8px;
    &:first-child { margin-left: 0; }
  }
}

.panel-body {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, rgba(238, 243, 248, 0.4) 0%, rgba(221, 233, 243, 0.6) 100%);
}
.graph-canvas {
  width: 100%; height: 100%;
  min-height: 480px;
}

.graph-empty {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  text-align: center;
  .graph-empty-icon {
    width: 64px; height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(74,144,226,0.18) 0%, rgba(74,144,226,0.06) 100%);
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 16px;
    i { font-size: 30px; color: $primary; }
  }
  .graph-empty-title { font-size: 14px; color: #606266; margin: 0 0 6px; }
  .graph-empty-hint { font-size: 12px; color: #909399; margin: 0; }
}

.panel-legend {
  display: flex; align-items: center; gap: 16px;
  padding: 10px 18px;
  border-top: 1px solid rgba(0, 21, 41, 0.05);
  background: rgba(255, 255, 255, 0.5);
  font-size: 12px; color: #606266;
  .legend-item { display: flex; align-items: center; gap: 6px; }
  .legend-dot {
    width: 12px; height: 12px; border-radius: 50%;
    border: 2px solid;
  }
  .legend-doc { background: #4a90e2; border-color: #6ec5ff; }
  .legend-entity { background: #67c23a; border-color: #95d475; }
}
</style>
