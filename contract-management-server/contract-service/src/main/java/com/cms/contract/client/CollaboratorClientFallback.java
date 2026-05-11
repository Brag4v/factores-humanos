package com.cms.contract.client;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.dto.AverageRatingResponse;
import com.cms.contract.client.dto.CollaboratorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CollaboratorClientFallback implements CollaboratorClient {

    @Override
    public ApiResponse<CollaboratorResponse> getCollaboratorByNationalId(String nationalId) {
        log.warn("Fallback: Unable to get collaborator with national ID: {}", nationalId);
        return ApiResponse.error("Collaborator service is unavailable");
    }

    @Override
    public ApiResponse<Boolean> checkCollaboratorExists(String nationalId) {
        log.warn("Fallback: Unable to check if collaborator exists with national ID: {}", nationalId);
        return ApiResponse.error("Collaborator service is unavailable");
    }

    @Override
    public ApiResponse<AverageRatingResponse> getAverageRating(String collaboratorId) {
        log.warn("Fallback: Unable to get average rating for collaborator: {}", collaboratorId);
        return ApiResponse.error("Collaborator service is unavailable");
    }

    @Override
    public ApiResponse<Boolean> isEligibleForRenewal(String collaboratorId) {
        log.warn("Fallback: Unable to check renewal eligibility for collaborator: {}", collaboratorId);
        return ApiResponse.error("Collaborator service is unavailable");
    }
}
