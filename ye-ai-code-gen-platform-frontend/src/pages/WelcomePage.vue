<template>
  <div class="welcome-page">
    <div class="background-gradient"></div>
    
    <div class="content">
      <div class="logo-wrapper">
        <Logo :animate="true" />
      </div>
      
      <div class="description">
        <h1 class="title">零代码应用生成平台</h1>
        <p class="subtitle">快速构建 · 简洁设计 · 高效开发</p>
      </div>
      
      <div class="features">
        <div class="feature-item">
          <div class="feature-icon">🍁</div>
          <div class="feature-text">
            <h3>自然优雅</h3>
            <p>以枫叶为灵感，简约而不简单</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">⚡</div>
          <div class="feature-text">
            <h3>快速开发</h3>
            <p>拖拽式设计，所见即所得</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">✨</div>
          <div class="feature-text">
            <h3>简洁高效</h3>
            <p>去除冗余，专注核心功能</p>
          </div>
        </div>
      </div>
      
      <button class="enter-button" @click="enterPlatform">
        进入平台
      </button>
    </div>
    
    <div class="decoration-circles">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import Logo from '@/components/Logo.vue'

const router = useRouter()

const enterPlatform = () => {
  router.push('/home')
}
</script>

<style scoped>
.welcome-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF5F5 0%, #FFEBEE 50%, #FFF8E1 100%);
  overflow: hidden;
}

.background-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 112, 67, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(211, 47, 47, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3rem;
  padding: 2rem;
}

.logo-wrapper {
  animation: fadeInUp 1s ease-out;
}

.description {
  text-align: center;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.02em;
}

.subtitle {
  font-size: 1.125rem;
  color: #666;
  margin: 0;
  font-weight: 400;
}

.features {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
  justify-content: center;
  animation: fadeInUp 1s ease-out 0.4s both;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  font-size: 2rem;
}

.feature-text h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.feature-text p {
  margin: 0;
  font-size: 0.875rem;
  color: #666;
}

.enter-button {
  padding: 1rem 3rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
  border: none;
  border-radius: 2rem;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeInUp 1s ease-out 0.6s both;
  box-shadow: 0 4px 20px rgba(211, 47, 47, 0.3);
}

.enter-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 30px rgba(211, 47, 47, 0.4);
}

.enter-button:active {
  transform: scale(0.98);
}

.decoration-circles {
  position: absolute;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: #D32F2F;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: #FF7043;
  bottom: -50px;
  left: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: #FFB74D;
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 1.75rem;
  }
  
  .features {
    flex-direction: column;
    gap: 1rem;
  }
  
  .feature-item {
    width: 100%;
    max-width: 300px;
  }
}
</style>