<script setup lang="ts">
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  InfoCircleOutlined,
  LinkOutlined,
} from '@ant-design/icons-vue'
import {
  getAppCover,
  getAppTitle,
  getAppUserAvatar,
  getAppUserInitial,
  getAppUserName,
  type LoginUserLike,
} from '@/utils/appDisplay'

const props = withDefaults(
  defineProps<{
    app: API.AppVO
    loginUser?: LoginUserLike
    editable?: boolean
    deletable?: boolean
  }>(),
  {
    editable: false,
    deletable: false,
  },
)

const emit = defineEmits<{
  open: [app: API.AppVO]
  work: [app: API.AppVO]
  edit: [app: API.AppVO]
  delete: [app: API.AppVO]
  detail: [app: API.AppVO]
}>()
</script>

<template>
  <article class="app-card" @click="emit('open', props.app)">
    <div class="cover-wrap">
      <img :src="getAppCover(props.app)" :alt="getAppTitle(props.app)" />
      <div class="card-actions" @click.stop>
        <a-tooltip title="查看对话">
          <a-button type="primary" shape="circle" @click="emit('open', props.app)">
            <template #icon><EyeOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip v-if="props.app.deployKey" title="查看作品">
          <a-button shape="circle" @click="emit('work', props.app)">
            <template #icon><LinkOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip title="应用详情">
          <a-button shape="circle" @click="emit('detail', props.app)">
            <template #icon><InfoCircleOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip v-if="props.editable" title="编辑">
          <a-button shape="circle" @click="emit('edit', props.app)">
            <template #icon><EditOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-popconfirm
          v-if="props.deletable"
          title="确定删除该应用？"
          @confirm="emit('delete', props.app)"
        >
          <a-button danger shape="circle">
            <template #icon><DeleteOutlined /></template>
          </a-button>
        </a-popconfirm>
      </div>
    </div>
    <div class="app-meta">
      <a-avatar :src="getAppUserAvatar(props.app, props.loginUser)">
        {{ getAppUserInitial(props.app, props.loginUser) }}
      </a-avatar>
      <div class="app-meta-text">
        <h3>{{ getAppTitle(props.app) }}</h3>
        <p>{{ getAppUserName(props.app, props.loginUser) }}</p>
      </div>
    </div>
  </article>
</template>

<style scoped>
.app-card {
  cursor: pointer;
}

.cover-wrap {
  position: relative;
  height: 230px;
  overflow: hidden;
  background: #f7f8fa;
  border: 1px solid #eef0f3;
  border-radius: 16px;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: top center;
  transition: transform 0.25s ease;
}

.app-card:hover .cover-wrap img {
  transform: scale(1.02);
}

.card-actions {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.34);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.app-card:hover .card-actions {
  opacity: 1;
  pointer-events: auto;
}

.card-actions :deep(.ant-btn) {
  width: 44px;
  height: 44px;
  font-size: 17px;
}

.app-meta {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  margin-top: 18px;
}

.app-meta :deep(.ant-avatar) {
  width: 42px;
  height: 42px;
  line-height: 42px;
  flex: 0 0 auto;
  background: #18a689;
  color: #fff;
  font-weight: 700;
}

.app-meta-text {
  min-width: 0;
}

.app-meta h3 {
  overflow: hidden;
  margin: 0 0 4px;
  color: #111827;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-meta p {
  overflow: hidden;
  margin: 0;
  color: #667085;
  font-size: 15px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
