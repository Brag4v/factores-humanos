package com.cms.collaborator.repository;

import com.cms.collaborator.entity.Collaborator;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
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

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, String>, JpaSpecificationExecutor<Collaborator> {

    Optional<Collaborator> findByEmail(String email);

    Optional<Collaborator> findByEmployeeCode(String employeeCode);

    List<Collaborator> findByStatus(CollaboratorStatus status);

    Page<Collaborator> findByStatus(CollaboratorStatus status, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmailAndNationalIdNot(String email, String nationalId);

    boolean existsByEmployeeCodeAndNationalIdNot(String employeeCode, String nationalId);

    List<Collaborator> findByDepartment(String department);

    Page<Collaborator> findByDepartment(String department, Pageable pageable);

    @Query("SELECT c FROM Collaborator c WHERE c.status = :status AND c.department = :department")
    List<Collaborator> findByStatusAndDepartment(
            @Param("status") CollaboratorStatus status,
            @Param("department") String department
    );

    @Query("SELECT c FROM Collaborator c WHERE c.hireDate BETWEEN :startDate AND :endDate")
    List<Collaborator> findByHireDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT DISTINCT c FROM Collaborator c JOIN c.performanceReviews pr " +
            "WHERE pr.reviewPeriodEnd >= :sinceDate")
    List<Collaborator> findCollaboratorsWithRecentReviews(@Param("sinceDate") LocalDate sinceDate);

    @Query("SELECT c FROM Collaborator c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:department IS NULL OR c.department = :department) AND " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(CAST(c.firstName AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) " +
            "OR LOWER(CAST(c.lastName AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) " +
            "OR LOWER(CAST(c.email AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')))")
    Page<Collaborator> findWithFilters(
            @Param("status") CollaboratorStatus status,
            @Param("department") String department,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.status = :status")
    long countByStatus(@Param("status") CollaboratorStatus status);
}
