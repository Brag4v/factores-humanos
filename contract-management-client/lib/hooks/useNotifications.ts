import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationsApi } from '@/lib/api/notifications'

export const notificationKeys = {
  all: ['notifications'] as const,
  byContract: (contractId: string) => [...notificationKeys.all, 'contract', contractId] as const,
  pending: () => [...notificationKeys.all, 'pending'] as const,
}

export function useContractNotifications(contractId: string) {
  return useQuery({
    queryKey: notificationKeys.byContract(contractId),
    queryFn: () => notificationsApi.getByContract(contractId),
    enabled: !!contractId,
  })
}

export function usePendingNotifications() {
  return useQuery({
    queryKey: notificationKeys.pending(),
    queryFn: () => notificationsApi.getPending(),
  })
}

export function useCancelNotification(contractId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => notificationsApi.cancel(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: notificationKeys.byContract(contractId) }),
  })
}
