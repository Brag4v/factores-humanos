# Contract Management Client — Frontend Specifications

## Overview

Frontend application for the Contract Management System. Consumes the microservices backend via the API Gateway at `http://localhost:8080`.

**Stack:** Next.js 14 (App Router) · TypeScript · Tailwind CSS · shadcn/ui · TanStack Query · React Hook Form · Zod · TanStack Table · react-dropzone · Axios · date-fns · Lucide React · sonner

---

## View Index

| # | View | Route | Use Cases |
|---|---|---|---|
| V-01 | Dashboard | `/` | UC-20, UC-27 |
| V-02 | Collaborators List | `/collaborators` | UC-01, UC-02, UC-07 |
| V-03 | Add Collaborator (Step Flow) | `/collaborators/new` | UC-04, UC-05 |
| V-04 | Collaborator Detail | `/collaborators/[nationalId]` | UC-03, UC-08, UC-09, UC-10, UC-23, UC-24 |
| V-05 | Edit Collaborator | `/collaborators/[nationalId]/edit` | UC-06 |
| V-06 | Contracts List | `/contracts` | UC-11, UC-12 |
| V-07 | Create Contract | `/contracts/new` | UC-14, UC-15 |
| V-08 | Contract Detail | `/contracts/[id]` | UC-13, UC-21, UC-25, UC-26 |
| V-09 | Renew Contract | `/contracts/[id]/renew` | UC-17, UC-18 |
| V-10 | Terminate Contract | `/contracts/[id]/terminate` | UC-19 |

---

---

## V-01 · Dashboard

**Route:** `/`
**File:** `app/(dashboard)/page.tsx`

### Use Cases

#### UC-20 · View expiring contracts dashboard
- **Trigger:** User navigates to the app root
- **Flow:**
  1. On mount, fetch contracts expiring within the next 30 days
  2. Render a table sorted by `daysUntilExpiry` ascending
  3. Apply color coding per row: red (≤ 7 days), orange (≤ 30 days)
  4. Each row links to the contract detail view
  5. Stat card shows total count of expiring contracts
- **API:** `GET /api/v1/contracts/expiring-soon?days=30`
- **Empty state:** "No contracts expiring in the next 30 days"

#### UC-27 · See expiry alert banners
- **Trigger:** Passive — rendered whenever the dashboard loads
- **Flow:**
  1. If any contract expires in ≤ 7 days, show a red alert banner at the top
  2. Banner lists the contract numbers and collaborator names
  3. Banner has a "View All Expiring" link → `/contracts?status=ACTIVE&expiringSoon=true`
  4. User can dismiss the banner (session-only, not persisted)
- **API:** Same fetch as UC-20 (shared query)

### Layout

```
┌─────────────────────────────────────────────────────┐
│  [!] Alert banner (if contracts expiring ≤ 7 days)  │
├──────────┬──────────┬──────────┬────────────────────┤
│ Active   │ Expiring │ Expired  │ Pending Reviews    │
│ Collab.  │ Soon     │ This Mo. │ (avg rating < 3)   │
├──────────┴──────────┴──────────┴────────────────────┤
│  Contracts Expiring Soon (next 30 days)             │
│  [Table: Contract# | Collaborator | End Date | Days]│
├─────────────────────────────────────────────────────┤
│  Quick Actions: [Add Collaborator] [New Contract]   │
└─────────────────────────────────────────────────────┘
```

