<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader />
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