import { apiClient } from './client'
import type { ApiResponse, PageResponse, NotificationResponse, NotificationStatus, NotificationType } from '@/lib/types'

export const notificationsApi = {
  getAll: async (params?: {
    status?: NotificationStatus
    type?: NotificationType
    contractId?: string
    page?: number
    size?: number
  }): Promise<PageResponse<NotificationResponse>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<NotificationResponse>>>('/api/v1/notifications', { params })
    return res.data.data
  },

  getById: async (id: string): Promise<NotificationResponse> => {
    const res = await apiClient.get<ApiResponse<NotificationResponse>>(`/api/v1/notifications/${id}`)
    return res.data.data
  },

  getByContract: async (contractId: string): Promise<NotificationResponse[]> => {
    const res = await apiClient.get<ApiResponse<NotificationResponse[]>>(`/api/v1/notifications/contract/${contractId}`)
    return res.data.data
  },

  getPending: async (): Promise<NotificationResponse[]> => {
    const res = await apiClient.get<ApiResponse<NotificationResponse[]>>('/api/v1/notifications/pending')
    return res.data.data
  },

  cancel: async (id: string): Promise<NotificationResponse> => {
    const res = await apiClient.put<ApiResponse<NotificationResponse>>(`/api/v1/notifications/${id}/cancel`)
    return res.data.data
  },
}
