package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Config;
import com.bkav.lk.domain.UploadedFile;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.PatientRecordDTO;
import com.bkav.lk.repository.ConfigRepository;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.service.*;
import com.bkav.lk.service.impl.PatientRecordServiceImpl;
import com.bkav.lk.service.impl.UploadedFileServiceImpl;
import com.bkav.lk.service.mapper.PatientRecordMapper;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.validation.validator.HealthInsuranceCodeValidator;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PatientRecordResource {

    private static final Logger log = LoggerFactory.getLogger(PatientRecordResource.class);

    @Value("${spring.application.name}")
    private String applicationName;

    private static final String ENTITY_NAME = "Patient Records";

    private final PatientRecordService patientRecordService;

    private final UserService userService;

    private final DoctorAppointmentService doctorAppointmentService;

    private final UploadedFileService uploadedFileService;

    private final ActivityLogService activityLogService;

    private final PatientRecordMapper patientRecordMapper;

    private final RestTemplateHelper restTemplateHelper;

    private final ConfigRepository configRepository;

    @Value("${social-insurance.insurance_code_check_url}")
    private String INSURANCE_CODE_CHECK_URL;

    public PatientRecordResource(
            PatientRecordServiceImpl patientRecordService,
            UserService userService,
            DoctorAppointmentService doctorAppointmentService,
            UploadedFileServiceImpl uploadedFileService,
            ActivityLogService activityLogService,
            PatientRecordMapper patientRecordMapper,
            RestTemplateHelper restTemplateHelper, ConfigRepository configRepository) {
        this.patientRecordService = patientRecordService;
        this.userService = userService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.uploadedFileService = uploadedFileService;
        this.activityLogService = activityLogService;
        this.patientRecordMapper = patientRecordMapper;
        this.restTemplateHelper = restTemplateHelper;
        this.configRepository = configRepository;
    }

    @GetMapping("/patient-records/{id}")
    public ResponseEntity<PatientRecordDTO> findOne(@PathVariable Long id) {
        Optional<PatientRecordDTO> dto = patientRecordService.findOne(id);
        if (!dto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(dto);
    }

    @GetMapping("/patient-records/user/{userId}")
    public ResponseEntity<List<PatientRecordDTO>> findByUserId(@PathVariable Long userId, @RequestParam String relationshipName) {
        List<PatientRecordDTO> list = patientRecordService.findByUserId(userId);
        if (!relationshipName.toLowerCase().equals(Constants.RELATIONSHIP.ME)) {
            List<PatientRecordDTO> patientRecordDTOS = list.stream()
                    .filter(item -> item.getRelationship() != null && item.getRelationship().toLowerCase().equals(Constants.RELATIONSHIP.ME))
                    .collect(Collectors.toList());
            ;
            return ResponseEntity.ok().body(patientRecordDTOS);
        }
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/mobile/patient-records/user")
    public ResponseEntity<List<PatientRecordDTO>> findByUser(@RequestParam MultiValueMap<String, String> queryParams,
                                                             Pageable pageable) {
        Optional<String> optionalLogin = SecurityUtils.getCurrentUserLogin();
        if (!optionalLogin.isPresent()) {
            return ResponseEntity.noContent().build();
        }
        Optional<User> user = userService.findByLogin(optionalLogin.get());
        if (!user.isPresent()) {
            return ResponseEntity.noContent().build();
        }
        queryParams.add("userId", String.valueOf(user.get().getId()));

        Page<PatientRecordDTO> result = patientRecordService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), result);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @GetMapping("/patient-records")
    public ResponseEntity<List<PatientRecordDTO>> search(
            @RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.info("REST request to search for a page of patient records for query {}", queryParams);
        if (!StrUtil.isBlank(queryParams.get("ageFrom").get(0)) && !StrUtil.isBlank(queryParams.get("ageTo").get(0))) {
            int ageFrom = Integer.parseInt(queryParams.get("ageFrom").get(0));
            int ageTo = Integer.parseInt(queryParams.get("ageTo").get(0));
            if (ageTo > 0 && ageFrom > ageTo) {
                throw new BadRequestAlertException("AgeFrom is no more than AgeTo",
                        ENTITY_NAME, "validate.minAgeFrom");
            }
        }

        Page<PatientRecordDTO> result = patientRecordService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), result);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @PostMapping("/patient-records")
    public ResponseEntity<PatientRecordDTO> save(
            @RequestParam(name = "file", required = false) MultipartFile avatar,
            @ModelAttribute @Valid PatientRecordDTO patientRecordDTO) throws URISyntaxException {
        if (patientRecordDTO.getId() != null) {
            throw new BadRequestAlertException("A new Patient Record cannot already have an ID",
                    ENTITY_NAME, "idexists");
        }

        //Kiểm tra ngày sinh > ngày hiện tại
        Date date = new Date();
        if (patientRecordDTO.getDob().isAfter(date.toInstant())) {
            throw new BadRequestAlertException("Date of birth cannot be greater than the current date", ENTITY_NAME, "patient_record.dob_max_today");
        }

        //kiêm tra định dạng số BHYT
        if (!HealthInsuranceCodeValidator.isValid(patientRecordDTO.getHealthInsuranceCode())) {
            throw new BadRequestAlertException(HealthInsuranceCodeValidator.ERROR_DEFAULT_MESSAGE,
                    ENTITY_NAME, "health_insurance_code.wrong_format");
        }

        // Kiểm tra số BHYT từ cổng BHXH
        Config config = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_INSURANCE_HOST);
        String url = config.getPropertyValue()
                + this.INSURANCE_CODE_CHECK_URL + "?"
                + "maThe=" + patientRecordDTO.getHealthInsuranceCode()
                + "&hoTen=" + patientRecordDTO.getName()
                + "&ngaySinh=" + DateUtils.convertFromInstantToString(patientRecordDTO.getDob());
        Map<String, String> rs = restTemplateHelper.execute(url, HttpMethod.GET, null, Map.class);
        if (rs != null) {
            if (rs.get("code").equals("wrong_information")) {
                throw new BadRequestAlertException("Health Insurance wrong information",
                        ENTITY_NAME, "insurance_code.wrong_information");
            }
            if (rs.get("code").equals("expired")) {
                throw new BadRequestAlertException("Health Insurance Code Expired",
                        ENTITY_NAME, "insurance_code.expired");
            }
            if (rs.get("code").equals("invalid")) {
                throw new BadRequestAlertException("Health Insurance Code Invalid",
                        ENTITY_NAME, "insurance_code.invalid");
            }
        }
        if (!StringUtils.isEmpty(patientRecordDTO.getHealthInsuranceCode())) {
            boolean isHealthPatientCodeExisted = patientRecordService.existsByHealthInsuranceCode(
                    patientRecordDTO.getHealthInsuranceCode());
            if (isHealthPatientCodeExisted) {
                throw new BadRequestAlertException("A new Patient Record has health insurance code already exists",
                        ENTITY_NAME, "health_insurance_code.exists");
            }
        }

        String relationshipName = Utils.getRelationshipName(patientRecordDTO.getRelationship().toLowerCase());
        if (StringUtils.isEmpty(relationshipName)) {
            throw new BadRequestAlertException("A new Patient Record relationship is invalid",
                    ENTITY_NAME, "relationship_invalid");
        } else {
            boolean hasPersonalPatientRecord = patientRecordService.existsPersonalPatientRecord();
            if (!hasPersonalPatientRecord && !relationshipName.equalsIgnoreCase(Constants.RELATIONSHIP.ME)) {
                throw new BadRequestAlertException("Can't create family Patient Record without personal Patient Record",
                        ENTITY_NAME, "personal_patient_record_isnull");
            }
            if (hasPersonalPatientRecord && relationshipName.equalsIgnoreCase(Constants.RELATIONSHIP.ME)) {
                throw new BadRequestAlertException("Can't create more than 1 personal Patient Record",
                        ENTITY_NAME, "personal_patient_record_exists");
            }
        }

        UploadedFile uploadedFile = null;
        if (avatar != null) {
            try {
                uploadedFile = uploadedFileService.store(avatar);
                patientRecordDTO.setAvatar(uploadedFile.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        PatientRecordDTO result = patientRecordService.save(patientRecordDTO);
        activityLogService.create(Constants.CONTENT_TYPE.PATIENT_RECORD,
                patientRecordMapper.toEntity(result));

        return ResponseEntity.created(new URI("/api/patient-records/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @PutMapping("/patient-records")
    public ResponseEntity<PatientRecordDTO> edit(
            @RequestParam(name = "file", required = false) MultipartFile avatar,
            @ModelAttribute @Valid PatientRecordDTO patientRecordDTO) throws URISyntaxException {
        if (patientRecordDTO.getId() == null) {
            throw new BadRequestAlertException("A new Patient Record cannot update if ID null",
                    ENTITY_NAME, "idnull");
        }

        //Kiểm tra ngày sinh > ngày hiện tại
        Date date = new Date();
        if (patientRecordDTO.getDob().isAfter(date.toInstant())) {
            throw new BadRequestAlertException("Date of birth cannot be greater than the current date", ENTITY_NAME, "patient_record.dob_max_today");
        }

        //kiêm tra định dạng số BHYT
        if (!HealthInsuranceCodeValidator.isValid(patientRecordDTO.getHealthInsuranceCode())) {
            throw new BadRequestAlertException(HealthInsuranceCodeValidator.ERROR_DEFAULT_MESSAGE,
                    ENTITY_NAME, "health_insurance_code.wrong_format");
        }

        // Kiểm tra số BHYT từ cổng BHXH
        Config config = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_INSURANCE_HOST);
        String url = config.getPropertyValue()
                + this.INSURANCE_CODE_CHECK_URL + "?"
                + "maThe=" + patientRecordDTO.getHealthInsuranceCode()
                + "&hoTen=" + patientRecordDTO.getName()
                + "&ngaySinh=" + DateUtils.convertFromInstantToString(patientRecordDTO.getDob());
        Map<String, String> rs = restTemplateHelper.execute(url, HttpMethod.GET, null, Map.class);
        if (rs != null) {
            if (rs.get("code").equals("wrong_information")) {
                throw new BadRequestAlertException("Health Insurance wrong information",
                        ENTITY_NAME, "insurance_code.wrong_information");
            }
            if (rs.get("code").equals("expired")) {
                throw new BadRequestAlertException("Health Insurance Code Expired",
                        ENTITY_NAME, "insurance_code.expired");
            }
            if (rs.get("code").equals("invalid")) {
                throw new BadRequestAlertException("Health Insurance Code Invalid",
                        ENTITY_NAME, "insurance_code.invalid");
            }
        }

        PatientRecordDTO currentPatientRecord = patientRecordService.findOne(patientRecordDTO.getId()).orElse(null);
        if (!StringUtils.isEmpty(patientRecordDTO.getHealthInsuranceCode())) {
            if (Objects.nonNull(currentPatientRecord)) {
                if (!StringUtils.isEmpty(currentPatientRecord.getHealthInsuranceCode()) &&
                        !currentPatientRecord.getHealthInsuranceCode().equals(patientRecordDTO.getHealthInsuranceCode().trim())) {
                    boolean isHealthPatientCodeExisted = patientRecordService.existsByHealthInsuranceCode(
                            patientRecordDTO.getHealthInsuranceCode());
                    if (isHealthPatientCodeExisted) {
                        throw new BadRequestAlertException("A new Patient Record has health insurance code already exists",
                                ENTITY_NAME, "health_insurance_code.exists");
                    }
                }
            } else {
                throw new BadRequestAlertException("Not found patient record with ID = " + patientRecordDTO.getId(),
                        ENTITY_NAME, "id_incorrect");
            }
        }

        String relationshipName = Utils.getRelationshipName(patientRecordDTO.getRelationship().toLowerCase());
        if (StringUtils.isEmpty(relationshipName)) {
            throw new BadRequestAlertException("A new Patient Record relationship is invalid",
                    ENTITY_NAME, "relationship_invalid");
        } else {
            String currentRelationshipName = Utils.getRelationshipName(currentPatientRecord.getRelationship().toLowerCase());
            if (Constants.RELATIONSHIP.ME.equalsIgnoreCase(currentRelationshipName)
            && !currentRelationshipName.equalsIgnoreCase(relationshipName)) {
                throw new BadRequestAlertException("can't change my patient record to another relationship",
                        ENTITY_NAME, "change_relationship_invalid");
            }
            boolean hasPersonalPatientRecord = patientRecordService.existsPersonalPatientRecord();
            boolean isPersonalPatientRecord = patientRecordService.existsPersonalPatientRecord(currentPatientRecord.getId());
            if (hasPersonalPatientRecord && !isPersonalPatientRecord && relationshipName.equalsIgnoreCase(Constants.RELATIONSHIP.ME)) {
                throw new BadRequestAlertException("Can't create more than 1 personal Patient Record",
                        ENTITY_NAME, "personal_patient_record_exists");
            }
        }

        UploadedFile uploadedFile = null;
        if (avatar != null) {
            try {
                uploadedFileService.deleteByStoredName(currentPatientRecord.getAvatar());
                uploadedFile = uploadedFileService.store(avatar);
                patientRecordDTO.setAvatar(uploadedFile.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        PatientRecordDTO result = patientRecordService.save(patientRecordDTO);
        activityLogService.update(Constants.CONTENT_TYPE.PATIENT_RECORD,
                patientRecordMapper.toEntity(currentPatientRecord),
                patientRecordMapper.toEntity(result));

        return ResponseEntity.created(new URI("/api/patient-records/" + result.getId()))
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @DeleteMapping("/patient-records/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long patientRecordId) {
        log.info("REST request to delete a patient records with ID = {}", patientRecordId);
        boolean hasAppointments = doctorAppointmentService.existByPatientRecordId(patientRecordId);
        if (!hasAppointments) {
            boolean isPersonalPatientRecord = patientRecordService.existsPersonalPatientRecord(patientRecordId);
            if (!isPersonalPatientRecord) {
                PatientRecordDTO result = patientRecordService.delete(patientRecordId);
                activityLogService.delete(Constants.CONTENT_TYPE.PATIENT_RECORD, patientRecordMapper.toEntity(result));
            } else {
                boolean hasRelativeRecord = patientRecordService.existsRelativePatientRecord(patientRecordId);
                if (!hasRelativeRecord) {
                    PatientRecordDTO result = patientRecordService.delete(patientRecordId);
                    activityLogService.delete(Constants.CONTENT_TYPE.PATIENT_RECORD, patientRecordMapper.toEntity(result));
                } else {
                    throw new BadRequestAlertException("Patient record already has relative patient record", ENTITY_NAME, "patient-record.relative-record-exist");
                }
            }
        } else {
            throw new BadRequestAlertException("Patient record already has appointments", ENTITY_NAME, "patient-record.appointments-exist");
        }
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME,
                patientRecordId.toString())).build();
    }

}
