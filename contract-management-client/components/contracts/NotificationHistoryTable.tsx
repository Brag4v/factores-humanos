'use client'

import { useState } from 'react'
import { format } from 'date-fns'
import { toast } from 'sonner'
import { Bell } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { NotificationStatusBadge } from '@/components/shared/StatusBadge'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { useContractNotifications, useCancelNotification } from '@/lib/hooks/useNotifications'
import { NOTIFICATION_TYPE_LABELS } from '@/constants'

interface NotificationHistoryTableProps {
  contractId: string
}

export function NotificationHistoryTable({ contractId }: NotificationHistoryTableProps) {
  const { data: notifications, isLoading } = useContractNotifications(contractId)
  const cancelNotification = useCancelNotification(contractId)
  const [cancelTargetId, setCancelTargetId] = useState<string | null>(null)

  if (isLoading) {
    return (
      <div className="space-y-2">
        {[...Array(3)].map((_, i) => (
          <Skeleton key={i} className="h-10 w-full rounded-md" />
        ))}
      </div>
    )
  }

  if (!notifications?.length) {
    return (
      <EmptyState
        icon={Bell}
        title="No notifications"
        description="No notifications have been sent for this contract."
      />
    )
  }

  return (
    <>
      <div className="rounded-md border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">Type</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">Recipient</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">Status</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">Scheduled</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">Sent At</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y">
            {notifications.map((n) => (
              <tr key={n.id} className="hover:bg-muted/30 transition-colors">
                <td className="px-4 py-3 font-medium">
                  {NOTIFICATION_TYPE_LABELS[n.notificationType]}
                </td>
                <td className="px-4 py-3">
                  <div className="text-sm">{n.recipientName}</div>
                  <div className="text-xs text-muted-foreground">{n.recipientEmail}</div>
                </td>
                <td className="px-4 py-3">
                  <NotificationStatusBadge status={n.status} />
                </td>
                <td className="px-4 py-3 text-muted-foreground whitespace-nowrap">
                  {format(new Date(n.scheduledAt), 'MMM d, yyyy')}
                </td>
                <td className="px-4 py-3 text-muted-foreground whitespace-nowrap">
                  {n.sentAt ? format(new Date(n.sentAt), 'MMM d, yyyy') : '—'}
                </td>
                <td className="px-4 py-3 text-right">
                  {n.status === 'PENDING' && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-red-600 hover:text-red-700"
                      onClick={() => setCancelTargetId(n.id)}
                    >
                      Cancel
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ConfirmDialog
        open={!!cancelTargetId}
        onOpenChange={(open) => { if (!open) setCancelTargetId(null) }}
        title="Cancel notification"
        description="This scheduled notification will not be sent. This action cannot be undone."
        confirmLabel="Cancel notification"
        variant="destructive"
        isLoading={cancelNotification.isPending}
        onConfirm={async () => {
          if (!cancelTargetId) return
          await cancelNotification.mutateAsync(cancelTargetId, {
            onSuccess: () => {
              toast.success('Notification cancelled.')
              setCancelTargetId(null)
            },
            onError: (err: unknown) => toast.error((err as Error)?.message ?? 'Failed to cancel notification.'),
          })
        }}
      />
    </>
  )
}
