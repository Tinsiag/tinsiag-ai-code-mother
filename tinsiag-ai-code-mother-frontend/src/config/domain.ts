const trimTrailingSlashes = (value: string) => value.replace(/\/+$/, '')
const trimLeadingSlashes = (value: string) => value.replace(/^\/+/, '')

const joinUrl = (baseUrl: string, path: string) => {
  const normalizedBaseUrl = trimTrailingSlashes(baseUrl.trim())
  const normalizedPath = trimLeadingSlashes(path)

  return normalizedPath ? `${normalizedBaseUrl}/${normalizedPath}` : normalizedBaseUrl
}

const appDeployDomain = import.meta.env.VITE_APP_DEPLOY_DOMAIN || 'http://localhost'
const appPreviewDomain = import.meta.env.VITE_APP_PREVIEW_DOMAIN || 'http://localhost:8123/api/static'

export const getAppDeployUrl = (deployKey: string) => joinUrl(appDeployDomain, deployKey)

export const getAppPreviewUrl = (codeGenType: string, appId: string) =>
  joinUrl(appPreviewDomain, `${codeGenType}_${appId}/`)
