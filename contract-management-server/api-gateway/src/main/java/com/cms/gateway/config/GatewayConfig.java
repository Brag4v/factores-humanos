package com.cms.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${services.collaborator.url:http://localhost:8081}")
    private String collaboratorServiceUrl;

    @Value("${services.contract.url:http://localhost:8082}")
    private String contractServiceUrl;

    @Value("${services.notification.url:http://localhost:8083}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Collaborator Service Routes
                .route("collaborator-service", r -> r
                        .path("/api/v1/collaborators/**", "/api/v1/performance-reviews/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addResponseHeader("X-Response-Source", "collaborator-service"))
                        .uri(collaboratorServiceUrl))

                // Contract Service Routes
                .route("contract-service", r -> r
                        .path("/api/v1/contracts/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addResponseHeader("X-Response-Source", "contract-service"))
                        .uri(contractServiceUrl))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addResponseHeader("X-Response-Source", "notification-service"))
                        .uri(notificationServiceUrl))

                // Swagger UI routes for each service
                .route("collaborator-swagger", r -> r
                        .path("/collaborator-service/api-docs/**", "/collaborator-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/collaborator-service/(?<segment>.*)", "/${segment}"))
                        .uri(collaboratorServiceUrl))

                .route("contract-swagger", r -> r
                        .path("/contract-service/api-docs/**", "/contract-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/contract-service/(?<segment>.*)", "/${segment}"))
                        .uri(contractServiceUrl))

                .route("notification-swagger", r -> r
                        .path("/notification-service/api-docs/**", "/notification-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/notification-service/(?<segment>.*)", "/${segment}"))
                        .uri(notificationServiceUrl))

                .build();
    }
}
