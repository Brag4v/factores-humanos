package com.cms.collaborator.service;

import com.cms.collaborator.dto.request.PerformanceReviewRequest;
import com.cms.collaborator.dto.response.AverageRatingResponse;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PerformanceReviewService {

    PerformanceReviewResponse submitReview(PerformanceReviewRequest request);

    PerformanceReviewResponse getReviewById(UUID id);

    Page<PerformanceReviewResponse> getReviewsByCollaborator(String collaboratorId, Pageable pageable);

    List<PerformanceReviewResponse> getAllReviewsByCollaborator(String collaboratorId);

    PerformanceReviewResponse getLatestReview(String collaboratorId);

    AverageRatingResponse calculateAverageRating(String collaboratorId);

    boolean isEligibleForRenewal(String collaboratorId);
}
