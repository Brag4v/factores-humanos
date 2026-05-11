package com.cms.collaborator.controller;

import com.cms.collaborator.dto.request.PerformanceReviewRequest;
import com.cms.collaborator.dto.response.AverageRatingResponse;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import com.cms.collaborator.service.PerformanceReviewService;
import com.cms.common.dto.ApiResponse;
import com.cms.common.dto.PageResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performance-reviews")
@RequiredArgsConstructor
@Tag(name = "Performance Reviews", description = "Performance review management APIs")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;

    @PostMapping
    @Operation(summary = "Submit a new performance review")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collaborator not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate review for period")
    })
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> submitReview(
            @Valid @RequestBody PerformanceReviewRequest request) {
        PerformanceReviewResponse response = performanceReviewService.submitReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get performance review by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> getReviewById(
            @Parameter(description = "Review ID") @PathVariable UUID id) {
        PerformanceReviewResponse response = performanceReviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/collaborator/{collaboratorId}")
    @Operation(summary = "Get all reviews for a collaborator with pagination")
    public ResponseEntity<ApiResponse<PageResponse<PerformanceReviewResponse>>> getReviewsByCollaborator(
            @Parameter(description = "Collaborator National ID") @PathVariable String collaboratorId,
            @PageableDefault(size = 20, sort = "reviewPeriodEnd", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PerformanceReviewResponse> page = performanceReviewService
                .getReviewsByCollaborator(collaboratorId, pageable);

        PageResponse<PerformanceReviewResponse> pageResponse = PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/collaborator/{collaboratorId}/latest")
    @Operation(summary = "Get the latest performance review for a collaborator")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest review found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No reviews found for collaborator")
    })
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> getLatestReview(
            @Parameter(description = "Collaborator National ID") @PathVariable String collaboratorId) {
        PerformanceReviewResponse response = performanceReviewService.getLatestReview(collaboratorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/collaborator/{collaboratorId}/average-rating")
    @Operation(summary = "Get average rating for a collaborator")
    public ResponseEntity<ApiResponse<AverageRatingResponse>> getAverageRating(
            @Parameter(description = "Collaborator National ID") @PathVariable String collaboratorId) {
        AverageRatingResponse response = performanceReviewService.calculateAverageRating(collaboratorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/collaborator/{collaboratorId}/renewal-eligibility")
    @Operation(summary = "Check if collaborator is eligible for contract renewal")
    public ResponseEntity<ApiResponse<Boolean>> checkRenewalEligibility(
            @Parameter(description = "Collaborator National ID") @PathVariable String collaboratorId) {
        boolean isEligible = performanceReviewService.isEligibleForRenewal(collaboratorId);
        return ResponseEntity.ok(ApiResponse.success(isEligible));
    }
}
