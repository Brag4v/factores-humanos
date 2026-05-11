# Contract Management System — API Report

## Architecture Overview

**Microservices** (Java 17 + Spring Boot 3.2.3 + Spring Cloud 2023.0.0):

| Service | Port | Database | Role |
|---|---|---|---|
| `api-gateway` | 8080 | — | Routing, CORS |
| `collaborator-service` | 8081 | `collaborator_db` (pg:5433) | Employees & performance reviews |
| `contract-service` | 8082 | `contract_db` (pg:5434) | Contract lifecycle |
| `notification-service` | 8083 | `notification_db` (pg:5435) | Email scheduling & delivery |

**Stack:** JPA/Hibernate + Flyway migrations + MapStruct + OpenFeign (inter-service) + SpringDoc (Swagger) + MailHog (dev SMTP)

---

## Entities & Types

### `Collaborator` (collaborator-service)

| Field | Type | Constraints |
|---|---|---|
| `nationalId` | String (PK) | NotBlank, max 20, Unique |
| `employeeCode` | String | NotBlank, max 50, Unique |
| `firstName` / `lastName` | String | NotBlank, max 100 |
| `email` | String | NotBlank, Email, max 255, Unique |
| `phone` | String | max 20, optional |
| `department` | String | max 100, optional |
| `position` | String | NotBlank, max 100 |
| `status` | `CollaboratorStatus` | NotNull |
| `hireDate` | LocalDate | NotNull, PastOrPresent |
| `createdAt` / `updatedAt` | LocalDateTime | auto-auditing |

**Enum `CollaboratorStatus`:** `ACTIVE`, `INACTIVE`, `ON_HOLD`

**Relations:** One Collaborator → Many `PerformanceReview` (cascade, orphan removal)

**Indexes:** `idx_collaborator_email`, `idx_collaborator_employee_code`, `idx_collaborator_status`, `idx_collaborator_department`

---

### `PerformanceReview` (collaborator-service)

| Field | Type | Constraints |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `collaborator` | Collaborator | NotNull, ManyToOne (lazy) |
| `reviewerName` | String | NotBlank, max 200 |
| `reviewerEmail` | String | Email, max 255, optional |
| `reviewPeriodStart` / `reviewPeriodEnd` | LocalDate | NotNull |
| `rating` | BigDecimal | NotNull, min 1.00 max 5.00, precision 3 scale 2 |
| `performanceCategory` | `PerformanceCategory` | NotNull |
| `strengths` / `areasForImprovement` / `comments` | String (TEXT) | optional |
| `isEligibleRenewal` | Boolean | auto-computed: `rating >= 3.0` |
| `createdAt` / `updatedAt` | LocalDateTime | auto-auditing |

**Enum `PerformanceCategory`:** `EXCEEDS_EXPECTATIONS`, `MEETS_EXPECTATIONS`, `BELOW_EXPECTATIONS`, `NEEDS_IMPROVEMENT`

**Custom validation:** `@AssertTrue` ensures `reviewPeriodEnd >= reviewPeriodStart`. `isEligibleRenewal` auto-computed on `@PrePersist` / `@PreUpdate`.

**Indexes:** `idx_review_collaborator`, `idx_review_period_end`, `idx_review_rating`

---

### `Contract` (contract-service)

| Field | Type | Constraints |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `contractNumber` | String | NotBlank, max 50, Unique |
| `collaboratorId` | String | NotBlank, max 20 (logical FK) |
| `contractType` | `ContractType` | NotNull |
| `startDate` / `endDate` | LocalDate | NotNull; endDate > startDate |
| `salary` | BigDecimal | min 0.00, optional, precision 12 scale 2 |
| `currency` | String | max 3, default `"USD"` |
| `termsAndConditions` | String (TEXT) | optional |
| `status` | `ContractStatus` | NotNull |
| `previousContractId` | UUID | self-ref for renewals |
| `renewalCount` | Integer | default 0 |
| `autoRenewal` | Boolean | default false |
| `noticePeriodDays` | Integer | default 30 |
| `createdAt` / `updatedAt` | LocalDateTime | auto-auditing |

