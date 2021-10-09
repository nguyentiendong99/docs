package com.bkav.lk.service.mapper;

import com.bkav.lk.dto.MedicalResultDTO;
import com.bkav.lk.web.rest.vm.ShortedMedicalResultVM;

import java.util.List;
import java.util.stream.Collectors;

public abstract class MedicalResultDecorator implements MedicalResultMapper {

    @Override
    public ShortedMedicalResultVM toVM(MedicalResultDTO medicalResultDTO) {
        ShortedMedicalResultVM response = new ShortedMedicalResultVM();
        response.setDoctorAppointmentCode(medicalResultDTO.getDoctorAppointmentCode());
        response.setMedicalSpecialityName(medicalResultDTO.getMedicalSpecialityName());
        response.setExaminationDate(medicalResultDTO.getExaminationDate());
        return response;
    }

    @Override
    public List<ShortedMedicalResultVM> toVM(List<MedicalResultDTO> medicalResultDTO) {
        return medicalResultDTO.stream().map(this::toVM).collect(Collectors.toList());
    }

    @Override
    public MedicalResultDTO toDto(ShortedMedicalResultVM shortedMedicalResultVM) {
        MedicalResultDTO response = new MedicalResultDTO();
        response.setDoctorAppointmentCode(shortedMedicalResultVM.getDoctorAppointmentCode());
        response.setMedicalSpecialityName(shortedMedicalResultVM.getMedicalSpecialityName());
        response.setExaminationDate(shortedMedicalResultVM.getExaminationDate());
        return response;
    }

    @Override
    public List<MedicalResultDTO> toCollectionDto(List<ShortedMedicalResultVM> shortedMedicalResultVMs) {
        return shortedMedicalResultVMs.stream().map(this::toDto).collect(Collectors.toList());
    }
}
