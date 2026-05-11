package com.cms.contract.repository;

import com.cms.contract.entity.Contract;
import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {

    Optional<Contract> findByContractNumber(String contractNumber);

    Optional<Contract> findByCollaboratorIdAndStatus(String collaboratorId, ContractStatus status);

    List<Contract> findByCollaboratorId(String collaboratorId);

    Optional<Contract> findFirstByCollaboratorIdOrderByCreatedAtDesc(String collaboratorId);

    List<Contract> findByStatus(ContractStatus status);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    boolean existsByCollaboratorIdAndStatusIn(String collaboratorId, List<ContractStatus> statuses);

    boolean existsByContractNumber(String contractNumber);

    @Query("SELECT c FROM Contract c WHERE c.endDate BETWEEN :startDate AND :endDate AND c.status = :status")
    List<Contract> findExpiringContracts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ContractStatus status
    );

    @Query("SELECT c FROM Contract c WHERE c.endDate <= :date AND c.status = 'ACTIVE'")
    List<Contract> findExpiredActiveContracts(@Param("date") LocalDate date);

    @Query("SELECT c FROM Contract c WHERE " +
            "c.status = 'ACTIVE' AND " +
            "c.endDate BETWEEN CURRENT_DATE AND :expiryDate")
    List<Contract> findContractsExpiringSoon(@Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT c FROM Contract c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:contractType IS NULL OR c.contractType = :contractType) AND " +
            "(:collaboratorId IS NULL OR c.collaboratorId = :collaboratorId)")
    Page<Contract> findWithFilters(
            @Param("status") ContractStatus status,
            @Param("contractType") ContractType contractType,
            @Param("collaboratorId") String collaboratorId,
            Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countByStatus(@Param("status") ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE c.previousContractId = :contractId")
    Optional<Contract> findRenewalContract(@Param("contractId") UUID contractId);

    List<Contract> findByPreviousContractId(UUID previousContractId);

    @Query("SELECT c FROM Contract c WHERE c.autoRenewal = true AND c.status = 'ACTIVE' " +
            "AND c.endDate BETWEEN CURRENT_DATE AND :date")
    List<Contract> findAutoRenewalContractsExpiringSoon(@Param("date") LocalDate date);
}
