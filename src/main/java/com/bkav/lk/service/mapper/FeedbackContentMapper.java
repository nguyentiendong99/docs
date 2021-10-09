package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.FeedbackContent;
import com.bkav.lk.dto.FeedbackContentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackContentMapper extends EntityMapper<FeedbackContentDTO, FeedbackContent>  {
    FeedbackContent toEntity(FeedbackContentDTO feedbackDTO);

    FeedbackContentDTO toDto(FeedbackContent feedback);

    default FeedbackContent fromId(Long id){
        if(id == null){
            return null;
        }
        FeedbackContent feedback = new FeedbackContent();
        feedback.setId(id);
        return feedback;
    }

}
