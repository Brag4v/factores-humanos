import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { collaboratorsApi, reviewsApi } from '@/lib/api/collaborators'
import type { CollaboratorListParams, CollaboratorRequest, PerformanceReviewRequest } from '@/lib/types'

export const collaboratorKeys = {
  all: ['collaborators'] as const,
  lists: () => [...collaboratorKeys.all, 'list'] as const,
  list: (params?: CollaboratorListParams) => [...collaboratorKeys.lists(), params] as const,
  details: () => [...collaboratorKeys.all, 'detail'] as const,
  detail: (id: string) => [...collaboratorKeys.details(), id] as const,
  fullDetail: (id: string) => [...collaboratorKeys.details(), id, 'full'] as const,
  reviews: (id: string) => [...collaboratorKeys.all, id, 'reviews'] as const,
  averageRating: (id: string) => [...collaboratorKeys.all, id, 'average-rating'] as const,
  eligibility: (id: string) => [...collaboratorKeys.all, id, 'eligibility'] as const,
}

export function useCollaborators(params?: CollaboratorListParams) {
  return useQuery({
    queryKey: collaboratorKeys.list(params),
    queryFn: () => collaboratorsApi.getAll(params),
  })
}

export function useCollaborator(nationalId: string) {
  return useQuery({
    queryKey: collaboratorKeys.detail(nationalId),
    queryFn: () => collaboratorsApi.getById(nationalId),
    enabled: !!nationalId,
  })
}

export function useCollaboratorDetails(nationalId: string) {
  return useQuery({
    queryKey: collaboratorKeys.fullDetail(nationalId),
    queryFn: () => collaboratorsApi.getDetails(nationalId),
    enabled: !!nationalId,
  })
}

export function useCollaboratorsCount() {
  return useQuery({
    queryKey: collaboratorKeys.list({ status: 'ACTIVE', size: 1 }),
    queryFn: () => collaboratorsApi.getAll({ status: 'ACTIVE', size: 1 }),
    select: (data) => data.totalElements,
  })
}

export function useCreateCollaborator() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CollaboratorRequest) => collaboratorsApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: collaboratorKeys.all }),
  })
}

export function useUpdateCollaborator(nationalId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CollaboratorRequest) => collaboratorsApi.update(nationalId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: collaboratorKeys.detail(nationalId) })
      qc.invalidateQueries({ queryKey: collaboratorKeys.lists() })
    },
  })
}

export function useDeleteCollaborator() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (nationalId: string) => collaboratorsApi.delete(nationalId),
    onSuccess: () => qc.invalidateQueries({ queryKey: collaboratorKeys.all }),
  })
}

export function useCollaboratorReviews(collaboratorId: string, params?: { page?: number; size?: number }) {
  return useQuery({
    queryKey: collaboratorKeys.reviews(collaboratorId),
    queryFn: () => reviewsApi.getByCollaborator(collaboratorId, params),
    enabled: !!collaboratorId,
  })
}

export function useAverageRating(collaboratorId: string) {
  return useQuery({
    queryKey: collaboratorKeys.averageRating(collaboratorId),
    queryFn: () => reviewsApi.getAverageRating(collaboratorId),
    enabled: !!collaboratorId,
  })
}

export function useRenewalEligibility(collaboratorId: string) {
  return useQuery({
    queryKey: collaboratorKeys.eligibility(collaboratorId),
    queryFn: () => reviewsApi.getRenewalEligibility(collaboratorId),
    enabled: !!collaboratorId,
  })
}

export function useCreateReview() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: PerformanceReviewRequest) => reviewsApi.create(data),
    onSuccess: (_, variables) => {
      qc.invalidateQueries({ queryKey: collaboratorKeys.reviews(variables.collaboratorId) })
      qc.invalidateQueries({ queryKey: collaboratorKeys.averageRating(variables.collaboratorId) })
      qc.invalidateQueries({ queryKey: collaboratorKeys.eligibility(variables.collaboratorId) })
      qc.invalidateQueries({ queryKey: collaboratorKeys.fullDetail(variables.collaboratorId) })
    },
  })
}