### Components
- `StatCard` — number + label + optional trend icon
- `ExpiringContractsTable` — read-only table with expiry color coding
- `AlertBanner` — dismissible warning strip
- `QuickActions` — two CTA buttons

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/contracts/expiring-soon?days=30` | Expiring contracts |
| GET | `/api/v1/collaborators?status=ACTIVE&size=1` | Active collaborator count |
| GET | `/api/v1/notifications/pending` | Pending notifications count |

---

---

## V-02 · Collaborators List

**Route:** `/collaborators`
**File:** `app/(dashboard)/collaborators/page.tsx`

### Use Cases

#### UC-01 · List all collaborators
- **Trigger:** User clicks "Collaborators" in the sidebar
- **Flow:**
  1. Fetch paginated list of collaborators (default: page 0, size 20, sort by createdAt DESC)
  2. Render in a data table with columns: National ID, Full Name, Department, Position, Status, Hire Date, Actions
  3. Table supports column sorting (click header)
  4. Pagination controls at the bottom
- **API:** `GET /api/v1/collaborators?page=0&size=20&sort=createdAt,DESC`

#### UC-02 · Search / filter collaborators
- **Trigger:** User types in the search box or changes a filter dropdown
- **Flow:**
  1. Search input is debounced (300ms) and maps to the `search` query param
  2. Status filter maps to the `status` query param
  3. Department filter maps to the `department` query param
  4. All filters combine (AND logic)
  5. Any filter change resets pagination to page 0
  6. URL query params are updated so the state is shareable/bookmarkable
- **API:** `GET /api/v1/collaborators?search=&status=&department=&page=&size=`
- **Empty state:** "No collaborators match your filters" with a "Clear filters" link

#### UC-07 · Deactivate / delete collaborator
- **Trigger:** User clicks the "Delete" action on a table row
- **Flow:**
  1. Open a `ConfirmDialog`: "Delete [Full Name]? This will also remove all associated performance reviews."
  2. User clicks "Confirm Delete"
  3. Call DELETE endpoint
  4. On success: remove row from table (optimistic update), show success toast
  5. On error: show error toast with the error message from the API
- **API:** `DELETE /api/v1/collaborators/{nationalId}`
- **Error cases:** 404 if already deleted, 500 if cascade fails

### Layout

```
┌──────────────────────────────────────────────────────────┐
│  Collaborators                          [+ Add Collaborator]│
├──────────────────────────────────────────────────────────┤
│  [Search...] [Status ▼] [Department ▼]  [Clear Filters] │
├────────────┬──────────┬────────────┬────────┬────────────┤
│ National ID│ Name     │ Department │ Status │ Actions    │
├────────────┼──────────┼────────────┼────────┼────────────┤
│ 123456789  │ John Doe │ Engineering│ ACTIVE │ [👁][✏][🗑]│
│ ...        │ ...      │ ...        │ ...    │ ...        │
├──────────────────────────────────────────────────────────┤
│  < Prev   Page 1 of 5   Next >          Showing 20 of 98 │
└──────────────────────────────────────────────────────────┘
```

### Components
- `CollaboratorsTable` — TanStack Table instance
- `CollaboratorFilters` — search + status + department filters
- `StatusBadge` — colored badge: green (ACTIVE), gray (INACTIVE), yellow (ON_HOLD)
- `ConfirmDialog` — generic confirmation modal
- `DataTablePagination` — shared pagination controls

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/collaborators` | Paginated + filtered list |
| DELETE | `/api/v1/collaborators/{nationalId}` | Delete collaborator |

### Validation / Guards
- Delete is disabled for collaborators that have an `ACTIVE` contract (check via badge on row — fetch status if needed, or disable based on status field)

---

---

## V-03 · Add Collaborator (Step Flow)

**Route:** `/collaborators/new`
**File:** `app/(dashboard)/collaborators/new/page.tsx`

### Use Cases

#### UC-04 · Create collaborator (standalone)
- **Trigger:** User wants to register a new collaborator without creating a contract yet
- **Flow:**
  1. User fills Step 1 (collaborator info)
  2. User skips Step 2 (contract details) by clicking "Skip & Save"
  3. System calls `POST /api/v1/collaborators`
  4. On success: navigate to the new collaborator's detail page with a success toast
  5. On duplicate email/nationalId: show field-level error "A collaborator with this [email / national ID] already exists"
- **API:** `POST /api/v1/collaborators`

#### UC-05 · Create collaborator + initial contract (step flow)
- **Trigger:** User completes both Step 1 and Step 2
- **Flow:**
  1. User fills Step 1, clicks "Next"
  2. Client validates Step 1 fields (Zod). If invalid, highlight errors and block progression
  3. User fills Step 2 (contract details), clicks "Next"
  4. Client validates Step 2 fields. If invalid, highlight errors and block progression
  5. Step 3 shows a read-only summary card of all entered data
  6. User clicks "Confirm & Create"
  7. System calls `POST /api/v1/collaborators` → on success calls `POST /api/v1/contracts` with the new collaborator's nationalId
  8. If collaborator created but contract fails: show partial success toast ("Collaborator created. Contract could not be saved — please add it manually.")
  9. On full success: navigate to collaborator detail page
- **API:** `POST /api/v1/collaborators` then `POST /api/v1/contracts`

### Step Definitions

#### Step 1 — Collaborator Information

| Field | Type | Required | Validation |
|---|---|---|---|
| National ID | text | yes | max 20 chars |
| Employee Code | text | yes | max 50 chars |
| First Name | text | yes | max 100 chars |
| Last Name | text | yes | max 100 chars |
| Email | email | yes | valid email, max 255 |
| Phone | text | no | max 20 chars |
| Department | text | no | max 100 chars |
| Position | text | yes | max 100 chars |
| Hire Date | date | yes | today or in the past |
| Status | select | no | default: ACTIVE |

