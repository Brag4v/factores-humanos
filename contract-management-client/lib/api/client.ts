import axios from 'axios'
import { API_BASE_URL } from '@/constants'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const message = error.response.data?.message ?? 'An unexpected error occurred'
      return Promise.reject(new Error(message))
    }
    if (error.request) {
      return Promise.reject(new Error('Could not connect to the server. Please check your connection.'))
    }
    return Promise.reject(error)
  }
)
