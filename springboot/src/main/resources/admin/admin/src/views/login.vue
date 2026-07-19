<template>
  <div class="login-bg">
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
  background: #1e3a5f;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 极简装饰条 - 不要厚重的极光 */
.login-bg::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 4px;
  background: var(--biz-primary);
}

.login-glass-card {
  position: relative;
  z-index: 10;
  width: 400px;
  padding: 36px 32px 24px;
  background: #fff;
  border: 1px solid var(--biz-border);
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-logo {
  width: 48px; height: 48px;
  margin: 0 auto 12px;
  border-radius: 6px;
  background: var(--biz-primary);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 24px;
}
.login-title {
  font-size: 18px; font-weight: 600;
  color: var(--biz-text-1);
  letter-spacing: 1px;
}
.login-subtitle {
  margin-top: 4px;
  font-size: 12px; color: var(--biz-text-3);
  letter-spacing: 0.5px;
}

.login-form >>> .el-input__inner {
  background: #f8fafc;
  border: 1px solid var(--biz-border);
  color: var(--biz-text-1);
  height: 40px;
  border-radius: 4px;
}
.login-form >>> .el-input__inner::placeholder { color: var(--biz-text-4); }
.login-form >>> .el-input__prefix,
.login-form >>> .el-input__suffix { color: var(--biz-text-3); }
.login-form >>> .el-input__icon { line-height: 40px; }
.login-form >>> .el-form-item__error { color: #ef4444; }

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 4px;
  border: none;
  border-radius: 4px;
  background: var(--biz-primary);
}
.login-btn:hover { background: var(--biz-primary-light); }

.login-footer {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--biz-text-4);
}
</style>
