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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractDocumentRepository documentRepository;

    @Mock
    private ContractMapper contractMapper;

    @Mock
    private CollaboratorClient collaboratorClient;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Contract contract;
    private ContractRequest contractRequest;
    private ContractResponse contractResponse;
    private UUID contractId;

    @BeforeEach
    void setUp() {
        contractId = UUID.randomUUID();

        contract = Contract.builder()
                .id(contractId)
                .contractNumber("CTR-20240101-0001")
                .collaboratorId("NAT001")
                .contractType(ContractType.FULL_TIME)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .salary(new BigDecimal("50000.00"))
                .currency("USD")
                .status(ContractStatus.ACTIVE)
                .renewalCount(0)
                .autoRenewal(false)
                .noticePeriodDays(30)
                .build();

        contractRequest = ContractRequest.builder()
                .collaboratorId("NAT001")
                .contractType(ContractType.FULL_TIME)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .salary(new BigDecimal("50000.00"))
                .currency("USD")
                .build();

        contractResponse = ContractResponse.builder()
                .id(contractId)
                .contractNumber("CTR-20240101-0001")
                .collaboratorId("NAT001")
                .contractType(ContractType.FULL_TIME)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .salary(new BigDecimal("50000.00"))
                .currency("USD")
                .status(ContractStatus.ACTIVE)
                .renewalCount(0)
                .build();
    }

    @Nested
    class CreateContract {

        @Test
        void shouldCreateContractSuccessfully() {
            when(collaboratorClient.checkCollaboratorExists("NAT001"))
                    .thenReturn(ApiResponse.<Boolean>builder().success(true).data(true).build());
            when(contractRepository.existsByCollaboratorIdAndStatusIn(eq("NAT001"), any())).thenReturn(false);
            when(contractMapper.toEntity(contractRequest)).thenReturn(contract);
            when(contractRepository.save(contract)).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            ContractResponse result = contractService.createContract(contractRequest);

            assertThat(result.getCollaboratorId()).isEqualTo("NAT001");
            assertThat(result.getContractType()).isEqualTo(ContractType.FULL_TIME);
            verify(contractRepository).save(contract);
        }

        @Test
        void shouldThrowWhenActiveContractExists() {
            when(collaboratorClient.checkCollaboratorExists("NAT001"))
                    .thenReturn(ApiResponse.<Boolean>builder().success(true).data(true).build());
            when(contractRepository.existsByCollaboratorIdAndStatusIn(eq("NAT001"), any())).thenReturn(true);

            assertThatThrownBy(() -> contractService.createContract(contractRequest))
                    .isInstanceOf(ContractAlreadyExistsException.class);
        }

        @Test
        void shouldProceedWhenCollaboratorServiceUnavailable() {
            when(collaboratorClient.checkCollaboratorExists("NAT001"))
                    .thenThrow(new RuntimeException("Service unavailable"));
            when(contractRepository.existsByCollaboratorIdAndStatusIn(eq("NAT001"), any())).thenReturn(false);
            when(contractMapper.toEntity(contractRequest)).thenReturn(contract);
            when(contractRepository.save(contract)).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            ContractResponse result = contractService.createContract(contractRequest);

            assertThat(result).isNotNull();
        }

        @Test
        void shouldSetInitialContractFields() {
            Contract newContract = Contract.builder().build();

            when(collaboratorClient.checkCollaboratorExists("NAT001"))
                    .thenReturn(ApiResponse.<Boolean>builder().success(true).data(true).build());
            when(contractRepository.existsByCollaboratorIdAndStatusIn(eq("NAT001"), any())).thenReturn(false);
            when(contractMapper.toEntity(contractRequest)).thenReturn(newContract);
            when(contractRepository.save(newContract)).thenReturn(newContract);
            when(contractMapper.toResponse(newContract)).thenReturn(contractResponse);

            contractService.createContract(contractRequest);

            assertThat(newContract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
            assertThat(newContract.getRenewalCount()).isEqualTo(0);
            assertThat(newContract.getContractNumber()).startsWith("CTR-");
        }
    }

    @Nested
    class GetContract {

        @Test
        void shouldReturnContractById() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            ContractResponse result = contractService.getContractById(contractId);

            assertThat(result.getId()).isEqualTo(contractId);
        }

        @Test
        void shouldThrowWhenContractNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(contractRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getContractById(unknownId))
                    .isInstanceOf(ContractNotFoundException.class);
        }
    }

    @Nested
    class GetContractsByCollaborator {

        @Test
        void shouldReturnContractsSortedByCreatedAtDesc() {
            when(contractRepository.findByCollaboratorId("NAT001")).thenReturn(List.of(contract));
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            List<ContractResponse> result = contractService.getContractsByCollaborator("NAT001");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class GetAllContracts {

        @Test
        void shouldReturnPagedContracts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contract> page = new PageImpl<>(List.of(contract));

            when(contractRepository.findAll(pageable)).thenReturn(page);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            Page<ContractResponse> result = contractService.getAllContracts(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    class UpdateContract {

        @Test
        void shouldUpdateOnlyProvidedFields() {
            ContractUpdateRequest updateRequest = ContractUpdateRequest.builder()
                    .salary(new BigDecimal("60000.00"))
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractRepository.save(contract)).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            contractService.updateContract(contractId, updateRequest);

            assertThat(contract.getSalary()).isEqualTo(new BigDecimal("60000.00"));
            assertThat(contract.getContractType()).isEqualTo(ContractType.FULL_TIME);
        }

        @Test
        void shouldUpdateAllFields() {
            ContractUpdateRequest updateRequest = ContractUpdateRequest.builder()
                    .contractType(ContractType.PART_TIME)
                    .startDate(LocalDate.of(2024, 6, 1))
                    .endDate(LocalDate.of(2025, 6, 1))
                    .salary(new BigDecimal("30000.00"))
                    .currency("EUR")
                    .termsAndConditions("New terms")
                    .status(ContractStatus.PENDING)
                    .autoRenewal(true)
                    .noticePeriodDays(60)
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractRepository.save(contract)).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            contractService.updateContract(contractId, updateRequest);

            assertThat(contract.getContractType()).isEqualTo(ContractType.PART_TIME);
            assertThat(contract.getCurrency()).isEqualTo("EUR");
            assertThat(contract.getAutoRenewal()).isTrue();
            assertThat(contract.getNoticePeriodDays()).isEqualTo(60);
        }

        @Test
        void shouldThrowWhenContractNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(contractRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.updateContract(unknownId, ContractUpdateRequest.builder().build()))
                    .isInstanceOf(ContractNotFoundException.class);
        }
    }

    @Nested
    class RenewContract {

        @Test
        void shouldRenewContractSuccessfully() {
            RenewalRequest renewalRequest = RenewalRequest.builder()
                    .newEndDate(LocalDate.of(2026, 1, 1))
                    .newSalary(new BigDecimal("55000.00"))
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(collaboratorClient.isEligibleForRenewal("NAT001"))
                    .thenReturn(ApiResponse.<Boolean>builder().success(true).data(true).build());
            when(contractRepository.saveAndFlush(contract)).thenReturn(contract);
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));
            when(contractMapper.toResponse(any(Contract.class))).thenReturn(contractResponse);

            contractService.renewContract(contractId, renewalRequest);

            assertThat(contract.getStatus()).isEqualTo(ContractStatus.RENEWED);
            verify(contractRepository).saveAndFlush(contract);
            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        void shouldThrowWhenNotEligibleForRenewal() {
            RenewalRequest renewalRequest = RenewalRequest.builder()
                    .newEndDate(LocalDate.of(2026, 1, 1))
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(collaboratorClient.isEligibleForRenewal("NAT001"))
                    .thenReturn(ApiResponse.<Boolean>builder().success(true).data(false).build());

            assertThatThrownBy(() -> contractService.renewContract(contractId, renewalRequest))
                    .isInstanceOf(RenewalNotEligibleException.class);
        }

        @Test
        void shouldDefaultToEligibleWhenServiceUnavailable() {
            RenewalRequest renewalRequest = RenewalRequest.builder()
                    .newEndDate(LocalDate.of(2026, 1, 1))
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(collaboratorClient.isEligibleForRenewal("NAT001"))
                    .thenThrow(new RuntimeException("Service down"));
            when(contractRepository.saveAndFlush(contract)).thenReturn(contract);
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));
            when(contractMapper.toResponse(any(Contract.class))).thenReturn(contractResponse);

            contractService.renewContract(contractId, renewalRequest);

            assertThat(contract.getStatus()).isEqualTo(ContractStatus.RENEWED);
        }

        @Test
        void shouldCarryOverFieldsFromPreviousContract() {
            RenewalRequest renewalRequest = RenewalRequest.builder()
                    .newEndDate(LocalDate.of(2026, 1, 1))
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(collaboratorClient.isEligibleForRenewal("NAT001"))
                    .thenReturn(ApiResponse.<Boolean>builder().success(true).data(true).build());
            when(contractRepository.saveAndFlush(contract)).thenReturn(contract);
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> {
                Contract saved = inv.getArgument(0);
                assertThat(saved.getCollaboratorId()).isEqualTo("NAT001");
                assertThat(saved.getSalary()).isEqualTo(new BigDecimal("50000.00"));
                assertThat(saved.getCurrency()).isEqualTo("USD");
                assertThat(saved.getStartDate()).isEqualTo(contract.getEndDate().plusDays(1));
                assertThat(saved.getPreviousContractId()).isEqualTo(contractId);
                assertThat(saved.getRenewalCount()).isEqualTo(1);
                assertThat(saved.getStatus()).isEqualTo(ContractStatus.ACTIVE);
                return saved;
            });
            when(contractMapper.toResponse(any(Contract.class))).thenReturn(contractResponse);

            contractService.renewContract(contractId, renewalRequest);
        }
    }

    @Nested
    class TerminateContract {

        @Test
        void shouldTerminateContractSuccessfully() {
            TerminationRequest terminationRequest = TerminationRequest.builder()
                    .reason("Poor performance")
                    .effectiveDate(LocalDate.of(2024, 6, 15))
                    .additionalNotes("Final warning issued")
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractRepository.save(contract)).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            contractService.terminateContract(contractId, terminationRequest);

            assertThat(contract.getStatus()).isEqualTo(ContractStatus.TERMINATED);
            assertThat(contract.getEndDate()).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(contract.getTermsAndConditions()).contains("TERMINATION");
            assertThat(contract.getTermsAndConditions()).contains("Poor performance");
        }

        @Test
        void shouldThrowWhenAlreadyTerminated() {
            contract.setStatus(ContractStatus.TERMINATED);
            TerminationRequest terminationRequest = TerminationRequest.builder()
                    .reason("Again")
                    .effectiveDate(LocalDate.now())
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> contractService.terminateContract(contractId, terminationRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already terminated");
        }

        @Test
        void shouldHandleNullAdditionalNotes() {
            TerminationRequest terminationRequest = TerminationRequest.builder()
                    .reason("Restructuring")
                    .effectiveDate(LocalDate.of(2024, 6, 15))
                    .additionalNotes(null)
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractRepository.save(contract)).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            contractService.terminateContract(contractId, terminationRequest);

            assertThat(contract.getTermsAndConditions()).contains("N/A");
        }
    }

    @Nested
    class ExistsActiveContract {

        @Test
        void shouldReturnTrueWhenActiveContractExists() {
            when(contractRepository.existsByCollaboratorIdAndStatusIn(eq("NAT001"), any())).thenReturn(true);

            assertThat(contractService.existsActiveContractForCollaborator("NAT001")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNoActiveContract() {
            when(contractRepository.existsByCollaboratorIdAndStatusIn(eq("NAT001"), any())).thenReturn(false);

            assertThat(contractService.existsActiveContractForCollaborator("NAT001")).isFalse();
        }
    }

    @Nested
    class UploadDocument {

        @Test
        void shouldUploadDocumentSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "contract.pdf", "application/pdf", "PDF content".getBytes());

            ContractDocument savedDoc = ContractDocument.builder()
                    .id(UUID.randomUUID())
                    .contractId(contractId)
                    .fileName("contract.pdf")
                    .fileType("application/pdf")
                    .fileSize(11L)
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(documentRepository.save(any(ContractDocument.class))).thenReturn(savedDoc);

            var result = contractService.uploadDocument(contractId, file);

            assertThat(result.getFileName()).isEqualTo("contract.pdf");
            verify(documentRepository).save(any(ContractDocument.class));
        }

        @Test
        void shouldThrowWhenContractNotFoundForUpload() {
            UUID unknownId = UUID.randomUUID();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "doc.pdf", "application/pdf", "data".getBytes());

            when(contractRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.uploadDocument(unknownId, file))
                    .isInstanceOf(ContractNotFoundException.class);
        }
    }

    @Nested
    class GetDocuments {

        @Test
        void shouldReturnDocumentsForContract() {
            ContractDocument doc = ContractDocument.builder()
                    .id(UUID.randomUUID())
                    .contractId(contractId)
                    .fileName("file.pdf")
                    .fileType("application/pdf")
                    .fileSize(100L)
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(documentRepository.findByContractIdOrderByUploadedAtDesc(contractId)).thenReturn(List.of(doc));

            var result = contractService.getDocuments(contractId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFileName()).isEqualTo("file.pdf");
        }
    }

    @Nested
    class GetDocumentData {

        @Test
        void shouldReturnDocumentData() {
            UUID docId = UUID.randomUUID();
            ContractDocument doc = ContractDocument.builder()
                    .id(docId)
                    .contractId(contractId)
                    .fileName("file.pdf")
                    .fileData("content".getBytes())
                    .build();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

            ContractDocument result = contractService.getDocumentData(contractId, docId);

            assertThat(result.getFileName()).isEqualTo("file.pdf");
        }

        @Test
        void shouldThrowWhenDocumentNotFound() {
            UUID docId = UUID.randomUUID();

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(documentRepository.findById(docId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getDocumentData(contractId, docId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
