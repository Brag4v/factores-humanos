package com.cms.collaborator.dto.request;

import com.cms.collaborator.entity.enums.PerformanceCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewRequest {

    @NotBlank(message = "Collaborator National ID is required")
    private String collaboratorId;

    @NotBlank(message = "Reviewer name is required")
    @Size(max = 200, message = "Reviewer name must not exceed 200 characters")
    private String reviewerName;

    @Email(message = "Reviewer email must be valid")
    @Size(max = 255, message = "Reviewer email must not exceed 255 characters")
    private String reviewerEmail;

    @NotNull(message = "Review period start date is required")
    private LocalDate reviewPeriodStart;

    @NotNull(message = "Review period end date is required")
    private LocalDate reviewPeriodEnd;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.00", message = "Rating must be at least 1.00")
    @DecimalMax(value = "5.00", message = "Rating must not exceed 5.00")
    private BigDecimal rating;

    @NotNull(message = "Performance category is required")
    private PerformanceCategory performanceCategory;

    @Size(max = 5000, message = "Strengths must not exceed 5000 characters")
    private String strengths;

    @Size(max = 5000, message = "Areas for improvement must not exceed 5000 characters")
    private String areasForImprovement;

    @Size(max = 5000, message = "Comments must not exceed 5000 characters")
    private String comments;

    @AssertTrue(message = "Review period end date must be after or equal to start date")
    private boolean isValidDateRange() {
        if (reviewPeriodStart == null || reviewPeriodEnd == null) {
            return true;
        }
        return !reviewPeriodEnd.isBefore(reviewPeriodStart);
    }
}
