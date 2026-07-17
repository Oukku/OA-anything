<template>
  <div class="login-bg">
    <el-card style="width: 420px">
      <div slot="header" style="text-align: center; font-size: 22px; font-weight: bold">
        OA 办公自动化系统 V2
      </div>
      <el-form :model="form" :rules="rules" ref="form" @submit.native.prevent="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="el-icon-lock" type="password" placeholder="密码" show-password @keyup.enter.native="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">登 录</el-button>
        </el-form-item>
      </el-form>
      <div style="color: #909399; font-size: 12px; text-align: center">默认账号 admin / admin</div>
    </el-card>
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
