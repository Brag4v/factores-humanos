package com.cms.notification.dto.response;

import com.cms.notification.entity.enums.NotificationStatus;
import com.cms.notification.entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private UUID id;
    private UUID contractId;
    private String collaboratorId;
    private NotificationType notificationType;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String messageBody;
    private Integer daysUntilExpiry;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
