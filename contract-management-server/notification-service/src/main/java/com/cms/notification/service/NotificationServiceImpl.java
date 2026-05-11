package com.cms.notification.service;

import com.cms.common.exception.ResourceNotFoundException;
import com.cms.notification.dto.request.NotificationRequest;
import com.cms.notification.dto.request.SendNotificationRequest;
import com.cms.notification.dto.response.NotificationResponse;
import com.cms.notification.entity.ExpiryNotification;
import com.cms.notification.entity.enums.NotificationStatus;
import com.cms.notification.entity.enums.NotificationType;
import com.cms.notification.mapper.NotificationMapper;
import com.cms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    @Value("${notification.max-retries:3}")
    private int maxRetries;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for contract: {}", request.getContractId());

        ExpiryNotification notification = notificationMapper.toEntity(request);
        notification.setStatus(NotificationStatus.PENDING);

        ExpiryNotification savedNotification = notificationRepository.save(notification);
        log.info("Created notification with ID: {}", savedNotification.getId());

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional
    public NotificationResponse sendNotification(UUID notificationId) {
        log.info("Sending notification with ID: {}", notificationId);

        ExpiryNotification notification = findNotificationById(notificationId);

        if (notification.getStatus() == NotificationStatus.SENT) {
            log.warn("Notification {} has already been sent", notificationId);
            return notificationMapper.toResponse(notification);
        }

        if (notification.getStatus() == NotificationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot send a cancelled notification");
        }

        return doSendNotification(notification);
    }

    @Override
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Creating and sending notification for contract: {}", request.getContractId());

        ExpiryNotification notification = notificationMapper.toEntity(request);
        notification.setStatus(NotificationStatus.PENDING);
        notification = notificationRepository.save(notification);

        return doSendNotification(notification);
    }

    @Override
    public NotificationResponse getNotificationById(UUID id) {
        log.debug("Fetching notification with ID: {}", id);
        ExpiryNotification notification = findNotificationById(id);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public List<NotificationResponse> getNotificationsByContract(UUID contractId) {
        log.debug("Fetching notifications for contract: {}", contractId);
        List<ExpiryNotification> notifications = notificationRepository.findByContractIdOrderByCreatedAtDesc(contractId);
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    public List<NotificationResponse> getPendingNotifications() {
        log.debug("Fetching pending notifications");
        List<ExpiryNotification> notifications = notificationRepository.findByStatus(NotificationStatus.PENDING);
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        log.debug("Fetching all notifications with pagination");
        return notificationRepository.findAll(pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public Page<NotificationResponse> getNotificationsWithFilters(
            NotificationStatus status,
            NotificationType notificationType,
            UUID contractId,
            Pageable pageable) {
        log.debug("Fetching notifications with filters - status: {}, type: {}, contract: {}",
                status, notificationType, contractId);
        return notificationRepository.findWithFilters(status, notificationType, contractId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse cancelNotification(UUID id) {
        log.info("Cancelling notification with ID: {}", id);

        ExpiryNotification notification = findNotificationById(id);

        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot cancel a notification that has already been sent");
        }

        notification.markAsCancelled();
        ExpiryNotification savedNotification = notificationRepository.save(notification);

        log.info("Cancelled notification with ID: {}", id);
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional
    public void processScheduledNotifications() {
        log.info("Processing scheduled notifications");

        List<ExpiryNotification> pendingNotifications =
                notificationRepository.findPendingNotificationsToSend(LocalDateTime.now());

        log.info("Found {} pending notifications to process", pendingNotifications.size());

        for (ExpiryNotification notification : pendingNotifications) {
            try {
                doSendNotification(notification);
            } catch (Exception e) {
                log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void retryFailedNotifications() {
        log.info("Retrying failed notifications");

        List<ExpiryNotification> failedNotifications =
                notificationRepository.findFailedNotificationsForRetry(maxRetries);

        log.info("Found {} failed notifications to retry", failedNotifications.size());

        for (ExpiryNotification notification : failedNotifications) {
            try {
                notification.setStatus(NotificationStatus.PENDING);
                doSendNotification(notification);
            } catch (Exception e) {
                log.error("Retry failed for notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    @Async
    @Transactional
    protected NotificationResponse doSendNotification(ExpiryNotification notification) {
        try {
            boolean sent = emailService.sendEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getMessageBody()
            );

            if (sent) {
                notification.markAsSent();
                log.info("Successfully sent notification {} to {}", notification.getId(), notification.getRecipientEmail());
            } else {
                notification.markAsFailed("Email sending failed");
                log.warn("Failed to send notification {} to {}", notification.getId(), notification.getRecipientEmail());
            }
        } catch (Exception e) {
            notification.markAsFailed(e.getMessage());
            log.error("Error sending notification {}: {}", notification.getId(), e.getMessage(), e);
        }

        ExpiryNotification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(savedNotification);
    }

    private ExpiryNotification findNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
    }
}
