<template>
  <div class="logo-container" :class="{ 'animate-float': animate }">
    <svg viewBox="0 0 200 200" class="logo-svg">
      <defs>
        <linearGradient :id="gradientId" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#D32F2F;stop-opacity:1" />
          <stop offset="100%" style="stop-color:#FF7043;stop-opacity:1" />
        </linearGradient>
        <filter :id="shadowId" x="-50%" y="-50%" width="200%" height="200%">
          <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="#D32F2F" flood-opacity="0.3" />
        </filter>
      </defs>
      
      <g :filter="`url(#${shadowId})`" class="maple-leaf">
        <path 
          d="M100 30 
             L120 50 L145 45 L135 70 L160 80 L140 100 L160 120 
             L135 130 L145 155 L120 150 L100 170 
             L80 150 L55 155 L65 130 L40 120 L60 100 
             L40 80 L65 70 L55 45 L80 50 Z" 
          :fill="`url(#${gradientId})`"
          stroke="#C62828"
          stroke-width="2"
        />
        <path 
          d="M100 50 L100 150" 
          stroke="#FFEBEE"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path 
          d="M100 70 L85 55 M100 90 L75 75 M100 110 L75 125 M100 130 L85 145" 
          stroke="#FFEBEE"
          stroke-width="1.5"
          stroke-linecap="round"
        />
        <path 
          d="M100 70 L115 55 M100 90 L125 75 M100 110 L125 125 M100 130 L115 145" 
          stroke="#FFEBEE"
          stroke-width="1.5"
          stroke-linecap="round"
        />
      </g>
      
      <g class="speed-lines speed-lines-left">
        <line x1="30" y1="100" x2="50" y2="100" stroke="#FF7043" stroke-width="3" stroke-linecap="round" opacity="0.8" />
        <line x1="25" y1="80" x2="40" y2="85" stroke="#FF7043" stroke-width="2" stroke-linecap="round" opacity="0.6" />
        <line x1="25" y1="120" x2="40" y2="115" stroke="#FF7043" stroke-width="2" stroke-linecap="round" opacity="0.6" />
      </g>
      
      <g class="speed-lines speed-lines-right">
        <line x1="150" y1="100" x2="170" y2="100" stroke="#FF7043" stroke-width="3" stroke-linecap="round" opacity="0.8" />
        <line x1="160" y1="80" x2="175" y2="85" stroke="#FF7043" stroke-width="2" stroke-linecap="round" opacity="0.6" />
        <line x1="160" y1="120" x2="175" y2="115" stroke="#FF7043" stroke-width="2" stroke-linecap="round" opacity="0.6" />
      </g>
    </svg>
    
    <div class="logo-text">
      <span class="logo-name">YeCode</span>
      <span class="logo-tagline">零代码平台</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  animate?: boolean
}>()

const uniqueId = Math.random().toString(36).substr(2, 9)
const gradientId = ref(`mapleGradient-${uniqueId}`)
const shadowId = ref(`shadow-${uniqueId}`)
</script>

<style scoped>
.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.logo-svg {
  width: 120px;
  height: 120px;
}

.maple-leaf {
  transform-origin: center;
  animation: pulse 3s ease-in-out infinite;
}

.speed-lines-left {
  animation: slide-left 2s ease-in-out infinite;
}

.speed-lines-right {
  animation: slide-right 2s ease-in-out infinite;
}

.logo-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.logo-name {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.logo-tagline {
  font-size: 0.875rem;
  color: #666;
  font-weight: 500;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes slide-left {
  0%, 100% {
    transform: translateX(0);
    opacity: 0;
  }
  50% {
    transform: translateX(5px);
    opacity: 1;
  }
}

@keyframes slide-right {
  0%, 100% {
    transform: translateX(0);
    opacity: 0;
  }
  50% {
    transform: translateX(-5px);
    opacity: 1;
  }
}

.animate-float {
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>
