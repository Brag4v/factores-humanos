package com.cms.collaborator.exception;

import com.cms.common.exception.ResourceNotFoundException;

public class CollaboratorNotFoundException extends ResourceNotFoundException {

    public CollaboratorNotFoundException(String nationalId) {
        super("Collaborator", "nationalId", nationalId);
    }

    public CollaboratorNotFoundException(String fieldName, Object fieldValue) {
        super("Collaborator", fieldName, fieldValue);
    }
}
