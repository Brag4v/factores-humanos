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
public class NotificationResponse {

    private UUID id;
    private UUID contractId;
    private UUID collaboratorId;
    private String notificationType;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}
