package com.cms.notification.scheduler;

import com.cms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Process pending notifications every minute.
     * Checks for notifications where scheduledAt <= now and status = PENDING
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void processPendingNotifications() {
        log.debug("Running scheduled task: processPendingNotifications");
        try {
            notificationService.processScheduledNotifications();
        } catch (Exception e) {
            log.error("Error processing scheduled notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Retry failed notifications every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void retryFailedNotifications() {
        log.debug("Running scheduled task: retryFailedNotifications");
        try {
            notificationService.retryFailedNotifications();
        } catch (Exception e) {
            log.error("Error retrying failed notifications: {}", e.getMessage(), e);
        }
    }
}
