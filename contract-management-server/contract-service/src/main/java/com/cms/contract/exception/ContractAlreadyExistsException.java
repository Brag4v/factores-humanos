package com.cms.contract.exception;

import com.cms.common.exception.DuplicateResourceException;

public class ContractAlreadyExistsException extends DuplicateResourceException {

    public ContractAlreadyExistsException(String collaboratorId) {
        super(String.format("An active contract already exists for collaborator with national ID: %s", collaboratorId));
    }
}
