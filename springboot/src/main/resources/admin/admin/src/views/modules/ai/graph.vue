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
          <el-select v-model="kbId" size="small" placeholder="选择知识库" style="width: 180px" @change="loadDocuments">
            <el-option v-for="kb in kbs" :key="kb.id" :label="kb.name" :value="kb.id" />
          </el-select>
          <span class="toolbar-label">文档</span>
          <el-select v-model="docId" size="small" placeholder="选择文档" style="width: 220px" @change="loadGraph" :disabled="!documents.length">
            <el-option v-for="d in documents" :key="d.ragDocId" :label="d.filename" :value="d.ragDocId" />
          </el-select>
          <el-button v-if="hasGraph" size="mini" icon="el-icon-refresh" @click="loadGraph" :loading="loading">刷新</el-button>
        </div>
      </div>

      <div class="panel-body">
        <!-- 骨架屏 -->
        <div v-if="loading" class="graph-skeleton">
          <div class="skeleton-row" v-for="i in 6" :key="i">
            <div class="skeleton-circle" :style="{ width: (20 + i * 8) + 'px', height: (20 + i * 8) + 'px', marginLeft: (i * 30) + 'px' }"></div>
            <div class="skeleton-line"></div>
          </div>
          <div class="skeleton-tip">
            <i class="el-icon-loading"></i>
            <span>正在加载知识图谱...</span>
          </div>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="graph-error">
          <div class="graph-error-icon"><i class="el-icon-warning-outline"></i></div>
          <p class="graph-error-title">加载失败</p>
          <p class="graph-error-msg">{{ error }}</p>
          <el-button type="primary" size="small" icon="el-icon-refresh" @click="loadGraph">重试</el-button>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!hasGraph && !loading" class="graph-empty">
          <div class="graph-empty-icon"><i class="el-icon-share"></i></div>
          <p class="graph-empty-title">暂无图谱数据</p>
          <p class="graph-empty-hint">{{ emptyHint }}</p>
        </div>

        <!-- 图谱画布 -->
        <div ref="cy" class="graph-canvas" v-show="!loading && !error && hasGraph"></div>
      </div>

      <div class="panel-legend" v-if="hasGraph && !loading && !error">
        <div class="legend-item">
          <span class="legend-dot legend-doc"></span>
          <span>文档节点</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot legend-entity"></span>
          <span>实体节点</span>
        </div>
        <div class="legend-tip" v-if="hasGraph">
          <i class="el-icon-info"></i>
          <span>当前图谱节点较少：实体抽取需要更强 LLM（如 Qwen2.5-72B）才能生成完整实体关系</span>
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
      error: null,
      cy: null,
      hasGraph: false,
      emptyHint: '选择文档后将自动渲染实体-关系图'
    }
  },
  mounted() {
    this.loadKbs()
  },
  beforeDestroy() {
    this.destroyCy()
  },
  methods: {
    destroyCy() {
      if (this.cy) {
        try { this.cy.removeAllListeners(); this.cy.destroy() } catch (e) { /* ignore */ }
        this.cy = null
      }
    },
    async loadKbs() {
      const res = await this.$http.get('/ai/kb/list')
      if (res && res.code === 0) {
        this.kbs = res.data || []
        if (this.kbs.length) {
          this.kbId = this.kbs[0].id
          this.loadDocuments()
        } else {
          this.emptyHint = '请先在「知识库管理」中创建知识库并上传文档'
        }
      }
    },
    async loadDocuments() {
      if (!this.kbId) return
      this.docId = null
      this.destroyCy()
      this.hasGraph = false
      this.error = null
      const res = await this.$http.get('/rag/documents?kbId=' + this.kbId)
      this.documents = (res && res.code === 0 ? res.data : []).filter(d => d.ragDocId)
      if (this.documents.length) {
        this.docId = this.documents[0].ragDocId
        this.loadGraph()
      } else {
        this.emptyHint = '该知识库下暂无已索引文档'
      }
    },
    /** 带超时的 fetch */
    fetchWithTimeout(promise, timeoutMs = 15000, errMsg = '加载超时（>15s）') {
      return new Promise((resolve, reject) => {
        const timer = setTimeout(() => reject(new Error(errMsg)), timeoutMs)
        promise.then(
          v => { clearTimeout(timer); resolve(v) },
          e => { clearTimeout(timer); reject(e) }
        )
      })
    },
    async loadGraph() {
      if (!this.docId) return
      this.loading = true
      this.error = null
      this.hasGraph = false
      this.destroyCy()
      try {
        const res = await this.fetchWithTimeout(
          this.$http.get('/rag/graph/' + this.docId),
          15000,
          '图谱加载超时（>15s），请检查 RAG 服务状态'
        )
        const data = (res && res.code === 0) ? res.data : { nodes: [], edges: [] }
        const nodes = data.nodes || []
        const edges = data.edges || []
        if (nodes.length === 0) {
          this.emptyHint = '该文档暂无实体关系数据（实体抽取失败或文档为空）'
          return
        }
        this.hasGraph = true
        this.$nextTick(() => this.renderGraph(data))
      } catch (e) {
        console.error('loadGraph failed:', e)
        this.error = (e && e.message) || '加载失败'
      } finally {
        this.loading = false
      }
    },
    renderGraph(data) {
      if (this.cy) this.destroyCy()
      if (!window.cytoscape) {
        this.error = 'Cytoscape.js 未加载，请检查依赖'
        this.hasGraph = false
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
        if (!this.$refs.cy) return
        const container = this.$refs.cy
        if (container.offsetWidth === 0 || container.offsetHeight === 0) {
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
          try { this.cy && this.cy.resize() } catch (e) { /* ignore */ }
        } catch (e) {
          console.error('cytoscape init failed:', e)
          this.error = '图谱渲染失败：' + (e.message || e)
          this.hasGraph = false
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

/* 骨架屏 */
.graph-skeleton {
  position: absolute; inset: 0;
  padding: 30px;
  overflow: hidden;

  .skeleton-row {
    display: flex; align-items: center; gap: 14px;
    margin-bottom: 28px;
    animation: skeleton-pulse 1.4s ease-in-out infinite;
  }
  .skeleton-circle {
    border-radius: 50%;
    background: linear-gradient(90deg, #e2e8f0 25%, #cbd5e1 50%, #e2e8f0 75%);
    background-size: 200% 100%;
    animation: skeleton-shimmer 1.4s linear infinite;
    flex-shrink: 0;
  }
  .skeleton-line {
    flex: 1;
    height: 8px;
    background: linear-gradient(90deg, #e2e8f0 25%, #cbd5e1 50%, #e2e8f0 75%);
    background-size: 200% 100%;
    animation: skeleton-shimmer 1.4s linear infinite;
    border-radius: 4px;
  }
  .skeleton-tip {
    position: absolute; bottom: 24px; left: 0; right: 0;
    text-align: center;
    color: var(--biz-text-3);
    font-size: 13px;
    i { margin-right: 6px; }
  }
}
@keyframes skeleton-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

/* 错误状态 */
.graph-error {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  text-align: center; gap: 8px;

  .graph-error-icon {
    width: 56px; height: 56px;
    border-radius: 6px;
    background: rgba(245, 108, 108, 0.1);
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 6px;
    i { font-size: 26px; color: #f56c6c; }
  }
  .graph-error-title { font-size: 14px; color: var(--biz-text-1); margin: 0; font-weight: 600; }
  .graph-error-msg { font-size: 12px; color: var(--biz-text-3); margin: 0 0 10px; max-width: 380px; }
}

/* 空状态 */
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
  .graph-empty-hint { font-size: 12px; color: var(--biz-text-4); margin: 0; max-width: 400px; }
}

.panel-legend {
  display: flex; align-items: center; gap: 16px;
  padding: 10px 18px;
  border-top: 1px solid var(--biz-border);
  background: #f8fafc;
  font-size: 12px; color: var(--biz-text-3);
  flex-wrap: wrap;
  .legend-item { display: flex; align-items: center; gap: 6px; }
  .legend-dot {
    width: 12px; height: 12px; border-radius: 50%;
    border: 2px solid;
  }
  .legend-doc { background: #1e40af; border-color: #3b82f6; }
  .legend-entity { background: #15803d; border-color: #22c55e; }
  .legend-tip {
    display: flex; align-items: center; gap: 4px;
    margin-left: auto;
    color: var(--biz-text-4);
    i { color: var(--biz-primary-light); }
  }
}
</style>
