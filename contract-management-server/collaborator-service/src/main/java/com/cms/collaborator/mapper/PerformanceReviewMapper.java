package com.cms.collaborator.mapper;

import com.cms.collaborator.dto.request.PerformanceReviewRequest;
import com.cms.collaborator.dto.response.PerformanceReviewResponse;
import com.cms.collaborator.entity.PerformanceReview;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PerformanceReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collaborator", ignore = true)
    @Mapping(target = "isEligibleRenewal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PerformanceReview toEntity(PerformanceReviewRequest request);

    @Mapping(target = "collaboratorId", source = "collaborator.nationalId")
    @Mapping(target = "collaboratorName", expression = "java(review.getCollaborator() != null ? review.getCollaborator().getFullName() : null)")
    PerformanceReviewResponse toResponse(PerformanceReview review);

    List<PerformanceReviewResponse> toResponseList(List<PerformanceReview> reviews);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collaborator", ignore = true)
    @Mapping(target = "isEligibleRenewal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget PerformanceReview review, PerformanceReviewRequest request);
}
