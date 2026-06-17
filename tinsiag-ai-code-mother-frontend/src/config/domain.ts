const trimTrailingSlashes = (value: string) => value.replace(/\/+$/, '')
const trimLeadingSlashes = (value: string) => value.replace(/^\/+/, '')

const joinUrl = (baseUrl: string, path: string) => {
  const normalizedBaseUrl = trimTrailingSlashes(baseUrl.trim())
  const normalizedPath = trimLeadingSlashes(path)

  return normalizedPath ? `${normalizedBaseUrl}/${normalizedPath}` : normalizedBaseUrl
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'
const appDeployDomain =
  import.meta.env.VITE_DEPLOY_DOMAIN || import.meta.env.VITE_APP_DEPLOY_DOMAIN || 'http://localhost'
const appPreviewDomain = import.meta.env.VITE_APP_PREVIEW_DOMAIN || joinUrl(apiBaseUrl, 'static')

export const getAppDeployUrl = (deployKey: string) => joinUrl(appDeployDomain, deployKey)

/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型配置
 */
export const CODE_GEN_TYPE_CONFIG = {
  [CodeGenTypeEnum.HTML]: {
    label: '原生 HTML 模式',
    value: CodeGenTypeEnum.HTML,
  },
  [CodeGenTypeEnum.MULTI_FILE]: {
    label: '原生多文件模式',
    value: CodeGenTypeEnum.MULTI_FILE,
  },
  [CodeGenTypeEnum.VUE_PROJECT]: {
    label: 'Vue 项目模式',
    value: CodeGenTypeEnum.VUE_PROJECT,
  },
}

export const getCodeGenTypeLabel = (codeGenType?: string) =>
  CODE_GEN_TYPE_CONFIG[codeGenType as CodeGenTypeEnum]?.label || codeGenType || '-'

export const getAppPreviewUrl = (codeGenType: string, appId: string) => {
  const basePath = `${codeGenType}_${appId}/`
  // 如果是 Vue 项目，浏览地址需要添加 dist 后缀
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return joinUrl(appPreviewDomain, `${basePath}dist/index.html`)
  }
  return joinUrl(appPreviewDomain, basePath)
}
