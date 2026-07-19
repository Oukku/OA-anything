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
        text-color="rgba(255,255,255,0.78)"
        active-text-color="#ffffff"
        router
        class="app-menu"
      >
        <el-menu-item index="/home"><i class="el-icon-house"></i><span>首页</span></el-menu-item>
        <el-menu-item index="/users"><i class="el-icon-user"></i><span>用户管理</span></el-menu-item>
        <el-menu-item index="/employee"><i class="el-icon-user-solid"></i><span>员工管理</span></el-menu-item>
        <el-menu-item index="/dept"><i class="el-icon-s-operation"></i><span>部门管理</span></el-menu-item>
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
  background: var(--biz-bg);
}

/* ===== Sidebar (藏青商务风) ===== */
.app-aside {
  background: #1e3a5f;
  border-right: 1px solid #0f2540;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.app-logo {
  height: 56px;
  display: flex; align-items: center; gap: 10px;
  padding: 0 18px;
  font-size: 14px; font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.app-logo i {
  font-size: 20px;
  color: #60a5fa;
}
.app-menu {
  flex: 1; overflow-y: auto;
  border-right: none !important;
  padding: 8px 0;
}
.app-menu >>> .el-menu-item,
.app-menu >>> .el-submenu__title {
  height: 42px; line-height: 42px;
  margin: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  transition: all .2s;
}
.app-menu >>> .el-menu-item:hover,
.app-menu >>> .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.06) !important;
}
.app-menu >>> .el-menu-item.is-active {
  background: var(--biz-primary) !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(30, 64, 175, 0.32);
}
.app-menu >>> .el-submenu .el-menu-item {
  padding-left: 50px !important;
  background: transparent !important;
}
.app-menu >>> .el-submenu .el-menu-item.is-active {
  background: var(--biz-primary) !important;
  color: #fff !important;
}
.app-menu::-webkit-scrollbar { width: 4px; }
.app-menu::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 4px; }

/* ===== Header (白底商务风) ===== */
.app-header {
  height: 52px !important;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--biz-border);
}
.app-page-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: var(--biz-text-1);
}
.app-page-title i { color: var(--biz-primary); font-size: 16px; }

.app-user { cursor: pointer; }
.app-user-inner {
  display: flex; align-items: center; gap: 8px;
  padding: 5px 10px;
  border-radius: 4px;
  border: 1px solid var(--biz-border);
  background: #fff;
  transition: all .2s;
}
.app-user-inner:hover { background: #f8fafc; border-color: var(--biz-primary-light); }
.app-user-avatar {
  width: 24px; height: 24px;
  border-radius: 50%;
  background: var(--biz-primary);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 13px;
}
.app-user-name { font-size: 13px; color: var(--biz-text-2); }

/* ===== Main 内容区 ===== */
.app-main {
  padding: 16px;
  background: var(--biz-bg);
}
</style>
