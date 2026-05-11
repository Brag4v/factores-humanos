package com.cms.contract.client;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.dto.NotificationRequest;
import com.cms.contract.client.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${notification.service.url:http://localhost:8083}",
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/api/v1/notifications")
    ApiResponse<NotificationResponse> createNotification(@RequestBody NotificationRequest request);

    @PostMapping("/api/v1/notifications/send")
    ApiResponse<NotificationResponse> sendNotification(@RequestBody NotificationRequest request);
}
