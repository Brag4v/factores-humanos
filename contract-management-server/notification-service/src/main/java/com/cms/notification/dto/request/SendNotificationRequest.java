package com.cms.notification.dto.request;

import com.cms.notification.entity.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    @NotBlank(message = "Collaborator ID is required")
    private String collaboratorId;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    private String recipientEmail;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 200, message = "Recipient name must not exceed 200 characters")
    private String recipientName;

    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @NotBlank(message = "Message body is required")
    private String messageBody;

    @NotNull(message = "Days until expiry is required")
    private Integer daysUntilExpiry;
}
