<script setup lang="ts">
import GlobalHeader, { type MenuItem } from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import {
  HomeOutlined,
  AppstoreOutlined,
  CodeOutlined,
} from '@ant-design/icons-vue'

const menuItems: MenuItem[] = [
  { key: 'home', label: '首页', icon: HomeOutlined, path: '/' },
  { key: 'apps', label: '应用', icon: AppstoreOutlined, path: '/apps' },
  { key: 'generate', label: '代码生成', icon: CodeOutlined, path: '/generate' },
]
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader :menu-items="menuItems" />
    <a-layout-content class="basic-layout-content">
      <router-view />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.basic-layout-content {
  flex: 1;
  padding: 24px 50px;
  background: #f0f2f5;
}

@media (max-width: 576px) {
  .basic-layout-content {
    padding: 12px 24px;
  }
}
</style>
