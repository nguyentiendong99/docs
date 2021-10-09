package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DeclarationQuestion;
import com.bkav.lk.dto.DeclarationQuestionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeclarationQuestionMapper extends EntityMapper<DeclarationQuestionDTO, DeclarationQuestion> {

    DeclarationQuestion toEntity(DeclarationQuestionDTO declarationQuestionDTO);

    DeclarationQuestionDTO toDto(DeclarationQuestion declarationQuestion);

    default DeclarationQuestion fromId(Long id){
        if(id == null){
            return null;
        }
        DeclarationQuestion declarationQuestion = new DeclarationQuestion();
        declarationQuestion.setId(id);
        return declarationQuestion;
    }

}