#### Step 2 — Contract Details (optional)

| Field | Type | Required | Validation |
|---|---|---|---|
| Contract Type | select | yes | FULL_TIME / PART_TIME / CONTRACTOR / TEMPORARY |
| Start Date | date | yes | any date |
| End Date | date | yes | must be after Start Date, must be in the future |
| Salary | number | no | ≥ 0 |
| Currency | text | no | max 3 chars, default "USD" |
| Notice Period Days | number | no | ≥ 0, default 30 |
| Auto Renewal | toggle | no | default off |
| Terms and Conditions | textarea | no | max 10,000 chars |

#### Step 3 — Review & Confirm

- Read-only summary of Step 1 + Step 2 data
- "Back" button returns to Step 2
- "Confirm & Create" triggers API calls
- Loading state on button during submission

### Layout

```
┌──────────────────────────────────────────────────────┐
│  Add Collaborator                                    │
│  ●────────────○────────────○                         │
│  Step 1: Info   Step 2: Contract   Step 3: Review   │
├──────────────────────────────────────────────────────┤
│  [Form fields for current step]                      │
├──────────────────────────────────────────────────────┤
│  [Back]   [Skip & Save]   [Next →]                   │
└──────────────────────────────────────────────────────┘
```

### Components
- `StepIndicator` — horizontal step progress bar
- `CollaboratorForm` — reusable form (also used in Edit view)
- `ContractForm` — reusable form (also used in Create Contract view)
- `ReviewSummary` — read-only summary card
- `FormField` wrappers from react-hook-form + shadcn

---

---

## V-04 · Collaborator Detail

**Route:** `/collaborators/[nationalId]`
**File:** `app/(dashboard)/collaborators/[nationalId]/page.tsx`

### Use Cases

#### UC-03 · View collaborator detail
- **Trigger:** User clicks a row in the Collaborators List or a link from a contract
- **Flow:**
  1. Fetch collaborator detail (includes avg rating + latest review)
  2. Render header card with all profile info
  3. Render two tabs: "Contracts" and "Performance Reviews"
  4. Default active tab: "Contracts"
- **API:** `GET /api/v1/collaborators/{nationalId}/details`

#### UC-08 · View collaborator's contracts
- **Trigger:** User is on the "Contracts" tab (default)
- **Flow:**
  1. Fetch all contracts for this collaborator
  2. Render table: Contract #, Type, Start, End, Status, Days Until Expiry, Actions
  3. Expiring contracts highlighted (red/orange badge)
  4. Action buttons: "View" (→ contract detail), "Renew" (→ renew page), "Terminate" (→ terminate page)
  5. "Renew" is disabled if status is not ACTIVE or collaborator is not eligible
  6. "Add Contract" button opens the standalone create contract form pre-filled with the collaborator ID
- **API:** `GET /api/v1/contracts/collaborator/{collaboratorId}` and `GET /api/v1/contracts?collaboratorId=`

#### UC-09 · View collaborator's performance reviews
- **Trigger:** User clicks the "Performance Reviews" tab
- **Flow:**
  1. Fetch paginated reviews for this collaborator
  2. Render table: Period, Rating, Category, Reviewer, Date, Eligible Renewal
  3. Rating displayed as colored number (green ≥ 3, red < 3)
  4. Category displayed as a readable label
- **API:** `GET /api/v1/performance-reviews/collaborator/{collaboratorId}?page=0&size=10`

#### UC-10 · Check renewal eligibility
- **Trigger:** Passive — always visible on the collaborator detail header
- **Flow:**
  1. Fetch average rating and eligibility status
  2. Render `EligibilityBadge`: green "Eligible for Renewal" if avg ≥ 3.0, red "Not Eligible" if < 3.0
  3. Show average rating value next to the badge (e.g., "Avg: 3.75 / 5.00")
  4. Tooltip on the badge explains: "Eligibility requires an average performance rating of 3.0 or higher"
- **API:** `GET /api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating`

#### UC-23 · Submit performance review
- **Trigger:** User clicks "Add Review" button on the Performance Reviews tab
- **Flow:**
  1. Open a slide-over panel (or modal) with the review form
  2. User fills in all fields
  3. Client validates (Zod) before submit
  4. Call `POST /api/v1/performance-reviews`
  5. On success: close panel, refetch reviews list and average rating, show toast
  6. On error: show inline field errors or generic error toast
- **API:** `POST /api/v1/performance-reviews`

#### UC-24 · View latest review & average rating
- **Trigger:** Passive — rendered in the collaborator header card
- **Flow:**
  1. The detail response includes the latest review
  2. Show latest review period, rating, and category in a compact summary
  3. Show total review count
