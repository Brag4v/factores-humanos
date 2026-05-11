# Contract Management System — Project Context & FE Planning

---

## 1. Project Context

### What is this?

A **Contract Management System (CMS)** built as a microservices backend (Java / Spring Boot) with a dedicated frontend client. The system centralizes the lifecycle of employee contracts: creation, monitoring, renewal, and termination — all linked to a collaborator (employee) record and enriched with performance reviews that drive renewal eligibility decisions.

### Core Domain Concepts

| Concept | Description |
|---|---|
| **Collaborator** | An employee tracked in the system. Has a unique national ID, employee code, and status. |
| **Contract** | A formal agreement tied to a collaborator. Has a type, salary, dates, and a status lifecycle. |
| **Performance Review** | A periodic evaluation of a collaborator. Drives renewal eligibility (avg rating ≥ 3.0). |
| **Notification** | An automated email alert sent when a contract is expiring soon, renewed, or terminated. |

### Collaborator Statuses
`ACTIVE` · `INACTIVE` · `ON_HOLD`

### Contract Statuses
`PENDING` → `ACTIVE` → `EXPIRED` / `RENEWED` / `TERMINATED`

### Contract Types
`FULL_TIME` · `PART_TIME` · `CONTRACTOR` · `TEMPORARY`

### Renewal Rule
A contract can only be renewed if the collaborator's **average performance rating ≥ 3.0** (scale 1–5). This is enforced by the backend.

### Automated Behaviors (backend-driven)
- Contracts expiring in ≤ 30 days are flagged and notifications are sent automatically
- Notification checkpoints: 30, 14, 7, and 1 day before expiry
- Contracts past their end date are automatically transitioned to `EXPIRED`
- Notification types: `RENEWAL_REMINDER` (8+ days), `EXPIRY_WARNING` (1–7 days), `EXPIRED_NOTICE` (already expired)

### Backend Services

| Service | Port | Responsibility |
|---|---|---|
| API Gateway | 8080 | Centralized routing, CORS |
| collaborator-service | 8081 | Collaborators & performance reviews |
| contract-service | 8082 | Contract lifecycle |
| notification-service | 8083 | Email scheduling & delivery |

**Base API URL:** `http://localhost:8080`

---

## 2. Full Use Case Inventory

### Collaborator Use Cases

| # | Use Case | Actor | Trigger |
|---|---|---|---|
| UC-01 | List all collaborators | HR Manager | Navigates to Collaborators module |
| UC-02 | Search / filter collaborators | HR Manager | Filters by status, department, or free text |
| UC-03 | View collaborator detail | HR Manager | Clicks a collaborator row |
| UC-04 | Create collaborator | HR Manager | Clicks "Add Collaborator" |
| UC-05 | Create collaborator + initial contract (step flow) | HR Manager | Completes step 1 then step 2 |
| UC-06 | Edit collaborator info | HR Manager | Clicks "Edit" on collaborator detail |
| UC-07 | Deactivate / delete collaborator | HR Manager | Clicks "Delete" on collaborator row |
| UC-08 | View collaborator's contracts | HR Manager | Inside collaborator detail view |
| UC-09 | View collaborator's performance reviews | HR Manager | Inside collaborator detail view |
| UC-10 | Check renewal eligibility | System / HR | Displayed on collaborator detail |

### Contract Use Cases

| # | Use Case | Actor | Trigger |
|---|---|---|---|
| UC-11 | List all contracts | HR Manager | Navigates to Contracts module |
| UC-12 | Filter contracts | HR Manager | Filters by status, type, expiry date, collaborator |
| UC-13 | View contract detail | HR Manager | Clicks a contract row |
| UC-14 | Create a contract (standalone) | HR Manager | Clicks "New Contract" in Contracts module |
| UC-15 | Create contract linked to a collaborator | HR Manager | From collaborator step-flow or detail view |
| UC-16 | Edit contract | HR Manager | Clicks "Edit" on contract detail |
| UC-17 | Renew contract | HR Manager | Clicks "Renew" on an expiring/active contract |
| UC-18 | Upload renewal document | HR Manager | During renewal flow (file attachment) |
| UC-19 | Terminate contract | HR Manager | Clicks "Terminate" on a contract |
| UC-20 | View expiring contracts dashboard | HR Manager | Navigates to dashboard / alert banner |
| UC-21 | Download contract file | HR Manager | Clicks download on contract detail |

### Performance Review Use Cases

