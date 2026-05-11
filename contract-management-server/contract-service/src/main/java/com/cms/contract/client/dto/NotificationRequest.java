package com.cms.contract.client.dto;

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
public class NotificationRequest {

    private UUID contractId;
    private String collaboratorId;
    private String notificationType;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String messageBody;
    private Integer daysUntilExpiry;
    private LocalDateTime scheduledAt;
}
