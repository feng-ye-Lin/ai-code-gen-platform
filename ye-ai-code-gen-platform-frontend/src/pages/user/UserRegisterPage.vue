<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/userController'

const router = useRouter()

const formState = ref<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!formState.value.userAccount || !formState.value.userPassword || !formState.value.checkPassword) {
    return
  }
  if (formState.value.userPassword !== formState.value.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  try {
    loading.value = true
    const res = await register(formState.value)
    if (res.data.code === 0) {
      message.success('注册成功')
      router.push('/user/login')
    } else {
      message.error(res.data.message ?? '注册失败')
    }
  } catch (e: unknown) {
    const errorMessage = e instanceof Error ? e.message : '注册失败'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div id="userRegisterPage">
    <div class="container">
      <h2 class="title">YeCode AI 应用生成 - 用户注册</h2>
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
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请再次输入密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" size="large" />
        </a-form-item>
        <div class="tips">
          已有账号？
          <router-link to="/user/login">去登录</router-link>
        </div>
        <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%; background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%); border: none; box-shadow: 0 4px 20px rgba(211, 47, 47, 0.25);" :loading="loading" size="large">注册</a-button>
      </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
#userRegisterPage {
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
