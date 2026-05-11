package com.cms.notification.repository;

import com.cms.notification.entity.ExpiryNotification;
import com.cms.notification.entity.enums.NotificationStatus;
import com.cms.notification.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<ExpiryNotification, UUID> {

    List<ExpiryNotification> findByContractId(UUID contractId);

    List<ExpiryNotification> findByCollaboratorId(String collaboratorId);

    List<ExpiryNotification> findByStatus(NotificationStatus status);

    Page<ExpiryNotification> findByStatus(NotificationStatus status, Pageable pageable);

    List<ExpiryNotification> findByStatusAndScheduledAtBefore(NotificationStatus status, LocalDateTime dateTime);

    boolean existsByContractIdAndNotificationTypeAndStatusIn(
            UUID contractId,
            NotificationType notificationType,
            List<NotificationStatus> statuses
    );

    @Query("SELECT n FROM ExpiryNotification n WHERE n.status = 'PENDING' AND n.scheduledAt <= :now")
    List<ExpiryNotification> findPendingNotificationsToSend(@Param("now") LocalDateTime now);

    @Query("SELECT n FROM ExpiryNotification n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries")
    List<ExpiryNotification> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);

    @Query("SELECT n FROM ExpiryNotification n WHERE " +
            "(:status IS NULL OR n.status = :status) AND " +
            "(:notificationType IS NULL OR n.notificationType = :notificationType) AND " +
            "(:contractId IS NULL OR n.contractId = :contractId)")
    Page<ExpiryNotification> findWithFilters(
            @Param("status") NotificationStatus status,
            @Param("notificationType") NotificationType notificationType,
            @Param("contractId") UUID contractId,
            Pageable pageable
    );

    @Query("SELECT COUNT(n) FROM ExpiryNotification n WHERE n.status = :status")
    long countByStatus(@Param("status") NotificationStatus status);

    @Query("SELECT n FROM ExpiryNotification n WHERE n.contractId = :contractId ORDER BY n.createdAt DESC")
    List<ExpiryNotification> findByContractIdOrderByCreatedAtDesc(@Param("contractId") UUID contractId);

    List<ExpiryNotification> findByCollaboratorIdAndStatus(String collaboratorId, NotificationStatus status);
}
