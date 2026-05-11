package com.cms.notification.service;

import com.cms.notification.dto.request.NotificationRequest;
import com.cms.notification.dto.request.SendNotificationRequest;
import com.cms.notification.dto.response.NotificationResponse;
import com.cms.notification.entity.enums.NotificationStatus;
import com.cms.notification.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse sendNotification(UUID notificationId);

    NotificationResponse sendNotification(SendNotificationRequest request);

    NotificationResponse getNotificationById(UUID id);

    List<NotificationResponse> getNotificationsByContract(UUID contractId);

    List<NotificationResponse> getPendingNotifications();

    Page<NotificationResponse> getAllNotifications(Pageable pageable);

    Page<NotificationResponse> getNotificationsWithFilters(
            NotificationStatus status,
            NotificationType notificationType,
            UUID contractId,
            Pageable pageable
    );

    NotificationResponse cancelNotification(UUID id);

    void processScheduledNotifications();

    void retryFailedNotifications();
}
