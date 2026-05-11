package com.cms.contract.dto.response;

import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpiringContractResponse {

    private UUID id;
    private String contractNumber;
    private String collaboratorId;
    private String collaboratorName;
    private String collaboratorEmail;
    private ContractType contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long daysUntilExpiry;
    private BigDecimal salary;
    private String currency;
    private ContractStatus status;
    private Integer renewalCount;
    private Boolean autoRenewal;
    private Boolean isEligibleForRenewal;
    private BigDecimal averagePerformanceRating;
}