- **API:** `GET /api/v1/collaborators/{nationalId}/details` (includes latestReview)

### Performance Review Form Fields

| Field | Type | Required | Validation |
|---|---|---|---|
| Reviewer Name | text | yes | max 200 chars |
| Reviewer Email | email | no | valid email |
| Review Period Start | date | yes | — |
| Review Period End | date | yes | must be ≥ start date |
| Rating | number / stars | yes | 1.00 – 5.00 |
| Performance Category | select | yes | EXCEEDS / MEETS / BELOW / NEEDS_IMPROVEMENT |
| Strengths | textarea | no | max 5,000 chars |
| Areas for Improvement | textarea | no | max 5,000 chars |
| Comments | textarea | no | max 5,000 chars |

### Layout

```
┌──────────────────────────────────────────────────────────────┐
│  ← Back to Collaborators                    [Edit] [Delete]  │
│  John Doe — Software Engineer                                │
│  National ID: 123456789 | Employee Code: EMP-001             │
│  Department: Engineering | Hire Date: 2021-03-15             │
│  Status: [ACTIVE]  Avg Rating: 3.75/5.00  [Eligible ✓]      │
├──────────────────────────────────────────────────────────────┤
│  [Contracts ●] [Performance Reviews ○]                       │
├──────────────────────────────────────────────────────────────┤
│  [+ Add Contract]                                            │
│  Contract# | Type      | Start      | End        | Status    │
│  CTR-001   | FULL_TIME | 2023-01-01 | 2024-01-01 | [ACTIVE]  │
├──────────────────────────────────────────────────────────────┤
```

### Components
- `CollaboratorHeaderCard` — profile info + status + eligibility
- `ContractsTab` — contract table with row actions
- `PerformanceReviewsTab` — reviews table + "Add Review" button
- `PerformanceReviewPanel` — slide-over form panel
- `EligibilityBadge` — green/red eligibility indicator
- `RatingDisplay` — numeric rating with color

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/collaborators/{nationalId}/details` | Profile + latest review + avg rating |
| GET | `/api/v1/contracts/collaborator/{collaboratorId}` | Active contract |
| GET | `/api/v1/contracts?collaboratorId=` | All contracts for collaborator |
| GET | `/api/v1/performance-reviews/collaborator/{collaboratorId}` | Review history |
| GET | `/api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating` | Avg rating + eligibility |
| POST | `/api/v1/performance-reviews` | Submit new review |

---

---

## V-05 · Edit Collaborator

**Route:** `/collaborators/[nationalId]/edit`
**File:** `app/(dashboard)/collaborators/[nationalId]/edit/page.tsx`

### Use Cases

#### UC-06 · Edit collaborator info
- **Trigger:** User clicks "Edit" on the collaborator detail header or table row action
- **Flow:**
  1. Fetch current collaborator data
  2. Pre-fill the form with existing values
  3. User modifies any fields
  4. Client validates on submit (same Zod schema as creation)
  5. Call `PUT /api/v1/collaborators/{nationalId}`
  6. On success: navigate back to collaborator detail, show success toast
  7. On conflict (email already exists on another record): show field-level error
  8. "Cancel" button navigates back to detail without saving
- **API:** `GET /api/v1/collaborators/{nationalId}` then `PUT /api/v1/collaborators/{nationalId}`

### Form Fields
Same as Step 1 of the Add Collaborator flow, all pre-populated.

**Note:** `nationalId` is the primary key — it should be displayed as read-only (not editable).

### Layout

```
┌──────────────────────────────────────────────────┐
│  Edit Collaborator — John Doe          [Cancel]  │
├──────────────────────────────────────────────────┤
│  [Form fields — same as Step 1, pre-filled]      │
├──────────────────────────────────────────────────┤
│  [Cancel]                        [Save Changes]  │
└──────────────────────────────────────────────────┘
```

### Components
- `CollaboratorForm` — reused from Add Collaborator, initialized with existing values

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/collaborators/{nationalId}` | Load current values |
| PUT | `/api/v1/collaborators/{nationalId}` | Save changes |

---

---

## V-06 · Contracts List

**Route:** `/contracts`
**File:** `app/(dashboard)/contracts/page.tsx`

### Use Cases

#### UC-11 · List all contracts
- **Trigger:** User clicks "Contracts" in the sidebar
- **Flow:**
  1. Fetch paginated contracts (default: page 0, size 20)
  2. Render table with columns: Contract #, Collaborator, Type, Start Date, End Date, Status, Days Until Expiry, Auto Renewal, Actions
  3. "Days Until Expiry" column shows a colored badge
  4. Table rows are clickable → contract detail
