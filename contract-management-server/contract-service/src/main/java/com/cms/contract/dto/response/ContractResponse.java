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
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractResponse {

    private UUID id;
    private String contractNumber;
    private String collaboratorId;
    private ContractType contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal salary;
    private String currency;
    private String termsAndConditions;
    private ContractStatus status;
    private UUID previousContractId;
    private Integer renewalCount;
    private Boolean autoRenewal;
    private Integer noticePeriodDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long daysUntilExpiry;
    private Boolean isExpiringSoon;
}
