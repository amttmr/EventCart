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
    const status = axiosError.response?.status
    const backendMessage = axiosError.response?.data?.message

    if (status === 502) {
      return backendMessage ?? 'API Gateway could not reach the target service. Check gateway and backend service health.'
    }

    if (status === 503) {
      return backendMessage ?? 'A backend service is temporarily unavailable. Check service logs and health endpoints.'
    }

    if (!axiosError.response) {
      return 'API is unreachable. Check whether the API Gateway is running on http://localhost:8080.'
    }

    return backendMessage ?? axiosError.message
  }

  return error instanceof Error ? error.message : 'Unexpected error'
}