| # | Use Case | Actor | Trigger |
|---|---|---|---|
| UC-22 | View collaborator performance history | HR Manager | Inside collaborator detail |
| UC-23 | Submit performance review | HR Manager | Clicks "Add Review" on collaborator detail |
| UC-24 | View latest review & average rating | HR Manager | Shown on collaborator detail card |

### Notification Use Cases

| # | Use Case | Actor | Trigger |
|---|---|---|---|
| UC-25 | View notification history for a contract | HR Manager | Inside contract detail |
| UC-26 | Cancel a pending notification | HR Manager | Notification detail action |
| UC-27 | See expiry alert banners | HR Manager | On dashboard or contracts list |

---

## 3. Frontend Planning Strategy

### 3.1 Recommended Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Framework | **Next.js 14+ (App Router)** | SSR/SSG, file-based routing, full-stack ready |
| Language | **TypeScript** | Type safety aligned with structured BE contracts |
| Styling | **Tailwind CSS** | Utility-first, rapid UI iteration |
| Component library | **shadcn/ui** | Accessible, unstyled base with Tailwind integration |
| State / server state | **TanStack Query (React Query)** | Data fetching, caching, pagination, mutations |
| Forms | **React Hook Form + Zod** | Performant forms with schema validation |
| File upload | **react-dropzone** | Drag-and-drop with preview |
| Tables | **TanStack Table** | Headless, flexible, sortable, filterable |
| Date handling | **date-fns** | Lightweight, tree-shakeable |
| Icons | **Lucide React** | Consistent icon set |
| HTTP client | **Axios** | Interceptors for auth headers, error handling |
| Notifications (toast) | **sonner** | Lightweight toast library |

---

### 3.2 Application Structure

```
src/
├── app/
│   ├── (dashboard)/
│   │   ├── layout.tsx                  # Sidebar + topbar shell
│   │   ├── page.tsx                    # Dashboard / home
│   │   ├── collaborators/
│   │   │   ├── page.tsx                # Collaborators list
│   │   │   ├── new/
│   │   │   │   └── page.tsx            # Add collaborator step flow
│   │   │   └── [nationalId]/
│   │   │       ├── page.tsx            # Collaborator detail
│   │   │       └── edit/
│   │   │           └── page.tsx        # Edit collaborator
│   │   └── contracts/
│   │       ├── page.tsx                # Contracts list
│   │       ├── new/
│   │       │   └── page.tsx            # Create contract
│   │       └── [id]/
│   │           ├── page.tsx            # Contract detail
│   │           ├── renew/
│   │           │   └── page.tsx        # Renew contract + file upload
│   │           └── terminate/
│   │               └── page.tsx        # Terminate contract
├── components/
│   ├── ui/                             # shadcn/ui base components
│   ├── collaborators/                  # Collaborator-specific components
│   ├── contracts/                      # Contract-specific components
│   ├── performance-reviews/            # Review-specific components
│   ├── shared/                         # Reusable: PageHeader, DataTable, etc.
│   └── layout/                         # Sidebar, Topbar, Breadcrumbs
├── lib/
│   ├── api/                            # Axios instances + typed API functions
│   ├── hooks/                          # React Query hooks per domain
│   ├── schemas/                        # Zod validation schemas
│   ├── types/                          # TypeScript types matching BE DTOs
│   └── utils/                          # Formatters, date utils, constants
└── constants/
    └── index.ts                        # Enum mirrors, labels, colors
```

---

### 3.3 Views & Screens

---

#### VIEW 1 — Dashboard (Home)

**Route:** `/`

**Purpose:** Overview of the system state at a glance.

**Components:**
- Stats cards: Total active collaborators, active contracts, contracts expiring soon, expired this month
- Expiring contracts table (next 30 days) with urgency color coding
- Quick action buttons: "Add Collaborator", "New Contract"
- Recent notifications list (last 5)

**API calls:**
- `GET /api/v1/contracts/expiring-soon?days=30`
- `GET /api/v1/collaborators?status=ACTIVE`
- `GET /api/v1/notifications/pending`

---

#### VIEW 2 — Collaborators List

**Route:** `/collaborators`

**Purpose:** Full CRUD table for collaborators.

**Components:**
- Search input (debounced, hits `search` query param)
- Filter bar: Status dropdown (`ACTIVE`, `INACTIVE`, `ON_HOLD`), Department select
- Data table columns: National ID, Full Name, Department, Position, Status badge, Hire Date, Actions
- Actions per row: View detail, Edit (inline or modal), Delete (confirmation modal)
- "Add Collaborator" button → navigates to step flow
- Pagination controls
- Status badge with color: green (ACTIVE), gray (INACTIVE), yellow (ON_HOLD)

