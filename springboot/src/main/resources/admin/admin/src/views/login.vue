<template>
  <div class="login-bg">
    <div class="login-aurora login-aurora-1"></div>
    <div class="login-aurora login-aurora-2"></div>
    <div class="login-aurora login-aurora-3"></div>

    <div class="login-glass-card">
      <div class="login-header">
        <div class="login-logo">
          <i class="el-icon-office-building"></i>
        </div>
        <div class="login-title">OA 办公自动化系统</div>
        <div class="login-subtitle">智能协作 · 知识驱动 · 高效办公</div>
      </div>

      <el-form :model="form" :rules="rules" ref="form" @submit.native.prevent="onSubmit" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="el-icon-lock" type="password" placeholder="密码" show-password @keyup.enter.native="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="onSubmit">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">默认账号 admin / admin</div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      form: { username: 'admin', password: 'admin' },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        this.$store.dispatch('login', this.form).then(res => {
          this.loading = false
          if (res.code === 0) {
            this.$message.success('登录成功')
            this.$router.push('/')
          } else {
            this.$message.error(res.msg || '登录失败')
          }
        }).catch(() => { this.loading = false })
      })
    }
  }
}
</script>

<style scoped>
.login-bg {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #0a2540 0%, #0d3b66 35%, #1e5b8a 70%, #2b7bc4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-aurora {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
  pointer-events: none;
  z-index: 1;
}
.login-aurora-1 {
  width: 520px; height: 520px;
  background: radial-gradient(circle, #4a90e2 0%, transparent 70%);
  top: -120px; left: -80px;
}
.login-aurora-2 {
  width: 460px; height: 460px;
  background: radial-gradient(circle, #6ec5ff 0%, transparent 70%);
  bottom: -140px; right: -60px;
}
.login-aurora-3 {
  width: 360px; height: 360px;
  background: radial-gradient(circle, #2d6da3 0%, transparent 70%);
  top: 40%; right: 22%;
  opacity: 0.35;
}

.login-glass-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 40px 36px 28px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 21, 41, 0.32);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.login-logo {
  width: 56px; height: 56px;
  margin: 0 auto 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 28px;
  box-shadow: 0 6px 18px rgba(74, 144, 226, 0.45);
}
.login-title {
  font-size: 22px; font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
}
.login-subtitle {
  margin-top: 6px;
  font-size: 12px; color: rgba(255, 255, 255, 0.65);
  letter-spacing: 0.5px;
}

.login-form >>> .el-input__inner {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #fff;
  height: 42px;
  border-radius: 8px;
}
.login-form >>> .el-input__inner::placeholder { color: rgba(255, 255, 255, 0.55); }
.login-form >>> .el-input__prefix,
.login-form >>> .el-input__suffix { color: rgba(255, 255, 255, 0.7); }
.login-form >>> .el-input__icon { line-height: 42px; }
.login-form >>> .el-form-item__error { color: #ffd3d3; }

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 4px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #4a90e2 0%, #2b7bc4 100%);
  box-shadow: 0 6px 18px rgba(74, 144, 226, 0.45);
}
.login-btn:hover { opacity: 0.92; }

.login-footer {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}
</style>
