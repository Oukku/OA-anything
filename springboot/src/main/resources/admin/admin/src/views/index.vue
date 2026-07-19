<template>
  <el-container class="app-shell">
    <el-aside width="220px" class="app-aside">
      <div class="app-logo">
        <i class="el-icon-office-building"></i>
        <span>OA 管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="transparent"
        text-color="rgba(255,255,255,0.82)"
        active-text-color="#6ec5ff"
        router
        class="app-menu"
      >
        <el-menu-item index="/home"><i class="el-icon-house"></i><span>首页</span></el-menu-item>
        <el-menu-item index="/users"><i class="el-icon-user"></i><span>用户管理</span></el-menu-item>
        <el-menu-item index="/employee"><i class="el-icon-user-solid"></i><span>员工管理</span></el-menu-item>
        <el-menu-item index="/gonggao"><i class="el-icon-bell"></i><span>公告管理</span></el-menu-item>
        <el-menu-item index="/wenjian"><i class="el-icon-folder"></i><span>文件管理</span></el-menu-item>
        <el-menu-item index="/gongzuorizhi"><i class="el-icon-document"></i><span>工作日志</span></el-menu-item>
        <el-submenu index="/ai">
          <template slot="title"><i class="el-icon-cpu"></i><span>AI 中心</span></template>
          <el-menu-item index="/ai/chat"><i class="el-icon-chat-dot-square"></i><span>AI 对话</span></el-menu-item>
          <el-menu-item index="/ai/kb"><i class="el-icon-collection"></i><span>知识库管理</span></el-menu-item>
          <el-menu-item index="/ai/graph"><i class="el-icon-share"></i><span>知识图谱</span></el-menu-item>
          <el-menu-item index="/ai/config"><i class="el-icon-setting"></i><span>AI 配置</span></el-menu-item>
        </el-submenu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="app-page-title">
          <i class="el-icon-magic-stick"></i>
          <span>{{ $route.meta.title || '工作台' }}</span>
        </div>
        <el-dropdown @command="onCmd" class="app-user">
          <span class="app-user-inner">
            <span class="app-user-avatar"><i class="el-icon-user-solid"></i></span>
            <span class="app-user-name">{{ userInfo.username || '用户' }}</span>
            <i class="el-icon-arrow-down"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="logout" icon="el-icon-switch-button">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  computed: {
    userInfo() { return this.$store.state.userInfo }
  },
  methods: {
    onCmd(cmd) {
      if (cmd === 'logout') {
        this.$store.dispatch('logout')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.app-shell {
  height: 100vh;
  background: linear-gradient(135deg, #eef3f8 0%, #dde9f3 100%);
}

/* ===== Sidebar (深色玻璃) ===== */
.app-aside {
  background: rgba(0, 21, 41, 0.78);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.app-logo {
  height: 60px;
  display: flex; align-items: center; gap: 10px;
  padding: 0 18px;
  font-size: 15px; font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(90deg, rgba(74,144,226,0.18) 0%, transparent 100%);
}
.app-logo i {
  font-size: 22px;
  color: #6ec5ff;
}
.app-menu {
  flex: 1; overflow-y: auto;
  border-right: none !important;
}
.app-menu >>> .el-menu-item,
.app-menu >>> .el-submenu__title {
  height: 46px; line-height: 46px;
  border-radius: 8px;
  margin: 4px 8px;
  transition: background .2s;
}
.app-menu >>> .el-menu-item:hover,
.app-menu >>> .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.08) !important;
}
.app-menu >>> .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(74,144,226,0.32) 0%, rgba(74,144,226,0.08) 100%) !important;
  color: #6ec5ff !important;
  box-shadow: inset 3px 0 0 #6ec5ff;
}
.app-menu >>> .el-submenu .el-menu-item {
  padding-left: 50px !important;
  background: transparent !important;
}
.app-menu >>> .el-submenu .el-menu-item.is-active {
  background: rgba(74,144,226,0.18) !important;
}
.app-menu::-webkit-scrollbar { width: 4px; }
.app-menu::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.18); border-radius: 4px; }

/* ===== Header (浅色玻璃) ===== */
.app-header {
  height: 56px !important;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow: 0 2px 10px rgba(0, 21, 41, 0.04);
}
.app-page-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 16px; font-weight: 500; color: #1f2d3d;
}
.app-page-title i { color: #4a90e2; font-size: 18px; }

.app-user { cursor: pointer; }
.app-user-inner {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 10px;
  border-radius: 20px;
  background: rgba(74, 144, 226, 0.08);
  transition: background .2s;
}
.app-user-inner:hover { background: rgba(74, 144, 226, 0.16); }
.app-user-avatar {
  width: 26px; height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 14px;
}
.app-user-name { font-size: 13px; color: #1f2d3d; }

/* ===== Main 内容区 ===== */
.app-main {
  padding: 16px;
  background: transparent;
}
</style>
