package com.bkav.lk.service.impl;

import com.bkav.lk.domain.MedicalResult;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.MedicalResultDTO;
import com.bkav.lk.repository.MedicalResultRepository;
import com.bkav.lk.service.MedicalResultService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.MedicalResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalResultServiceImpl implements MedicalResultService {

    private final MedicalResultRepository medicalResultRepository;
    private final MedicalResultMapper medicalResultMapper;
    private final UserService userService;

    @Autowired
    public MedicalResultServiceImpl(
            MedicalResultRepository medicalResultRepository,
            MedicalResultMapper medicalResultMapper,
            UserService userService) {
        this.medicalResultRepository = medicalResultRepository;
        this.medicalResultMapper = medicalResultMapper;
        this.userService = userService;
    }

    @Override
    public List<MedicalResultDTO> findByPatientId(Long patientId) {
        User user = userService.getUserWithAuthorities().orElse(null);
        return medicalResultMapper.toDto(medicalResultRepository.findByPatientIdAndCreatedBy(patientId, user.getLogin()));
    }

    @Override
    public List<MedicalResultDTO> saveAll(List<MedicalResultDTO> medicalResultDTOs) {
        List<MedicalResult> results = medicalResultRepository.saveAll(medicalResultMapper.toEntity(medicalResultDTOs));
        return medicalResultMapper.toDto(results);
    }

    @Override
    public List<String> findByCurrentUser() {
        List<String> values = null;
        User user = userService.getUserWithAuthorities().orElse(null);
        List<MedicalResult> results = medicalResultRepository.findByCreatedBy(user.getLogin());
        if (!results.isEmpty()) {
            values = results.stream().map(o -> o.getDoctorAppointmentCode()).collect(Collectors.toList());
        }
        return values;
    }
}
