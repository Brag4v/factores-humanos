package com.cms.collaborator.service;

import com.cms.collaborator.dto.request.PerformanceReviewRequest;
import com.cms.collaborator.dto.response.AverageRatingResponse;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import com.cms.collaborator.entity.Collaborator;
import com.cms.collaborator.entity.PerformanceReview;
import com.cms.collaborator.exception.CollaboratorNotFoundException;
import com.cms.collaborator.mapper.PerformanceReviewMapper;
import com.cms.collaborator.repository.CollaboratorRepository;
import com.cms.collaborator.repository.PerformanceReviewRepository;
import com.cms.common.exception.DuplicateResourceException;
import com.cms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceReviewServiceImpl implements PerformanceReviewService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final PerformanceReviewMapper performanceReviewMapper;

    private static final BigDecimal RENEWAL_THRESHOLD = new BigDecimal("3.00");

    @Override
    @Transactional
    public PerformanceReviewResponse submitReview(PerformanceReviewRequest request) {
        log.info("Submitting performance review for collaborator: {}", request.getCollaboratorId());

        // Validate collaborator exists
        Collaborator collaborator = collaboratorRepository.findById(request.getCollaboratorId())
                .orElseThrow(() -> new CollaboratorNotFoundException(request.getCollaboratorId()));

        // Check for duplicate review period
        if (performanceReviewRepository.existsByCollaboratorNationalIdAndReviewPeriodStartAndReviewPeriodEnd(
                request.getCollaboratorId(),
                request.getReviewPeriodStart(),
                request.getReviewPeriodEnd())) {
            throw new DuplicateResourceException(
                    "A performance review already exists for this collaborator and review period");
        }

        PerformanceReview review = performanceReviewMapper.toEntity(request);
        review.setCollaborator(collaborator);

        PerformanceReview savedReview = performanceReviewRepository.save(review);
        log.info("Created performance review with ID: {}", savedReview.getId());

        return performanceReviewMapper.toResponse(savedReview);
    }

    @Override
    public PerformanceReviewResponse getReviewById(UUID id) {
        log.debug("Fetching performance review with ID: {}", id);
        PerformanceReview review = findReviewById(id);
        return performanceReviewMapper.toResponse(review);
    }

    @Override
    public Page<PerformanceReviewResponse> getReviewsByCollaborator(String collaboratorId, Pageable pageable) {
        log.debug("Fetching performance reviews for collaborator: {}", collaboratorId);
        validateCollaboratorExists(collaboratorId);
        return performanceReviewRepository.findByCollaboratorNationalId(collaboratorId, pageable)
                .map(performanceReviewMapper::toResponse);
    }

    @Override
    public List<PerformanceReviewResponse> getAllReviewsByCollaborator(String collaboratorId) {
        log.debug("Fetching all performance reviews for collaborator: {}", collaboratorId);
        validateCollaboratorExists(collaboratorId);
        List<PerformanceReview> reviews = performanceReviewRepository
                .findByCollaboratorNationalIdOrderByCreatedAtDesc(collaboratorId);
        return performanceReviewMapper.toResponseList(reviews);
    }

    @Override
    public PerformanceReviewResponse getLatestReview(String collaboratorId) {
        log.debug("Fetching latest performance review for collaborator: {}", collaboratorId);
        validateCollaboratorExists(collaboratorId);

        return performanceReviewRepository.findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc(collaboratorId)
                .map(performanceReviewMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No performance reviews found for collaborator with id: " + collaboratorId));
    }

    @Override
    public AverageRatingResponse calculateAverageRating(String collaboratorId) {
        log.debug("Calculating average rating for collaborator: {}", collaboratorId);

        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new CollaboratorNotFoundException(collaboratorId));

        Optional<BigDecimal> avgRating = performanceReviewRepository
                .calculateAverageRatingByCollaboratorId(collaboratorId);
        long totalReviews = performanceReviewRepository.countByCollaboratorId(collaboratorId);

        BigDecimal averageRating = avgRating
                .map(rating -> rating.setScale(2, RoundingMode.HALF_UP))
                .orElse(null);

        boolean isEligible = averageRating != null &&
                averageRating.compareTo(RENEWAL_THRESHOLD) >= 0;

        return AverageRatingResponse.builder()
                .collaboratorId(collaboratorId)
                .collaboratorName(collaborator.getFullName())
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .isEligibleForRenewal(isEligible)
                .build();
    }

    @Override
    public boolean isEligibleForRenewal(String collaboratorId) {
        log.debug("Checking renewal eligibility for collaborator: {}", collaboratorId);
        validateCollaboratorExists(collaboratorId);

        Optional<BigDecimal> avgRating = performanceReviewRepository
                .calculateAverageRatingByCollaboratorId(collaboratorId);

        return avgRating.isPresent() && avgRating.get().compareTo(RENEWAL_THRESHOLD) >= 0;
    }

    private PerformanceReview findReviewById(UUID id) {
        return performanceReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", id));
    }

    private void validateCollaboratorExists(String collaboratorId) {
        if (!collaboratorRepository.existsById(collaboratorId)) {
            throw new CollaboratorNotFoundException(collaboratorId);
        }
    }
}
