<script setup lang="ts">
import {
  ArrowUpOutlined,
  EditOutlined,
  PaperClipOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'

const props = withDefaults(
  defineProps<{
    value: string
    placeholder?: string
    loading?: boolean
    disabled?: boolean
    showEdit?: boolean
    minRows?: number
    maxRows?: number
  }>(),
  {
    placeholder: '',
    loading: false,
    disabled: false,
    showEdit: false,
    minRows: 3,
    maxRows: 5,
  },
)

const emit = defineEmits<{
  'update:value': [value: string]
  submit: []
}>()
</script>

<template>
  <div class="prompt-composer">
    <a-textarea
      :value="props.value"
      :bordered="false"
      :auto-size="{ minRows: props.minRows, maxRows: props.maxRows }"
      :disabled="props.disabled"
      :placeholder="props.placeholder"
      @update:value="emit('update:value', $event)"
      @pressEnter.ctrl="emit('submit')"
    />
    <div class="composer-actions">
      <a-space>
        <a-button shape="round" :disabled="props.disabled">
          <template #icon><PaperClipOutlined /></template>
          上传
        </a-button>
        <a-button v-if="props.showEdit" shape="round" :disabled="props.disabled">
          <template #icon><EditOutlined /></template>
          编辑
        </a-button>
        <a-button shape="round" disabled>
          <template #icon><ThunderboltOutlined /></template>
          优化
        </a-button>
      </a-space>
      <a-button
        type="primary"
        shape="circle"
        size="large"
        :loading="props.loading"
        :disabled="props.disabled"
        @click="emit('submit')"
      >
        <template #icon><ArrowUpOutlined /></template>
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}
</style>
