package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorScheduleTime;
import com.bkav.lk.dto.DoctorScheduleTimeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorScheduleTimeMapper extends EntityMapper<DoctorScheduleTimeDTO, DoctorScheduleTime> {

    DoctorScheduleTime toEntity(DoctorScheduleTimeDTO dto);

    DoctorScheduleTimeDTO toDto(DoctorScheduleTime entity);

    default DoctorScheduleTime fromId(Long id){
        if(id == null){
            return null;
        }
        DoctorScheduleTime entity = new DoctorScheduleTime();
        entity.setId(id);
        return entity;
    }

}
