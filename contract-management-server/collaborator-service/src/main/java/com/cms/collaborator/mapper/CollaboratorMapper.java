package com.cms.collaborator.mapper;

import com.cms.collaborator.dto.request.CollaboratorRequest;
import com.cms.collaborator.dto.response.CollaboratorDetailResponse;
import com.cms.collaborator.dto.response.CollaboratorResponse;
import com.cms.collaborator.entity.Collaborator;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CollaboratorMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "performanceReviews", ignore = true)
    @Mapping(target = "status", defaultExpression = "java(com.cms.collaborator.entity.enums.CollaboratorStatus.ACTIVE)")
    Collaborator toEntity(CollaboratorRequest request);

    @Mapping(target = "fullName", expression = "java(collaborator.getFullName())")
    CollaboratorResponse toResponse(Collaborator collaborator);

    @Mapping(target = "fullName", expression = "java(collaborator.getFullName())")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "isEligibleForRenewal", ignore = true)
    @Mapping(target = "latestReview", ignore = true)
    CollaboratorDetailResponse toDetailResponse(Collaborator collaborator);

    List<CollaboratorResponse> toResponseList(List<Collaborator> collaborators);

    @Mapping(target = "nationalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "performanceReviews", ignore = true)
    void updateEntity(@MappingTarget Collaborator collaborator, CollaboratorRequest request);
}
