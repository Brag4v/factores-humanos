import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'
import {
  COLLABORATOR_STATUS_LABELS,
  CONTRACT_STATUS_LABELS,
  COLLABORATOR_STATUS_COLORS,
  CONTRACT_STATUS_COLORS,
  NOTIFICATION_STATUS_LABELS,
  NOTIFICATION_STATUS_COLORS,
} from '@/constants'
import type { CollaboratorStatus, ContractStatus, NotificationStatus } from '@/lib/types'

interface CollaboratorStatusBadgeProps {
  status: CollaboratorStatus
  className?: string
}

export function CollaboratorStatusBadge({ status, className }: CollaboratorStatusBadgeProps) {
  return (
    <Badge className={cn('border-0 font-medium', COLLABORATOR_STATUS_COLORS[status], className)}>
      {COLLABORATOR_STATUS_LABELS[status]}
    </Badge>
  )
}

interface ContractStatusBadgeProps {
  status: ContractStatus
  className?: string
}

export function ContractStatusBadge({ status, className }: ContractStatusBadgeProps) {
  return (
    <Badge className={cn('border-0 font-medium', CONTRACT_STATUS_COLORS[status], className)}>
      {CONTRACT_STATUS_LABELS[status]}
    </Badge>
  )
}

interface NotificationStatusBadgeProps {
  status: NotificationStatus
  className?: string
}

export function NotificationStatusBadge({ status, className }: NotificationStatusBadgeProps) {
  return (
    <Badge className={cn('border-0 font-medium', NOTIFICATION_STATUS_COLORS[status], className)}>
      {NOTIFICATION_STATUS_LABELS[status]}
    </Badge>
  )
}

interface EligibilityBadgeProps {
  eligible: boolean
  className?: string
}

export function EligibilityBadge({ eligible, className }: EligibilityBadgeProps) {
  return (
    <Badge
      className={cn(
        'border-0 font-medium',
        eligible ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800',
        className
      )}
    >
      {eligible ? 'Eligible for Renewal' : 'Not Eligible'}
    </Badge>
  )
}
