package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorFeedback;
import com.bkav.lk.dto.DoctorFeedbackDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DoctorFeedBackMapper extends EntityMapper<DoctorFeedbackDTO, DoctorFeedback> {
    @Mappings({
            @Mapping(target = "doctor.id", source = "doctorFeedbackDTO.doctorId"),
            @Mapping(target = "doctor.name", source = "doctorFeedbackDTO.doctorName"),
            @Mapping(target = "user.id", source = "doctorFeedbackDTO.userId"),
            @Mapping(target = "user.name", source = "doctorFeedbackDTO.userName"),
            @Mapping(target = "user.avatar", source = "doctorFeedbackDTO.userAvatar")
    })
    DoctorFeedback toEntity(DoctorFeedbackDTO doctorFeedbackDTO);

    @Mappings({
            @Mapping(target = "doctorId", source = "doctorFeedback.doctor.id"),
            @Mapping(target = "doctorName", source = "doctorFeedback.doctor.name"),
            @Mapping(target = "userId", source = "doctorFeedback.user.id"),
            @Mapping(target = "userName", source = "doctorFeedback.user.name"),
            @Mapping(target = "userAvatar", source = "doctorFeedback.user.avatar")
    })
    DoctorFeedbackDTO toDto(DoctorFeedback doctorFeedback);

    default DoctorFeedback fromId(Long id) {
        if (id == null)
            return null;
        DoctorFeedback doctorFeedback = new DoctorFeedback();
        doctorFeedback.setId(id);
        return doctorFeedback;
    }
}
