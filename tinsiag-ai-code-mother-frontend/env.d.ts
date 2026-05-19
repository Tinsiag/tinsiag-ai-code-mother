/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_DEPLOY_DOMAIN?: string
  readonly VITE_APP_PREVIEW_DOMAIN?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
