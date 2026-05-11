import type {
  CollaboratorStatus,
  ContractStatus,
  ContractType,
  PerformanceCategory,
  NotificationStatus,
  NotificationType,
} from '@/lib/types'

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export const RENEWAL_ELIGIBILITY_THRESHOLD = 3.0
export const DEFAULT_PAGE_SIZE = 20
export const DEFAULT_EXPIRY_WARNING_DAYS = 30

// ─── Labels ───────────────────────────────────────────────────────────────────

export const COLLABORATOR_STATUS_LABELS: Record<CollaboratorStatus, string> = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  ON_HOLD: 'On Hold',
}

export const CONTRACT_STATUS_LABELS: Record<ContractStatus, string> = {
  ACTIVE: 'Active',
  EXPIRED: 'Expired',
  RENEWED: 'Renewed',
  TERMINATED: 'Terminated',
  PENDING: 'Pending',
}

export const CONTRACT_TYPE_LABELS: Record<ContractType, string> = {
  FULL_TIME: 'Full Time',
  PART_TIME: 'Part Time',
  CONTRACTOR: 'Contractor',
  TEMPORARY: 'Temporary',
}

export const PERFORMANCE_CATEGORY_LABELS: Record<PerformanceCategory, string> = {
  EXCEEDS_EXPECTATIONS: 'Exceeds Expectations',
  MEETS_EXPECTATIONS: 'Meets Expectations',
  BELOW_EXPECTATIONS: 'Below Expectations',
  NEEDS_IMPROVEMENT: 'Needs Improvement',
}

export const NOTIFICATION_STATUS_LABELS: Record<NotificationStatus, string> = {
  PENDING: 'Pending',
  SENT: 'Sent',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
}

export const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  EXPIRY_WARNING: 'Expiry Warning',
  RENEWAL_REMINDER: 'Renewal Reminder',
  EXPIRED_NOTICE: 'Expired Notice',
}

// ─── Colors ───────────────────────────────────────────────────────────────────

export const CONTRACT_STATUS_COLORS: Record<ContractStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  EXPIRED: 'bg-red-100 text-red-800',
  RENEWED: 'bg-blue-100 text-blue-800',
  TERMINATED: 'bg-gray-100 text-gray-800',
  PENDING: 'bg-yellow-100 text-yellow-800',
}

export const COLLABORATOR_STATUS_COLORS: Record<CollaboratorStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  INACTIVE: 'bg-gray-100 text-gray-800',
  ON_HOLD: 'bg-yellow-100 text-yellow-800',
}

export const NOTIFICATION_STATUS_COLORS: Record<NotificationStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  SENT: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
}
