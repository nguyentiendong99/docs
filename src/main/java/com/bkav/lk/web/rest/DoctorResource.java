package com.bkav.lk.web.rest;

import com.bkav.lk.domain.UploadedFile;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DoctorCustomConfigDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.service.*;
import com.bkav.lk.service.impl.DoctorServiceImpl;
import com.bkav.lk.service.storage.FileSystemStorageService;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.bkav.lk.web.rest.vm.HisDoctor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DoctorResource {

    private static final Logger log = LoggerFactory.getLogger(DoctorResource.class);

    private static final String ENTITY_NAME = "doctor";
    private static final String DOCTOR_EXCEL_TEMPLATE_NAME = "doctors.xlsx";
    private static final String DOCTOR_EXCEL_EXPORT_NAME = "doctors-export.xlsx";

    private final DoctorService doctorService;
    private final DoctorAppointmentService doctorAppointmentService;
    private final StorageService storageService;
    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;
    private final HealthFacilitiesService healthFacilitiesService;
    private final UserService userService;
    private final UploadedFileService uploadedFileService;
    private final ObjectMapper mapper;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    public DoctorResource(
            DoctorServiceImpl doctorService, DoctorAppointmentService doctorAppointmentService,
            FileSystemStorageService storageService, DoctorAppointmentConfigurationService doctorAppointmentConfigurationService,
            HealthFacilitiesService healthFacilitiesService, UserService userService, UploadedFileService uploadedFileService) {
        this.doctorService = doctorService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.storageService = storageService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.userService = userService;
        this.uploadedFileService = uploadedFileService;
        this.mapper = new ObjectMapper();
    }

    @GetMapping("/doctors/in-thirty-days")
    public ResponseEntity<List<DoctorDTO>> findAllDoctorsWithin30Days(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        if (!queryParams.containsKey("healthFacilityId")) {
            throw new BadRequestAlertException("healthFacilitiesId is invalid !", "Doctor", "healthFacilityId_invalid");
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            Long healthFacilityId = Long.valueOf(queryParams.get("healthFacilityId").get(0));
            Optional<HealthFacilitiesDTO> optional = healthFacilitiesService.findOne(healthFacilityId);
            if (!optional.isPresent()) {
                throw new BadRequestAlertException("healthFacilities not exist !", "Doctor", "healthFacility_not_exist");
            }
        }

        Page<DoctorDTO> page = doctorService.findAllDoctorsWithin30Days(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/doctors/time-selected")
    public ResponseEntity<List<DoctorDTO>> findAllByTimeSelected(@RequestParam MultiValueMap<String, String> queryParams) {
        if ((queryParams.containsKey("healthFacilitiesId") && !StrUtil.isBlank(queryParams.get("healthFacilitiesId").get(0)))
                && (queryParams.containsKey("startTime") && !StrUtil.isBlank(queryParams.get("startTime").get(0)))
                && (queryParams.containsKey("endTime") && !StrUtil.isBlank(queryParams.get("endTime").get(0)))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Long healthFacilitiesId = Long.valueOf(queryParams.get("healthFacilitiesId").get(0));
            ZonedDateTime startDateTime = ZonedDateTime.of(LocalDateTime.parse(queryParams.get("startTime").get(0), formatter), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime endDateTime = ZonedDateTime.of(LocalDateTime.parse(queryParams.get("endTime").get(0), formatter), ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant startTime = startDateTime.toInstant();
            Instant endTime = endDateTime.toInstant();
            if (queryParams.containsKey("medicalSpecialityId") && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
                Long medicalSpecialityId = Long.valueOf(queryParams.get("medicalSpecialityId").get(0));
                // TH: co ca medicalSpecialityId va clinicId
                if (queryParams.containsKey("clinicId") && !StrUtil.isBlank(queryParams.get("clinicId").get(0))) {
                    Long clinicId = Long.valueOf(queryParams.get("clinicId").get(0));
                    return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId, medicalSpecialityId, clinicId, startDateTime.toLocalDate().toString(), startTime, endTime));
                }
                return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId, medicalSpecialityId, null, startDateTime.toLocalDate().toString(), startTime, endTime));
            }
            if (queryParams.containsKey("clinicId") && !StrUtil.isBlank(queryParams.get("clinicId").get(0))) {
                Long clinicId = Long.valueOf(queryParams.get("clinicId").get(0));
                // TH: co ca clinicId va medicalSpecialityId
                if (queryParams.containsKey("medicalSpecialityId") && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
                    Long medicalSpecialityId = Long.valueOf(queryParams.get("medicalSpecialityId").get(0));
                    return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId, medicalSpecialityId, clinicId, startDateTime.toLocalDate().toString(), startTime, endTime));
                }
                return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId,null, clinicId, startDateTime.toLocalDate().toString(), startTime, endTime));
            }
            return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId, startDateTime.toLocalDate().toString(), startTime, endTime));
        }
        throw new BadRequestAlertException("query params is invalid !", "Doctor", "query_params_invalid");
    }

    @GetMapping("/public/doctors/time-selected")
    public ResponseEntity<List<DoctorDTO>> findAllByTimeSelectedPublic(@RequestParam MultiValueMap<String, String> queryParams) {
        if ((queryParams.containsKey("healthFacilitiesId") && !StrUtil.isBlank(queryParams.get("healthFacilitiesId").get(0)))
                && (queryParams.containsKey("startTime") && !StrUtil.isBlank(queryParams.get("startTime").get(0)))
                && (queryParams.containsKey("endTime") && !StrUtil.isBlank(queryParams.get("endTime").get(0)))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Long healthFacilitiesId = Long.valueOf(queryParams.get("healthFacilitiesId").get(0));
            ZonedDateTime startDateTime = ZonedDateTime.of(LocalDateTime.parse(queryParams.get("startTime").get(0), formatter), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime endDateTime = ZonedDateTime.of(LocalDateTime.parse(queryParams.get("endTime").get(0), formatter), ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant startTime = startDateTime.toInstant();
            Instant endTime = endDateTime.toInstant();
            if (queryParams.containsKey("medicalSpecialityId") && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
                Long medicalSpecialityId = Long.valueOf(queryParams.get("medicalSpecialityId").get(0));
                return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId, medicalSpecialityId,null, startDateTime.toLocalDate().toString(), startTime, endTime));
            }
            return ResponseEntity.ok().body(doctorService.findAllByTimeSelected(healthFacilitiesId, startDateTime.toLocalDate().toString(), startTime, endTime));
        }
        throw new BadRequestAlertException("query params is invalid !", "Doctor", "query_params_invalid");
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> search(
            @RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("REST request to search for a page of Doctors for query {}", queryParams);
        Page<DoctorDTO> page = doctorService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<DoctorDTO> findById(@PathVariable(name = "id") Long id) {
        DoctorDTO result;
        try {
            result = doctorService.findById(id);
        } catch (BadRequestAlertException ex) {
            throw ex;
        }
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/doctors/findAll")
    public ResponseEntity<List<DoctorDTO>> findAll() {
        List<DoctorDTO> listDoctorDTO = doctorService.findAll();
        return ResponseEntity.ok().body(listDoctorDTO);
    }

    @GetMapping("/doctors/custom-config/{id}")
    public ResponseEntity<List<DoctorCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id) {
        log.info("REST request to get list custom config by doctor id input: {}", id);
        return ResponseEntity.ok().body(doctorService.findAllCustomConfigByDoctorId(id));
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorDTO> create(
            @RequestParam(name = "file", required = false) MultipartFile avatar,
            @RequestParam(name = "listDoctorCustomConfig", required = false) String doctorCustomConfigStr,
            @ModelAttribute @Valid DoctorDTO doctorDTO,
            @RequestHeader("healthFacilityId") Long healthFacilityId) throws URISyntaxException {
        log.debug("REST request to save Style : {}", doctorDTO);
        if (doctorDTO.getId() != null) {
            throw new BadRequestAlertException("A new doctor cannot already has ID", ENTITY_NAME, "idexist");
        }

        try {
            doctorDTO.setDoctorCustomConfigDTOS(mapper.readValue(doctorCustomConfigStr, new TypeReference<List<DoctorCustomConfigDTO>>() {}));
        } catch (JsonProcessingException e) {
            log.error("Parse JSON occur error: {}", e.getMessage());
        }

        UploadedFile uploadedFile = null;
        if (avatar != null) {
            try {
                uploadedFile = uploadedFileService.store(avatar);
                doctorDTO.setAvatar(uploadedFile.getStoredName());
            } catch (IOException e) {
                log.error("Can't save file, detail: {}", e.getMessage());
            }
        }

        doctorDTO.setHealthFacilityId(healthFacilityId);
        DoctorDTO result = doctorService.create(doctorDTO);
        return ResponseEntity.created(new URI("/api/doctors/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @PutMapping("/doctors")
    public ResponseEntity<DoctorDTO> update(
            @RequestParam(name = "file", required = false) MultipartFile avatar,
            @RequestParam(name = "listDoctorCustomConfig", required = false) String doctorCustomConfigStr,
            @ModelAttribute @Valid DoctorDTO doctorDTO,
            @RequestHeader("healthFacilityId") Long healthFacilityId) {
        log.debug("REST request to update Doctor : {}", doctorDTO);
        if (doctorDTO.getId() == null) {
            throw new BadRequestAlertException("A doctor cannot save when not has ID", ENTITY_NAME, "idnull");
        }

        try {
            doctorDTO.setDoctorCustomConfigDTOS(mapper.readValue(doctorCustomConfigStr, new TypeReference<List<DoctorCustomConfigDTO>>() {}));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }

        UploadedFile uploadedFile = null;
        if (avatar != null) {
            try {
                uploadedFile = uploadedFileService.store(avatar);
                doctorDTO.setAvatar(uploadedFile.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }

        DoctorDTO result;
        try {
            doctorDTO.setHealthFacilityId(healthFacilityId);
            result = doctorService.update(doctorDTO);
        } catch (BadRequestAlertException ex) {
            throw ex;
        }
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Object> delete(@PathVariable(name = "id") Long id) {
        boolean hasAppointment = doctorAppointmentService.existByDoctorId(id);
        if (!hasAppointment)
            doctorService.delete(id);
        else
            throw new BadRequestAlertException("Doctor already has appointments", ENTITY_NAME, "doctor.appointments-exist");
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
                        ENTITY_NAME, id.toString()))
                .build();
    }

    @DeleteMapping("/doctors/deleteAll")
    public ResponseEntity<Object> deleteAllById(@RequestParam(name = "ids") List<Long> ids) {
        log.info("List of doctor's id  for deleting: {}", ids);
        doctorService.deleteAll(ids);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/doctors/health-facility/{id}")
    public ResponseEntity<List<DoctorDTO>> findByHealthFacilityId(@PathVariable(name = "id") Long healthFacilityId) {
        log.info("List of doctor with health facility id = {}", healthFacilityId);
        List<DoctorDTO> result = doctorService.findAllDoctorByHealthFacilityId(healthFacilityId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/doctors/health-facility/{id}/user")
    public ResponseEntity<List<DoctorDTO>> findByHealthFacilityIdAndUser(@PathVariable(name = "id") Long healthFacilityId) {
        log.info("List of doctor with health facility id = {}", healthFacilityId);
        List<UserDTO> users = userService.getListUserIsAssignedDoctors(healthFacilityId);
        List<Long> doctorIdList = new ArrayList<>();
        users.forEach(u -> {
            doctorIdList.add(u.getDoctorId());
        });
        List<DoctorDTO> result = doctorService.findAllDoctorByHealthFacilityId(healthFacilityId);

        if (!users.isEmpty()) {
                result = result.stream()
                        .filter(item -> doctorIdList.contains(item.getId()))
                        .collect(Collectors.toList());
        }
//        if(user.getDoctorId() != null){
//            result = result.stream().filter(item -> item.getId() == user.getDoctorId()).collect(Collectors.toList());
//        }
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/doctors/main-health-facility/{id}")
    public ResponseEntity<List<DoctorDTO>> findByMainHealthFacilityId(@PathVariable(name = "id") Long healthFacilityId) {
        log.info("List of doctor with main health facility id = {}", healthFacilityId);
        List<DoctorDTO> result = doctorService.findByMainHealthFacilityId(healthFacilityId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/doctors/code/{code}")
    public ResponseEntity<DoctorDTO> findByCode(@PathVariable(name = "code") String code) {
        log.info("Find a doctor with code = {}", code);
        DoctorDTO result = doctorService.findByCode(code);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/doctors/is-code-exist/{code}")
    public ResponseEntity<Boolean> isDoctorCodeExist(@PathVariable(name = "code") String code) {
        log.info("Check doctor with code: {} exist or not", code);
        Boolean result = doctorService.isDoctorCodeExist(code);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/doctors/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadDoctor(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("healthFacilityId") Long healthFacilityId) {
        log.info("REST request upload doctor by file: {}", file.getOriginalFilename());
        List<DoctorDTO> doctorDTOs;

        ResultExcel resultExcel = new ResultExcel();
        List<ErrorExcel> details = new ArrayList<>();

        try {
            doctorDTOs = doctorService.addDoctorsByExcelFile(file.getInputStream(), details);
            if (details.isEmpty()) {
                resultExcel.setSucess(true);
                if (doctorDTOs.size() > 0) {
                    doctorDTOs.stream().forEach(o -> {
                        o.setHealthFacilityId(healthFacilityId);
                    });
                    doctorService.createAll(doctorDTOs);
                }
            } else {
                resultExcel.setSucess(false);
            }
            resultExcel.setErrorExcels(details);

        } catch (IOException e) {
            log.error("Error: ", e);
        }

        return ResponseEntity.ok().body(resultExcel);
    }

    @GetMapping("/public/doctors/export-excel")
    public ResponseEntity<String> exportDoctorExcel(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) throws IOException {

        log.debug("REST request to search for a page of Doctors for query {}", queryParams);
        List<DoctorDTO> list = doctorService.search(queryParams, pageable).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/doctors-export.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/doctors-export-" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @GetMapping("/public/doctors/export")
    public void exportExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/doctors-export-" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=doctors-export-" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/public/doctors/download/template-excel")
    public void downloadTemplateExcelDoctor(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment; filename=" + DOCTOR_EXCEL_TEMPLATE_NAME);
        IOUtils.copy(storageService.downloadExcelTemplateFromResource(DOCTOR_EXCEL_TEMPLATE_NAME), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/doctors/hasAppointment")
    public ResponseEntity<List<DoctorDTO>> findByDoctorIdsAndExistsAppointment(@RequestParam(name = "ids") List<Long> ids) {
        log.debug("REST request to get list of Doctors in ids: {}", ids);
        List<DoctorDTO> results = doctorService.findByDoctorIdsAndExistsAppointment(ids);
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/doctors/async-his")
    public ResponseEntity<List<HisDoctor>> asyncDoctorListFromHis(@RequestHeader("healthFacilityId") Long healthFacilityId) {
        Optional<HealthFacilitiesDTO> healthFacilitiesDTOOptional = healthFacilitiesService.findOne(healthFacilityId);
        if (healthFacilitiesDTOOptional.isPresent()) {
            List<HisDoctor> doctorList = doctorService.getListDoctorFromHis(healthFacilitiesDTOOptional.get().getCode());
            return ResponseEntity.ok().body(doctorList);
        } else {
            throw new BadRequestAlertException("HealthFacilities not exist", ENTITY_NAME, "health_facility.not_exist");
        }
    }
}
