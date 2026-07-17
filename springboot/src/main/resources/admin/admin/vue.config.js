const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  publicPath: '/',
  outputDir: 'dist',
  productionSourceMap: false,
  devServer: {
    port: 8081,
    host: '0.0.0.0',
    open: false,
    historyApiFallback: true,
    proxy: {
      '/springboot-oa-v2': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/rag-service': {
        target: 'http://localhost:8001',
        changeOrigin: true
      }
    }
  }
})
