// ─── Enums ───────────────────────────────────────────────────────────────────

export type CollaboratorStatus = 'ACTIVE' | 'INACTIVE' | 'ON_HOLD'
export type ContractStatus = 'ACTIVE' | 'EXPIRED' | 'RENEWED' | 'TERMINATED' | 'PENDING'
export type ContractType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACTOR' | 'TEMPORARY'
export type PerformanceCategory =
  | 'EXCEEDS_EXPECTATIONS'
  | 'MEETS_EXPECTATIONS'
  | 'BELOW_EXPECTATIONS'
  | 'NEEDS_IMPROVEMENT'
export type NotificationType = 'EXPIRY_WARNING' | 'RENEWAL_REMINDER' | 'EXPIRED_NOTICE'
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'CANCELLED'

// ─── API wrappers ─────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
}

export interface FieldError {
  field: string
  message: string
  rejectedValue: unknown
}

export interface ErrorResponse {
  status: number
  message: string
  errorCode: string
  errors: FieldError[]
  path: string
  timestamp: string
}

// ─── Collaborator ─────────────────────────────────────────────────────────────

export interface CollaboratorResponse {
  nationalId: string
  employeeCode: string
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone: string | null
  department: string | null
  position: string
  status: CollaboratorStatus
  hireDate: string
  createdAt: string
  updatedAt: string
}

export interface CollaboratorDetailResponse extends CollaboratorResponse {
  averageRating: number | null
  totalReviews: number
  isEligibleForRenewal: boolean
  latestReview: PerformanceReviewResponse | null
}

export interface CollaboratorRequest {
  nationalId: string
  employeeCode?: string
  firstName: string
  lastName: string
  email: string
  phone?: string
  department?: string
  position: string
  status?: CollaboratorStatus
  hireDate: string
}

// ─── Performance Review ───────────────────────────────────────────────────────

export interface PerformanceReviewResponse {
  id: string
  collaboratorId: string
  collaboratorName: string
  reviewerName: string
  reviewerEmail: string | null
  reviewPeriodStart: string
  reviewPeriodEnd: string
  rating: number
  performanceCategory: PerformanceCategory
  strengths: string | null
  areasForImprovement: string | null
  comments: string | null
  isEligibleRenewal: boolean
  createdAt: string
  updatedAt: string
}

export interface AverageRatingResponse {
  collaboratorId: string
  collaboratorName: string
  averageRating: number | null
  totalReviews: number
  isEligibleForRenewal: boolean
}

export interface PerformanceReviewRequest {
  collaboratorId: string
  reviewerName: string
  reviewerEmail?: string
  reviewPeriodStart: string
  reviewPeriodEnd: string
  rating: number
  performanceCategory: PerformanceCategory
  strengths?: string
  areasForImprovement?: string
  comments?: string
}

// ─── Contract ─────────────────────────────────────────────────────────────────

export interface ContractResponse {
  id: string
  contractNumber: string
  collaboratorId: string
  contractType: ContractType
  startDate: string
  endDate: string
  salary: number | null
  currency: string
  termsAndConditions: string | null
  status: ContractStatus
  previousContractId: string | null
  renewalCount: number
  autoRenewal: boolean
  noticePeriodDays: number
  createdAt: string
  updatedAt: string
  daysUntilExpiry: number
  isExpiringSoon: boolean
}

export interface ContractDocumentResponse {
  id: string
  contractId: string
  fileName: string
  fileType: string
  fileSize: number
  uploadedAt: string
}

export interface ExpiringContractResponse {
  id: string
  contractNumber: string
  collaboratorId: string
  collaboratorName: string
  collaboratorEmail: string
  contractType: ContractType
  startDate: string
  endDate: string
  daysUntilExpiry: number
  salary: number | null
  currency: string
  status: ContractStatus
  renewalCount: number
  autoRenewal: boolean
  isEligibleForRenewal: boolean
  averagePerformanceRating: number | null
}

export interface ContractRequest {
  collaboratorId: string
  contractType: ContractType
  startDate: string
  endDate: string
  salary?: number
  currency?: string
  termsAndConditions?: string
  autoRenewal?: boolean
  noticePeriodDays?: number
}

export interface ContractUpdateRequest {
  contractType?: ContractType
  startDate?: string
  endDate?: string
  salary?: number
  currency?: string
  termsAndConditions?: string
  status?: ContractStatus
  autoRenewal?: boolean
  noticePeriodDays?: number
}

export interface RenewalRequest {
  newEndDate: string
  newSalary?: number
  newTermsAndConditions?: string
  autoRenewal?: boolean
  noticePeriodDays?: number
  renewalNotes?: string
}

export interface TerminationRequest {
  reason: string
  effectiveDate: string
  additionalNotes?: string
}

// ─── Notification ─────────────────────────────────────────────────────────────

export interface NotificationResponse {
  id: string
  contractId: string
  collaboratorId: string
  notificationType: NotificationType
  recipientEmail: string
  recipientName: string
  subject: string
  messageBody: string
  daysUntilExpiry: number
  status: NotificationStatus
  sentAt: string | null
  failureReason: string | null
  retryCount: number
  scheduledAt: string
  createdAt: string
  updatedAt: string
}

// ─── Query params ─────────────────────────────────────────────────────────────

export interface PaginationParams {
  page?: number
  size?: number
  sort?: string
}

export interface CollaboratorListParams extends PaginationParams {
  status?: CollaboratorStatus
  department?: string
  search?: string
}

export interface ContractListParams extends PaginationParams {
  status?: ContractStatus
  contractType?: ContractType
  collaboratorId?: string
}