**Enum `ContractStatus`:** `ACTIVE`, `EXPIRED`, `RENEWED`, `TERMINATED`, `PENDING`

**Enum `ContractType`:** `FULL_TIME`, `PART_TIME`, `CONTRACTOR`, `TEMPORARY`

**Helper methods:** `isExpired()`, `isExpiringSoon(days)`, `getDaysUntilExpiry()`, `isActive()`

**Indexes:** `idx_contract_collaborator`, `idx_contract_status`, `idx_contract_end_date`, `idx_contract_number`

---

### `ExpiryNotification` (notification-service)

| Field | Type | Constraints |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `contractId` | UUID | NotNull |
| `collaboratorId` | String | NotBlank, max 20 |
| `notificationType` | `NotificationType` | NotNull |
| `recipientEmail` | String | NotBlank, Email, max 255 |
| `recipientName` | String | NotBlank, max 200 |
| `subject` | String | NotBlank, max 500 |
| `messageBody` | String (TEXT) | NotBlank |
| `daysUntilExpiry` | Integer | NotNull |
| `status` | `NotificationStatus` | NotNull |
| `sentAt` | LocalDateTime | set on delivery |
| `failureReason` | String (TEXT) | optional |
| `retryCount` | Integer | default 0 |
| `scheduledAt` | LocalDateTime | NotNull |
| `createdAt` / `updatedAt` | LocalDateTime | auto-auditing |

**Enum `NotificationStatus`:** `PENDING`, `SENT`, `FAILED`, `CANCELLED`

**Enum `NotificationType`:** `EXPIRY_WARNING`, `RENEWAL_REMINDER`, `EXPIRED_NOTICE`

**Helper methods:** `markAsSent()`, `markAsFailed(reason)`, `markAsCancelled()`, `canRetry(maxRetries)`

**Indexes:** `idx_notification_contract`, `idx_notification_collaborator`, `idx_notification_status`, `idx_notification_scheduled`, `idx_notification_type`

---

## Entity Relations

```
Collaborator (nationalId PK)
  └─── 1:N ─── PerformanceReview (collaboratorId FK)

Contract (id UUID PK)
  ├── collaboratorId ──► Collaborator.nationalId  (logical, no DB FK)
  ├── previousContractId ──► Contract.id           (self-ref renewals)
  └── 1:N ──► ExpiryNotification.contractId        (logical)

ExpiryNotification
  ├── contractId ──► Contract.id
  └── collaboratorId ──► Collaborator.nationalId
```

**Inter-service calls (OpenFeign):**
- `contract-service` → `collaborator-service`: validate collaborator exists, get average rating, check renewal eligibility
- `contract-service` → `notification-service`: create/send notifications on renew/terminate

---

## Endpoints

All responses are wrapped in `ApiResponse<T>` (`{ success, message, data, timestamp }`).
Lists use `PageResponse<T>` (`{ content[], page, size, totalElements, totalPages, first, last }`).

### Collaborator Service — `/api/v1/collaborators`

| Method | Path | Request Body | Success Response | Notes |
|---|---|---|---|---|
| `POST` | `/api/v1/collaborators` | `CollaboratorRequest` | 201 `CollaboratorResponse` | Unique email + employeeCode enforced |
| `GET` | `/api/v1/collaborators` | query: `status`, `department`, `search`, pageable | 200 `PageResponse<CollaboratorResponse>` | Filtered paginated list |
| `GET` | `/api/v1/collaborators/{nationalId}` | — | 200 `CollaboratorResponse` | |
| `GET` | `/api/v1/collaborators/{nationalId}/details` | — | 200 `CollaboratorDetailResponse` | Includes performance summary |
| `GET` | `/api/v1/collaborators/{nationalId}/performance-reviews` | — | 200 `List<PerformanceReviewResponse>` | |
| `GET` | `/api/v1/collaborators/{nationalId}/exists` | — | 200 `Boolean` | Used by other services |
| `PUT` | `/api/v1/collaborators/{nationalId}` | `CollaboratorRequest` | 200 `CollaboratorResponse` | |
| `DELETE` | `/api/v1/collaborators/{nationalId}` | — | 204 No Content | Cascades to reviews |

