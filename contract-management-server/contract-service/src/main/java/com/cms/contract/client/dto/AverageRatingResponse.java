package com.cms.contract.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageRatingResponse {

    private String collaboratorId;
    private String collaboratorName;
    private BigDecimal averageRating;
    private Long totalReviews;
    private Boolean isEligibleForRenewal;
}
