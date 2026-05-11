package com.cms.collaborator.dto.response;

import com.cms.collaborator.entity.enums.PerformanceCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceReviewResponse {

    private UUID id;
    private String collaboratorId;
    private String collaboratorName;
    private String reviewerName;
    private String reviewerEmail;
    private LocalDate reviewPeriodStart;
    private LocalDate reviewPeriodEnd;
    private BigDecimal rating;
    private PerformanceCategory performanceCategory;
    private String strengths;
    private String areasForImprovement;
    private String comments;
    private Boolean isEligibleRenewal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
