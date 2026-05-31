<script setup lang="ts">
</script>

<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <span class="footer-text">零代码应用生成平台 by</span>
      <a href="http://fengcode.com" target="_blank" rel="noopener noreferrer" class="footer-link">FengYe</a>
    </div>
  </a-layout-footer>
</template>

<style scoped>
.global-footer {
  text-align: center;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 24px 32px;
  flex-shrink: 0;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
}

.footer-text {
  color: #666;
}

.footer-link {
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.3s ease;
}

.footer-link:hover {
  opacity: 0.8;
}
</style>