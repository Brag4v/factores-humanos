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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private ExpiryNotification notification;
    private NotificationResponse notificationResponse;
    private UUID notificationId;
    private UUID contractId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "maxRetries", 3);

        notificationId = UUID.randomUUID();
        contractId = UUID.randomUUID();

        notification = ExpiryNotification.builder()
                .id(notificationId)
                .contractId(contractId)
                .collaboratorId("NAT001")
                .notificationType(NotificationType.EXPIRY_WARNING)
                .recipientEmail("john@example.com")
                .recipientName("John Doe")
                .subject("Contract Expiring Soon")
                .messageBody("<p>Your contract is expiring</p>")
                .daysUntilExpiry(30)
                .status(NotificationStatus.PENDING)
                .scheduledAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .contractId(contractId)
                .collaboratorId("NAT001")
                .notificationType(NotificationType.EXPIRY_WARNING)
                .recipientEmail("john@example.com")
                .status(NotificationStatus.PENDING)
                .build();
    }

    @Nested
    class CreateNotification {

        @Test
        void shouldCreateNotificationSuccessfully() {
            NotificationRequest request = NotificationRequest.builder()
                    .contractId(contractId)
                    .collaboratorId("NAT001")
                    .notificationType(NotificationType.EXPIRY_WARNING)
                    .recipientEmail("john@example.com")
                    .recipientName("John Doe")
                    .subject("Contract Expiring")
                    .messageBody("Body")
                    .daysUntilExpiry(30)
                    .build();

            when(notificationMapper.toEntity(request)).thenReturn(notification);
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.createNotification(request);

            assertThat(result.getContractId()).isEqualTo(contractId);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }
    }

    @Nested
    class SendNotificationById {

        @Test
        void shouldSendPendingNotification() {
            NotificationResponse sentResponse = NotificationResponse.builder()
                    .id(notificationId)
                    .status(NotificationStatus.SENT)
                    .build();

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(emailService.sendEmail("john@example.com", "Contract Expiring Soon", "<p>Your contract is expiring</p>"))
                    .thenReturn(true);
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(sentResponse);

            NotificationResponse result = notificationService.sendNotification(notificationId);

            assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        }

        @Test
        void shouldReturnImmediatelyWhenAlreadySent() {
            notification.setStatus(NotificationStatus.SENT);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            notificationService.sendNotification(notificationId);

            verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        }

        @Test
        void shouldThrowWhenCancelled() {
            notification.setStatus(NotificationStatus.CANCELLED);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.sendNotification(notificationId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cancelled");
        }

        @Test
        void shouldMarkAsFailedWhenEmailFails() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            notificationService.sendNotification(notificationId);

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(notification.getRetryCount()).isEqualTo(1);
        }

        @Test
        void shouldMarkAsFailedWhenEmailThrows() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(emailService.sendEmail(anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("SMTP error"));
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            notificationService.sendNotification(notificationId);

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(notification.getFailureReason()).isEqualTo("SMTP error");
        }

        @Test
        void shouldThrowWhenNotificationNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(notificationRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.sendNotification(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class SendNotificationWithRequest {

        @Test
        void shouldCreateAndSendNotification() {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .contractId(contractId)
                    .collaboratorId("NAT001")
                    .notificationType(NotificationType.RENEWAL_REMINDER)
                    .recipientEmail("john@example.com")
                    .recipientName("John Doe")
                    .subject("Renewal Reminder")
                    .messageBody("Please renew")
                    .daysUntilExpiry(15)
                    .build();

            ExpiryNotification newNotification = ExpiryNotification.builder()
                    .id(UUID.randomUUID())
                    .recipientEmail("john@example.com")
                    .subject("Renewal Reminder")
                    .messageBody("Please renew")
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .build();

            when(notificationMapper.toEntity(request)).thenReturn(newNotification);
            when(notificationRepository.save(newNotification)).thenReturn(newNotification);
            when(emailService.sendEmail("john@example.com", "Renewal Reminder", "Please renew")).thenReturn(true);
            when(notificationMapper.toResponse(newNotification)).thenReturn(notificationResponse);

            notificationService.sendNotification(request);

            verify(emailService).sendEmail("john@example.com", "Renewal Reminder", "Please renew");
        }
    }

    @Nested
    class GetNotification {

        @Test
        void shouldReturnNotificationById() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.getNotificationById(notificationId);

            assertThat(result.getId()).isEqualTo(notificationId);
        }

        @Test
        void shouldThrowWhenNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(notificationRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.getNotificationById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetNotificationsByContract {

        @Test
        void shouldReturnNotificationsByContract() {
            when(notificationRepository.findByContractIdOrderByCreatedAtDesc(contractId))
                    .thenReturn(List.of(notification));
            when(notificationMapper.toResponseList(List.of(notification)))
                    .thenReturn(List.of(notificationResponse));

            List<NotificationResponse> result = notificationService.getNotificationsByContract(contractId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class GetPendingNotifications {

        @Test
        void shouldReturnPendingNotifications() {
            when(notificationRepository.findByStatus(NotificationStatus.PENDING))
                    .thenReturn(List.of(notification));
            when(notificationMapper.toResponseList(List.of(notification)))
                    .thenReturn(List.of(notificationResponse));

            List<NotificationResponse> result = notificationService.getPendingNotifications();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class GetAllNotifications {

        @Test
        void shouldReturnPagedNotifications() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ExpiryNotification> page = new PageImpl<>(List.of(notification));

            when(notificationRepository.findAll(pageable)).thenReturn(page);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            Page<NotificationResponse> result = notificationService.getAllNotifications(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    class CancelNotification {

        @Test
        void shouldCancelPendingNotification() {
            NotificationResponse cancelledResponse = NotificationResponse.builder()
                    .id(notificationId)
                    .status(NotificationStatus.CANCELLED)
                    .build();

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(notification)).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(cancelledResponse);

            NotificationResponse result = notificationService.cancelNotification(notificationId);

            assertThat(result.getStatus()).isEqualTo(NotificationStatus.CANCELLED);
        }

        @Test
        void shouldThrowWhenCancellingSentNotification() {
            notification.setStatus(NotificationStatus.SENT);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.cancelNotification(notificationId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already been sent");
        }
    }

    @Nested
    class ProcessScheduledNotifications {

        @Test
        void shouldProcessAllPendingScheduled() {
            ExpiryNotification n1 = ExpiryNotification.builder()
                    .id(UUID.randomUUID())
                    .recipientEmail("a@test.com")
                    .subject("Sub1")
                    .messageBody("Body1")
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .build();
            ExpiryNotification n2 = ExpiryNotification.builder()
                    .id(UUID.randomUUID())
                    .recipientEmail("b@test.com")
                    .subject("Sub2")
                    .messageBody("Body2")
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .build();

            when(notificationRepository.findPendingNotificationsToSend(any(LocalDateTime.class)))
                    .thenReturn(List.of(n1, n2));
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any())).thenReturn(notificationResponse);

            notificationService.processScheduledNotifications();

            verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
        }

        @Test
        void shouldContinueProcessingAfterFailure() {
            ExpiryNotification n1 = ExpiryNotification.builder()
                    .id(UUID.randomUUID())
                    .recipientEmail("a@test.com")
                    .subject("Sub1")
                    .messageBody("Body1")
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .build();
            ExpiryNotification n2 = ExpiryNotification.builder()
                    .id(UUID.randomUUID())
                    .recipientEmail("b@test.com")
                    .subject("Sub2")
                    .messageBody("Body2")
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .build();

            when(notificationRepository.findPendingNotificationsToSend(any(LocalDateTime.class)))
                    .thenReturn(List.of(n1, n2));
            when(emailService.sendEmail("a@test.com", "Sub1", "Body1"))
                    .thenThrow(new RuntimeException("Fail"));
            when(emailService.sendEmail("b@test.com", "Sub2", "Body2"))
                    .thenReturn(true);
            when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any())).thenReturn(notificationResponse);

            notificationService.processScheduledNotifications();

            verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    class RetryFailedNotifications {

        @Test
        void shouldRetryFailedNotifications() {
            notification.setStatus(NotificationStatus.FAILED);

            when(notificationRepository.findFailedNotificationsForRetry(3))
                    .thenReturn(List.of(notification));
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any())).thenReturn(notificationResponse);

            notificationService.retryFailedNotifications();

            verify(emailService).sendEmail("john@example.com", "Contract Expiring Soon", "<p>Your contract is expiring</p>");
        }
    }
}
