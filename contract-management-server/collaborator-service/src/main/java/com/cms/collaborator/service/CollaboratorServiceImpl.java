package com.cms.collaborator.service;

import com.cms.collaborator.dto.request.CollaboratorRequest;
import com.cms.collaborator.dto.response.AverageRatingResponse;
import com.cms.collaborator.dto.response.CollaboratorDetailResponse;
import com.cms.collaborator.dto.response.CollaboratorResponse;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import com.cms.collaborator.entity.Collaborator;
import com.cms.collaborator.entity.PerformanceReview;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
import com.cms.collaborator.exception.CollaboratorNotFoundException;
import com.cms.collaborator.mapper.CollaboratorMapper;
import com.cms.collaborator.mapper.PerformanceReviewMapper;
import com.cms.collaborator.repository.CollaboratorRepository;
import com.cms.collaborator.repository.PerformanceReviewRepository;
import com.cms.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final CollaboratorMapper collaboratorMapper;
    private final PerformanceReviewMapper performanceReviewMapper;

    @Override
    @Transactional
    public CollaboratorResponse createCollaborator(CollaboratorRequest request) {
        log.info("Creating new collaborator with national ID: {}", request.getNationalId());

        if (collaboratorRepository.existsById(request.getNationalId())) {
            throw new DuplicateResourceException("Collaborator", "nationalId", request.getNationalId());
        }

        if (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()) {
            request.setEmployeeCode(generateEmployeeCode());
        }

        validateUniqueConstraints(request, null);

        Collaborator collaborator = collaboratorMapper.toEntity(request);
        if (collaborator.getStatus() == null) {
            collaborator.setStatus(CollaboratorStatus.ACTIVE);
        }

        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);
        log.info("Created collaborator with national ID: {}", savedCollaborator.getNationalId());

        return collaboratorMapper.toResponse(savedCollaborator);
    }

    @Override
    public CollaboratorResponse getCollaboratorByNationalId(String nationalId) {
        log.debug("Fetching collaborator with national ID: {}", nationalId);
        Collaborator collaborator = findCollaboratorByNationalId(nationalId);
        return collaboratorMapper.toResponse(collaborator);
    }

    @Override
    public CollaboratorDetailResponse getCollaboratorDetailByNationalId(String nationalId) {
        log.debug("Fetching collaborator details with national ID: {}", nationalId);
        Collaborator collaborator = findCollaboratorByNationalId(nationalId);

        CollaboratorDetailResponse response = collaboratorMapper.toDetailResponse(collaborator);

        // Enrich with performance data
        enrichWithPerformanceData(response, nationalId);

        return response;
    }

    @Override
    public Page<CollaboratorResponse> getAllCollaborators(Pageable pageable) {
        log.debug("Fetching all collaborators with pagination");
        return collaboratorRepository.findAll(pageable)
                .map(collaboratorMapper::toResponse);
    }

    @Override
    public Page<CollaboratorResponse> getCollaboratorsWithFilters(
            CollaboratorStatus status,
            String department,
            String searchTerm,
            Pageable pageable) {
        log.debug("Fetching collaborators with filters - status: {}, department: {}, search: {}",
                status, department, searchTerm);
        return collaboratorRepository.findWithFilters(status, department, searchTerm, pageable)
                .map(collaboratorMapper::toResponse);
    }

    @Override
    @Transactional
    public CollaboratorResponse updateCollaborator(String nationalId, CollaboratorRequest request) {
        log.info("Updating collaborator with national ID: {}", nationalId);

        Collaborator collaborator = findCollaboratorByNationalId(nationalId);
        validateUniqueConstraints(request, nationalId);

        collaboratorMapper.updateEntity(collaborator, request);
        Collaborator updatedCollaborator = collaboratorRepository.save(collaborator);

        log.info("Updated collaborator with national ID: {}", nationalId);
        return collaboratorMapper.toResponse(updatedCollaborator);
    }

    @Override
    @Transactional
    public void deleteCollaborator(String nationalId) {
        log.info("Deleting collaborator with national ID: {}", nationalId);

        if (!collaboratorRepository.existsById(nationalId)) {
            throw new CollaboratorNotFoundException(nationalId);
        }

        collaboratorRepository.deleteById(nationalId);
        log.info("Deleted collaborator with national ID: {}", nationalId);
    }

    @Override
    public boolean existsByNationalId(String nationalId) {
        return collaboratorRepository.existsById(nationalId);
    }

    private Collaborator findCollaboratorByNationalId(String nationalId) {
        return collaboratorRepository.findById(nationalId)
                .orElseThrow(() -> new CollaboratorNotFoundException(nationalId));
    }

    private String generateEmployeeCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "EMP-" + timestamp + "-" + random;
    }

    private void validateUniqueConstraints(CollaboratorRequest request, String excludeNationalId) {
        if (excludeNationalId == null) {
            if (collaboratorRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Collaborator", "email", request.getEmail());
            }
            if (collaboratorRepository.existsByEmployeeCode(request.getEmployeeCode())) {
                throw new DuplicateResourceException("Collaborator", "employeeCode", request.getEmployeeCode());
            }
        } else {
            if (collaboratorRepository.existsByEmailAndNationalIdNot(request.getEmail(), excludeNationalId)) {
                throw new DuplicateResourceException("Collaborator", "email", request.getEmail());
            }
            if (collaboratorRepository.existsByEmployeeCodeAndNationalIdNot(request.getEmployeeCode(), excludeNationalId)) {
                throw new DuplicateResourceException("Collaborator", "employeeCode", request.getEmployeeCode());
            }
        }
    }

    private void enrichWithPerformanceData(CollaboratorDetailResponse response, String collaboratorId) {
        // Get average rating
        Optional<BigDecimal> avgRating = performanceReviewRepository
                .calculateAverageRatingByCollaboratorId(collaboratorId);
        response.setAverageRating(avgRating.orElse(null));

        // Get total reviews count
        long totalReviews = performanceReviewRepository.countByCollaboratorId(collaboratorId);
        response.setTotalReviews((int) totalReviews);

        // Determine renewal eligibility
        boolean isEligible = avgRating.isPresent() &&
                avgRating.get().compareTo(new BigDecimal("3.00")) >= 0;
        response.setIsEligibleForRenewal(isEligible);

        // Get latest review
        Optional<PerformanceReview> latestReview = performanceReviewRepository
                .findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc(collaboratorId);
        latestReview.ifPresent(review ->
                response.setLatestReview(performanceReviewMapper.toResponse(review)));
    }
}
