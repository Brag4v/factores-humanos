package com.cms.contract.exception;

import com.cms.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ContractNotFoundException extends ResourceNotFoundException {

    public ContractNotFoundException(UUID id) {
        super("Contract", "id", id);
    }

    public ContractNotFoundException(String fieldName, Object fieldValue) {
        super("Contract", fieldName, fieldValue);
    }
}
