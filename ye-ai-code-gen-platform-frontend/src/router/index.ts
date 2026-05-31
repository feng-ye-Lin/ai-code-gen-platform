import { createRouter, createWebHistory } from 'vue-router'
import WelcomePage from '../pages/WelcomePage.vue'
import HomePage from '../pages/HomePage.vue'

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
  ],
})

export default router
