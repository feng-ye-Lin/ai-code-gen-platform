import { createRouter, createWebHistory } from 'vue-router'
import WelcomePage from '../pages/WelcomePage.vue'
import HomePage from '../pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserProfilePage from '@/pages/user/UserProfilePage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import ChatHistoryManagePage from '@/pages/admin/ChatHistoryManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'welcome',
      component: WelcomePage,
    },
    {
      path: '/home',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../pages/AboutView.vue'),
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/user/profile',
      name: '个人中心',
      component: UserProfilePage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/admin/chatHistoryManage',
      name: '对话管理',
      component: ChatHistoryManagePage,
    },
    {
      path: '/app/chat/:appId',
      name: '应用对话',
      component: AppChatPage,
    },
    {
      path: '/app/edit/:appId',
      name: '应用编辑',
      component: AppEditPage,
    },
  ],
})

export default router