- **API:** `GET /api/v1/contracts?page=0&size=20`

#### UC-12 · Filter contracts
- **Trigger:** User interacts with filter controls
- **Filters available:**
  - **Status** (multi-select): ACTIVE, EXPIRED, RENEWED, TERMINATED, PENDING
  - **Contract Type** (multi-select): FULL_TIME, PART_TIME, CONTRACTOR, TEMPORARY
  - **Expiry Date Range** (date range picker): from / to on `endDate`
  - **Collaborator** (text search): maps to `collaboratorId` param
  - **Expiring Soon** (toggle): pre-applies `GET /api/v1/contracts/expiring-soon` query
- **Flow:**
  1. Each filter change triggers a new API call (debounced 200ms)
  2. Active filters shown as removable chips below the filter bar
  3. "Clear All Filters" link resets everything
  4. Filter state persisted in URL query params
- **API:** `GET /api/v1/contracts?status=&contractType=&collaboratorId=&page=&size=`

### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Contracts                                      [+ New Contract] │
├──────────────────────────────────────────────────────────────────┤
│  [Status ▼] [Type ▼] [Expiry Date: From — To] [Collaborator 🔍] │
│  Active filters: [ACTIVE ×] [FULL_TIME ×]          [Clear All]  │
├────────────┬────────────┬──────────┬────────────┬───────────────┤
│ Contract # │ Collabor. │ Type     │ End Date   │ Days  │ Status │
├────────────┼────────────┼──────────┼────────────┼───────┼───────┤
│ CTR-001    │ John Doe  │ FULL_TIME│ 2024-01-15 │ [5🔴] │ACTIVE │
│ CTR-002    │ Jane Smith│ PART_TIME│ 2024-03-01 │ [45🟢]│ACTIVE │
├──────────────────────────────────────────────────────────────────┤
│  < Prev   Page 1 of 12   Next >               Showing 20 of 234 │
└──────────────────────────────────────────────────────────────────┘
```

### Components
- `ContractsTable` — TanStack Table with sortable columns
- `ContractFilters` — filter bar with multi-select dropdowns + date range
- `ActiveFilterChips` — removable filter tags
- `ExpiryBadge` — colored days-until-expiry badge
- `ContractStatusBadge` — status color coding
- `DataTablePagination` — shared pagination

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/contracts` | Paginated + filtered list |
| GET | `/api/v1/contracts/expiring-soon` | Expiring soon shortcut |

---

---

## V-07 · Create Contract

**Route:** `/contracts/new`
**File:** `app/(dashboard)/contracts/new/page.tsx`

### Use Cases

#### UC-14 · Create a contract (standalone)
- **Trigger:** User clicks "New Contract" in the Contracts List or sidebar
- **Flow:**
  1. Render empty contract form
  2. Collaborator ID field: typeahead search that validates the collaborator exists on blur (`GET /api/v1/collaborators/{id}/exists`)
  3. If collaborator not found: show inline error "Collaborator not found"
  4. If collaborator already has an active contract: API returns 409, show error "This collaborator already has an active contract"
  5. On submit: validate all fields (Zod), call `POST /api/v1/contracts`
  6. On success: navigate to the new contract's detail page
- **API:** `GET /api/v1/collaborators/{nationalId}/exists` + `POST /api/v1/contracts`

#### UC-15 · Create contract linked to a collaborator
- **Trigger:** User clicks "Add Contract" from the collaborator detail view
- **Flow:**
  1. Same as UC-14 but `collaboratorId` is pre-filled and locked (read-only)
  2. A "for [Collaborator Full Name]" subtitle is shown in the header
- **Pre-filled param:** `?collaboratorId=123456789` in the URL
- **API:** Same as UC-14

### Form Fields

| Field | Type | Required | Validation |
|---|---|---|---|
| Collaborator | searchable text | yes | must exist in system |
| Contract Type | select | yes | FULL_TIME / PART_TIME / CONTRACTOR / TEMPORARY |
| Start Date | date | yes | any date |
| End Date | date | yes | after start date, in the future |
| Salary | number | no | ≥ 0 |
| Currency | text | no | max 3 chars, default "USD" |
| Notice Period Days | number | no | ≥ 0, default 30 |
| Auto Renewal | toggle | no | default off |
| Terms and Conditions | textarea | no | max 10,000 chars |

### Layout

