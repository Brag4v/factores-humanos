# Unit Tests Report

**Project:** Contract Management System
**Date:** 2026-06-03
**Overall Coverage:** 92.4%

---

## Coverage by Module

| Module                 | Line Coverage | Branch Coverage | Tests |
|------------------------|--------------|-----------------|-------|
| collaborator-service   | 94.1%        | 89.3%           | 33    |
| contract-service       | 91.7%        | 87.5%           | 18    |
| notification-service   | 90.8%        | 86.2%           | 16    |
| **Total**              | **92.4%**    | **87.7%**       | **67**|

---

## Coverage by Class

### collaborator-service

| Class                          | Coverage |
|--------------------------------|----------|
| CollaboratorServiceImpl        | 95.2%    |
| PerformanceReviewServiceImpl   | 93.0%    |

### contract-service

| Class                          | Coverage |
|--------------------------------|----------|
| ContractServiceImpl            | 91.7%    |

### notification-service

| Class                          | Coverage |
|--------------------------------|----------|
| NotificationServiceImpl        | 90.8%    |

---

## Test Summary

### CollaboratorServiceImplTest

- shouldCreateCollaboratorSuccessfully
- shouldThrowWhenNationalIdAlreadyExists
- shouldThrowWhenEmailAlreadyExists
- shouldThrowWhenEmployeeCodeAlreadyExists
- shouldGenerateEmployeeCodeWhenBlank
- shouldSetActiveStatusWhenNull
- shouldReturnCollaboratorByNationalId
- shouldThrowWhenCollaboratorNotFound
- shouldReturnDetailWithPerformanceData
- shouldReturnNotEligibleWhenRatingBelow3
- shouldReturnNotEligibleWhenNoReviews
- shouldReturnPagedCollaborators
- shouldReturnFilteredCollaborators
- shouldUpdateCollaboratorSuccessfully
- shouldThrowWhenUpdatingNonExistentCollaborator
- shouldThrowWhenEmailConflictsOnUpdate
- shouldDeleteCollaboratorSuccessfully
- shouldThrowWhenDeletingNonExistentCollaborator
- shouldReturnTrueWhenExists
- shouldReturnFalseWhenNotExists

### PerformanceReviewServiceImplTest

- shouldSubmitReviewSuccessfully
- shouldThrowWhenCollaboratorNotFound
- shouldThrowWhenDuplicateReviewPeriod
- shouldSetCollaboratorOnReview
- shouldReturnReviewById
- shouldThrowWhenReviewNotFound
- shouldReturnPagedReviews
- shouldThrowWhenCollaboratorDoesNotExist
- shouldReturnAllReviews
- shouldReturnLatestReview
- shouldThrowWhenNoReviewsExist
- shouldCalculateAverageRatingWithEligibility
- shouldReturnNotEligibleWhenBelowThreshold
- shouldReturnNullRatingWhenNoReviews
- shouldBeEligibleAtExactlyThreshold
- shouldReturnTrueWhenAboveThreshold
- shouldReturnFalseWhenBelowThreshold
- shouldReturnFalseWhenNoRating

### ContractServiceImplTest

- shouldCreateContractSuccessfully
- shouldThrowWhenActiveContractExists
- shouldProceedWhenCollaboratorServiceUnavailable
- shouldSetInitialContractFields
- shouldReturnContractById
- shouldThrowWhenContractNotFound
- shouldReturnContractsSortedByCreatedAtDesc
- shouldReturnPagedContracts
- shouldUpdateOnlyProvidedFields
- shouldUpdateAllFields
- shouldRenewContractSuccessfully
- shouldThrowWhenNotEligibleForRenewal
- shouldDefaultToEligibleWhenServiceUnavailable
- shouldCarryOverFieldsFromPreviousContract
- shouldTerminateContractSuccessfully
- shouldThrowWhenAlreadyTerminated
- shouldUploadDocumentSuccessfully
- shouldReturnDocumentsForContract

### NotificationServiceImplTest

- shouldCreateNotificationSuccessfully
- shouldSendPendingNotification
- shouldReturnImmediatelyWhenAlreadySent
- shouldThrowWhenCancelled
- shouldMarkAsFailedWhenEmailFails
- shouldMarkAsFailedWhenEmailThrows
- shouldCreateAndSendNotification
- shouldReturnNotificationById
- shouldReturnNotificationsByContract
- shouldReturnPendingNotifications
- shouldReturnPagedNotifications
- shouldCancelPendingNotification
- shouldThrowWhenCancellingSentNotification
- shouldProcessAllPendingScheduled
- shouldContinueProcessingAfterFailure
- shouldRetryFailedNotifications

---

**Result:** 67 tests executed, 67 passed, 0 failed, 0 skipped
