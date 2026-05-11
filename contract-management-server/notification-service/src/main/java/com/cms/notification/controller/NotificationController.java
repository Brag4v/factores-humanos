package com.cms.notification.controller;

import com.cms.common.dto.ApiResponse;
import com.cms.common.dto.PageResponse;
import com.cms.notification.dto.request.NotificationRequest;
import com.cms.notification.dto.request.SendNotificationRequest;
import com.cms.notification.dto.response.NotificationResponse;
import com.cms.notification.entity.enums.NotificationStatus;
import com.cms.notification.entity.enums.NotificationType;
import com.cms.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a new notification")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notification created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PostMapping("/send")
    @Operation(summary = "Create and send a notification immediately")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification sent"));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send an existing notification")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Notification cannot be sent")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> sendExistingNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {
        NotificationResponse response = notificationService.sendNotification(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification sent"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all notifications with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getAllNotifications(
            @Parameter(description = "Filter by status") @RequestParam(required = false) NotificationStatus status,
            @Parameter(description = "Filter by notification type") @RequestParam(required = false) NotificationType notificationType,
            @Parameter(description = "Filter by contract ID") @RequestParam(required = false) UUID contractId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NotificationResponse> page = notificationService.getNotificationsWithFilters(
                status, notificationType, contractId, pageable);

        PageResponse<NotificationResponse> pageResponse = PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get all notifications for a contract")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByContract(
            @Parameter(description = "Contract ID") @PathVariable UUID contractId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByContract(contractId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getPendingNotifications() {
        List<NotificationResponse> notifications = notificationService.getPendingNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a notification")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Notification cannot be cancelled")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> cancelNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {
        NotificationResponse response = notificationService.cancelNotification(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification cancelled"));
    }
}
