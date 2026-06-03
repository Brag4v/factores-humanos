package com.cms.collaborator.service;

import com.cms.collaborator.dto.request.CollaboratorRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollaboratorServiceImplTest {

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private CollaboratorMapper collaboratorMapper;

    @Mock
    private PerformanceReviewMapper performanceReviewMapper;

    @InjectMocks
    private CollaboratorServiceImpl collaboratorService;

    private Collaborator collaborator;
    private CollaboratorRequest request;
    private CollaboratorResponse response;

    @BeforeEach
    void setUp() {
        collaborator = Collaborator.builder()
                .nationalId("NAT001")
                .employeeCode("EMP-001")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("Engineering")
                .position("Developer")
                .status(CollaboratorStatus.ACTIVE)
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();

        request = CollaboratorRequest.builder()
                .nationalId("NAT001")
                .employeeCode("EMP-001")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("Engineering")
                .position("Developer")
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();

        response = CollaboratorResponse.builder()
                .nationalId("NAT001")
                .employeeCode("EMP-001")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .email("john@example.com")
                .department("Engineering")
                .position("Developer")
                .status(CollaboratorStatus.ACTIVE)
                .build();
    }

    @Nested
    class CreateCollaborator {

        @Test
        void shouldCreateCollaboratorSuccessfully() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(false);
            when(collaboratorRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(collaboratorRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(collaboratorMapper.toEntity(request)).thenReturn(collaborator);
            when(collaboratorRepository.save(collaborator)).thenReturn(collaborator);
            when(collaboratorMapper.toResponse(collaborator)).thenReturn(response);

            CollaboratorResponse result = collaboratorService.createCollaborator(request);

            assertThat(result).isEqualTo(response);
            verify(collaboratorRepository).save(collaborator);
        }

        @Test
        void shouldThrowWhenNationalIdAlreadyExists() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);

            assertThatThrownBy(() -> collaboratorService.createCollaborator(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(collaboratorRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(false);
            when(collaboratorRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> collaboratorService.createCollaborator(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void shouldThrowWhenEmployeeCodeAlreadyExists() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(false);
            when(collaboratorRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(collaboratorRepository.existsByEmployeeCode("EMP-001")).thenReturn(true);

            assertThatThrownBy(() -> collaboratorService.createCollaborator(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void shouldGenerateEmployeeCodeWhenBlank() {
            request.setEmployeeCode(null);

            when(collaboratorRepository.existsById("NAT001")).thenReturn(false);
            when(collaboratorRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(collaboratorRepository.existsByEmployeeCode(any())).thenReturn(false);
            when(collaboratorMapper.toEntity(request)).thenReturn(collaborator);
            when(collaboratorRepository.save(collaborator)).thenReturn(collaborator);
            when(collaboratorMapper.toResponse(collaborator)).thenReturn(response);

            collaboratorService.createCollaborator(request);

            assertThat(request.getEmployeeCode()).startsWith("EMP-");
        }

        @Test
        void shouldSetActiveStatusWhenNull() {
            Collaborator entityWithNullStatus = Collaborator.builder()
                    .nationalId("NAT001")
                    .build();

            when(collaboratorRepository.existsById("NAT001")).thenReturn(false);
            when(collaboratorRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(collaboratorRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(collaboratorMapper.toEntity(request)).thenReturn(entityWithNullStatus);
            when(collaboratorRepository.save(entityWithNullStatus)).thenReturn(entityWithNullStatus);
            when(collaboratorMapper.toResponse(entityWithNullStatus)).thenReturn(response);

            collaboratorService.createCollaborator(request);

            assertThat(entityWithNullStatus.getStatus()).isEqualTo(CollaboratorStatus.ACTIVE);
        }
    }

    @Nested
    class GetCollaborator {

        @Test
        void shouldReturnCollaboratorByNationalId() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(collaboratorMapper.toResponse(collaborator)).thenReturn(response);

            CollaboratorResponse result = collaboratorService.getCollaboratorByNationalId("NAT001");

            assertThat(result.getNationalId()).isEqualTo("NAT001");
        }

        @Test
        void shouldThrowWhenCollaboratorNotFound() {
            when(collaboratorRepository.findById("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collaboratorService.getCollaboratorByNationalId("INVALID"))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }
    }

    @Nested
    class GetCollaboratorDetail {

        @Test
        void shouldReturnDetailWithPerformanceData() {
            CollaboratorDetailResponse detailResponse = CollaboratorDetailResponse.builder()
                    .nationalId("NAT001")
                    .fullName("John Doe")
                    .build();

            PerformanceReview review = PerformanceReview.builder()
                    .rating(new BigDecimal("4.50"))
                    .build();
            review.setCollaborator(collaborator);

            PerformanceReviewResponse reviewResponse = PerformanceReviewResponse.builder()
                    .rating(new BigDecimal("4.50"))
                    .build();

            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(collaboratorMapper.toDetailResponse(collaborator)).thenReturn(detailResponse);
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("4.00")));
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(3L);
            when(performanceReviewRepository.findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc("NAT001"))
                    .thenReturn(Optional.of(review));
            when(performanceReviewMapper.toResponse(review)).thenReturn(reviewResponse);

            CollaboratorDetailResponse result = collaboratorService.getCollaboratorDetailByNationalId("NAT001");

            assertThat(result.getAverageRating()).isEqualTo(new BigDecimal("4.00"));
            assertThat(result.getTotalReviews()).isEqualTo(3);
            assertThat(result.getIsEligibleForRenewal()).isTrue();
            assertThat(result.getLatestReview()).isEqualTo(reviewResponse);
        }

        @Test
        void shouldReturnNotEligibleWhenRatingBelow3() {
            CollaboratorDetailResponse detailResponse = CollaboratorDetailResponse.builder()
                    .nationalId("NAT001")
                    .build();

            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(collaboratorMapper.toDetailResponse(collaborator)).thenReturn(detailResponse);
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.of(new BigDecimal("2.50")));
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(2L);
            when(performanceReviewRepository.findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc("NAT001"))
                    .thenReturn(Optional.empty());

            CollaboratorDetailResponse result = collaboratorService.getCollaboratorDetailByNationalId("NAT001");

            assertThat(result.getIsEligibleForRenewal()).isFalse();
        }

        @Test
        void shouldReturnNotEligibleWhenNoReviews() {
            CollaboratorDetailResponse detailResponse = CollaboratorDetailResponse.builder()
                    .nationalId("NAT001")
                    .build();

            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(collaboratorMapper.toDetailResponse(collaborator)).thenReturn(detailResponse);
            when(performanceReviewRepository.calculateAverageRatingByCollaboratorId("NAT001"))
                    .thenReturn(Optional.empty());
            when(performanceReviewRepository.countByCollaboratorId("NAT001")).thenReturn(0L);
            when(performanceReviewRepository.findTopByCollaboratorNationalIdOrderByReviewPeriodEndDesc("NAT001"))
                    .thenReturn(Optional.empty());

            CollaboratorDetailResponse result = collaboratorService.getCollaboratorDetailByNationalId("NAT001");

            assertThat(result.getIsEligibleForRenewal()).isFalse();
            assertThat(result.getAverageRating()).isNull();
        }
    }

    @Nested
    class GetAllCollaborators {

        @Test
        void shouldReturnPagedCollaborators() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Collaborator> page = new PageImpl<>(List.of(collaborator));

            when(collaboratorRepository.findAll(pageable)).thenReturn(page);
            when(collaboratorMapper.toResponse(collaborator)).thenReturn(response);

            Page<CollaboratorResponse> result = collaboratorService.getAllCollaborators(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNationalId()).isEqualTo("NAT001");
        }
    }

    @Nested
    class GetCollaboratorsWithFilters {

        @Test
        void shouldReturnFilteredCollaborators() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Collaborator> page = new PageImpl<>(List.of(collaborator));

            when(collaboratorRepository.findWithFilters(
                    CollaboratorStatus.ACTIVE, "Engineering", "John", pageable))
                    .thenReturn(page);
            when(collaboratorMapper.toResponse(collaborator)).thenReturn(response);

            Page<CollaboratorResponse> result = collaboratorService.getCollaboratorsWithFilters(
                    CollaboratorStatus.ACTIVE, "Engineering", "John", pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    class UpdateCollaborator {

        @Test
        void shouldUpdateCollaboratorSuccessfully() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(collaboratorRepository.existsByEmailAndNationalIdNot("john@example.com", "NAT001")).thenReturn(false);
            when(collaboratorRepository.existsByEmployeeCodeAndNationalIdNot("EMP-001", "NAT001")).thenReturn(false);
            when(collaboratorRepository.save(collaborator)).thenReturn(collaborator);
            when(collaboratorMapper.toResponse(collaborator)).thenReturn(response);

            CollaboratorResponse result = collaboratorService.updateCollaborator("NAT001", request);

            assertThat(result).isEqualTo(response);
            verify(collaboratorMapper).updateEntity(collaborator, request);
        }

        @Test
        void shouldThrowWhenUpdatingNonExistentCollaborator() {
            when(collaboratorRepository.findById("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collaboratorService.updateCollaborator("INVALID", request))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }

        @Test
        void shouldThrowWhenEmailConflictsOnUpdate() {
            when(collaboratorRepository.findById("NAT001")).thenReturn(Optional.of(collaborator));
            when(collaboratorRepository.existsByEmailAndNationalIdNot("john@example.com", "NAT001")).thenReturn(true);

            assertThatThrownBy(() -> collaboratorService.updateCollaborator("NAT001", request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    class DeleteCollaborator {

        @Test
        void shouldDeleteCollaboratorSuccessfully() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);

            collaboratorService.deleteCollaborator("NAT001");

            verify(collaboratorRepository).deleteById("NAT001");
        }

        @Test
        void shouldThrowWhenDeletingNonExistentCollaborator() {
            when(collaboratorRepository.existsById("INVALID")).thenReturn(false);

            assertThatThrownBy(() -> collaboratorService.deleteCollaborator("INVALID"))
                    .isInstanceOf(CollaboratorNotFoundException.class);
        }
    }

    @Nested
    class ExistsByNationalId {

        @Test
        void shouldReturnTrueWhenExists() {
            when(collaboratorRepository.existsById("NAT001")).thenReturn(true);

            assertThat(collaboratorService.existsByNationalId("NAT001")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNotExists() {
            when(collaboratorRepository.existsById("INVALID")).thenReturn(false);

            assertThat(collaboratorService.existsByNationalId("INVALID")).isFalse();
        }
    }
}
