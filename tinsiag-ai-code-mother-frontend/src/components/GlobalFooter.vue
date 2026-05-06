<script setup lang="ts"></script>

<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <span>
        小新乁のAI零代码生成平台 by
        <a href="https://tinsiag.cn" target="_blank" rel="noopener">Tinsiag</a>
      </span>
      <a-divider type="vertical" />
      <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">浙ICP备2025220396号-1</a>
    </div>
  </a-layout-footer>
</template>

<style scoped>
.global-footer {
  text-align: center;
  padding: 16px 24px;
  background: #f0f2f5;
}

.footer-content {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.footer-content a {
  color: rgba(0, 0, 0, 0.45);
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #1890ff;
}

@media (max-width: 576px) {
  .global-footer {
    padding: 12px 16px;
  }

  .footer-content {
    font-size: 12px;
  }
}
</style>
