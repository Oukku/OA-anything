<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #001529; color: #fff">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: bold; background: #002140">
        OA 管理系统
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="#001529"
        text-color="rgba(255,255,255,0.85)"
        active-text-color="#1890ff"
        router
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
      <el-header style="background: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08); display: flex; align-items: center; justify-content: space-between">
        <div style="font-size: 18px; font-weight: 500">{{ $route.meta.title || '' }}</div>
        <el-dropdown @command="onCmd">
          <span style="cursor: pointer">
            <i class="el-icon-user-solid"></i>
            {{ userInfo.username || '用户' }}
            <i class="el-icon-arrow-down"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-header>
      <el-main style="padding: 16px; background: #f0f2f5">
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
