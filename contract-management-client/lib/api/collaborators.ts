import { apiClient } from './client'
import type {
  ApiResponse,
  PageResponse,
  CollaboratorResponse,
  CollaboratorDetailResponse,
  CollaboratorRequest,
  PerformanceReviewResponse,
  PerformanceReviewRequest,
  AverageRatingResponse,
  CollaboratorListParams,
} from '@/lib/types'

export const collaboratorsApi = {
  getAll: async (params?: CollaboratorListParams): Promise<PageResponse<CollaboratorResponse>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<CollaboratorResponse>>>('/api/v1/collaborators', { params })
    return res.data.data
  },

  getById: async (nationalId: string): Promise<CollaboratorResponse> => {
    const res = await apiClient.get<ApiResponse<CollaboratorResponse>>(`/api/v1/collaborators/${nationalId}`)
    return res.data.data
  },

  getDetails: async (nationalId: string): Promise<CollaboratorDetailResponse> => {
    const res = await apiClient.get<ApiResponse<CollaboratorDetailResponse>>(`/api/v1/collaborators/${nationalId}/details`)
    return res.data.data
  },

  exists: async (nationalId: string): Promise<boolean> => {
    const res = await apiClient.get<ApiResponse<boolean>>(`/api/v1/collaborators/${nationalId}/exists`)
    return res.data.data
  },

  create: async (data: CollaboratorRequest): Promise<CollaboratorResponse> => {
    const res = await apiClient.post<ApiResponse<CollaboratorResponse>>('/api/v1/collaborators', data)
    return res.data.data
  },

  update: async (nationalId: string, data: CollaboratorRequest): Promise<CollaboratorResponse> => {
    const res = await apiClient.put<ApiResponse<CollaboratorResponse>>(`/api/v1/collaborators/${nationalId}`, data)
    return res.data.data
  },

  delete: async (nationalId: string): Promise<void> => {
    await apiClient.delete(`/api/v1/collaborators/${nationalId}`)
  },

  getReviews: async (nationalId: string): Promise<PerformanceReviewResponse[]> => {
    const res = await apiClient.get<ApiResponse<PerformanceReviewResponse[]>>(
      `/api/v1/collaborators/${nationalId}/performance-reviews`
    )
    return res.data.data
  },
}

export const reviewsApi = {
  getByCollaborator: async (
    collaboratorId: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<PerformanceReviewResponse>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<PerformanceReviewResponse>>>(
      `/api/v1/performance-reviews/collaborator/${collaboratorId}`,
      { params }
    )
    return res.data.data
  },

  getLatest: async (collaboratorId: string): Promise<PerformanceReviewResponse> => {
    const res = await apiClient.get<ApiResponse<PerformanceReviewResponse>>(
      `/api/v1/performance-reviews/collaborator/${collaboratorId}/latest`
    )
    return res.data.data
  },

  getAverageRating: async (collaboratorId: string): Promise<AverageRatingResponse> => {
    const res = await apiClient.get<ApiResponse<AverageRatingResponse>>(
      `/api/v1/performance-reviews/collaborator/${collaboratorId}/average-rating`
    )
    return res.data.data
  },

  getRenewalEligibility: async (collaboratorId: string): Promise<boolean> => {
    const res = await apiClient.get<ApiResponse<boolean>>(
      `/api/v1/performance-reviews/collaborator/${collaboratorId}/renewal-eligibility`
    )
    return res.data.data
  },

  create: async (data: PerformanceReviewRequest): Promise<PerformanceReviewResponse> => {
    const res = await apiClient.post<ApiResponse<PerformanceReviewResponse>>('/api/v1/performance-reviews', data)
    return res.data.data
  },
}
