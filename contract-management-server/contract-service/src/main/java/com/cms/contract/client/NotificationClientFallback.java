package com.cms.contract.client;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.dto.NotificationRequest;
import com.cms.contract.client.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public ApiResponse<NotificationResponse> createNotification(NotificationRequest request) {
        log.warn("Fallback: Unable to create notification for contract: {}", request.getContractId());
        return ApiResponse.error("Notification service is unavailable");
    }

    @Override
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request) {
        log.warn("Fallback: Unable to send notification for contract: {}", request.getContractId());
        return ApiResponse.error("Notification service is unavailable");
    }
}