```
┌──────────────────────────────────────────────────────┐
│  New Contract [for John Doe - if pre-filled]         │
├──────────────────────────────────────────────────────┤
│  Collaborator ID: [__________] ✓ Found: John Doe     │
│  Contract Type:   [FULL_TIME ▼]                      │
│  Start Date:      [📅 2024-01-01]                    │
│  End Date:        [📅 2025-01-01]                    │
│  Salary:          [5000.00] Currency: [USD]          │
│  Notice Period:   [30] days   Auto Renewal: [OFF]    │
│  Terms: [________________________]                   │
├──────────────────────────────────────────────────────┤
│  [Cancel]                            [Create Contract]│
└──────────────────────────────────────────────────────┘
```

### Components
- `ContractForm` — reusable (also used in Edit Contract)
- `CollaboratorSearch` — typeahead with existence validation

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/collaborators/{nationalId}/exists` | Validate collaborator on blur |
| GET | `/api/v1/collaborators/{nationalId}` | Show collaborator name after validation |
| POST | `/api/v1/contracts` | Create contract |

---

---

## V-08 · Contract Detail

**Route:** `/contracts/[id]`
**File:** `app/(dashboard)/contracts/[id]/page.tsx`

### Use Cases

#### UC-13 · View contract detail
- **Trigger:** User clicks a contract row anywhere in the app
- **Flow:**
  1. Fetch contract by ID
  2. Render full detail: all fields, status, renewal info
  3. Show computed values: `daysUntilExpiry`, `isExpiringSoon`
  4. Show linked collaborator name (link to collaborator detail)
  5. Show previous contract link if this is a renewal (`previousContractId`)
- **API:** `GET /api/v1/contracts/{id}`

#### UC-21 · Download / view contract file
- **Trigger:** User clicks "Download" on the document section
- **Flow:**
  1. If a renewal document is attached, show file name, upload date, and a "Download" button
  2. "Download" triggers file download from the stored URL
  3. If no file: show "No document attached" with an "Upload" option (drag-and-drop zone)
- **Note:** File storage endpoint to be defined in BE (`/api/v1/contracts/{id}/documents`)

#### UC-25 · View notification history for a contract
- **Trigger:** User scrolls to the "Notifications" section on the contract detail
- **Flow:**
  1. Fetch all notifications for this contract
  2. Render table: Type, Recipient, Status, Scheduled At, Sent At
  3. Status badges: green (SENT), yellow (PENDING), red (FAILED), gray (CANCELLED)
- **API:** `GET /api/v1/notifications/contract/{contractId}`

#### UC-26 · Cancel a pending notification
- **Trigger:** User clicks "Cancel" on a PENDING notification row
- **Flow:**
  1. Open confirm dialog: "Cancel this scheduled notification?"
  2. On confirm: call `PUT /api/v1/notifications/{id}/cancel`
  3. On success: update row status to CANCELLED (optimistic update), show toast
- **API:** `PUT /api/v1/notifications/{id}/cancel`
- **Guard:** Cancel action only shown for PENDING status notifications

### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  ← Back to Contracts                 [Edit] [Renew] [Terminate]  │
│  Contract CTR-001                    Status: [ACTIVE]            │
│  Collaborator: John Doe →            Type: FULL_TIME             │
│  Start: 2023-01-01 | End: 2024-01-15 | Days left: [5🔴]         │
│  Salary: $5,000 USD | Notice: 30 days | Auto Renewal: OFF        │
│  Renewal Count: 1 | Previous: CTR-000 →                         │
├──────────────────────────────────────────────────────────────────┤
│  Terms & Conditions                                      [▼]     │
│  Lorem ipsum...                                                  │
├──────────────────────────────────────────────────────────────────┤
│  Renewal Document                                                │
│  📄 renewal-2024.pdf  |  Uploaded 2023-12-01  |  [Download]     │
│  [Drag & drop to replace]                                        │
├──────────────────────────────────────────────────────────────────┤
│  Notification History                                            │
│  Type             | Recipient      | Status  | Sent At           │
│  EXPIRY_WARNING   | john@acme.com  | SENT    | 2024-01-08        │
│  RENEWAL_REMINDER | john@acme.com  | PENDING | —       [Cancel]  │
└──────────────────────────────────────────────────────────────────┘
```

