package com.cms.collaborator.controller;

import com.cms.collaborator.dto.request.CollaboratorRequest;
import com.cms.collaborator.dto.response.CollaboratorDetailResponse;
import com.cms.collaborator.dto.response.CollaboratorResponse;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
import com.cms.collaborator.service.CollaboratorService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/collaborators")
@RequiredArgsConstructor
@Tag(name = "Collaborators", description = "Collaborator management APIs")
public class CollaboratorController {

    private final CollaboratorService collaboratorService;
    private final PerformanceReviewService performanceReviewService;

    @PostMapping
    @Operation(summary = "Create a new collaborator")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Collaborator created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Collaborator already exists")
    })
    public ResponseEntity<ApiResponse<CollaboratorResponse>> createCollaborator(
            @Valid @RequestBody CollaboratorRequest request) {
        CollaboratorResponse response = collaboratorService.createCollaborator(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping("/{nationalId}")
    @Operation(summary = "Get collaborator by National ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Collaborator found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collaborator not found")
    })
    public ResponseEntity<ApiResponse<CollaboratorResponse>> getCollaboratorByNationalId(
            @Parameter(description = "Collaborator National ID") @PathVariable String nationalId) {
        CollaboratorResponse response = collaboratorService.getCollaboratorByNationalId(nationalId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{nationalId}/details")
    @Operation(summary = "Get collaborator details with performance summary")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Collaborator details found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collaborator not found")
    })
    public ResponseEntity<ApiResponse<CollaboratorDetailResponse>> getCollaboratorDetails(
            @Parameter(description = "Collaborator National ID") @PathVariable String nationalId) {
        CollaboratorDetailResponse response = collaboratorService.getCollaboratorDetailByNationalId(nationalId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all collaborators with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<CollaboratorResponse>>> getAllCollaborators(
            @Parameter(description = "Filter by status") @RequestParam(required = false) CollaboratorStatus status,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Search term for name or email") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CollaboratorResponse> page = collaboratorService.getCollaboratorsWithFilters(
                status, department, search, pageable);

        PageResponse<CollaboratorResponse> pageResponse = PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PutMapping("/{nationalId}")
    @Operation(summary = "Update collaborator")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Collaborator updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collaborator not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate email or employee code")
    })
    public ResponseEntity<ApiResponse<CollaboratorResponse>> updateCollaborator(
            @Parameter(description = "Collaborator National ID") @PathVariable String nationalId,
            @Valid @RequestBody CollaboratorRequest request) {
        CollaboratorResponse response = collaboratorService.updateCollaborator(nationalId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Collaborator updated successfully"));
    }

    @DeleteMapping("/{nationalId}")
    @Operation(summary = "Delete collaborator")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Collaborator deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collaborator not found")
    })
    public ResponseEntity<Void> deleteCollaborator(
            @Parameter(description = "Collaborator National ID") @PathVariable String nationalId) {
        collaboratorService.deleteCollaborator(nationalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{nationalId}/performance-reviews")
    @Operation(summary = "Get all performance reviews for a collaborator")
    public ResponseEntity<ApiResponse<List<PerformanceReviewResponse>>> getCollaboratorPerformanceReviews(
            @Parameter(description = "Collaborator National ID") @PathVariable String nationalId) {
        List<PerformanceReviewResponse> reviews = performanceReviewService.getAllReviewsByCollaborator(nationalId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/{nationalId}/exists")
    @Operation(summary = "Check if collaborator exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCollaboratorExists(
            @Parameter(description = "Collaborator National ID") @PathVariable String nationalId) {
        boolean exists = collaboratorService.existsByNationalId(nationalId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
