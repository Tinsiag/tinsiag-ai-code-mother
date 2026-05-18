<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'

const route = useRoute()
const isWorkspacePage = computed(() => route.path.startsWith('/app/chat/'))
</script>

<template>
  <a-layout class="basic-layout" :class="{ 'workspace-layout': isWorkspacePage }">
    <GlobalHeader />
    <a-layout-content
      class="basic-layout-content"
      :class="{ 'workspace-content': isWorkspacePage }"
    >
      <router-view />
    </a-layout-content>
    <GlobalFooter v-if="!isWorkspacePage" />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.workspace-layout {
  height: 100vh;
  overflow: hidden;
}

.basic-layout-content {
  display: flex;
  flex-direction: column;
  flex: 1 0 auto;
  min-height: 0;
  background: #f6f8fb;
}

.workspace-content {
  flex: 1 1 auto;
  overflow: hidden;
}
</style>
