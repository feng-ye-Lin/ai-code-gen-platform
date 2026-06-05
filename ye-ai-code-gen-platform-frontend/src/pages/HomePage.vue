<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 提示词输入
const promptInput = ref('')
const loadingCreate = ref(false)

// 快捷提示词
const quickPrompts = [
  {
    icon: '📝',
    title: '个人博客',
    prompt: '帮我创建一个个人博客网站，包含首页展示最新文章列表、文章详情页、关于我页面和标签分类功能。首页采用卡片式布局展示文章，支持按时间倒序排列，点击文章卡片进入详情页阅读全文。整体风格简洁现代，配色使用深蓝和白色为主，响应式设计适配手机和电脑端。',
  },
  {
    icon: '🛒',
    title: '电商落地页',
    prompt: '帮我创建一个电商产品落地页，包含顶部导航栏、轮播横幅广告区、热门商品推荐网格、限时秒杀倒计时模块、用户评价展示区和底部页脚。商品卡片包含图片、名称、价格和购买按钮。整体采用红白配色，突出促销氛围，页面滚动时导航栏固定在顶部。',
  },
  {
    icon: '💼',
    title: '作品集展示',
    prompt: '帮我创建一个个人作品集展示网站，包含顶部个人介绍区域、作品展示网格、技能标签云和联系方式区域。作品展示采用瀑布流布局，每个作品卡片包含封面图、标题和简短描述，hover 时展示详情遮罩。整体风格为暗色系科技感，使用深灰背景搭配青色高亮。',
  },
  {
    icon: '📊',
    title: '数据仪表盘',
    prompt: '帮我创建一个数据仪表盘页面，包含顶部统计卡片（用户数、收入、订单量、转化率）、折线图趋势区、柱状图对比区和最近订单列表。统计卡片带有图标和环比增长率显示，图表区域使用 Canvas 绘制。整体采用深色主题，配色以深蓝背景搭配蓝绿渐变图表。',
  },
]

// 我的应用列表
const myApps = ref<API.AppVO[]>([])
const myAppsTotal = ref(0)
const myAppsLoading = ref(false)
const myAppsSearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 精选应用列表
const goodApps = ref<API.AppVO[]>([])
const goodAppsTotal = ref(0)
const goodAppsLoading = ref(false)
const goodAppsSearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 创建应用
const handleCreateApp = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  loadingCreate.value = true
  try {
    const res = await addApp({
      appName: '我的应用',
      initPrompt: promptInput.value.trim(),
      codeGenType: 'web',
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功')
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error(res.data.message ?? '创建失败')
    }
  } catch {
    message.error('创建失败')
  } finally {
    loadingCreate.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage(myAppsSearch)
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myAppsTotal.value = res.data.data.totalRow ?? 0
    }
  } catch {
    console.error('加载我的应用失败', error)
  } finally {
    myAppsLoading.value = false
  }
}

// 加载精选应用
const loadGoodApps = async () => {
  goodAppsLoading.value = true
  try {
    const res = await listGoodAppVoByPage(goodAppsSearch)
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodAppsTotal.value = res.data.data.totalRow ?? 0
    }
  } catch {
    console.error('加载精选应用失败', error)
  } finally {
    goodAppsLoading.value = false
  }
}

// 进入应用对话页面（查看模式）
const goToApp = (appId: string) => {
  router.push(`/app/chat/${appId}?view=1`)
}

// 查看部署的作品
const viewWork = (app: API.AppVO) => {
  if (!app.deployKey) return
  const deployDomain = import.meta.env.VITE_DEPLOY_DOMAIN
  window.open(`${deployDomain}/${app.deployKey}/`, '_blank')
}

// 我的应用分页
const handleMyAppsPageChange = (page: number) => {
  myAppsSearch.pageNum = page
  loadMyApps()
}

// 精选应用分页
const handleGoodAppsPageChange = (page: number) => {
  goodAppsSearch.pageNum = page
  loadGoodApps()
}

onMounted(() => {
  if (loginUserStore.loginUser.id) {
    loadMyApps()
  }
  loadGoodApps()
})
</script>

