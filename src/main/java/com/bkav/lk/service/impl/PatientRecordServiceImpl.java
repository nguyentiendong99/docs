package com.bkav.lk.service.impl;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.PatientRecordDTO;
import com.bkav.lk.repository.AreaRepository;
import com.bkav.lk.repository.DoctorAppointmentRepository;
import com.bkav.lk.repository.MedicalDeclarationInfoRepository;
import com.bkav.lk.repository.PatientRecordRepository;
import com.bkav.lk.service.PatientRecordService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.PatientRecordMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.vm.HisPatientRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class PatientRecordServiceImpl implements PatientRecordService {

    private static final Logger log = LoggerFactory.getLogger(PatientRecordServiceImpl.class);

    private static final String ENTITY_NAME = "patient_record";

    //<editor-fold desc="INIT">
    private final UserService userService;
    private final PatientRecordRepository patientRecordRepository;
    private final PatientRecordMapper patientRecordMapper;
    private final AreaRepository areaRepository;
    private final MedicalDeclarationInfoRepository medicalDeclarationInfoRepository;
    private final DoctorAppointmentRepository doctorAppointmentRepository;

    public PatientRecordServiceImpl(UserService userService,
                                    PatientRecordRepository patientRecordRepository,
                                    PatientRecordMapper patientRecordMapper,
                                    AreaRepository areaRepository, MedicalDeclarationInfoRepository medicalDeclarationInfoRepository, DoctorAppointmentRepository doctorAppointmentRepository) {
        this.userService = userService;
        this.patientRecordRepository = patientRecordRepository;
        this.patientRecordMapper = patientRecordMapper;
        this.areaRepository = areaRepository;
        this.medicalDeclarationInfoRepository = medicalDeclarationInfoRepository;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
    }
    //</editor-fold>

    @Override
    public Optional<PatientRecordDTO> findOne(Long id) {
        return patientRecordRepository.findById(id).map(patientRecordMapper::toDto);
    }

    @Override
    public List<PatientRecordDTO> findByUserId(Long userId) {
        List<PatientRecord> listPatientRecord = patientRecordRepository.findByUserIdAndStatus(userId, Constants.ENTITY_STATUS.ACTIVE);
        List<PatientRecordDTO> listDto = new ArrayList();
        listPatientRecord.forEach(patientRecord -> listDto.add(patientRecordMapper.toDto(patientRecord)));
        return listDto;
    }

    @Override
    public List<PatientRecordDTO> getPhone(String code) {
        List<PatientRecord> patientRecord = patientRecordRepository.getPhoneByPatient(code.toLowerCase(), Constants.ENTITY_STATUS.ACTIVE);
        return patientRecord != null ? patientRecordMapper.toDto(patientRecord) : null;
    }

    @Override
    public List<PatientRecordDTO> findByAreaCode(String code) {
        List<PatientRecord> patientRecordList;
        if (code.equals("0")) {
            patientRecordList = patientRecordRepository.findByCity_AreaCode("15");
        } else {
            patientRecordList = patientRecordRepository.findByDistrict_AreaCode(code);
        }
        return patientRecordMapper.toDto(patientRecordList);
    }

    @Override
    public Page<PatientRecordDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<PatientRecordDTO> result = patientRecordMapper.toDto(
                patientRecordRepository.search(queryParams, pageable));
        return new PageImpl<>(result, pageable, patientRecordRepository.count(queryParams));
    }

    @Override
    @Transactional
    public PatientRecordDTO save(PatientRecordDTO patientRecordDTO) {
        PatientRecord result = null;
        User currentUser = userService.getUserWithAuthorities().orElse(null);
        PatientRecord newPatientRecord = patientRecordMapper.toEntity(patientRecordDTO);
        newPatientRecord.setEmail(!StringUtils.isEmpty(patientRecordDTO.getEmail()) ? patientRecordDTO.getEmail().toLowerCase().trim() : null);
        newPatientRecord.setName(patientRecordDTO.getName().trim());
        newPatientRecord.setUserId(currentUser != null ? currentUser.getId() : null);
        newPatientRecord.setRelationship(patientRecordDTO.getRelationship().toLowerCase().trim());
        newPatientRecord.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        newPatientRecord.setCity(areaRepository.findByAreaCode(patientRecordDTO.getCityCode()));
        newPatientRecord.setDistrict(areaRepository.findByAreaCode(patientRecordDTO.getDistrictCode()));
        newPatientRecord.setWard(areaRepository.findByAreaCode(patientRecordDTO.getWardCode()));
        result = patientRecordRepository.save(newPatientRecord);
        result.setPatientRecordCode("HSBN-" + result.getId());
        result = patientRecordRepository.save(result);
        return patientRecordMapper.toDto(result);
    }

    @Override
    public boolean existsByHealthInsuranceCode(String healthInsuranceCode) {
        return patientRecordRepository.existsByHealthInsuranceCodeAndStatus(healthInsuranceCode, Constants.ENTITY_STATUS.ACTIVE);
    }

    @Override
    @Transactional
    public PatientRecordDTO delete(Long patientRecordId) {
        PatientRecord result = patientRecordRepository.findById(patientRecordId)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        List<MedicalDeclarationInfo> medicalDeclarationInfos = medicalDeclarationInfoRepository.findByPatientRecordIdAndStatusNot(
                patientRecordId, Constants.ENTITY_STATUS.DELETED);
        if (!medicalDeclarationInfos.isEmpty()) {
            throw new BadRequestAlertException("Can't delete patient record that already has medical declaration info", ENTITY_NAME, "patient_record.exist_declaration");
        }
        result.setStatus(Constants.ENTITY_STATUS.DELETED);
        return patientRecordMapper.toDto(patientRecordRepository.save(result));
    }

    @Override
    public PatientRecordDTO findByCode(String patientCode) {
        List<PatientRecord> results = patientRecordRepository.findByPatientRecordCode(patientCode);
        return !CollectionUtils.isEmpty(results) ? patientRecordMapper.toDto(results.get(0)) : null;
    }

    @Override
    public boolean existsPersonalPatientRecord() {
        User currentUser = userService.getUserWithAuthorities().orElse(null);
        if (Objects.nonNull(currentUser)) {
            return patientRecordRepository.existsByUserIdAndRelationshipAndStatusNot(currentUser.getId(), Constants.RELATIONSHIP.ME, Constants.ENTITY_STATUS.DELETED);
        }
        return false;
    }

    @Override
    public boolean existsPersonalPatientRecord(Long id) {
        User currentUser = userService.getUserWithAuthorities().orElse(null);
        if (Objects.nonNull(currentUser)) {
            return patientRecordRepository.existsByUserIdAndIdAndRelationshipAndStatusNot(currentUser.getId(), id, Constants.RELATIONSHIP.ME, Constants.ENTITY_STATUS.DELETED);
        }
        return false;
    }

    @Override
    public boolean existsRelativePatientRecord(Long id) {
        User currentUser = userService.getUserWithAuthorities().orElse(null);
        if (Objects.nonNull(currentUser)) {
            return patientRecordRepository.existsByUserIdAndRelationshipNotAndStatusNot(currentUser.getId(), Constants.RELATIONSHIP.ME, Constants.ENTITY_STATUS.DELETED);
        }
        return false;
    }

    @Override
    public HisPatientRecord convertToHisPatientRecord(String bookingCode) {
        HisPatientRecord hisPatientRecord = null;
        Optional<DoctorAppointment> doctorAppointmentOptional = doctorAppointmentRepository.findByBookingCode(bookingCode);
        if (doctorAppointmentOptional.isPresent()) {
            DoctorAppointment doctorAppointment = doctorAppointmentOptional.get();
            hisPatientRecord = new HisPatientRecord(doctorAppointment);
            doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.DONE);
            doctorAppointmentRepository.save(doctorAppointment);
        }
        return hisPatientRecord;
    }
}
