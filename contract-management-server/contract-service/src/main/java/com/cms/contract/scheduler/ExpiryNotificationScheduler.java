package com.cms.contract.scheduler;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.CollaboratorClient;
import com.cms.contract.client.NotificationClient;
import com.cms.contract.client.dto.CollaboratorResponse;
import com.cms.contract.client.dto.NotificationRequest;
import com.cms.contract.entity.Contract;
import com.cms.contract.service.ExpiryCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryNotificationScheduler {

    private final ExpiryCheckService expiryCheckService;
    private final CollaboratorClient collaboratorClient;
    private final NotificationClient notificationClient;

    @Value("${contract.expiry.notification.enabled:true}")
    private boolean notificationEnabled;

    @Value("${contract.expiry.notification.days-before:30}")
    private int daysBeforeExpiry;

    /**
     * Runs daily at 8 AM to check for expiring contracts and send notifications
     */
    @Scheduled(cron = "${contract.expiry.notification.cron:0 0 8 * * *}")
    public void checkExpiringContractsAndNotify() {
        if (!notificationEnabled) {
            log.info("Contract expiry notifications are disabled");
            return;
        }

        log.info("Starting expiry notification check for contracts expiring within {} days", daysBeforeExpiry);

        try {
            List<Contract> expiringContracts = expiryCheckService.findContractsExpiringSoon(daysBeforeExpiry);
            log.info("Found {} contracts expiring soon", expiringContracts.size());

            int successCount = 0;
            int failureCount = 0;

            for (Contract contract : expiringContracts) {
                try {
                    sendExpiryNotification(contract);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send notification for contract {}: {}",
                            contract.getContractNumber(), e.getMessage());
                    failureCount++;
                }
            }

            log.info("Expiry notification check completed. Success: {}, Failures: {}", successCount, failureCount);
        } catch (Exception e) {
            log.error("Error during expiry notification check: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs daily at midnight to update expired contracts
     */
    @Scheduled(cron = "${contract.expiry.update.cron:0 0 0 * * *}")
    public void updateExpiredContracts() {
        log.info("Starting expired contracts update");
        try {
            expiryCheckService.updateExpiredContracts();
            log.info("Expired contracts update completed");
        } catch (Exception e) {
            log.error("Error during expired contracts update: {}", e.getMessage(), e);
        }
    }

    private void sendExpiryNotification(Contract contract) {
        // Get collaborator info
        ApiResponse<CollaboratorResponse> collaboratorResponse =
                collaboratorClient.getCollaboratorByNationalId(contract.getCollaboratorId());

        if (!collaboratorResponse.isSuccess() || collaboratorResponse.getData() == null) {
            log.warn("Could not get collaborator info for contract {}", contract.getContractNumber());
            return;
        }

        CollaboratorResponse collaborator = collaboratorResponse.getData();
        long daysUntilExpiry = contract.getDaysUntilExpiry();

        // Determine notification type based on days until expiry
        String notificationType;
        if (daysUntilExpiry <= 0) {
            notificationType = "EXPIRED_NOTICE";
        } else if (daysUntilExpiry <= 7) {
            notificationType = "EXPIRY_WARNING";
        } else {
            notificationType = "RENEWAL_REMINDER";
        }

        String subject = String.format("Contract %s - %s",
                contract.getContractNumber(),
                notificationType.replace("_", " "));

        String messageBody = buildNotificationMessage(contract, collaborator, daysUntilExpiry);

        NotificationRequest request = NotificationRequest.builder()
                .contractId(contract.getId())
                .collaboratorId(contract.getCollaboratorId())
                .notificationType(notificationType)
                .recipientEmail(collaborator.getEmail())
                .recipientName(collaborator.getFullName())
                .subject(subject)
                .messageBody(messageBody)
                .daysUntilExpiry((int) daysUntilExpiry)
                .scheduledAt(LocalDateTime.now())
                .build();

        try {
            notificationClient.sendNotification(request);
            log.info("Sent {} notification for contract {}", notificationType, contract.getContractNumber());
        } catch (Exception e) {
            log.error("Failed to send notification via notification service: {}", e.getMessage());
        }
    }

    private String buildNotificationMessage(Contract contract, CollaboratorResponse collaborator, long daysUntilExpiry) {
        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(collaborator.getFullName()).append(",\n\n");

        if (daysUntilExpiry <= 0) {
            message.append("Your contract (").append(contract.getContractNumber())
                    .append(") has expired as of ").append(contract.getEndDate()).append(".\n\n");
            message.append("Please contact HR to discuss your employment status.\n\n");
        } else {
            message.append("Your contract (").append(contract.getContractNumber())
                    .append(") will expire on ").append(contract.getEndDate())
                    .append(" (in ").append(daysUntilExpiry).append(" days).\n\n");

            if (contract.getAutoRenewal()) {
                message.append("Your contract is set for automatic renewal. ");
                message.append("If you wish to make changes, please contact HR.\n\n");
            } else {
                message.append("Please contact HR to discuss contract renewal options.\n\n");
            }
        }

        message.append("Contract Details:\n");
        message.append("- Contract Number: ").append(contract.getContractNumber()).append("\n");
        message.append("- Type: ").append(contract.getContractType()).append("\n");
        message.append("- Start Date: ").append(contract.getStartDate()).append("\n");
        message.append("- End Date: ").append(contract.getEndDate()).append("\n\n");

        message.append("Best regards,\nContract Management System");

        return message.toString();
    }
}
