package com.cms.contract.repository;

import com.cms.contract.entity.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContractDocumentRepository extends JpaRepository<ContractDocument, UUID> {
    List<ContractDocument> findByContractIdOrderByUploadedAtDesc(UUID contractId);
}
