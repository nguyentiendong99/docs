package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.dto.ClinicDTO;
import com.bkav.lk.dto.DoctorScheduleDTO;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import com.bkav.lk.service.ClinicService;
import com.bkav.lk.service.HealthFacilitiesService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DoctorScheduleMapperDecorator implements DoctorScheduleMapper {

    @Autowired
    private DoctorScheduleMapper delegate;

    @Autowired
    private HealthFacilitiesService healthFacilitiesService;

    @Autowired
    private ClinicService clinicService;

    @Override
    public DoctorScheduleDTO toDto(DoctorSchedule schedule) {
        DoctorScheduleDTO dto = delegate.toDto(schedule);
        if (schedule == null) {
            return null;
        }
        return dto;
    }

    @Override
    public DoctorSchedule toEntity(DoctorScheduleDTO scheduleDTO) {
        DoctorSchedule entity = delegate.toEntity(scheduleDTO);
        return entity;
    }

    @Override
    public List<DoctorScheduleDTO> toDto(List<DoctorSchedule> entityList) {
        if (entityList == null) {
            return null;
        }

        List<DoctorScheduleDTO> list = new ArrayList<>(entityList.size());
        for (DoctorSchedule schedule : entityList) {
            DoctorScheduleDTO dto = toDto(schedule);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

}
