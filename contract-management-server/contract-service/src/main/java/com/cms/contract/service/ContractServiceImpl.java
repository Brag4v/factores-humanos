package com.cms.contract.service;

import com.cms.common.dto.ApiResponse;
import com.cms.common.exception.ResourceNotFoundException;
import com.cms.contract.client.CollaboratorClient;
import com.cms.contract.client.dto.AverageRatingResponse;
import com.cms.contract.client.dto.CollaboratorResponse;
import com.cms.contract.dto.request.ContractRequest;
import com.cms.contract.dto.request.ContractUpdateRequest;
import com.cms.contract.dto.request.RenewalRequest;
import com.cms.contract.dto.request.TerminationRequest;
import com.cms.contract.dto.response.ContractDocumentResponse;
import com.cms.contract.dto.response.ContractResponse;
import com.cms.contract.dto.response.ExpiringContractResponse;
import com.cms.contract.entity.Contract;
import com.cms.contract.entity.ContractDocument;
import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import com.cms.contract.exception.ContractAlreadyExistsException;
import com.cms.contract.exception.ContractNotFoundException;
import com.cms.contract.exception.RenewalNotEligibleException;
import com.cms.contract.mapper.ContractMapper;
import com.cms.contract.repository.ContractDocumentRepository;
import com.cms.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractDocumentRepository documentRepository;
    private final ContractMapper contractMapper;
    private final CollaboratorClient collaboratorClient;

    private static final List<ContractStatus> ACTIVE_STATUSES = List.of(
            ContractStatus.ACTIVE, ContractStatus.PENDING
    );

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        log.info("Creating new contract for collaborator: {}", request.getCollaboratorId());

        // Validate collaborator exists
        validateCollaboratorExists(request.getCollaboratorId());

        // Check no active contract exists for this collaborator
        if (existsActiveContractForCollaborator(request.getCollaboratorId())) {
            throw new ContractAlreadyExistsException(request.getCollaboratorId());
        }

        Contract contract = contractMapper.toEntity(request);
        contract.setContractNumber(generateContractNumber());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setRenewalCount(0);

        Contract savedContract = contractRepository.save(contract);
        log.info("Created contract with ID: {} and number: {}", savedContract.getId(), savedContract.getContractNumber());

        return contractMapper.toResponse(savedContract);
    }

    @Override
    public ContractResponse getContractById(UUID id) {
        log.debug("Fetching contract with ID: {}", id);
        Contract contract = findContractById(id);
        return contractMapper.toResponse(contract);
    }

    @Override
    public List<ContractResponse> getContractsByCollaborator(String collaboratorId) {
        log.debug("Fetching all contracts for collaborator: {}", collaboratorId);
        return contractRepository.findByCollaboratorId(collaboratorId)
                .stream()
                .sorted(java.util.Comparator.comparing(Contract::getCreatedAt).reversed())
                .map(contractMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ContractResponse> getAllContracts(Pageable pageable) {
        log.debug("Fetching all contracts with pagination");
        return contractRepository.findAll(pageable)
                .map(contractMapper::toResponse);
    }

    @Override
    public Page<ContractResponse> getContractsWithFilters(
            ContractStatus status,
            ContractType contractType,
            String collaboratorId,
            Pageable pageable) {
        log.debug("Fetching contracts with filters - status: {}, type: {}, collaborator: {}",
                status, contractType, collaboratorId);
        return contractRepository.findWithFilters(status, contractType, collaboratorId, pageable)
                .map(contractMapper::toResponse);
    }

    @Override
    public List<ExpiringContractResponse> getExpiringContracts(int days) {
        log.debug("Fetching contracts expiring within {} days", days);
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        List<Contract> contracts = contractRepository.findContractsExpiringSoon(expiryDate);

        return contracts.stream()
                .map(this::enrichExpiringContractResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID id, ContractUpdateRequest request) {
        log.info("Updating contract with ID: {}", id);

        Contract contract = findContractById(id);

        // Update fields if provided
        if (request.getContractType() != null) {
            contract.setContractType(request.getContractType());
        }
        if (request.getStartDate() != null) {
            contract.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }
        if (request.getSalary() != null) {
            contract.setSalary(request.getSalary());
        }
        if (request.getCurrency() != null) {
            contract.setCurrency(request.getCurrency());
        }
        if (request.getTermsAndConditions() != null) {
            contract.setTermsAndConditions(request.getTermsAndConditions());
        }
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        }
        if (request.getAutoRenewal() != null) {
            contract.setAutoRenewal(request.getAutoRenewal());
        }
        if (request.getNoticePeriodDays() != null) {
            contract.setNoticePeriodDays(request.getNoticePeriodDays());
        }

        Contract updatedContract = contractRepository.save(contract);
        log.info("Updated contract with ID: {}", id);

        return contractMapper.toResponse(updatedContract);
    }

    @Override
    @Transactional
    public ContractResponse renewContract(UUID id, RenewalRequest request) {
        log.info("Renewing contract with ID: {}", id);

        Contract existingContract = findContractById(id);

        // Check renewal eligibility via performance review
        if (!isEligibleForRenewal(existingContract.getCollaboratorId())) {
            throw new RenewalNotEligibleException(existingContract.getCollaboratorId());
        }

        // Mark existing contract as renewed — saveAndFlush forces the UPDATE to hit the
        // DB before the INSERT below, otherwise Hibernate's default flush order (inserts
        // first) violates the unique-active-contract-per-collaborator constraint.
        existingContract.setStatus(ContractStatus.RENEWED);
        contractRepository.saveAndFlush(existingContract);

        // Create new contract linked to previous
        Contract newContract = Contract.builder()
                .contractNumber(generateContractNumber())
                .collaboratorId(existingContract.getCollaboratorId())
                .contractType(existingContract.getContractType())
                .startDate(existingContract.getEndDate().plusDays(1))
                .endDate(request.getNewEndDate())
                .salary(request.getNewSalary() != null ? request.getNewSalary() : existingContract.getSalary())
                .currency(existingContract.getCurrency())
                .termsAndConditions(request.getNewTermsAndConditions() != null ?
                        request.getNewTermsAndConditions() : existingContract.getTermsAndConditions())
                .status(ContractStatus.ACTIVE)
                .previousContractId(existingContract.getId())
                .renewalCount(existingContract.getRenewalCount() + 1)
                .autoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal() : existingContract.getAutoRenewal())
                .noticePeriodDays(request.getNoticePeriodDays() != null ?
                        request.getNoticePeriodDays() : existingContract.getNoticePeriodDays())
                .build();

        Contract savedContract = contractRepository.save(newContract);
        log.info("Created renewed contract with ID: {} (previous: {})", savedContract.getId(), existingContract.getId());

        return contractMapper.toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponse terminateContract(UUID id, TerminationRequest request) {
        log.info("Terminating contract with ID: {}", id);

        Contract contract = findContractById(id);

        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new IllegalArgumentException("Contract is already terminated");
        }

        contract.setStatus(ContractStatus.TERMINATED);
        contract.setEndDate(request.getEffectiveDate());

        // Store termination reason in terms and conditions (append)
        String terminationNote = String.format("\n\n--- TERMINATION ---\nReason: %s\nEffective Date: %s\nNotes: %s",
                request.getReason(),
                request.getEffectiveDate(),
                request.getAdditionalNotes() != null ? request.getAdditionalNotes() : "N/A");

        String existingTerms = contract.getTermsAndConditions() != null ? contract.getTermsAndConditions() : "";
        contract.setTermsAndConditions(existingTerms + terminationNote);

        Contract terminatedContract = contractRepository.save(contract);
        log.info("Terminated contract with ID: {}", id);

        return contractMapper.toResponse(terminatedContract);
    }

    @Override
    public boolean existsActiveContractForCollaborator(String collaboratorId) {
        return contractRepository.existsByCollaboratorIdAndStatusIn(collaboratorId, ACTIVE_STATUSES);
    }

    private Contract findContractById(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));
    }

    private void validateCollaboratorExists(String collaboratorId) {
        try {
            ApiResponse<Boolean> response = collaboratorClient.checkCollaboratorExists(collaboratorId);
            if (!response.isSuccess() || !Boolean.TRUE.equals(response.getData())) {
                throw new ResourceNotFoundException("Collaborator", "nationalId", collaboratorId);
            }
        } catch (Exception e) {
            log.warn("Could not verify collaborator existence, proceeding anyway: {}", e.getMessage());
            // In case of service unavailability, we proceed (fail-open for MVP)
        }
    }

    private boolean isEligibleForRenewal(String collaboratorId) {
        try {
            ApiResponse<Boolean> response = collaboratorClient.isEligibleForRenewal(collaboratorId);
            return response.isSuccess() && Boolean.TRUE.equals(response.getData());
        } catch (Exception e) {
            log.warn("Could not verify renewal eligibility, defaulting to true: {}", e.getMessage());
            return true; // Fail-open for MVP
        }
    }

    private ExpiringContractResponse enrichExpiringContractResponse(Contract contract) {
        ExpiringContractResponse response = contractMapper.toExpiringResponse(contract);

        try {
            // Get collaborator info
            ApiResponse<CollaboratorResponse> collaboratorResponse =
                    collaboratorClient.getCollaboratorByNationalId(contract.getCollaboratorId());
            if (collaboratorResponse.isSuccess() && collaboratorResponse.getData() != null) {
                CollaboratorResponse collaborator = collaboratorResponse.getData();
                response.setCollaboratorName(collaborator.getFullName());
                response.setCollaboratorEmail(collaborator.getEmail());
            }

            // Get performance rating
            ApiResponse<AverageRatingResponse> ratingResponse =
                    collaboratorClient.getAverageRating(contract.getCollaboratorId());
            if (ratingResponse.isSuccess() && ratingResponse.getData() != null) {
                AverageRatingResponse rating = ratingResponse.getData();
                response.setAveragePerformanceRating(rating.getAverageRating());
                response.setIsEligibleForRenewal(rating.getIsEligibleForRenewal());
            }
        } catch (Exception e) {
            log.warn("Could not enrich expiring contract response: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public ContractDocumentResponse uploadDocument(UUID contractId, MultipartFile file) throws IOException {
        findContractById(contractId);
        ContractDocument doc = ContractDocument.builder()
                .contractId(contractId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileData(file.getBytes())
                .build();
        ContractDocument saved = documentRepository.save(doc);
        log.info("Uploaded document {} for contract {}", saved.getId(), contractId);
        return toDocumentResponse(saved);
    }

    @Override
    public List<ContractDocumentResponse> getDocuments(UUID contractId) {
        findContractById(contractId);
        return documentRepository.findByContractIdOrderByUploadedAtDesc(contractId)
                .stream().map(this::toDocumentResponse).collect(Collectors.toList());
    }

    @Override
    public ContractDocument getDocumentData(UUID contractId, UUID documentId) {
        findContractById(contractId);
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
    }

    private ContractDocumentResponse toDocumentResponse(ContractDocument doc) {
        return ContractDocumentResponse.builder()
                .id(doc.getId())
                .contractId(doc.getContractId())
                .fileName(doc.getFileName())
                .fileType(doc.getFileType())
                .fileSize(doc.getFileSize())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }

    private String generateContractNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "CTR-" + timestamp + "-" + random;
    }
}
