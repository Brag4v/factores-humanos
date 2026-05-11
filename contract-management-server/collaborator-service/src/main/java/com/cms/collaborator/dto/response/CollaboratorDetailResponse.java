package com.cms.collaborator.dto.response;

import com.cms.collaborator.entity.enums.CollaboratorStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollaboratorDetailResponse {

    private String nationalId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private CollaboratorStatus status;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Performance summary
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Boolean isEligibleForRenewal;
    private PerformanceReviewResponse latestReview;
}
