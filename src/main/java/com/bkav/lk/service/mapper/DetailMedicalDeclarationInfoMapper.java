package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DetailMedicalDeclarationInfo;
import com.bkav.lk.dto.DetailMedicalDeclarationInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetailMedicalDeclarationInfoMapper extends EntityMapper<DetailMedicalDeclarationInfoDTO, DetailMedicalDeclarationInfo> {

    @Mapping(source = "medicalDeclarationInfoId", target = "medicalDeclarationInfo.id")
    @Mapping(source = "questionId", target = "declarationQuestion.id")
    DetailMedicalDeclarationInfo toEntity (DetailMedicalDeclarationInfoDTO detailMedicalDeclarationInfoDTO);

    @Mapping(source = "medicalDeclarationInfo.id", target = "medicalDeclarationInfoId")
    @Mapping(source = "declarationQuestion.id", target = "questionId")
    @Mapping(source = "declarationQuestion.type", target = "questionType")
    @Mapping(source = "declarationQuestion.content", target = "questionContent")
    @Mapping(source = "declarationQuestion.value", target = "questionValue")
    @Mapping(source = "declarationQuestion.status", target = "questionStatus")
    DetailMedicalDeclarationInfoDTO toDto (DetailMedicalDeclarationInfo detailMedicalDeclarationInfo);

    default DetailMedicalDeclarationInfo fromId(Long id){
        if(id == null){
            return null;
        }
        DetailMedicalDeclarationInfo detailMedicalDeclarationInfo = new DetailMedicalDeclarationInfo();
        detailMedicalDeclarationInfo.setId(id);
        return detailMedicalDeclarationInfo;
    }
}
