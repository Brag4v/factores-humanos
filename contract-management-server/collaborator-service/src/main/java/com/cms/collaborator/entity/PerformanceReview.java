package com.cms.collaborator.entity;

import com.cms.collaborator.entity.enums.PerformanceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "performance_reviews", indexes = {
        @Index(name = "idx_review_collaborator", columnList = "collaborator_id"),
        @Index(name = "idx_review_period_end", columnList = "review_period_end"),
        @Index(name = "idx_review_rating", columnList = "rating")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;

    @NotBlank(message = "Reviewer name is required")
    @Size(max = 200, message = "Reviewer name must not exceed 200 characters")
    @Column(name = "reviewer_name", nullable = false, length = 200)
    private String reviewerName;

    @Email(message = "Reviewer email must be valid")
    @Size(max = 255, message = "Reviewer email must not exceed 255 characters")
    @Column(name = "reviewer_email", length = 255)
    private String reviewerEmail;

    @NotNull(message = "Review period start date is required")
    @Column(name = "review_period_start", nullable = false)
    private LocalDate reviewPeriodStart;

    @NotNull(message = "Review period end date is required")
    @Column(name = "review_period_end", nullable = false)
    private LocalDate reviewPeriodEnd;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.00", message = "Rating must be at least 1.00")
    @DecimalMax(value = "5.00", message = "Rating must not exceed 5.00")
    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @NotNull(message = "Performance category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "performance_category", nullable = false, length = 30)
    private PerformanceCategory performanceCategory;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "is_eligible_renewal", nullable = false)
    @Builder.Default
    private Boolean isEligibleRenewal = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void calculateEligibility() {
        // Eligible for renewal if rating is 3.0 or above
        this.isEligibleRenewal = this.rating != null && 
                this.rating.compareTo(new BigDecimal("3.00")) >= 0;
    }

    // Validation for date range
    @AssertTrue(message = "Review period end date must be after start date")
    private boolean isValidDateRange() {
        if (reviewPeriodStart == null || reviewPeriodEnd == null) {
            return true; // Let @NotNull handle null validation
        }
        return !reviewPeriodEnd.isBefore(reviewPeriodStart);
    }
}
