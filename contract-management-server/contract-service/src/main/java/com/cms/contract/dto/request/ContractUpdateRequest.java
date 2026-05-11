package com.cms.contract.dto.request;

import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class ContractUpdateRequest {

    private ContractType contractType;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.00", message = "Salary must be non-negative")
    private BigDecimal salary;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;

    @Size(max = 10000, message = "Terms and conditions must not exceed 10000 characters")
    private String termsAndConditions;

    private ContractStatus status;

    private Boolean autoRenewal;

    @Min(value = 0, message = "Notice period must be non-negative")
    private Integer noticePeriodDays;
}