**API calls:**
- `GET /api/v1/collaborators?status=&department=&search=&page=&size=`
- `DELETE /api/v1/collaborators/{nationalId}`

**UX notes:**
- Delete shows a confirmation dialog: "This will also delete all associated performance reviews."
- Table rows are clickable → navigates to detail view

---

#### VIEW 3 — Add Collaborator (Step Flow)

**Route:** `/collaborators/new`

**Purpose:** Guided multi-step form to create a collaborator and optionally set up their first contract.

**Step 1 — Collaborator Information**

Fields:
- National ID `*`
- Employee Code `*`
- First Name `*` / Last Name `*`
- Email `*`
- Phone
- Department
- Position `*`
- Hire Date `*` (date picker, past or present only)
- Status (default: ACTIVE)

Validation (Zod):
- nationalId: required, max 20
- employeeCode: required, max 50
- email: required, valid email
- hireDate: required, not in the future

**Step 2 — Contract Details (optional, can skip)**

Fields:
- Contract Type `*` (FULL_TIME, PART_TIME, CONTRACTOR, TEMPORARY)
- Start Date `*`
- End Date `*` (must be after start date)
- Salary
- Currency (default USD)
- Notice Period Days
- Auto Renewal toggle
- Terms and Conditions (textarea)

Validation (Zod):
- contractType: required
- endDate > startDate
- salary: optional, ≥ 0

**Step 3 — Review & Confirm**

- Summary card: collaborator info + contract info
- "Back", "Submit" buttons
- On submit: POST collaborator → if contract included, POST contract

**Progress indicator:** horizontal step bar (Step 1 / Step 2 / Step 3)

**API calls:**
- `POST /api/v1/collaborators`
- `POST /api/v1/contracts`

---

#### VIEW 4 — Collaborator Detail

**Route:** `/collaborators/[nationalId]`

**Purpose:** Full profile of a collaborator with all related data.

**Sections:**

**A. Header card**
- Full name, employee code, national ID
- Department, position, hire date
- Status badge
- Actions: "Edit", "Delete", "Add Contract"

**B. Contract history tab**
- Table of all contracts: Number, Type, Start/End, Status, Days Until Expiry
- Expiring contracts highlighted in red/orange
- Action per contract: "View", "Renew", "Terminate"

**C. Performance reviews tab**
- Average rating widget (circular gauge or star display)
- Eligibility badge: "Eligible for Renewal" / "Not Eligible"
- Reviews table: Period, Rating, Category, Reviewer, Date
- "Add Review" button → opens form modal

**D. Performance review form (modal)**

Fields:
- Reviewer Name `*`
- Reviewer Email
- Review Period Start `*` / End `*`
- Rating `*` (1–5 numeric input or star selector)
- Performance Category `*`
- Strengths (textarea)
- Areas for Improvement (textarea)
- Comments (textarea)

**API calls:**
- `GET /api/v1/collaborators/{nationalId}/details`
- `GET /api/v1/contracts/collaborator/{collaboratorId}`
- `GET /api/v1/performance-reviews/collaborator/{collaboratorId}`
- `GET /api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating`
- `POST /api/v1/performance-reviews`

---

#### VIEW 5 — Edit Collaborator

**Route:** `/collaborators/[nationalId]/edit`

**Purpose:** Update collaborator information.

Same fields as Step 1 of the creation flow, pre-populated.

**API calls:**
- `GET /api/v1/collaborators/{nationalId}`
- `PUT /api/v1/collaborators/{nationalId}`

---

#### VIEW 6 — Contracts List

**Route:** `/contracts`

**Purpose:** Full filterable, paginated list of all contracts across all collaborators.

**Components:**
- Filter bar:
  - Status: multi-select (`ACTIVE`, `EXPIRED`, `RENEWED`, `TERMINATED`, `PENDING`)
  - Contract Type: multi-select (`FULL_TIME`, `PART_TIME`, `CONTRACTOR`, `TEMPORARY`)
  - Expiry Date range: date picker (from/to)
  - Collaborator ID: text search
- Data table columns: Contract #, Collaborator, Type, Start Date, End Date, Status, Days Until Expiry, Auto Renewal, Actions
- "Days Until Expiry" column: colored badge (red ≤7, orange ≤30, green otherwise)
- "New Contract" button
- Actions per row: View, Edit, Renew (disabled if not ACTIVE), Terminate (disabled if already terminated/expired)
- Pagination

