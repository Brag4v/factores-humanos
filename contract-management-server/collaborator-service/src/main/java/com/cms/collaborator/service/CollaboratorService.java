package com.cms.collaborator.service;

import com.cms.collaborator.dto.request.CollaboratorRequest;
import com.cms.collaborator.dto.response.CollaboratorDetailResponse;
import com.cms.collaborator.dto.response.CollaboratorResponse;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollaboratorService {

    CollaboratorResponse createCollaborator(CollaboratorRequest request);

    CollaboratorResponse getCollaboratorByNationalId(String nationalId);

    CollaboratorDetailResponse getCollaboratorDetailByNationalId(String nationalId);

    Page<CollaboratorResponse> getAllCollaborators(Pageable pageable);

    Page<CollaboratorResponse> getCollaboratorsWithFilters(
            CollaboratorStatus status,
            String department,
            String searchTerm,
            Pageable pageable
    );

    CollaboratorResponse updateCollaborator(String nationalId, CollaboratorRequest request);

    void deleteCollaborator(String nationalId);

    boolean existsByNationalId(String nationalId);
}