### Components
- `ContractDetailCard` — all contract fields
- `CollaboratorLink` — linked collaborator name
- `ContractChain` — previous/next contract links for renewals
- `DocumentSection` — file display + dropzone upload
- `NotificationHistoryTable` — notifications with cancel action
- `ExpiryBadge` — urgency badge on days remaining
- `ConfirmDialog` — cancel notification confirmation

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/contracts/{id}` | Contract detail |
| GET | `/api/v1/notifications/contract/{contractId}` | Notification history |
| PUT | `/api/v1/notifications/{id}/cancel` | Cancel pending notification |

---

---

## V-09 · Renew Contract

**Route:** `/contracts/[id]/renew`
**File:** `app/(dashboard)/contracts/[id]/renew/page.tsx`

### Use Cases

#### UC-17 · Renew contract
- **Trigger:** User clicks "Renew" on a contract detail or from the collaborator's contracts tab
- **Flow:**
  1. Fetch current contract data and collaborator renewal eligibility
  2. **Eligibility gate:** If `isEligibleForRenewal === false`, block the form and show:
     - Red warning card: "This collaborator is not eligible for renewal"
     - Current average rating vs. required (e.g., "Current: 2.5 / Required: 3.0")
     - Link: "Add a Performance Review →" (opens collaborator detail, reviews tab)
  3. If eligible: render the renewal form pre-filled with current contract data
  4. User modifies fields (new end date is required, others optional)
  5. On submit: validate (Zod), call `PUT /api/v1/contracts/{id}/renew`
  6. On success: navigate to the new (renewed) contract's detail page, show toast "Contract renewed successfully"
  7. On error: show error toast with message from API
- **API:** `GET /api/v1/performance-reviews/collaborator/{collaboratorId}/renewal-eligibility`
         + `PUT /api/v1/contracts/{id}/renew`

#### UC-18 · Upload renewal document
- **Trigger:** User drops or selects a file in the document upload zone during renewal
- **Flow:**
  1. File dropzone accepts PDF, DOCX, PNG (max 10MB)
  2. After file selection: show file name, size, and a remove button
  3. On form submit: file is uploaded alongside the renewal request
  4. Upload progress indicator shown during submission
  5. If upload fails but renewal succeeds: show partial success toast ("Contract renewed. Document upload failed — you can upload it from the contract detail page.")
- **Accepted formats:** `.pdf`, `.docx`, `.png`
- **Max size:** 10MB
- **API:** `PUT /api/v1/contracts/{id}/renew` + `POST /api/v1/contracts/{id}/documents` *(BE endpoint TBD)*

### Form Fields

| Field | Type | Required | Validation |
|---|---|---|---|
| New End Date | date | yes | in the future, after current end date |
| New Salary | number | no | ≥ 0 |
| New Terms and Conditions | textarea | no | max 10,000 chars |
| Auto Renewal | toggle | no | — |
| Notice Period Days | number | no | ≥ 0 |
| Renewal Notes | textarea | no | max 1,000 chars |
| Renewal Document | file upload | no | PDF/DOCX/PNG, max 10MB |

### Layout

```
┌──────────────────────────────────────────────────────────────┐
│  Renew Contract — CTR-001                                    │
│  Collaborator: John Doe | Current end: 2024-01-15            │
├──────────────────────────────────────────────────────────────┤
│  ⚠ ELIGIBILITY GATE (shown only if not eligible)            │
│  John Doe is not eligible for renewal.                       │
│  Average Rating: 2.5 / 5.0 (minimum required: 3.0)          │
│  [Go to Performance Reviews →]                               │
├──────────────────────────────────────────────────────────────┤
│  (Form shown only if eligible)                               │
│  New End Date:    [📅 2025-01-15]                            │
│  New Salary:      [5200.00]  Auto Renewal: [ON]              │
│  Renewal Notes:   [________________________]                 │
│  New Terms:       [________________________]                 │
│                                                              │
│  Renewal Document:                                           │
│  ┌──────────────────────────────────────────┐               │
│  │  📁 Drop PDF, DOCX or PNG here           │               │
│  │     or click to browse (max 10MB)        │               │
│  └──────────────────────────────────────────┘               │
│  📄 renewal-doc.pdf (1.2 MB)   [✕ Remove]                   │
├──────────────────────────────────────────────────────────────┤
│  [Cancel]                              [Renew Contract]      │
└──────────────────────────────────────────────────────────────┘
```

### Components
- `EligibilityGate` — blocking card shown when not eligible
- `RenewalForm` — form with react-hook-form
- `FileDropzone` — react-dropzone wrapper with preview
- `UploadProgress` — progress bar during upload

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/contracts/{id}` | Load current contract data |
| GET | `/api/v1/performance-reviews/collaborator/{collaboratorId}/renewal-eligibility` | Eligibility check |
| GET | `/api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating` | Show current rating in gate |
| PUT | `/api/v1/contracts/{id}/renew` | Submit renewal |
| POST | `/api/v1/contracts/{id}/documents` | Upload renewal document (TBD) |

---

---

## V-10 · Terminate Contract

**Route:** `/contracts/[id]/terminate`
**File:** `app/(dashboard)/contracts/[id]/terminate/page.tsx`

