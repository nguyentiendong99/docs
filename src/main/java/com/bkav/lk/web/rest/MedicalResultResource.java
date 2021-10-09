package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Config;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import com.bkav.lk.dto.MedicalResultDTO;
import com.bkav.lk.dto.PatientDTO;
import com.bkav.lk.repository.ConfigRepository;
import com.bkav.lk.service.DoctorAppointmentService;
import com.bkav.lk.service.HealthFacilitiesService;
import com.bkav.lk.service.MedicalResultService;
import com.bkav.lk.service.PatientService;
import com.bkav.lk.service.mapper.MedicalResultMapper;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.vm.HisPatientContentVM;
import com.bkav.lk.web.rest.vm.MedicalResultVM;
import com.bkav.lk.web.rest.vm.PatientPhoneVM;
import com.bkav.lk.web.rest.vm.ShortedMedicalResultVM;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/medical-results")
public class MedicalResultResource {

    private static final Logger log = LoggerFactory.getLogger(MedicalResultResource.class);

    private static final String ENTITY_NAME = "medical_result";

    private final DoctorAppointmentService doctorAppointmentService;

    private final PatientService patientService;

    private final RestTemplateHelper restTemplateHelper;

    private final ConfigRepository configRepository;

    private final MedicalResultService medicalResultService;

    private final MedicalResultMapper medicalResultMapper;

    private final HealthFacilitiesService healthFacilitiesService;

    @Value("${his.patient_check_url}")
    private String PATIENT_CHECK_URL;

    @Value("${his.medical_result_url}")
    private String MEDICAL_RESULT_URL;

    @Value("${his.patient_phone_url}")
    private String PATIENT_PHONE_URL;

    @Autowired
    public MedicalResultResource(
            DoctorAppointmentService doctorAppointmentService,
            PatientService patientService, RestTemplateHelper restTemplateHelper,
            ConfigRepository configRepository,
            MedicalResultService medicalResultService,
            MedicalResultMapper medicalResultMapper,
            HealthFacilitiesService healthFacilitiesService) {
        this.doctorAppointmentService = doctorAppointmentService;
        this.patientService = patientService;
        this.restTemplateHelper = restTemplateHelper;
        this.configRepository = configRepository;
        this.medicalResultService = medicalResultService;
        this.medicalResultMapper = medicalResultMapper;
        this.healthFacilitiesService = healthFacilitiesService;
    }

