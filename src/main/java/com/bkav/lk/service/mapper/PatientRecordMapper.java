package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.dto.PatientRecordDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AreaMapper.class})
public interface PatientRecordMapper extends EntityMapper<PatientRecordDTO, PatientRecord>{

    PatientRecord toEntity(PatientRecordDTO patientRecordDTO);

    @Mapping(source = "city.shortName", target = "cityName")
    @Mapping(source = "city.areaCode", target = "cityCode")
    @Mapping(source = "district.shortName", target = "districtName")
    @Mapping(source = "district.areaCode", target = "districtCode")
    @Mapping(source = "ward.shortName", target = "wardName")
    @Mapping(source = "ward.areaCode", target = "wardCode")
    PatientRecordDTO toDto(PatientRecord patientRecord);

    default PatientRecord fromId(Long id){
        if(id == null){
            return null;
        }
        PatientRecord patientRecord = new PatientRecord();
        patientRecord.setId(id);
        return patientRecord;
    }
}
