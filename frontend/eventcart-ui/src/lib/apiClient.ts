import axios, { AxiosError, type AxiosInstance } from 'axios'
import { appConfig } from '../app/config'
import type { ApiError, ApiResponse } from '../types/api'

let accessTokenProvider: () => string | undefined = () => undefined

export function setAccessTokenProvider(provider: () => string | undefined) {
  accessTokenProvider = provider
}

export const apiClient: AxiosInstance = axios.create({
  baseURL: appConfig.apiBaseUrl,
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

function createCorrelationId() {
  return `ui-${globalThis.crypto?.randomUUID?.() ?? Date.now().toString(36)}`
}

apiClient.interceptors.request.use((config) => {
  const token = accessTokenProvider()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Correlation-Id'] = createCorrelationId()
  return config
})

export async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await request
  return response.data.data
}

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>
    return axiosError.response?.data?.message ?? axiosError.message
  }

  return error instanceof Error ? error.message : 'Unexpected error'
}