### Performance Review — `/api/v1/performance-reviews`

| Method | Path | Request Body | Success Response | Notes |
|---|---|---|---|---|
| `POST` | `/api/v1/performance-reviews` | `PerformanceReviewRequest` | 201 `PerformanceReviewResponse` | Prevents duplicate reviews in same period |
| `GET` | `/api/v1/performance-reviews/{id}` | — | 200 `PerformanceReviewResponse` | |
| `GET` | `/api/v1/performance-reviews/collaborator/{collaboratorId}` | pageable | 200 `PageResponse<PerformanceReviewResponse>` | |
| `GET` | `/api/v1/performance-reviews/collaborator/{collaboratorId}/latest` | — | 200 `PerformanceReviewResponse` | |
| `GET` | `/api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating` | — | 200 `AverageRatingResponse` | |
| `GET` | `/api/v1/performance-reviews/collaborator/{collaboratorId}/renewal-eligibility` | — | 200 `Boolean` | `avg >= 3.0` |

### Contract Service — `/api/v1/contracts`

| Method | Path | Request Body | Success Response | Notes |
|---|---|---|---|---|
| `POST` | `/api/v1/contracts` | `ContractRequest` | 201 `ContractResponse` | Validates collaborator exists; no duplicate active contract |
| `GET` | `/api/v1/contracts` | query: `status`, `contractType`, `collaboratorId`, pageable | 200 `PageResponse<ContractResponse>` | |
| `GET` | `/api/v1/contracts/{id}` | — | 200 `ContractResponse` | |
| `GET` | `/api/v1/contracts/collaborator/{collaboratorId}` | — | 200 `ContractResponse` | Active contract only |
| `GET` | `/api/v1/contracts/expiring-soon` | query: `days` (default 30) | 200 `List<ExpiringContractResponse>` | Includes collaborator + performance data |
| `PUT` | `/api/v1/contracts/{id}` | `ContractUpdateRequest` | 200 `ContractResponse` | Partial update |
| `PUT` | `/api/v1/contracts/{id}/renew` | `RenewalRequest` | 200 `ContractResponse` | Requires `avg rating >= 3.0`; links to old contract |
| `PUT` | `/api/v1/contracts/{id}/terminate` | `TerminationRequest` | 200 `ContractResponse` | Status → TERMINATED |

### Notification Service — `/api/v1/notifications`

| Method | Path | Request Body | Success Response | Notes |
|---|---|---|---|---|
| `POST` | `/api/v1/notifications` | `NotificationRequest` | 201 `NotificationResponse` | Schedule for later |
| `POST` | `/api/v1/notifications/send` | `SendNotificationRequest` | 200 `NotificationResponse` | Create + send immediately |
| `POST` | `/api/v1/notifications/{id}/send` | — | 200 `NotificationResponse` | Send existing pending notification |
| `GET` | `/api/v1/notifications` | query: `status`, `type`, `contractId`, pageable | 200 `PageResponse<NotificationResponse>` | |
| `GET` | `/api/v1/notifications/{id}` | — | 200 `NotificationResponse` | |
| `GET` | `/api/v1/notifications/contract/{contractId}` | — | 200 `List<NotificationResponse>` | |
| `GET` | `/api/v1/notifications/pending` | — | 200 `List<NotificationResponse>` | |
| `PUT` | `/api/v1/notifications/{id}/cancel` | — | 200 `NotificationResponse` | Only PENDING notifications |

---

## Request Bodies (DTOs)

### `CollaboratorRequest`

```json
{
  "nationalId":    "string (max 20, required)",
  "employeeCode":  "string (max 50, required)",
  "firstName":     "string (max 100, required)",
  "lastName":      "string (max 100, required)",
  "email":         "string (email format, required)",
  "phone":         "string (max 20, optional)",
  "department":    "string (max 100, optional)",
  "position":      "string (max 100, required)",
  "status":        "ACTIVE | INACTIVE | ON_HOLD (optional)",
  "hireDate":      "yyyy-MM-dd (past or present, required)"
}
```

### `PerformanceReviewRequest`

