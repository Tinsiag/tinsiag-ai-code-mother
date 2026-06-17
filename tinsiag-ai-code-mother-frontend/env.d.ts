/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_DEPLOY_DOMAIN?: string
  readonly VITE_API_BASE_URL?: string
  readonly VITE_APP_DEPLOY_DOMAIN?: string
  readonly VITE_APP_PREVIEW_DOMAIN?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
