package com.cms.contract.entity;

import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_contract_collaborator", columnList = "collaborator_id"),
        @Index(name = "idx_contract_status", columnList = "status"),
        @Index(name = "idx_contract_end_date", columnList = "end_date"),
        @Index(name = "idx_contract_number", columnList = "contract_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Contract number is required")
    @Size(max = 50, message = "Contract number must not exceed 50 characters")
    @Column(name = "contract_number", unique = true, nullable = false, length = 50)
    private String contractNumber;

    @NotBlank(message = "Collaborator ID is required")
    @Column(name = "collaborator_id", nullable = false, length = 20)
    private String collaboratorId;

    @NotNull(message = "Contract type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 20)
    private ContractType contractType;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @DecimalMin(value = "0.00", message = "Salary must be non-negative")
    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.PENDING;

    @Column(name = "previous_contract_id")
    private UUID previousContractId;

    @Column(name = "renewal_count")
    @Builder.Default
    private Integer renewalCount = 0;

    @Column(name = "auto_renewal")
    @Builder.Default
    private Boolean autoRenewal = false;

    @Column(name = "notice_period_days")
    @Builder.Default
    private Integer noticePeriodDays = 30;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    // Helper methods
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int daysThreshold) {
        if (endDate == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(daysThreshold);
        return !endDate.isBefore(LocalDate.now()) && !endDate.isAfter(threshold);
    }

    public long getDaysUntilExpiry() {
        if (endDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    public boolean isActive() {
        return status == ContractStatus.ACTIVE && !isExpired();
    }
}
