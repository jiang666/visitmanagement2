<template>
  <div class="login-container">
    <div class="login-form">
      <div class="login-header">
        <h2>客户拜访管理系统</h2>
        <p>欢迎登录</p>
      </div>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          size="large"
          @keyup.enter="handleLogin"
          class="responsive-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">
            记住密码
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="userStore.loading"
              style="width: 100%"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>默认账户：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const userStore = useUserStore()
const loginFormRef = ref()

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      await userStore.userLogin(loginForm)
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-form {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);

  @media (max-width: 768px) {
    padding: 30px 20px;
    margin: 0 10px;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    color: #333;
    margin-bottom: 10px;
    font-size: 24px;

    @media (max-width: 768px) {
      font-size: 20px;
    }
  }

  p {
    color: #666;
    margin: 0;
    font-size: 16px;

    @media (max-width: 768px) {
      font-size: 14px;
    }
  }
}

.responsive-form {
  @media (max-width: 768px) {
    .el-form-item {
      margin-bottom: 20px;
    }

    .el-input {
      .el-input__wrapper {
        min-height: 44px;
      }
    }

    .el-button {
      height: 44px;
      font-size: 16px;
    }
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;

  p {
    color: #999;
    font-size: 14px;

    @media (max-width: 768px) {
      font-size: 12px;
    }
  }
}
</style>