```json
{
  "collaboratorId":       "string (required)",
  "reviewerName":         "string (max 200, required)",
  "reviewerEmail":        "string (email, optional)",
  "reviewPeriodStart":    "yyyy-MM-dd (required)",
  "reviewPeriodEnd":      "yyyy-MM-dd (required, must be >= start)",
  "rating":               "decimal 1.00–5.00 (required)",
  "performanceCategory":  "EXCEEDS_EXPECTATIONS | MEETS_EXPECTATIONS | BELOW_EXPECTATIONS | NEEDS_IMPROVEMENT",
  "strengths":            "string (max 5000, optional)",
  "areasForImprovement":  "string (max 5000, optional)",
  "comments":             "string (max 5000, optional)"
}
```

### `ContractRequest`

```json
{
  "collaboratorId":       "string (required)",
  "contractType":         "FULL_TIME | PART_TIME | CONTRACTOR | TEMPORARY (required)",
  "startDate":            "yyyy-MM-dd (required)",
  "endDate":              "yyyy-MM-dd (future, required, > startDate)",
  "salary":               "decimal >= 0.00 (optional)",
  "currency":             "string max 3 (optional, default USD)",
  "termsAndConditions":   "string max 10000 (optional)",
  "autoRenewal":          "boolean (optional)",
  "noticePeriodDays":     "integer >= 0 (optional)"
}
```

### `ContractUpdateRequest`

Same fields as `ContractRequest` plus `status`, all optional (partial update).

```json
{
  "contractType":         "FULL_TIME | PART_TIME | CONTRACTOR | TEMPORARY (optional)",
  "startDate":            "yyyy-MM-dd (optional)",
  "endDate":              "yyyy-MM-dd (optional)",
  "salary":               "decimal >= 0.00 (optional)",
  "currency":             "string max 3 (optional)",
  "termsAndConditions":   "string max 10000 (optional)",
  "status":               "ACTIVE | EXPIRED | RENEWED | TERMINATED | PENDING (optional)",
  "autoRenewal":          "boolean (optional)",
  "noticePeriodDays":     "integer >= 0 (optional)"
}
```

### `RenewalRequest`

```json
{
  "newEndDate":               "yyyy-MM-dd (future, required)",
  "newSalary":                "decimal >= 0.00 (optional)",
  "newTermsAndConditions":    "string max 10000 (optional)",
  "autoRenewal":              "boolean (optional)",
  "noticePeriodDays":         "integer >= 0 (optional)",
  "renewalNotes":             "string max 1000 (optional)"
}
```

### `TerminationRequest`

```json
{
  "reason":          "string max 1000 (required)",
  "effectiveDate":   "yyyy-MM-dd (required)",
  "additionalNotes": "string max 2000 (optional)"
}
```

### `NotificationRequest`

```json
{
  "contractId":       "UUID (required)",
  "collaboratorId":   "string (required)",
  "notificationType": "EXPIRY_WARNING | RENEWAL_REMINDER | EXPIRED_NOTICE (required)",
  "recipientEmail":   "string (email, required)",
  "recipientName":    "string max 200 (required)",
  "subject":          "string max 500 (required)",
  "messageBody":      "string (required)",
  "daysUntilExpiry":  "integer (required)",
  "scheduledAt":      "ISO datetime (optional)"
}
```

### `SendNotificationRequest`

```json
{
  "contractId":       "UUID (required)",
  "collaboratorId":   "string (required)",
  "notificationType": "EXPIRY_WARNING | RENEWAL_REMINDER | EXPIRED_NOTICE (required)",
  "recipientEmail":   "string (email, required)",
  "recipientName":    "string max 200 (required)",
  "subject":          "string max 500 (required)",
  "messageBody":      "string (required)",
  "daysUntilExpiry":  "integer (required)"
}
```

---

## Response Shapes

### `ApiResponse<T>` (all endpoints)

```json
{
  "success":   true,
  "message":   "string",
  "data":      "<T>",
  "timestamp": "2024-01-01T10:00:00"
}
```

### `PageResponse<T>` (paginated list endpoints)

