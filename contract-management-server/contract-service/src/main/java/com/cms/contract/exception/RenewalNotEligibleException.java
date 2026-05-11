package com.cms.contract.exception;

import com.cms.common.exception.BusinessRuleException;

public class RenewalNotEligibleException extends BusinessRuleException {

    public RenewalNotEligibleException(String collaboratorId) {
        super(String.format("Collaborator with national ID %s is not eligible for contract renewal based on performance reviews", 
                collaboratorId), "RENEWAL_NOT_ELIGIBLE");
    }
}
