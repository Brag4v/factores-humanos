package com.cms.contract.service;

import com.cms.contract.dto.request.ContractRequest;
import com.cms.contract.dto.request.ContractUpdateRequest;
import com.cms.contract.dto.request.RenewalRequest;
import com.cms.contract.dto.request.TerminationRequest;
import com.cms.contract.dto.response.ContractDocumentResponse;
import com.cms.contract.dto.response.ContractResponse;
import com.cms.contract.dto.response.ExpiringContractResponse;
import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ContractService {

    ContractResponse createContract(ContractRequest request);

    ContractResponse getContractById(UUID id);

    List<ContractResponse> getContractsByCollaborator(String collaboratorId);

    Page<ContractResponse> getAllContracts(Pageable pageable);

    Page<ContractResponse> getContractsWithFilters(
            ContractStatus status,
            ContractType contractType,
            String collaboratorId,
            Pageable pageable
    );

    List<ExpiringContractResponse> getExpiringContracts(int days);

    ContractResponse updateContract(UUID id, ContractUpdateRequest request);

    ContractResponse renewContract(UUID id, RenewalRequest request);

    ContractResponse terminateContract(UUID id, TerminationRequest request);

    boolean existsActiveContractForCollaborator(String collaboratorId);

    ContractDocumentResponse uploadDocument(UUID contractId, MultipartFile file) throws IOException;

    List<ContractDocumentResponse> getDocuments(UUID contractId);

    com.cms.contract.entity.ContractDocument getDocumentData(UUID contractId, UUID documentId);
}
