import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { contractsApi } from '@/lib/api/contracts'
import type { ContractListParams, ContractRequest, ContractUpdateRequest, RenewalRequest, TerminationRequest } from '@/lib/types'

export const contractKeys = {
  all: ['contracts'] as const,
  lists: () => [...contractKeys.all, 'list'] as const,
  list: (params?: ContractListParams) => [...contractKeys.lists(), params] as const,
  details: () => [...contractKeys.all, 'detail'] as const,
  detail: (id: string) => [...contractKeys.details(), id] as const,
  documents: (id: string) => [...contractKeys.all, 'documents', id] as const,
  expiring: (days: number) => [...contractKeys.all, 'expiring', days] as const,
  byCollaborator: (id: string) => [...contractKeys.all, 'collaborator', id] as const,
}

export function useContracts(params?: ContractListParams) {
  return useQuery({
    queryKey: contractKeys.list(params),
    queryFn: () => contractsApi.getAll(params),
  })
}

export function useContract(id: string) {
  return useQuery({
    queryKey: contractKeys.detail(id),
    queryFn: () => contractsApi.getById(id),
    enabled: !!id,
  })
}

export function useContractByCollaborator(collaboratorId: string) {
  return useQuery({
    queryKey: contractKeys.byCollaborator(collaboratorId),
    queryFn: () => contractsApi.getByCollaborator(collaboratorId),
    enabled: !!collaboratorId,
  })
}

export function useExpiringContracts(days = 30) {
  return useQuery({
    queryKey: contractKeys.expiring(days),
    queryFn: () => contractsApi.getExpiringSoon(days),
  })
}

export function useActiveContractsCount() {
  return useQuery({
    queryKey: contractKeys.list({ status: 'ACTIVE', size: 1 }),
    queryFn: () => contractsApi.getAll({ status: 'ACTIVE', size: 1 }),
    select: (data) => data.totalElements,
  })
}

export function useCreateContract() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: ContractRequest) => contractsApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: contractKeys.all }),
  })
}

export function useUpdateContract(id: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: ContractUpdateRequest) => contractsApi.update(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: contractKeys.detail(id) })
      qc.invalidateQueries({ queryKey: contractKeys.lists() })
    },
  })
}

export function useRenewContract(id: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: RenewalRequest) => contractsApi.renew(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: contractKeys.all }),
  })
}

export function useContractDocuments(id: string) {
  return useQuery({
    queryKey: contractKeys.documents(id),
    queryFn: () => contractsApi.getDocuments(id),
    enabled: !!id,
  })
}

export function useTerminateContract(id: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: TerminationRequest) => contractsApi.terminate(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: contractKeys.detail(id) })
      qc.invalidateQueries({ queryKey: contractKeys.lists() })
    },
  })
}
