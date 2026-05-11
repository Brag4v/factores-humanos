package com.cms.notification.mapper;

import com.cms.notification.dto.request.NotificationRequest;
import com.cms.notification.dto.request.SendNotificationRequest;
import com.cms.notification.dto.response.NotificationResponse;
import com.cms.notification.entity.ExpiryNotification;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "scheduledAt", expression = "java(request.getScheduledAt() != null ? request.getScheduledAt() : java.time.LocalDateTime.now())")
    ExpiryNotification toEntity(NotificationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "scheduledAt", expression = "java(java.time.LocalDateTime.now())")
    ExpiryNotification toEntity(SendNotificationRequest request);

    NotificationResponse toResponse(ExpiryNotification notification);

    List<NotificationResponse> toResponseList(List<ExpiryNotification> notifications);
}
