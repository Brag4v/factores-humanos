package com.cms.contract.mapper;

import com.cms.contract.dto.request.ContractRequest;
import com.cms.contract.dto.response.ContractResponse;
import com.cms.contract.dto.response.ExpiringContractResponse;
import com.cms.contract.entity.Contract;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contractNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "previousContractId", ignore = true)
    @Mapping(target = "renewalCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "currency", defaultValue = "USD")
    @Mapping(target = "autoRenewal", defaultValue = "false")
    @Mapping(target = "noticePeriodDays", defaultValue = "30")
    Contract toEntity(ContractRequest request);

    @Mapping(target = "daysUntilExpiry", expression = "java(contract.getDaysUntilExpiry())")
    @Mapping(target = "isExpiringSoon", expression = "java(contract.isExpiringSoon(30))")
    ContractResponse toResponse(Contract contract);

    @Mapping(target = "daysUntilExpiry", expression = "java(contract.getDaysUntilExpiry())")
    @Mapping(target = "collaboratorName", ignore = true)
    @Mapping(target = "collaboratorEmail", ignore = true)
    @Mapping(target = "isEligibleForRenewal", ignore = true)
    @Mapping(target = "averagePerformanceRating", ignore = true)
    ExpiringContractResponse toExpiringResponse(Contract contract);

    List<ContractResponse> toResponseList(List<Contract> contracts);

    List<ExpiringContractResponse> toExpiringResponseList(List<Contract> contracts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contractNumber", ignore = true)
    @Mapping(target = "collaboratorId", ignore = true)
    @Mapping(target = "previousContractId", ignore = true)
    @Mapping(target = "renewalCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(@MappingTarget Contract contract, ContractRequest request);
}