<template>
  <div class="home-page">
    <!-- 标题区域 -->
    <div class="hero-section">
      <h1 class="hero-title">一句话，生成你的应用</h1>
      <p class="hero-subtitle">与 AI 对话，快速创建和部署你的 Web 应用</p>

      <!-- 输入框区域 -->
      <div class="input-section">
        <a-textarea
          v-model:value="promptInput"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :auto-size="{ minRows: 4, maxRows: 8 }"
          class="prompt-input"
        />
        <div class="input-footer">
          <a-button
            type="primary"
            size="large"
            :loading="loadingCreate"
            @click="handleCreateApp"
            class="create-btn"
          >
            开始创建
          </a-button>
        </div>
      </div>

      <!-- 快捷提示词 -->
      <div class="quick-prompts">
        <div
          v-for="(item, index) in quickPrompts"
          :key="index"
          class="quick-prompt-card"
          @click="promptInput = item.prompt"
        >
          <div class="quick-prompt-icon">{{ item.icon }}</div>
          <div class="quick-prompt-text">{{ item.title }}</div>
        </div>
      </div>
    </div>

    <div class="content-section">
      <!-- 我的应用 -->
      <section class="app-section" v-if="loginUserStore.loginUser.id">
        <div class="section-header">
          <h2 class="section-title">我的应用</h2>
        </div>
        <div v-if="myAppsLoading" class="loading-wrapper">
          <a-spin size="large" />
        </div>
        <div v-else-if="myApps.length > 0" class="app-grid">
          <div
            v-for="app in myApps"
            :key="app.id"
            class="app-card"
          >
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
              <div v-else class="app-cover-placeholder">
                <template-icon />
              </div>
              <div class="app-actions">
                <button
                  v-if="app.deployKey"
                  class="action-btn action-btn-view"
                  @click.stop="viewWork(app)"
                >
                  查看作品
                </button>
                <button
                  class="action-btn action-btn-chat"
                  @click.stop="goToApp(app.id as string)"
                >
                  查看对话
                </button>
              </div>
            </div>
            <div class="app-info">
              <a-avatar :src="app.user?.userAvatar" size="small" class="app-avatar" />
              <div class="app-info-text">
                <h3 class="app-name">{{ app.appName }}</h3>
                <span class="app-author">{{ app.user?.userName || '我' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <a-empty description="暂无应用，输入提示词开始创建吧" />
        </div>
        <div v-if="myAppsTotal > (myAppsSearch.pageSize || 12)" class="pagination-wrapper">
          <a-pagination
            :current="myAppsSearch.pageNum"
            :total="myAppsTotal"
            :page-size="myAppsSearch.pageSize || 12"
            @change="handleMyAppsPageChange"
            :show-less-items="true"
          />
        </div>
      </section>

      <!-- 精选应用 -->
      <section class="app-section">
        <div class="section-header">
          <h2 class="section-title">精选应用</h2>
        </div>
        <div v-if="goodAppsLoading" class="loading-wrapper">
          <a-spin size="large" />
        </div>
        <div v-else-if="goodApps.length > 0" class="app-grid">
          <div
            v-for="app in goodApps"
            :key="app.id"
            class="app-card"
          >
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
              <div v-else class="app-cover-placeholder">
                <template-icon />
              </div>
              <div class="app-actions">
                <button
                  v-if="app.deployKey"
                  class="action-btn action-btn-view"
                  @click.stop="viewWork(app)"
                >
                  查看作品
                </button>
                <button
                  class="action-btn action-btn-chat"
                  @click.stop="goToApp(app.id as string)"
                >
                  查看对话
                </button>
              </div>
            </div>
            <div class="app-info">
              <a-avatar :src="app.user?.userAvatar" size="small" class="app-avatar" />
              <div class="app-info-text">
                <h3 class="app-name">{{ app.appName }}</h3>
                <span class="app-author">{{ app.user?.userName || '未知用户' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <a-empty description="暂无精选应用" />
        </div>
        <div v-if="goodAppsTotal > (goodAppsSearch.pageSize || 12)" class="pagination-wrapper">
          <a-pagination
            :current="goodAppsSearch.pageNum"
            :total="goodAppsTotal"
            :page-size="goodAppsSearch.pageSize || 12"
            @change="handleGoodAppsPageChange"
            :show-less-items="true"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, h } from 'vue'
export default defineComponent({
  components: {
    TemplateIcon: () =>
      h(
        'svg',
        {
          width: '48',
          height: '48',
          viewBox: '0 0 48 48',
          fill: 'none',
        },
        [
          h('rect', {
            width: '48',
            height: '48',
            rx: '8',
            fill: '#f5f5f5',
          }),
          h('path', {
            d: 'M14 14h20v20H14V14z',
            stroke: '#d9d9d9',
            'stroke-width': '2',
            'stroke-linecap': 'round',
            'stroke-linejoin': 'round',
          }),
          h('path', {
            d: 'M14 20h20M20 14v20',
            stroke: '#d9d9d9',
            'stroke-width': '2',
            'stroke-linecap': 'round',
          }),
        ],
      ),
  },
})
</script>

<style scoped>
.home-page {
  min-height: 100%;
  background: linear-gradient(135deg, #FDFBFF 0%, #FFF5F5 25%, #FFFAF5 50%, #F8FAFF 75%, #FFF5F5 100%);
  position: relative;
}

.home-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(ellipse at 15% 15%, rgba(211, 47, 47, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 85% 80%, rgba(255, 112, 67, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 40%, rgba(100, 140, 220, 0.03) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.home-page::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(100, 140, 220, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(100, 140, 220, 0.025) 1px, transparent 1px);
  background-size: 80px 80px;
  pointer-events: none;
  z-index: 0;
}

.hero-section {
  text-align: center;
  padding: 64px 24px 48px;
  width: 100%;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.input-section {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.prompt-input {
  margin-bottom: 16px;
  font-size: 16px;
  border-radius: 12px;
}

.prompt-input :deep(.ant-input) {
  font-size: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
}

.create-btn {
  min-width: 160px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, #d32f2f 0%, #ff7043 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.3);
}

.create-btn:hover {
  box-shadow: 0 6px 16px rgba(211, 47, 47, 0.4);
}

.quick-prompts {
  display: flex;
  gap: 16px;
  max-width: 800px;
  margin: 24px auto 0;
  position: relative;
  z-index: 1;
}

.quick-prompt-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  padding: 16px 12px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.quick-prompt-card:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(211, 47, 47, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.quick-prompt-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.quick-prompt-text {
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.content-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  position: relative;
  z-index: 1;
}

.app-section {
  margin-bottom: 48px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.app-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.app-card:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(211, 47, 47, 0.12);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.app-cover {
  position: relative;
  width: 100%;
  height: 160px;
  background: #f5f5f5;
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.app-card:hover .app-actions {
  opacity: 1;
}

.action-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn-view {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-btn-view:hover {
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn-chat {
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(211, 47, 47, 0.3);
}

.action-btn-chat:hover {
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.4);
}

.app-info {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-avatar {
  flex-shrink: 0;
}

.app-info-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.app-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-author {
  font-size: 13px;
  color: #999;
}

.empty-state {
  padding: 48px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .app-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }

  .quick-prompts {
    flex-wrap: wrap;
  }

  .quick-prompt-card {
    flex: 1 1 calc(50% - 8px);
    min-width: 140px;
  }
}
</style>
