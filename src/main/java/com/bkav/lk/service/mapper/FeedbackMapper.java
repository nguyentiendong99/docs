package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Feedback;
import com.bkav.lk.dto.FeedbackDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface FeedbackMapper extends EntityMapper<FeedbackDTO, Feedback> {
    @Mappings({
            @Mapping(target = "feedbackedUnit.id", source = "feedbackDTO.feedbackedUnitId"),
            @Mapping(target = "feedbackedUnit.name", source = "feedbackDTO.feedbackedUnitName"),
            @Mapping(target = "processingUnit.id", source = "feedbackDTO.processingUnitId"),
            @Mapping(target = "processingUnit.name", source = "feedbackDTO.processingUnitName")
    })
    Feedback toEntity(FeedbackDTO feedbackDTO);

    @Mappings({
            @Mapping(target = "feedbackedUnitId", source = "feedback.feedbackedUnit.id"),
            @Mapping(target = "feedbackedUnitName", source = "feedback.feedbackedUnit.name"),
            @Mapping(target = "processingUnitId", source = "feedback.processingUnit.id"),
            @Mapping(target = "processingUnitName", source = "feedback.processingUnit.name")
    })
    FeedbackDTO toDto(Feedback feedback);

    default Feedback fromId(Long id){
        if(id == null){
            return null;
        }
        Feedback feedback = new Feedback();
        feedback.setId(id);
        return feedback;
    }

}