```json
{
  "content":       [],
  "page":          0,
  "size":          20,
  "totalElements": 100,
  "totalPages":    5,
  "first":         true,
  "last":          false,
  "hasNext":       true,
  "hasPrevious":   false
}
```

### `ErrorResponse` (4xx / 5xx)

```json
{
  "status":    422,
  "message":   "string",
  "errorCode": "RESOURCE_NOT_FOUND | DUPLICATE_RESOURCE | VALIDATION_FAILED | BUSINESS_RULE_VIOLATION | SERVICE_UNAVAILABLE",
  "errors": [
    { "field": "email", "message": "must be valid", "rejectedValue": "bad" }
  ],
  "path":      "/api/v1/contracts",
  "timestamp": "2024-01-01T10:00:00"
}
```

### `CollaboratorResponse`

```json
{
  "nationalId":    "string",
  "employeeCode":  "string",
  "firstName":     "string",
  "lastName":      "string",
  "fullName":      "string (computed)",
  "email":         "string",
  "phone":         "string",
  "department":    "string",
  "position":      "string",
  "status":        "ACTIVE | INACTIVE | ON_HOLD",
  "hireDate":      "yyyy-MM-dd",
  "createdAt":     "ISO datetime",
  "updatedAt":     "ISO datetime"
}
```

### `CollaboratorDetailResponse`

Extends `CollaboratorResponse` with:

```json
{
  "averageRating":        3.75,
  "totalReviews":         4,
  "isEligibleForRenewal": true,
  "latestReview":         { }
}
```

### `PerformanceReviewResponse`

```json
{
  "id":                   "UUID",
  "collaboratorId":       "string",
  "collaboratorName":     "string",
  "reviewerName":         "string",
  "reviewerEmail":        "string",
  "reviewPeriodStart":    "yyyy-MM-dd",
  "reviewPeriodEnd":      "yyyy-MM-dd",
  "rating":               3.80,
  "performanceCategory":  "MEETS_EXPECTATIONS",
  "strengths":            "string",
  "areasForImprovement":  "string",
  "comments":             "string",
  "isEligibleRenewal":    true,
  "createdAt":            "ISO datetime",
  "updatedAt":            "ISO datetime"
}
```

### `AverageRatingResponse`

```json
{
  "collaboratorId":       "string",
  "collaboratorName":     "string",
  "averageRating":        3.75,
  "totalReviews":         4,
  "isEligibleForRenewal": true
}
```

### `ContractResponse`

```json
{
  "id":                 "UUID",
  "contractNumber":     "string",
  "collaboratorId":     "string",
  "contractType":       "FULL_TIME",
  "startDate":          "yyyy-MM-dd",
  "endDate":            "yyyy-MM-dd",
  "salary":             5000.00,
  "currency":           "USD",
  "termsAndConditions": "string",
  "status":             "ACTIVE",
  "previousContractId": "UUID",
  "renewalCount":       1,
  "autoRenewal":        false,
  "noticePeriodDays":   30,
  "createdAt":          "ISO datetime",
  "updatedAt":          "ISO datetime",
  "daysUntilExpiry":    12,
  "isExpiringSoon":     true
}
```

### `ExpiringContractResponse`

```json
{
  "id":                       "UUID",
  "contractNumber":           "string",
  "collaboratorId":           "string",
  "collaboratorName":         "string",
  "collaboratorEmail":        "string",
  "contractType":             "FULL_TIME",
  "startDate":                "yyyy-MM-dd",
  "endDate":                  "yyyy-MM-dd",
  "daysUntilExpiry":          12,
  "salary":                   5000.00,
  "currency":                 "USD",
  "status":                   "ACTIVE",
  "renewalCount":             1,
  "autoRenewal":              false,
  "isEligibleForRenewal":     true,
  "averagePerformanceRating": 3.80
}
```

### `NotificationResponse`

```json
{
  "id":               "UUID",
  "contractId":       "UUID",
  "collaboratorId":   "string",
  "notificationType": "EXPIRY_WARNING",
  "recipientEmail":   "string",
  "recipientName":    "string",
  "subject":          "string",
  "messageBody":      "string",
  "daysUntilExpiry":  12,
  "status":           "PENDING",
  "sentAt":           "ISO datetime",
  "failureReason":    "string",
  "retryCount":       0,
  "scheduledAt":      "ISO datetime",
  "createdAt":        "ISO datetime",
  "updatedAt":        "ISO datetime"
}
```

