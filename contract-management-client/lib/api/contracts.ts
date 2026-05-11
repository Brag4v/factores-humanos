import { apiClient } from './client'
import type {
  ApiResponse,
  PageResponse,
  ContractResponse,
  ContractDocumentResponse,
  ExpiringContractResponse,
  ContractRequest,
  ContractUpdateRequest,
  RenewalRequest,
  TerminationRequest,
  ContractListParams,
} from '@/lib/types'

export const contractsApi = {
  getAll: async (params?: ContractListParams): Promise<PageResponse<ContractResponse>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<ContractResponse>>>('/api/v1/contracts', { params })
    return res.data.data
  },

  getById: async (id: string): Promise<ContractResponse> => {
    const res = await apiClient.get<ApiResponse<ContractResponse>>(`/api/v1/contracts/${id}`)
    return res.data.data
  },

  getByCollaborator: async (collaboratorId: string): Promise<ContractResponse[]> => {
    const res = await apiClient.get<ApiResponse<ContractResponse[]>>(`/api/v1/contracts/collaborator/${collaboratorId}`)
    return res.data.data
  },

  getExpiringSoon: async (days = 30): Promise<ExpiringContractResponse[]> => {
    const res = await apiClient.get<ApiResponse<ExpiringContractResponse[]>>('/api/v1/contracts/expiring-soon', {
      params: { days },
    })
    return res.data.data
  },

  create: async (data: ContractRequest): Promise<ContractResponse> => {
    const res = await apiClient.post<ApiResponse<ContractResponse>>('/api/v1/contracts', data)
    return res.data.data
  },

  update: async (id: string, data: ContractUpdateRequest): Promise<ContractResponse> => {
    const res = await apiClient.put<ApiResponse<ContractResponse>>(`/api/v1/contracts/${id}`, data)
    return res.data.data
  },

  renew: async (id: string, data: RenewalRequest): Promise<ContractResponse> => {
    const res = await apiClient.put<ApiResponse<ContractResponse>>(`/api/v1/contracts/${id}/renew`, data)
    return res.data.data
  },

  terminate: async (id: string, data: TerminationRequest): Promise<ContractResponse> => {
    const res = await apiClient.put<ApiResponse<ContractResponse>>(`/api/v1/contracts/${id}/terminate`, data)
    return res.data.data
  },

  getDocuments: async (id: string): Promise<ContractDocumentResponse[]> => {
    const res = await apiClient.get<ApiResponse<ContractDocumentResponse[]>>(`/api/v1/contracts/${id}/documents`)
    return res.data.data
  },
}
