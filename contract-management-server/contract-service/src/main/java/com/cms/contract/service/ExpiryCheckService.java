package com.cms.contract.service;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.CollaboratorClient;
import com.cms.contract.client.dto.AverageRatingResponse;
import com.cms.contract.entity.Contract;
import com.cms.contract.entity.enums.ContractStatus;
import com.cms.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryCheckService {

    private final ContractRepository contractRepository;
    private final CollaboratorClient collaboratorClient;

    public List<Contract> findContractsExpiringSoon(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return contractRepository.findContractsExpiringSoon(expiryDate);
    }

    public List<Contract> findExpiredActiveContracts() {
        return contractRepository.findExpiredActiveContracts(LocalDate.now());
    }

    @Transactional
    public void updateExpiredContracts() {
        List<Contract> expiredContracts = findExpiredActiveContracts();
        log.info("Found {} expired contracts to update", expiredContracts.size());

        for (Contract contract : expiredContracts) {
            contract.setStatus(ContractStatus.EXPIRED);
            contractRepository.save(contract);
            log.info("Updated contract {} to EXPIRED status", contract.getContractNumber());
        }
    }

    public boolean isEligibleForRenewal(String collaboratorId) {
        try {
            ApiResponse<AverageRatingResponse> response = collaboratorClient.getAverageRating(collaboratorId);
            if (response.isSuccess() && response.getData() != null) {
                return Boolean.TRUE.equals(response.getData().getIsEligibleForRenewal());
            }
        } catch (Exception e) {
            log.warn("Could not check renewal eligibility for collaborator {}: {}", collaboratorId, e.getMessage());
        }
        return false;
    }

    public List<Contract> findAutoRenewalContractsExpiringSoon(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return contractRepository.findAutoRenewalContractsExpiringSoon(expiryDate);
    }
}