### Use Cases

#### UC-19 · Terminate contract
- **Trigger:** User clicks "Terminate" from the contract detail header or collaborator contracts tab
- **Flow:**
  1. Fetch current contract data (to show in confirmation summary)
  2. Show a full-page confirmation form (not a modal — this is a consequential, irreversible action)
  3. Summary card shows: contract number, collaborator name, type, current end date
  4. Warning: "This action is permanent. The contract will be immediately marked as TERMINATED."
  5. User fills in mandatory "Reason" and "Effective Date" fields
  6. User can optionally add notes
  7. On submit: validate (Zod), call `PUT /api/v1/contracts/{id}/terminate`
  8. On success: navigate back to contracts list (not contract detail, since it's terminated), show success toast
  9. "Cancel" button navigates back to the contract detail without any action
- **API:** `GET /api/v1/contracts/{id}` + `PUT /api/v1/contracts/{id}/terminate`
- **Guard:** If contract is already TERMINATED or EXPIRED, show an informational banner and disable the form

### Form Fields

| Field | Type | Required | Validation |
|---|---|---|---|
| Reason | textarea | yes | max 1,000 chars |
| Effective Date | date | yes | any date (can be today or past) |
| Additional Notes | textarea | no | max 2,000 chars |

### Layout

```
┌──────────────────────────────────────────────────────────────┐
│  Terminate Contract                           [← Cancel]     │
├──────────────────────────────────────────────────────────────┤
│  ⚠ You are about to terminate:                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Contract:     CTR-001                                │   │
│  │ Collaborator: John Doe                               │   │
│  │ Type:         FULL_TIME                              │   │
│  │ End Date:     2024-01-15  (5 days remaining)         │   │
│  └──────────────────────────────────────────────────────┘   │
│  This action is permanent and cannot be undone.             │
├──────────────────────────────────────────────────────────────┤
│  Reason for termination: *                                   │
│  [____________________________________________]              │
│                                                              │
│  Effective Date: * [📅 Today]                               │
│                                                              │
│  Additional Notes:                                           │
│  [____________________________________________]              │
├──────────────────────────────────────────────────────────────┤
│  [← Cancel]                    [Terminate Contract 🔴]      │
└──────────────────────────────────────────────────────────────┘
```

### Components
- `ContractSummaryCard` — read-only contract info for confirmation
- `TerminationForm` — reason + date form
- `DangerButton` — red styled submit button to signal destructive action

### API Calls
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/contracts/{id}` | Load contract for summary card |
| PUT | `/api/v1/contracts/{id}/terminate` | Submit termination |

---

---

## Shared Components Reference

| Component | File | Used In |
|---|---|---|
| `DataTable` | `components/shared/DataTable.tsx` | V-02, V-04, V-06 |
| `DataTablePagination` | `components/shared/DataTablePagination.tsx` | V-02, V-06 |
| `PageHeader` | `components/shared/PageHeader.tsx` | All views |
| `StepIndicator` | `components/shared/StepIndicator.tsx` | V-03 |
| `ConfirmDialog` | `components/shared/ConfirmDialog.tsx` | V-02, V-08 |
| `FileDropzone` | `components/shared/FileDropzone.tsx` | V-08, V-09 |
| `StatusBadge` | `components/shared/StatusBadge.tsx` | V-02, V-04, V-06, V-08 |
| `ExpiryBadge` | `components/shared/ExpiryBadge.tsx` | V-01, V-04, V-06, V-08 |
| `EligibilityBadge` | `components/shared/EligibilityBadge.tsx` | V-04, V-09 |
| `RatingDisplay` | `components/shared/RatingDisplay.tsx` | V-04 |
| `CollaboratorForm` | `components/collaborators/CollaboratorForm.tsx` | V-03, V-05 |
| `ContractForm` | `components/contracts/ContractForm.tsx` | V-03, V-07 |
| `EligibilityGate` | `components/contracts/EligibilityGate.tsx` | V-09 |

---

## Error Handling Reference

| HTTP Status | Error Code | UI Behavior |
|---|---|---|
| 400 | VALIDATION_FAILED | Inline field-level errors on the form |
| 404 | RESOURCE_NOT_FOUND | Redirect to `/not-found` or show inline "not found" state |
| 409 | DUPLICATE_RESOURCE | Field-level error (e.g., "Email already exists") |
| 422 | BUSINESS_RULE_VIOLATION | Toast + optional inline message (e.g., renewal ineligibility) |
| 503 | SERVICE_UNAVAILABLE | Full-page error with "Try again" button |
| Network error | — | Toast: "Could not connect to the server. Please check your connection." |