    @GetMapping("/exist-doctor-appointment")
    public ResponseEntity<HisPatientContentVM> findByHealthFacilityAndPatientRecord(@RequestParam MultiValueMap<String, String> queryParams) {
        log.info("REST request for check exist the doctor appointment with conditions: {}", queryParams);
        HisPatientContentVM response = null;
        PatientDTO patient = null;
        Long healthFacilityId = null;
        String patientRecordCode = null;
        String patientRecordName = null;
        boolean isPatientExist = false;
        if (queryParams.containsKey("healthFacilityId") && StringUtils.isNotBlank(queryParams.getFirst("healthFacilityId"))) {
            healthFacilityId = Long.parseLong(queryParams.getFirst("healthFacilityId").trim());
        } else {
            throw new BadRequestAlertException("Patient information is incorrect!", ENTITY_NAME, "patient_incorrect");
        }
        if (queryParams.containsKey("patientRecordCode") && StringUtils.isNotBlank(queryParams.getFirst("patientRecordCode"))) {
            patientRecordCode = queryParams.getFirst("patientRecordCode").trim();
        }
        if (queryParams.containsKey("patientRecordName") && StringUtils.isNotBlank(queryParams.getFirst("patientRecordName"))) {
            patientRecordName = queryParams.getFirst("patientRecordName").trim();
        }
//        List<DoctorAppointmentDTO> doctorAppointments = doctorAppointmentService.findByHealthFacilityAndPatient(queryParams);

        HealthFacilitiesDTO healthFacilitiesDTO = healthFacilitiesService.findById(healthFacilityId);
        if (Objects.isNull(healthFacilitiesDTO)) {
            throw new BadRequestAlertException("Patient information is incorrect!", ENTITY_NAME, "patient_incorrect");
        }

        Config config = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
        patient = this.patientService.findByPatientCodeAndPatientNameAndHealthFacility(patientRecordCode, patientRecordName, healthFacilitiesDTO.getCode());
        if (Objects.nonNull(patient)) {
            List<MedicalResultDTO> medicalResultDTOs = medicalResultService.findByPatientId(patient.getId());
            List<ShortedMedicalResultVM> medicalResultVMs = medicalResultMapper.toVM(medicalResultDTOs);
            response = new HisPatientContentVM();
            response.setConnectionCode(patient.getPatientCode());
            response.setPatientRecordName(patient.getPatientName());
            response.setPatientRecordPhone(patient.getPhone());
            response.setHealthFacilityCode(patient.getHealthFacilityCode());
            if (!CollectionUtils.isEmpty(medicalResultVMs)) {
                response.setHealthFacilityName(medicalResultDTOs.get(0).getHealthFacilityName());
                response.setMedicalResults(medicalResultVMs);
            } else {
                response = restTemplateHelper.execute(
                        config.getPropertyValue() + PATIENT_CHECK_URL + patientRecordCode + "/" + patientRecordName,
                        HttpMethod.GET, null, HisPatientContentVM.class);
                if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getMedicalResults())
                        && healthFacilitiesDTO.getCode().equals(response.getHealthFacilityCode())) {
                    String healthFacilityName = response.getHealthFacilityName();
                    Long patientId = patient.getId();
                    medicalResultDTOs = medicalResultMapper.toCollectionDto(response.getMedicalResults());
                    medicalResultDTOs.forEach(o -> {
                        o.setHealthFacilityName(healthFacilityName);
                        o.setPatientId(patientId);
                    });
                    medicalResultDTOs = medicalResultService.saveAll(medicalResultDTOs);
                    medicalResultVMs = medicalResultMapper.toVM(medicalResultDTOs);
                    if (!CollectionUtils.isEmpty(medicalResultVMs)) {
                        response.setHealthFacilityName(healthFacilityName);
                        response.setMedicalResults(medicalResultVMs);
                    }
                }
            }
            isPatientExist = true;
        } else {
            response = restTemplateHelper.execute(
                    config.getPropertyValue() + PATIENT_CHECK_URL + patientRecordCode + "/" + patientRecordName,
                    HttpMethod.GET, null, HisPatientContentVM.class);
            if (Objects.nonNull(response) && healthFacilitiesDTO.getCode().equals(response.getHealthFacilityCode())) {
                patient = this.patientService.savePatientInformation(response);
                String healthFacilityName = response.getHealthFacilityName();
                Long patientId = Objects.nonNull(patient) ? patient.getId() : null;
                List<MedicalResultDTO> medicalResultDTOs = medicalResultMapper.toCollectionDto(response.getMedicalResults());
                medicalResultDTOs.forEach(o -> {
                    o.setHealthFacilityName(healthFacilityName);
                    o.setPatientId(patientId);
                });
                medicalResultService.saveAll(medicalResultDTOs);
                isPatientExist = true;
            }
        }

        if (!isPatientExist) {
            throw new BadRequestAlertException("Patient information is incorrect!", ENTITY_NAME, "patient_incorrect");
        }

        // order desc examination date
        if (Objects.nonNull(response) && Objects.nonNull(response.getMedicalResults())) {
            response.getMedicalResults().sort(Comparator.comparing(o -> Long.parseLong(o.getExaminationDate()), Comparator.reverseOrder()));
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/specific-doctor-appointment")
    public ResponseEntity<MedicalResultVM> findByDoctorAppointmentCode(@RequestParam MultiValueMap<String, String> queryParams) {
        String doctorAppointmentCode = null;
        String patientRecordCode = null;
        String hisUrl = null;
        log.info("REST request for get a medical result with conditions: {}", queryParams);
        Config config = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
        if (queryParams.containsKey("doctorAppointmentCode") && StringUtils.isNotBlank(queryParams.getFirst("doctorAppointmentCode"))) {
            doctorAppointmentCode = queryParams.getFirst("doctorAppointmentCode").trim();
        } else {
            throw new BadRequestAlertException("Doctor appointment code is required", ENTITY_NAME, "medical_result.emptyfield");
        }
        if (queryParams.containsKey("patientRecordCode") && StringUtils.isNotBlank(queryParams.getFirst("patientRecordCode"))) {
            patientRecordCode = queryParams.getFirst("patientRecordCode").trim();
            hisUrl = config.getPropertyValue() + MEDICAL_RESULT_URL + patientRecordCode + "/" + doctorAppointmentCode;
        } else {
            hisUrl = config.getPropertyValue() + MEDICAL_RESULT_URL + doctorAppointmentCode;
        }
        MedicalResultVM response = restTemplateHelper.execute(hisUrl, HttpMethod.POST, null, MedicalResultVM.class);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/suggestion")
    public ResponseEntity<List<PatientDTO>> searchSuggestions(@RequestParam("healthFacilityId") Long healthFacilityId) {
        List<PatientDTO> results = patientService.findByCurrentUserAndHealthFacility(healthFacilityId);
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/old-appointment-code/{appointmentCode}/patient-phone")
    public ResponseEntity<PatientPhoneVM> findHisPatientPhone(@PathVariable("appointmentCode") String appointmentCode) {
        PatientPhoneVM patientPhone = null;
        PatientDTO patient = patientService.findByOldAppointmentCode(appointmentCode);
        if (Objects.isNull(patient)) {
            Config config = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
            String hisUrl = config.getPropertyValue() + PATIENT_PHONE_URL;
            Map<String, String> response = restTemplateHelper.execute(hisUrl, HttpMethod.GET, null, Map.class, appointmentCode);
            if (Objects.nonNull(response) && Objects.nonNull(response.get("his_sodienthoai"))) {
                patientPhone = new PatientPhoneVM(response.get("his_sodienthoai"));
            }
        } else {
            patientPhone = new PatientPhoneVM(patient.getPhone());
        }
        return ResponseEntity.ok().body(patientPhone);
    }

}