**API calls:**
- `GET /api/v1/contracts?status=&contractType=&collaboratorId=&page=&size=`

---

#### VIEW 7 — Create Contract (Standalone)

**Route:** `/contracts/new`

**Purpose:** Create a contract outside of the collaborator flow (e.g., for an existing collaborator).

**Form fields:**
- Collaborator ID `*` (searchable dropdown hitting collaborator exists endpoint)
- Contract Type `*`
- Start Date `*`
- End Date `*`
- Salary
- Currency
- Notice Period Days
- Auto Renewal toggle
- Terms and Conditions

**API calls:**
- `GET /api/v1/collaborators/{nationalId}/exists` (validate collaborator on blur)
- `POST /api/v1/contracts`

---

#### VIEW 8 — Contract Detail

**Route:** `/contracts/[id]`

**Purpose:** Full details of a single contract.

**Sections:**

**A. Header card**
- Contract number, status badge
- Collaborator name (link to collaborator detail)
- Contract type, salary, currency
- Start date, end date, days until expiry (highlighted if urgent)
- Auto renewal, notice period
- Renewal count, linked previous contract (if renewal)
- Actions: "Edit", "Renew", "Terminate"

**B. Terms & Conditions**
- Collapsible text block

**C. Renewal document**
- If a file was uploaded during renewal: file name, upload date, download button
- Drag-and-drop upload zone (if no file yet)

**D. Notification history**
- Table: Type, Sent At, Status, Recipient
- "Cancel" action on PENDING notifications

**E. Renewal history**
- Chain of contracts linked via `previousContractId`
- Timeline view: old contract → renewed contract → ...

**API calls:**
- `GET /api/v1/contracts/{id}`
- `GET /api/v1/notifications/contract/{contractId}`
- `PUT /api/v1/notifications/{id}/cancel`

---

#### VIEW 9 — Renew Contract

**Route:** `/contracts/[id]/renew`

**Purpose:** Renew an active contract with a new end date and optional document upload.

**Eligibility check:**
- Fetch collaborator renewal eligibility before rendering form
- If not eligible (avg rating < 3.0): show blocking warning with current rating and "Go to performance reviews" link

**Form fields:**
- New End Date `*` (must be in the future)
- New Salary (optional)
- New Terms and Conditions (optional)
- Auto Renewal toggle
- Notice Period Days
- Renewal Notes
- Renewal Document upload (drag-and-drop, accepts PDF/DOCX/PNG)

**File upload behavior:**
- Preview file name and size after selection
- Upload on submit alongside renewal request
- Display upload progress

**API calls:**
- `GET /api/v1/performance-reviews/collaborator/{collaboratorId}/renewal-eligibility`
- `GET /api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating`
- `PUT /api/v1/contracts/{id}/renew`
- File upload: `POST /api/v1/contracts/{id}/documents` *(to be defined in BE if not yet present)*

---

#### VIEW 10 — Terminate Contract

**Route:** `/contracts/[id]/terminate`

**Purpose:** Formally terminate a contract with a reason and effective date.

**Design:** Simple confirmation-style page (not a modal, since it has form fields).

**Form fields:**
- Reason `*` (textarea, max 1000 chars)
- Effective Date `*` (date picker)
- Additional Notes (textarea)

**Confirmation block:**
- Shows contract number, collaborator name, current end date
- Warning: "This action cannot be undone."

**API calls:**
- `GET /api/v1/contracts/{id}`
- `PUT /api/v1/contracts/{id}/terminate`

---

### 3.4 Shared / Global Components

| Component | Description |
|---|---|
| `DataTable` | Generic TanStack Table wrapper with sorting, pagination, column visibility |
| `PageHeader` | Title + breadcrumb + action button slot |
| `StatusBadge` | Color-coded badge for contract/collaborator status |
| `ExpiryBadge` | Days until expiry with red/orange/green coloring |
| `ConfirmDialog` | Reusable confirmation modal (delete, terminate) |
| `StepIndicator` | Horizontal step progress bar for multi-step flows |
| `FileDropzone` | react-dropzone wrapper with preview and progress |
| `RatingDisplay` | Star or numeric display of performance rating |
| `EligibilityBadge` | "Eligible" / "Not Eligible" renewal badge |
| `EmptyState` | Illustrated empty state for tables/lists |
| `ErrorState` | Error boundary fallback UI |
| `Skeleton loaders` | Per-component loading skeletons |
| `Sidebar` | Navigation: Dashboard, Collaborators, Contracts |
| `Topbar` | Breadcrumbs + user avatar placeholder |

