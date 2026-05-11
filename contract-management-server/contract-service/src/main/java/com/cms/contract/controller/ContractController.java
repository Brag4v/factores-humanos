package com.cms.contract.controller;

import com.cms.common.dto.ApiResponse;
import com.cms.common.dto.PageResponse;
import com.cms.contract.dto.request.ContractRequest;
import com.cms.contract.dto.request.ContractUpdateRequest;
import com.cms.contract.dto.request.RenewalRequest;
import com.cms.contract.dto.request.TerminationRequest;
import com.cms.contract.dto.response.ContractDocumentResponse;
import com.cms.contract.dto.response.ContractResponse;
import com.cms.contract.dto.response.ExpiringContractResponse;
import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.entity.enums.ContractType;
import com.cms.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Contract management APIs")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @Operation(summary = "Create a new contract")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Contract created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collaborator not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Active contract already exists for collaborator")
    })
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody ContractRequest request) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contract by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contract found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contract not found")
    })
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(
            @Parameter(description = "Contract ID") @PathVariable UUID id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/collaborator/{collaboratorId}")
    @Operation(summary = "Get all contracts for a collaborator")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByCollaborator(
            @Parameter(description = "Collaborator National ID") @PathVariable String collaboratorId) {
        List<ContractResponse> response = contractService.getContractsByCollaborator(collaboratorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all contracts with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<ContractResponse>>> getAllContracts(
            @Parameter(description = "Filter by status") @RequestParam(required = false) ContractStatus status,
            @Parameter(description = "Filter by contract type") @RequestParam(required = false) ContractType contractType,
            @Parameter(description = "Filter by collaborator National ID") @RequestParam(required = false) String collaboratorId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ContractResponse> page = contractService.getContractsWithFilters(
                status, contractType, collaboratorId, pageable);

        PageResponse<ContractResponse> pageResponse = PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get contracts expiring soon")
    public ResponseEntity<ApiResponse<List<ExpiringContractResponse>>> getExpiringContracts(
            @Parameter(description = "Number of days to look ahead") @RequestParam(defaultValue = "30") int days) {
        List<ExpiringContractResponse> contracts = contractService.getExpiringContracts(days);
        return ResponseEntity.ok(ApiResponse.success(contracts));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update contract")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contract updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contract not found")
    })
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @Parameter(description = "Contract ID") @PathVariable UUID id,
            @Valid @RequestBody ContractUpdateRequest request) {
        ContractResponse response = contractService.updateContract(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Contract updated successfully"));
    }

    @PutMapping("/{id}/renew")
    @Operation(summary = "Renew contract")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contract renewed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contract not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Collaborator not eligible for renewal")
    })
    public ResponseEntity<ApiResponse<ContractResponse>> renewContract(
            @Parameter(description = "Contract ID") @PathVariable UUID id,
            @Valid @RequestBody RenewalRequest request) {
        ContractResponse response = contractService.renewContract(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Contract renewed successfully"));
    }

    @PutMapping("/{id}/terminate")
    @Operation(summary = "Terminate contract")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contract terminated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data or contract already terminated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contract not found")
    })
    public ResponseEntity<ApiResponse<ContractResponse>> terminateContract(
            @Parameter(description = "Contract ID") @PathVariable UUID id,
            @Valid @RequestBody TerminationRequest request) {
        ContractResponse response = contractService.terminateContract(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Contract terminated successfully"));
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for a contract")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ContractDocumentResponse>> uploadDocument(
            @Parameter(description = "Contract ID") @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        ContractDocumentResponse response = contractService.uploadDocument(id, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document uploaded successfully"));
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "List documents for a contract")
    public ResponseEntity<ApiResponse<List<ContractDocumentResponse>>> getDocuments(
            @Parameter(description = "Contract ID") @PathVariable UUID id) {
        List<ContractDocumentResponse> response = contractService.getDocuments(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Documents retrieved successfully"));
    }

    @GetMapping("/{id}/documents/{docId}/download")
    @Operation(summary = "Download a contract document")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable UUID id,
            @PathVariable UUID docId) {
        var doc = contractService.getDocumentData(id, docId);
        String disposition = "attachment; filename=\"" + doc.getFileName() + "\"";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(doc.getFileData());
    }
}
