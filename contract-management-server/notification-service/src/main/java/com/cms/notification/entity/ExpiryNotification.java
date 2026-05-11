package com.cms.notification.entity;

import com.cms.notification.entity.enums.NotificationStatus;
import com.cms.notification.entity.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expiry_notifications", indexes = {
        @Index(name = "idx_notification_contract", columnList = "contract_id"),
        @Index(name = "idx_notification_collaborator", columnList = "collaborator_id"),
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_notification_type", columnList = "notification_type")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Contract ID is required")
    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @NotBlank(message = "Collaborator ID is required")
    @Column(name = "collaborator_id", nullable = false, length = 20)
    private String collaboratorId;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    @Size(max = 255, message = "Recipient email must not exceed 255 characters")
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 200, message = "Recipient name must not exceed 200 characters")
    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @NotBlank(message = "Message body is required")
    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @NotNull(message = "Days until expiry is required")
    @Column(name = "days_until_expiry", nullable = false)
    private Integer daysUntilExpiry;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @NotNull(message = "Scheduled time is required")
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
    }

    public void markAsCancelled() {
        this.status = NotificationStatus.CANCELLED;
    }

    public boolean canRetry(int maxRetries) {
        return this.retryCount < maxRetries && this.status == NotificationStatus.FAILED;
    }
}
