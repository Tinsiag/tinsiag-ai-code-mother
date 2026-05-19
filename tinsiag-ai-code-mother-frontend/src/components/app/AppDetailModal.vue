<script setup lang="ts">
import dayjs from 'dayjs'
import { computed } from 'vue'
import { getAppDeployUrl, getAppPreviewUrl } from '@/config/domain'
import { getAppCover, getAppTitle, getAppUserName, type LoginUserLike } from '@/utils/appDisplay'

const props = defineProps<{
  open: boolean
  app?: API.AppVO
  loginUser?: LoginUserLike
}>()

const emit = defineEmits<{
  'update:open': [open: boolean]
  edit: [app: API.AppVO]
}>()

const previewUrl = computed(() => {
  if (!props.app?.id || !props.app.codeGenType) {
    return ''
  }
  return getAppPreviewUrl(props.app.codeGenType, props.app.id)
})

const deployUrl = computed(() => {
  if (!props.app?.deployKey) {
    return ''
  }
  return getAppDeployUrl(props.app.deployKey)
})

const close = () => emit('update:open', false)
</script>

<template>
  <a-modal
    :open="props.open"
    :title="props.app ? getAppTitle(props.app) : '应用详情'"
    width="720px"
    :footer="null"
    @cancel="close"
  >
    <div v-if="props.app" class="detail-layout">
      <a-image class="detail-cover" :src="getAppCover(props.app)" :alt="getAppTitle(props.app)" />
      <a-descriptions bordered size="small" :column="1">
        <a-descriptions-item label="应用 ID">{{ props.app.id ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建者">
          {{ getAppUserName(props.app, props.loginUser) }}
        </a-descriptions-item>
        <a-descriptions-item label="生成类型">
          {{ props.app.codeGenType || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="优先级">
          {{ props.app.priority ?? 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ props.app.createTime ? dayjs(props.app.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ props.app.updateTime ? dayjs(props.app.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </a-descriptions-item>
        <a-descriptions-item v-if="props.app.initPrompt" label="初始提示词">
          {{ props.app.initPrompt }}
        </a-descriptions-item>
      </a-descriptions>
      <a-space wrap>
        <a-button v-if="previewUrl" :href="previewUrl" target="_blank">预览静态页面</a-button>
        <a-button v-if="deployUrl" :href="deployUrl" target="_blank">打开部署作品</a-button>
        <a-button @click="emit('edit', props.app)">编辑应用</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<style scoped>
.detail-layout {
  display: grid;
  gap: 18px;
}

.detail-cover {
  width: 100%;
  max-height: 280px;
  overflow: hidden;
  object-fit: cover;
  object-position: top center;
  border: 1px solid #eef0f3;
  border-radius: 10px;
}
</style>
