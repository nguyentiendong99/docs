package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.dto.DoctorScheduleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

//@DecoratedWith(DoctorScheduleMapperDecorator.class)
@Mapper(componentModel = "spring", uses = {DoctorMapper.class})
public interface DoctorScheduleMapper extends EntityMapper<DoctorScheduleDTO, DoctorSchedule> {

    @Mapping(source = "doctorId", target = "doctor")
    DoctorSchedule toEntity(DoctorScheduleDTO dto);

    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.name", target = "doctorName")
    @Mapping(source = "doctor.code", target = "doctorCode")
    @Mapping(source = "clinic.name", target = "clinicName")
    DoctorScheduleDTO toDto(DoctorSchedule entity);

    default DoctorSchedule fromId(Long id){
        if(id == null){
            return null;
        }
        DoctorSchedule obj = new DoctorSchedule();
        obj.setId(id);
        return obj;
    }

}
