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
    if (this.cy) {
      try { this.cy.removeAllListeners(); this.cy.destroy() } catch (e) { /* ignore */ }
      this.cy = null
    }
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
      // 销毁旧实例前先移除所有事件，避免 destroy 后 notify 空指针
      if (this.cy) {
        try {
          this.cy.removeAllListeners()
          this.cy.destroy()
        } catch (e) { /* ignore */ }
        this.cy = null
      }
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
                  'background-color': ele => ele.data('type') === 'document' ? '#1e40af' : '#15803d',
                  'background-opacity': 0.9,
                  'label': 'data(label)',
                  'width': 'data(size)',
                  'height': 'data(size)',
                  'color': '#0f172a',
                  'font-size': '12px',
                  'font-weight': 500,
                  'text-valign': 'bottom',
                  'text-halign': 'center',
                  'text-margin-y': 8,
                  'border-width': 2,
                  'border-color': ele => ele.data('type') === 'document' ? '#3b82f6' : '#22c55e',
                  'border-opacity': 0.6
                }
              },
              {
                selector: 'edge',
                style: {
                  'width': 1.5,
                  'line-color': 'rgba(30, 64, 175, 0.45)',
                  'target-arrow-color': 'rgba(30, 64, 175, 0.55)',
                  'target-arrow-shape': 'triangle',
                  'curve-style': 'bezier',
                  'label': 'data(label)',
                  'font-size': '10px',
                  'color': '#64748b',
                  'text-background-color': 'rgba(255,255,255,0.92)',
                  'text-background-padding': 2,
                  'text-background-shape': 'roundrectangle'
                }
              }
            ],
            layout: { name: 'cose', padding: 30, animate: true, nodeRepulsion: 8000, idealEdgeLength: 100 }
          })
          // resize 单独 try/catch，避免内部 notify 空指针冒泡
          try { this.cy && this.cy.resize() } catch (e) { /* ignore notify error */ }
        } catch (e) {
          console.error('cytoscape init failed:', e)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-graph { height: calc(100vh - 96px); }

.glass-card {
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: var(--biz-shadow);
  overflow: hidden;
  height: 100%;
  display: flex; flex-direction: column;
}

.graph-panel { display: flex; flex-direction: column; height: 100%; }

.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--biz-border);
  background: #f8fafc;
  flex-wrap: wrap; gap: 8px;
}
.panel-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 600; color: var(--biz-text-1);
  i { color: var(--biz-primary); font-size: 16px; }
}
.panel-toolbar {
  display: flex; align-items: center; gap: 8px;
  .toolbar-label {
    font-size: 12px; color: var(--biz-text-4);
    margin-left: 8px;
    &:first-child { margin-left: 0; }
  }
}

.panel-body {
  flex: 1;
  position: relative;
  background: var(--biz-bg);
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
    width: 56px; height: 56px;
    border-radius: 6px;
    background: rgba(30, 64, 175, 0.08);
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 14px;
    i { font-size: 26px; color: var(--biz-primary); }
  }
  .graph-empty-title { font-size: 14px; color: var(--biz-text-2); margin: 0 0 6px; }
  .graph-empty-hint { font-size: 12px; color: var(--biz-text-4); margin: 0; }
}

.panel-legend {
  display: flex; align-items: center; gap: 16px;
  padding: 10px 18px;
  border-top: 1px solid var(--biz-border);
  background: #f8fafc;
  font-size: 12px; color: var(--biz-text-3);
  .legend-item { display: flex; align-items: center; gap: 6px; }
  .legend-dot {
    width: 12px; height: 12px; border-radius: 50%;
    border: 2px solid;
  }
  .legend-doc { background: #1e40af; border-color: #3b82f6; }
  .legend-entity { background: #15803d; border-color: #22c55e; }
}
</style>
