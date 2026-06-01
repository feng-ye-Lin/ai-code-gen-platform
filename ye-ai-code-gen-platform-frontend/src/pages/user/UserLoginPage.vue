<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formState = ref<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!formState.value.userAccount || !formState.value.userPassword) {
    return
  }
  try {
    loading.value = true
    const res = await userLogin(formState.value)
    if (res.data.code === 0) {
      message.success('登录成功')
      loginUserStore.setLoginUser(res.data.data)
      const urlParams = new URLSearchParams(window.location.search)
      const redirect = urlParams.get('redirect')
      if (redirect) {
        window.location.href = redirect
      } else {
        router.push('/home')
      }
    } else {
      message.error(res.data.message ?? '登录失败')
    }
  } catch (e: unknown) {
    const errorMessage = e instanceof Error ? e.message : '登录失败'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div id="userLoginPage">
    <div class="container">
      <h2 class="title">YeCode AI 应用生成 - 用户登录</h2>
      <div class="desc">不写一行代码，生成完整应用</div>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large" />
        </a-form-item>
        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" size="large" />
        </a-form-item>
        <div class="tips">
          没有账号？
          <router-link to="/user/register">去注册</router-link>
        </div>
        <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%; background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%); border: none; box-shadow: 0 4px 20px rgba(211, 47, 47, 0.25);" :loading="loading" size="large">登录</a-button>
      </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #FFF5F5 0%, #FFEBEE 50%, #FFF8E1 100%);
}

.container {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.title {
  text-align: center;
  margin: 0 0 8px 0;
  padding: 0;
  font-size: 22px;
  font-weight: 700;
  color: #333;
  background: linear-gradient(135deg, #d32f2f 0%, #ff7043 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
  border: none;
}

.desc {
  text-align: center;
  color: #666;
  margin: 0 0 32px 0;
  padding: 0;
  font-size: 14px;
  line-height: 1.6;
}

.tips {
  text-align: right;
  margin: 0 0 24px 0;
  color: #666;
  font-size: 14px;
}

.tips a {
  color: #D32F2F;
  text-decoration: none;
  font-weight: 500;
}

.tips a:hover {
  text-decoration: underline;
}
</style>
