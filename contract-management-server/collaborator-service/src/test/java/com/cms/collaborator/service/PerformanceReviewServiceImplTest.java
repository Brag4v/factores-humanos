package com.cms.collaborator.service;

import com.cms.collaborator.dto.request.PerformanceReviewRequest;
import com.cms.collaborator.dto.response.AverageRatingResponse;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import com.cms.collaborator.entity.Collaborator;
import com.cms.collaborator.entity.PerformanceReview;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
import com.cms.collaborator.entity.enums.PerformanceCategory;
import com.cms.collaborator.exception.CollaboratorNotFoundException;
import com.cms.collaborator.mapper.PerformanceReviewMapper;
import com.cms.collaborator.repository.CollaboratorRepository;
import com.cms.collaborator.repository.PerformanceReviewRepository;
import com.cms.common.exception.DuplicateResourceException;
import com.cms.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceReviewServiceImplTest {

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private PerformanceReviewMapper performanceReviewMapper;

    @InjectMocks
    private PerformanceReviewServiceImpl performanceReviewService;

    private Collaborator collaborator;
    private PerformanceReview review;
    private PerformanceReviewRequest reviewRequest;
    private PerformanceReviewResponse reviewResponse;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();

        collaborator = Collaborator.builder()
                .nationalId("NAT001")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .position("Developer")
                .status(CollaboratorStatus.ACTIVE)
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();

        review = PerformanceReview.builder()
                .id(reviewId)
                .reviewerName("Jane Manager")
                .reviewerEmail("jane@example.com")
                .reviewPeriodStart(LocalDate.of(2024, 1, 1))
                .reviewPeriodEnd(LocalDate.of(2024, 6, 30))
                .rating(new BigDecimal("4.50"))
                .performanceCategory(PerformanceCategory.EXCEEDS_EXPECTATIONS)
                .build();
        review.setCollaborator(collaborator);

        reviewRequest = PerformanceReviewRequest.builder()
                .collaboratorId("NAT001")
                .reviewerName("Jane Manager")
                .reviewerEmail("jane@example.com")
                .reviewPeriodStart(LocalDate.of(2024, 1, 1))
                .reviewPeriodEnd(LocalDate.of(2024, 6, 30))
                .rating(new BigDecimal("4.50"))
                .performanceCategory(PerformanceCategory.EXCEEDS_EXPECTATIONS)
                .build();

        reviewResponse = PerformanceReviewResponse.builder()
                .id(reviewId)
                .collaboratorId("NAT001")
                .collaboratorName("John Doe")
                .reviewerName("Jane Manager")
                .rating(new BigDecimal("4.50"))
                .performanceCategory(PerformanceCategory.EXCEEDS_EXPECTATIONS)
                .build();
    }

    @Nested
    class SubmitReview {

        @Test
        void shouldSubmitReviewSuccessfully() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.existsByCollaboratorNationalIdAndReviewPeriodStartAndReviewPeriodEnd(
                    "NAT001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))).thenReturn(false);
            when(performanceReviewMapper.toEntity(reviewRequest)).thenReturn(review);
            when(performanceReviewRepository.save(review)).thenReturn(review);
            when(performanceReviewMapper.toResponse(review)).thenReturn(reviewResponse);

            PerformanceReviewResponse result = performanceReviewService.submitReview(reviewRequest);

            assertThat(result.getRating()).isEqualTo(new BigDecimal("4.50"));
            assertThat(result.getCollaboratorId()).isEqualTo("NAT001");
            verify(performanceReviewRepository).save(review);
        }

        @Test
        void shouldThrowWhenCollaboratorNotFound() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> performanceReviewService.submitReview(reviewRequest))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }

        @Test
        void shouldThrowWhenDuplicateReviewPeriod() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.existsByCollaboratorNationalIdAndReviewPeriodStartAndReviewPeriodEnd(
                    "NAT001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))).thenReturn(true);

            assertThatThrownBy(() -> performanceReviewService.submitReview(reviewRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void shouldSetCollaboratorOnReview() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.existsByCollaboratorNationalIdAndReviewPeriodStartAndReviewPeriodEnd(
                    any(), any(), any())).thenReturn(false);
            PerformanceReview newReview = PerformanceReview.builder().build();
            when(performanceReviewMapper.toEntity(reviewRequest)).thenReturn(newReview);
            when(performanceReviewRepository.save(newReview)).thenReturn(newReview);
            when(performanceReviewMapper.toResponse(newReview)).thenReturn(reviewResponse);

            performanceReviewService.submitReview(reviewRequest);

            assertThat(newReview.getCollaborator()).isEqualTo(collaborator);
        }
    }

    @Nested
    class GetReviewById {

        @Test
        void shouldReturnReviewById() {
            when(performanceReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(performanceReviewMapper.toResponse(review)).thenReturn(reviewResponse);

            PerformanceReviewResponse result = performanceReviewService.getReviewById(reviewId);

            assertThat(result.getId()).isEqualTo(reviewId);
        }

        @Test
        void shouldThrowWhenReviewNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(performanceReviewRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> performanceReviewService.getReviewById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetReviewsByCollaborator {

        @Test
        void shouldReturnPagedReviews() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PerformanceReview> page = new PageImpl<>(List.of(review));

            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.findByCollaboratorNationalId("NAT001", pageable)).thenReturn(page);
            when(performanceReviewMapper.toResponse(review)).thenReturn(reviewResponse);

            Page<PerformanceReviewResponse> result = performanceReviewService.getReviewsByCollaborator("NAT001", pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        void shouldThrowWhenCollaboratorDoesNotExist() {
            Pageable pageable = PageRequest.of(0, 10);
            when(collaboratorRepository.existsById("INVALID")).thenReturn(false);

            assertThatThrownBy(() -> performanceReviewService.getReviewsByCollaborator("INVALID", pageable))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }
    }

    @Nested
    class GetAllReviewsByCollaborator {

        @Test
        void shouldReturnAllReviews() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.findByCollaboratorNationalIdOrderByCreatedAtDesc("NAT001"))
                    .thenReturn(List.of(review));
            when(performanceReviewMapper.toResponseList(List.of(review))).thenReturn(List.of(reviewResponse));

            List<PerformanceReviewResponse> result = performanceReviewService.getAllReviewsByCollaborator("NAT001");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class GetLatestReview {

        @Test
        void shouldReturnLatestReview() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc("NAT001"))
                    .thenReturn(Optional.of(review));
            when(performanceReviewMapper.toResponse(review)).thenReturn(reviewResponse);

            PerformanceReviewResponse result = performanceReviewService.getLatestReview("NAT001");

            assertThat(result.getId()).isEqualTo(reviewId);
        }

        @Test
        void shouldThrowWhenNoReviewsExist() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc("NAT001"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> performanceReviewService.getLatestReview("NAT001"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class CalculateAverageRating {

        @Test
        void shouldCalculateAverageRatingWithEligibility() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("4.25")));
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(5L);

            AverageRatingResponse result = performanceReviewService.calculateAverageRating("NAT001");

            assertThat(result.getCollaboratorId()).isEqualTo("NAT001");
            assertThat(result.getCollaboratorName()).isEqualTo("John Doe");
            assertThat(result.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.25"));
            assertThat(result.getTotalReviews()).isEqualTo(5L);
            assertThat(result.getIsEligibleForRenewal()).isTrue();
        }

        @Test
        void shouldReturnNotEligibleWhenBelowThreshold() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("2.50")));
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(2L);

            AverageRatingResponse result = performanceReviewService.calculateAverageRating("NAT001");

            assertThat(result.getIsEligibleForRenewal()).isFalse();
        }

        @Test
        void shouldReturnNullRatingWhenNoReviews() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.empty());
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(0L);

            AverageRatingResponse result = performanceReviewService.calculateAverageRating("NAT001");

            assertThat(result.getAverageRating()).isNull();
            assertThat(result.getIsEligibleForRenewal()).isFalse();
        }

        @Test
        void shouldBeEligibleAtExactlyThreshold() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("3.00")));
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(1L);

            AverageRatingResponse result = performanceReviewService.calculateAverageRating("NAT001");

            assertThat(result.getIsEligibleForRenewal()).isTrue();
        }

        @Test
        void shouldThrowWhenCollaboratorNotFound() {
            when(collaboratorRepository.findById("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> performanceReviewService.calculateAverageRating("INVALID"))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }
    }

    @Nested
    class IsEligibleForRenewal {

        @Test
        void shouldReturnTrueWhenAboveThreshold() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("4.00")));

            assertThat(performanceReviewService.isEligibleForRenewal("NAT001")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenBelowThreshold() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("2.99")));

            assertThat(performanceReviewService.isEligibleForRenewal("NAT001")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNoRating() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.empty());

            assertThat(performanceReviewService.isEligibleForRenewal("NAT001")).isFalse();
        }

        @Test
        void shouldThrowWhenCollaboratorDoesNotExist() {
            when(collaboratorRepository.existsById("INVALID")).thenReturn(false);

            assertThatThrownBy(() -> performanceReviewService.isEligibleForRenewal("INVALID"))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }
    }
}