---

## Validations

### Jakarta Bean Validation (applied via `@Valid` in controllers)

| Constraint | Usage |
|---|---|
| `@NotBlank` | Required strings (null + blank rejected) |
| `@NotNull` | Required non-string fields |
| `@Email` | Valid email format |
| `@Size(max=N)` | String length limit |
| `@DecimalMin` / `@DecimalMax` | BigDecimal range |
| `@Min` / `@Max` | Integer range |
| `@PastOrPresent` | hireDate cannot be in the future |
| `@Future` | endDate on contracts must be in the future |
| `@AssertTrue` | Custom cross-field validations |
| `@Enumerated(EnumType.STRING)` | Persist enum as string in DB |

### Custom & Business Validations

| Rule | Where enforced | Mechanism |
|---|---|---|
| `reviewPeriodEnd >= reviewPeriodStart` | `PerformanceReviewRequest` | `@AssertTrue isValidDateRange()` |
| `contractEndDate > contractStartDate` | `ContractRequest` | `@AssertTrue isValidDateRange()` |
| Unique `email`, `employeeCode`, `nationalId` | DB + service layer | `DuplicateResourceException` (409) |
| No duplicate reviews in same period | `PerformanceReviewService` | `BusinessRuleException` (422) |
| No two active contracts per collaborator | `ContractService` | `BusinessRuleException` (422) |
| Renewal requires `avgRating >= 3.0` | `ContractService.renewContract()` | Feign call + `BusinessRuleException` |
| Cancel only PENDING notifications | `NotificationService` | `BusinessRuleException` |
| `isEligibleRenewal` auto-computed | `PerformanceReview` entity | `@PrePersist` / `@PreUpdate` |

### Exception Hierarchy

```
Exception (Java)
└── BaseException (abstract)
    ├── ResourceNotFoundException  → 404
    ├── DuplicateResourceException → 409
    ├── BusinessRuleException      → 422
    └── ValidationException        → 400
```

All exceptions are handled by a global `@ExceptionHandler` per service, returning `ErrorResponse`.

---

## Use Cases & Business Logic

### Collaborator Service

| Use Case | Description |
|---|---|
| Create collaborator | Validates unique `email`, `employeeCode`, `nationalId`; sets default status |
| Search / filter collaborators | By `status`, `department`, free-text search via JPA Specifications |
| Get collaborator details | Aggregates performance stats: avg rating, total reviews, latest review |
| Delete collaborator | Cascades to all associated performance reviews |
| Check existence | Lightweight endpoint consumed by contract-service via Feign |

### Performance Review Service

| Use Case | Description |
|---|---|
| Submit review | Blocks duplicate reviews for same period; auto-computes `isEligibleRenewal` |
| Get average rating | Computes aggregated rating with review count |
| Check renewal eligibility | Returns `true` if `avgRating >= 3.0` across all reviews |
| Get latest review | Returns most recent review for a collaborator |

### Contract Service

| Use Case | Description |
|---|---|
| Create contract | Verifies collaborator exists (Feign); blocks if active contract already exists |
| Renew contract | Checks eligibility (Feign → avg rating); creates new contract linked via `previousContractId`; increments `renewalCount`; triggers notification |
| Terminate contract | Updates status to `TERMINATED`; triggers notification cancellation |
| Get expiring contracts | Returns enriched list with collaborator name + performance rating from Feign calls |
| Scheduled expiry check | Daily 8 AM: scans contracts expiring in ≤30 days; sends notifications by urgency |
| Scheduled status update | Daily midnight: flips `ACTIVE` contracts past `endDate` to `EXPIRED` |

**Notification type assignment (scheduled expiry check):**

| Days Until Expiry | Notification Type |
|---|---|
| ≤ 0 (already expired) | `EXPIRED_NOTICE` |
| 1–7 | `EXPIRY_WARNING` |
| 8+ | `RENEWAL_REMINDER` |

### Notification Service

