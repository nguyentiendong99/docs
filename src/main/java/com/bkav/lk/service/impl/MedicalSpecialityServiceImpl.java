package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.domain.Doctor;
import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.MedicalSpeciality;
import com.bkav.lk.dto.MedicalSpecialityDTO;
import com.bkav.lk.repository.ClinicRepository;
import com.bkav.lk.repository.DoctorAppointmentRepository;
import com.bkav.lk.repository.DoctorRepository;
import com.bkav.lk.repository.MedicalSpecialityRepository;
import com.bkav.lk.service.MedicalSpecialityService;
import com.bkav.lk.service.mapper.MedicalSpecialityMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicalSpecialityServiceImpl implements MedicalSpecialityService {

    private static final Logger log = LoggerFactory.getLogger(MedicalSpecialityServiceImpl.class);
    private static final String ENTITY_NAME = "medical speciality";

    private final MedicalSpecialityRepository medicalSpecialityRepository;
    private final MedicalSpecialityMapper medicalSpecialityMapper;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAppointmentRepository doctorAppointmentRepository;

    @Autowired
    public MedicalSpecialityServiceImpl(
            MedicalSpecialityRepository medicalSpecialityRepository,
            MedicalSpecialityMapper medicalSpecialityMapper,
            ClinicRepository clinicRepository,
            DoctorRepository doctorRepository, DoctorAppointmentRepository doctorAppointmentRepository) {
        this.medicalSpecialityRepository = medicalSpecialityRepository;
        this.medicalSpecialityMapper = medicalSpecialityMapper;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
    }

    @Override
    public Page<MedicalSpecialityDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.info("Search medical speciality by conditions: {}", queryParams);
        List<MedicalSpeciality> medicalSpecialities = medicalSpecialityRepository.search(queryParams, pageable);
        return new PageImpl<>(
                medicalSpecialityMapper.toDto(medicalSpecialities), pageable,
                medicalSpecialityRepository.count(queryParams));
    }

    @Override
    public MedicalSpecialityDTO findById(Long medicalSpecialityId) {
        log.info("Find medical speciality by ID = {}", medicalSpecialityId);
        MedicalSpeciality result = medicalSpecialityRepository.findById(medicalSpecialityId)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        return medicalSpecialityMapper.toDto(result);
    }

    @Override
    public MedicalSpecialityDTO findOne(Long medicalSpecialityId) {
        Optional<MedicalSpeciality> result = medicalSpecialityRepository.findById(medicalSpecialityId);
        return result.map(medicalSpecialityMapper::toDto).orElse(null);
    }

    @Override
    public List<MedicalSpecialityDTO> findByHealthFacilityId(Long healthFacilityId) {
        log.info("Find medical speciality by health facility ID = {}", healthFacilityId);
        List<MedicalSpeciality> results = medicalSpecialityRepository.findByHealthFacilitiesIdAndStatus(
                healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
        return medicalSpecialityMapper.toDto(results);
    }

    @Override
    public List<MedicalSpecialityDTO> findByHealthFacilityId(Long healthFacilityId, Integer[] status) {
        log.info("Find medical speciality by health facility ID = {}", healthFacilityId);
        List<MedicalSpeciality> results = medicalSpecialityRepository.findByHealthFacilitiesIdAndStatus(
                healthFacilityId, status);
        return medicalSpecialityMapper.toDto(results);
    }

    @Override
    public List<MedicalSpecialityDTO> findByHealthFacilityIdAndExistClinic(Long healthFacilityId) {
        log.info("Find medical speciality include clinic by health facility ID = {}", healthFacilityId);
        List<MedicalSpeciality> results = medicalSpecialityRepository.findByHealthFacilitiesIdAndStatus(
                healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
        results.removeIf(o -> clinicRepository.findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE).isEmpty());
        return medicalSpecialityMapper.toDto(results);
    }

    @Override
    public boolean isDeactivable(Long id) {
        MultiValueMap<String, String> querySearchForAppointment = new LinkedMultiValueMap<>();
        querySearchForAppointment.set("medicalSpecialityId", id.toString());
        querySearchForAppointment.set("status", Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE + "," + Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
        List<DoctorAppointment> appointmentList = doctorAppointmentRepository.search(querySearchForAppointment, null);

        MultiValueMap<String, String> querySearchForDoctor = new LinkedMultiValueMap<>();
        querySearchForDoctor.set("medicalSpecialityId", id.toString());
        querySearchForDoctor.set("status", Constants.ENTITY_STATUS.ACTIVE.toString());
        List<Doctor> doctorList = doctorRepository.search(querySearchForDoctor, null);
        return appointmentList.size() == 0 && doctorList.size() == 0;
    }

    @Override
    public List<MedicalSpecialityDTO> findAllByIds(List<Long> ids) {
        log.info("Find medical speciality by list ID = {}", ids);
        return medicalSpecialityMapper.toDto(medicalSpecialityRepository.findAllById(ids));
    }

    @Override
    public MedicalSpecialityDTO save(MedicalSpecialityDTO medicalSpecialityDTO) {
        log.info("Save a health speciality with info: {}", medicalSpecialityDTO);
        if (medicalSpecialityDTO.getId() != null) {     //update
            MedicalSpeciality medicalSpeciality = medicalSpecialityRepository.findById(medicalSpecialityDTO.getId()).orElse(null);
            if (Objects.nonNull(medicalSpeciality) && (!medicalSpecialityDTO.getCode().equals(medicalSpeciality.getCode()) || !medicalSpecialityDTO.getName().equals(medicalSpeciality.getName()))) {
                medicalSpecialityDTO.setCode(this.generateMedicalSpecialityCode(
                        medicalSpecialityDTO.getCode(), medicalSpecialityDTO.getName(), !medicalSpecialityDTO.getName().equals(medicalSpeciality.getName())));
            }
        } else { //create
            medicalSpecialityDTO.setCode(this.generateMedicalSpecialityCode(
                    medicalSpecialityDTO.getCode(), medicalSpecialityDTO.getName(), false));
        }
        MedicalSpeciality result = medicalSpecialityRepository.save(medicalSpecialityMapper.toEntity(medicalSpecialityDTO));
        return medicalSpecialityMapper.toDto(result);
    }

    @Override
    public void delete(Long healthFacilityId) {
        log.info("Delete a medical speciality by ID = {}", healthFacilityId);
        MedicalSpeciality medicalSpeciality = medicalSpecialityRepository.findById(healthFacilityId)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));

        //Check have relationship with doctor or not
        List<Doctor> doctors = doctorRepository.findByMedicalSpecialityIdAndMedicalSpecialityStatusNot(medicalSpeciality.getId(), Constants.ENTITY_STATUS.DELETED);
        doctors = doctors.stream().filter(o -> !Constants.ENTITY_STATUS.DELETED.equals(o.getStatus())).collect(Collectors.toList());
        if (!doctors.isEmpty()) {
            throw new BadRequestAlertException("Medical speciality is already used by some doctor", ENTITY_NAME, "medical-speciality.is_used");
        }
        medicalSpeciality.setStatus(Constants.ENTITY_STATUS.DELETED);
        medicalSpecialityRepository.save(medicalSpeciality);
    }

    @Override
    public void deleteAll(List<Long> ids) {
        log.info("Delete multiple medical speciality by list ID = {}", ids);
        List<MedicalSpeciality> medicalSpecialities = medicalSpecialityRepository.findAllById(ids);
        medicalSpecialities.forEach(o -> o.setStatus(Constants.ENTITY_STATUS.DELETED));
        medicalSpecialityRepository.saveAll(medicalSpecialities);
    }

    @Override
    public boolean existByCode(String code) {
        return medicalSpecialityRepository.existsByCodeAndStatusGreaterThan(code, Constants.ENTITY_STATUS.DELETED);
    }

    private String generateMedicalSpecialityCode(String code, String name, boolean checkName) {
        int count = 0;
        MedicalSpeciality medicalSpeciality = null;
        String generateCode = null;
        String newCode = null;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCode(name.trim());
            while (true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                medicalSpeciality = medicalSpecialityRepository.findByCodeAndStatusGreaterThan(
                        newCode, Constants.ENTITY_STATUS.DELETED).orElse(null);
                if (Objects.isNull(medicalSpeciality)) {
                    break;
                }
                count++;
            }
        } else {
            newCode = code.trim();
            medicalSpeciality = medicalSpecialityRepository.findByCodeAndStatusGreaterThan(
                    newCode, Constants.ENTITY_STATUS.DELETED).orElse(null);
            if (Objects.nonNull(medicalSpeciality)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }
}
