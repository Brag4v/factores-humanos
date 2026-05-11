package com.cms.contract.dto.request;

import com.cms.contract.entity.enums.ContractType;
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
public class ContractRequest {

    @NotBlank(message = "Collaborator ID is required")
    private String collaboratorId;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @DecimalMin(value = "0.00", message = "Salary must be non-negative")
    private BigDecimal salary;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;

    @Size(max = 10000, message = "Terms and conditions must not exceed 10000 characters")
    private String termsAndConditions;

    private Boolean autoRenewal;

    @Min(value = 0, message = "Notice period must be non-negative")
    private Integer noticePeriodDays;

    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
