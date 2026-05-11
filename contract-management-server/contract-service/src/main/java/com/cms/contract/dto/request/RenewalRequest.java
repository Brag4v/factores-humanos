package com.cms.contract.dto.request;

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
public class RenewalRequest {

    @NotNull(message = "New end date is required")
    @Future(message = "New end date must be in the future")
    private LocalDate newEndDate;

    @DecimalMin(value = "0.00", message = "New salary must be non-negative")
    private BigDecimal newSalary;

    @Size(max = 10000, message = "New terms and conditions must not exceed 10000 characters")
    private String newTermsAndConditions;

    private Boolean autoRenewal;

    @Min(value = 0, message = "Notice period must be non-negative")
    private Integer noticePeriodDays;

    @Size(max = 1000, message = "Renewal notes must not exceed 1000 characters")
    private String renewalNotes;
}