---

### 3.5 Navigation Structure

```
Sidebar
├── Dashboard              /
├── Collaborators          /collaborators
│   └── Add Collaborator   /collaborators/new
└── Contracts              /contracts
    └── New Contract       /contracts/new
```

---

### 3.6 API Layer Design

All API calls go through `http://localhost:8080` (API Gateway).

```
lib/api/
├── client.ts              # Axios instance with base URL + interceptors
├── collaborators.ts       # All collaborator API functions
├── contracts.ts           # All contract API functions
├── performance-reviews.ts # All review API functions
└── notifications.ts       # All notification API functions
```

**React Query hooks:**

```
lib/hooks/
├── useCollaborators.ts          # useCollaboratorsList, useCollaborator, useCreateCollaborator, ...
├── useContracts.ts              # useContractsList, useContract, useCreateContract, useRenewContract, ...
├── usePerformanceReviews.ts     # useReviews, useAverageRating, useRenewalEligibility, ...
└── useNotifications.ts          # useContractNotifications, useCancelNotification, ...
```

---

### 3.7 Type Mirrors (TypeScript)

All types match the backend DTO shapes exactly:

```typescript
// Enums
type CollaboratorStatus = 'ACTIVE' | 'INACTIVE' | 'ON_HOLD'
type ContractStatus = 'ACTIVE' | 'EXPIRED' | 'RENEWED' | 'TERMINATED' | 'PENDING'
type ContractType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACTOR' | 'TEMPORARY'
type PerformanceCategory = 'EXCEEDS_EXPECTATIONS' | 'MEETS_EXPECTATIONS' | 'BELOW_EXPECTATIONS' | 'NEEDS_IMPROVEMENT'
type NotificationType = 'EXPIRY_WARNING' | 'RENEWAL_REMINDER' | 'EXPIRED_NOTICE'
type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'CANCELLED'

// Core response wrapper
interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

// Pagination wrapper
interface PageResponse<T> {
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
```

---

### 3.8 Form Validation Strategy (Zod)

Each form has a corresponding Zod schema in `lib/schemas/`:

```
lib/schemas/
├── collaborator.schema.ts   # CollaboratorFormSchema
├── contract.schema.ts       # ContractFormSchema, RenewalFormSchema, TerminationFormSchema
└── performance-review.schema.ts  # PerformanceReviewFormSchema
```

Schemas enforce the same rules as the backend (max lengths, date ordering, email format, etc.) for instant client-side feedback before the API call.

---

### 3.9 State Management Notes

- **Server state:** 100% managed by TanStack Query (no Redux / Zustand for API data)
- **Form state:** React Hook Form (local to each form component)
- **UI state:** React `useState` (modals open/close, active tab, step index)
- **No global client state store needed** for this scope

---

### 3.10 Key UX Decisions

| Decision | Rationale |
|---|---|
| Step flow for collaborator creation | Reduces cognitive load; contract creation is optional |
| Full page for renew/terminate | These are consequential actions; modals feel too lightweight |
| Inline edit via navigate-to-edit-page | Simpler than inline table editing; better mobile support |
| Expiry color coding everywhere | Urgency needs to be immediately visible across all views |
| Eligibility check before renewal form | Prevents wasted form fill if collaborator is ineligible |
| Confirmation dialog for delete | Destructive action; cascades to reviews |
| Tabs on collaborator detail | Contracts + reviews in one place without separate navigation |

---

### 3.11 View Summary Table

| # | View | Route | Primary Use Cases |
|---|---|---|---|
| 1 | Dashboard | `/` | UC-20, UC-27 |
| 2 | Collaborators List | `/collaborators` | UC-01, UC-02, UC-07 |
| 3 | Add Collaborator (step flow) | `/collaborators/new` | UC-04, UC-05 |
| 4 | Collaborator Detail | `/collaborators/[nationalId]` | UC-03, UC-08, UC-09, UC-10, UC-23 |
| 5 | Edit Collaborator | `/collaborators/[nationalId]/edit` | UC-06 |
| 6 | Contracts List | `/contracts` | UC-11, UC-12 |
| 7 | Create Contract | `/contracts/new` | UC-14, UC-15 |
| 8 | Contract Detail | `/contracts/[id]` | UC-13, UC-21, UC-25, UC-26 |
| 9 | Renew Contract | `/contracts/[id]/renew` | UC-17, UC-18 |
| 10 | Terminate Contract | `/contracts/[id]/terminate` | UC-19 |
