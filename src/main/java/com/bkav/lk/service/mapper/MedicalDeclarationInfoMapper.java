package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.dto.MedicalDeclarationInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalDeclarationInfoMapper extends EntityMapper<MedicalDeclarationInfoDTO, MedicalDeclarationInfo> {

    @Mapping(source = "patientRecordId", target = "patientRecord.id")
    MedicalDeclarationInfo toEntity(MedicalDeclarationInfoDTO medicalDeclarationInfoDTO);

    @Mapping(source = "patientRecord.id", target = "patientRecordId")
    @Mapping(source = "patientRecord.patientRecordCode", target = "patientRecordCode")
    @Mapping(source = "patientRecord.name", target = "patientRecordName")
    @Mapping(source = "patientRecord.address", target = "address")
    @Mapping(source = "patientRecord.gender", target = "gender")
    @Mapping(source = "patientRecord.dob", target = "dob")
    MedicalDeclarationInfoDTO toDto(MedicalDeclarationInfo entity);

    default MedicalDeclarationInfo fromId(Long id) {
        if (id == null) {
            return null;
        }
        MedicalDeclarationInfo medicalDeclarationInfo = new MedicalDeclarationInfo();
        medicalDeclarationInfo.setId(id);
        return medicalDeclarationInfo;
    }
}
