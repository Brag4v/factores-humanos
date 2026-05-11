package com.cms.contract.client;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.dto.AverageRatingResponse;
import com.cms.contract.client.dto.CollaboratorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "collaborator-service",
        url = "${collaborator.service.url:http://localhost:8081}",
        fallback = CollaboratorClientFallback.class
)
public interface CollaboratorClient {

    @GetMapping("/api/v1/collaborators/{nationalId}")
    ApiResponse<CollaboratorResponse> getCollaboratorByNationalId(@PathVariable("nationalId") String nationalId);

    @GetMapping("/api/v1/collaborators/{nationalId}/exists")
    ApiResponse<Boolean> checkCollaboratorExists(@PathVariable("nationalId") String nationalId);

    @GetMapping("/api/v1/performance-reviews/collaborator/{collaboratorId}/average-rating")
    ApiResponse<AverageRatingResponse> getAverageRating(@PathVariable("collaboratorId") String collaboratorId);

    @GetMapping("/api/v1/performance-reviews/collaborator/{collaboratorId}/renewal-eligibility")
    ApiResponse<Boolean> isEligibleForRenewal(@PathVariable("collaboratorId") String collaboratorId);
}