| Use Case | Description |
|---|---|
| Schedule notification | Stores with `scheduledAt` for deferred delivery |
| Send immediately | Creates + sends in one call via SMTP |
| Scheduled processor | Every 5 min: sends PENDING notifications where `scheduledAt <= now` |
| Retry scheduler | Every 15 min: retries FAILED notifications up to 3 times |
| Cancel notification | Only allowed on PENDING status; sets status to CANCELLED |

---

## Key Constants

| Constant | Value | Purpose |
|---|---|---|
| `RENEWAL_ELIGIBILITY_THRESHOLD` | `3.0` | Minimum avg rating to allow renewal |
| `DEFAULT_EXPIRY_WARNING_DAYS` | `30` | Days before expiry to start alerting |
| `NOTIFICATION_DAYS_BEFORE_EXPIRY` | `{30, 14, 7, 1}` | Standard notification checkpoints |
| `DEFAULT_NOTICE_PERIOD_DAYS` | `30` | Contract notice period default |
| `MAX_RETRY_COUNT` | `3` | Max notification retry attempts |
| `DEFAULT_PAGE_SIZE` | `20` | Pagination default |
| `MAX_PAGE_SIZE` | `100` | Pagination cap |
| `DEFAULT_SORT_FIELD` | `"createdAt"` | Default sort field |
| `DEFAULT_SORT_DIRECTION` | `"DESC"` | Default sort direction |
| `DEFAULT_CURRENCY` | `"USD"` | Default currency |
| `MIN_RATING` / `MAX_RATING` | `1.0` / `5.0` | Performance rating bounds |

---

## Authentication & Authorization

**Current state: none** — all endpoints are public (MVP). Spring Security is not configured.

The API Gateway injects tracing headers on every request:

| Header | Value |
|---|---|
| `X-Gateway-Source` | `api-gateway` |
| `X-Request-Id` | unique per request |
| `X-Response-Source` | service name (response) |

Audit fields (`createdBy` / `updatedBy`) are present on entities but not actively populated.

**CORS (gateway-level):**
- Allowed origins: `*`
- Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`
- Allowed headers: `*`
- Max age: 3600 seconds

---

## Database Setup

| Service | Database | Port | JDBC URL |
|---|---|---|---|
| Collaborator | `collaborator_db` | 5433 | `jdbc:postgresql://localhost:5433/collaborator_db` |
| Contract | `contract_db` | 5434 | `jdbc:postgresql://localhost:5434/contract_db` |
| Notification | `notification_db` | 5435 | `jdbc:postgresql://localhost:5435/notification_db` |

- **ORM:** JPA / Hibernate (`ddl-auto: validate`)
- **Migrations:** Flyway (`classpath:db/migration`, baseline on migrate)
- **Connection pool:** HikariCP — max 10, min idle 5, connection timeout 20s

---

## Infrastructure (Docker Compose)

| Component | Port(s) | Purpose |
|---|---|---|
| API Gateway | 8080 | Entry point |
| Collaborator Service | 8081 | |
| Contract Service | 8082 | |
| Notification Service | 8083 | |
| PostgreSQL (x3) | 5433, 5434, 5435 | Per-service databases |
| MailHog | 1025 (SMTP), 8025 (UI) | Dev email server |
| PgAdmin | 5050 | DB browser |

---

## API Documentation (Swagger)

| Service | URL |
|---|---|
| Gateway (aggregated) | `http://localhost:8080/swagger-ui.html` |
| Collaborator | `http://localhost:8081/swagger-ui.html` |
| Contract | `http://localhost:8082/swagger-ui.html` |
| Notification | `http://localhost:8083/swagger-ui.html` |

---

## Scheduled Jobs Summary

| Job | Service | Schedule | Action |
|---|---|---|---|
| Expiry check | contract-service | Daily 8 AM | Find contracts expiring soon, send notifications |
| Status updater | contract-service | Daily midnight | Flip ACTIVE expired contracts to EXPIRED |
| Notification processor | notification-service | Every 5 min | Send PENDING notifications whose `scheduledAt <= now` |
| Retry processor | notification-service | Every 15 min | Retry FAILED notifications (max 3 attempts) |
