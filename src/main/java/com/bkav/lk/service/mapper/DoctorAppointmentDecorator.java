package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.dto.DoctorAppointmentDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public abstract class DoctorAppointmentDecorator implements DoctorAppointmentMapper {

    @Autowired
    private DoctorAppointmentMapper delegate;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorMapper doctorMapper;

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public DoctorAppointmentDTO toDto(DoctorAppointment entity) {
        DoctorAppointmentDTO dto = delegate.toDto(entity);
        if (entity == null) {
            return null;
        }
        if (entity.getDoctor() != null) {
            if (entity.getDoctor().getId() != null) {
                dto.setDoctorId(entity.getDoctor().getId());
            } else {
                dto.setDoctorId(null);
            }
        } else {
            dto.setDoctorId(null);
        }

        if (entity.getStartTime() != null) {
            dto.setAppointmentDate(entity.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate().format(dateTimeFormatter));
            dto.setAppointmentTime(DateUtils.getFriendlyTimeFormat(entity.getStartTime(), entity.getEndTime()) + " " + DateUtils.friendlyTimeOfDayFormat(entity.getStartTime()));
        }

        return dto;
    }

    @Override
    public DoctorAppointment toEntity(DoctorAppointmentDTO dto) {
        DoctorAppointment entity = delegate.toEntity(dto);
        if (dto.getDoctorId() != null) {
            DoctorDTO doctorDTO = doctorService.findById(dto.getDoctorId());
            if (doctorDTO == null) {
                entity.setDoctor(null);
            } else {
                entity.setDoctor(doctorMapper.toEntity(doctorDTO));
            }
        } else {
            entity.setDoctor(null);
        }
        return entity;
    }

    @Override
    public List<DoctorAppointmentDTO> toDto(List<DoctorAppointment> entityList) {
        if (entityList == null) {
            return null;
        }

        List<DoctorAppointmentDTO> list = new ArrayList<>(entityList.size());
        for (DoctorAppointment entity : entityList) {
            DoctorAppointmentDTO dto = toDto(entity);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

}
