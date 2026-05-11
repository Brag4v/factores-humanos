package com.cms.contract.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminationRequest {

    @NotBlank(message = "Termination reason is required")
    @Size(max = 1000, message = "Termination reason must not exceed 1000 characters")
    private String reason;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    private String additionalNotes;
}
