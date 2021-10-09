package com.bkav.lk.web.rest;

import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.domain.UploadedFile;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.HealthFacilitiesMapper;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.bkav.lk.web.rest.vm.googlemap.GeocodeGeometry;
import com.bkav.lk.web.rest.vm.googlemap.GeocodeLocation;
import com.bkav.lk.web.rest.vm.googlemap.GeocodeObject;
import com.bkav.lk.web.rest.vm.googlemap.GeocodeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class HealthFacilitiesResource {

    @Value("${google-map.api-key}")
    private String googleApiKey;

    @Value("${google-map.url-geocode}")
    private String googleUrlGeocode;

    private final Logger log = LoggerFactory.getLogger(HealthFacilitiesResource.class);

    private static final String ENTITY_NAME = "Health Facilities";

    private final HealthFacilitiesService healthFacilitiesService;

    private final UploadedFileService uploadedFileService;

    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    private final ActivityLogService activityLogService;

    private final HealthFacilitiesMapper healthFacilitiesMapper;

    private final DoctorService doctorService;

    private final MedicalSpecialityService medicalSpecialityService;

    private final StorageService storageService;

    private final UserService userService;

    private final DoctorAppointmentService doctorAppointmentService;

    private final AreaService areaService;

    private final RestTemplateHelper restTemplateHelper;

    public HealthFacilitiesResource(HealthFacilitiesService healthFacilitiesService,
                                    UploadedFileService uploadedFileService,
                                    DoctorAppointmentConfigurationService doctorAppointmentConfigurationService,
                                    ActivityLogService activityLogService, HealthFacilitiesMapper healthFacilitiesMapper,
                                    DoctorService doctorService, MedicalSpecialityService medicalSpecialityService,
                                    StorageService storageService, UserService userService,
                                    DoctorAppointmentService doctorAppointmentService, AreaService areaService, RestTemplateHelper restTemplateHelper) {
        this.healthFacilitiesService = healthFacilitiesService;
        this.uploadedFileService = uploadedFileService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
        this.activityLogService = activityLogService;
        this.healthFacilitiesMapper = healthFacilitiesMapper;
        this.doctorService = doctorService;
        this.medicalSpecialityService = medicalSpecialityService;
        this.storageService = storageService;
        this.userService = userService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.areaService = areaService;
        this.restTemplateHelper = restTemplateHelper;
    }

    @GetMapping("/health-facilities/{id}")
    public ResponseEntity<HealthFacilitiesDTO> getHealthFacility(@PathVariable Long id) {
        log.debug("REST request to get healthFacilities : {}", id);
        Optional<HealthFacilitiesDTO> healthFacilitiesDTO = healthFacilitiesService.findOne(id);
        if (!healthFacilitiesDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(healthFacilitiesDTO);
    }

    // search cho mobi
    @GetMapping("/health-facilities/search")
    public ResponseEntity<List<HealthFacilitiesDTO>> search(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("REST request to search for a page of healthFacilities for query {}", queryParams);
        Page<HealthFacilitiesDTO> page = healthFacilitiesService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/health-facilities")
    public ResponseEntity<HealthFacilitiesDTO> createHealthFacility(
            @RequestParam(name = "formData") String formData,
            @RequestParam(name = "avatar", required = false) MultipartFile avatar,
            @RequestParam(name = "medicalprocess", required = false) MultipartFile medicalprocess) {
        log.debug("REST request to save HealthFacility");

        ObjectMapper mapper = new ObjectMapper();
        HealthFacilitiesDTO healthFacilitiesDTO = null;
        try {
            healthFacilitiesDTO = mapper.readValue(formData, HealthFacilitiesDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        if (healthFacilitiesDTO == null || healthFacilitiesDTO.getId() != null) {
            throw new BadRequestAlertException("A new Health Facility cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UploadedFile uploadedFile;
        UploadedFile medicalProcess;
        if (avatar != null) {
            try {
                uploadedFile = uploadedFileService.store(avatar);
                healthFacilitiesDTO.setImgPath(uploadedFile.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        if (medicalprocess != null) {
            try {
                medicalProcess = uploadedFileService.store(medicalprocess);
                healthFacilitiesDTO.setMedicalProcessPath(medicalProcess.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        // Check dia chi - thanh pho
        String[] addressElement = healthFacilitiesDTO.getAddress().split(",");
        if (addressElement.length >= 2) {
            String cityName = addressElement[addressElement.length - 2];
            List<AreaDTO> cities = areaService.findByNameAndParentCodeAndStatus(cityName, Constants.COUNTRY_VN.CODE, Constants.ENTITY_STATUS.ACTIVE);

            if (!cities.isEmpty()) {
                healthFacilitiesDTO.setCityCode(cities.get(0).getAreaCode());
            }
        }

        if (StringUtils.isEmpty(healthFacilitiesDTO.getCityCode())) {
            healthFacilitiesDTO.setCityCode(Constants.SYT_DEFAULT.cityCode); // Set mac dinh cityCode cua Tinh Yen Bai
        }

        HealthFacilitiesDTO result = healthFacilitiesService.save(healthFacilitiesDTO);
        if (result == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "healthfacilities", "", "Exception in creating new healthfacilities"))
                    .body(null);
        }
        activityLogService.create(Constants.CONTENT_TYPE.HEALTH_FACILITY, healthFacilitiesMapper.toEntity(result));
        return ResponseEntity.ok(result);
    }

    @GetMapping("health-facilities/all/tree")
    public ResponseEntity<Map<String, Object>> getAllConvertToTree(@RequestParam Map<String, String> params) {
        log.debug("REST request to get all HealthFacilities active, unactive and convert to tree");
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findByStatusActiveOrUnActive();
        healthFacilitiesDTOList.sort(Comparator.comparing(HealthFacilitiesDTO::getStatus).thenComparing(HealthFacilitiesDTO::getLastModifiedDate, Comparator.reverseOrder()));
        Map<String, Object> healthFacilitiesDTOListToTree = healthFacilitiesService.handleListToTree(healthFacilitiesDTOList, params);
        return ResponseEntity.ok().body(healthFacilitiesDTOListToTree);
    }

    @GetMapping("health-facilities/all")
    public ResponseEntity<List<HealthFacilitiesDTO>> getAll(@RequestParam(name = "status") List<Integer> status) {
        log.debug("REST request to get all HealthFacilities by status "  +  status);
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findByStatus(status);
        return ResponseEntity.ok().body(healthFacilitiesDTOList);
    }

    @DeleteMapping("/health-facilities/{id}")
    public ResponseEntity<Void> deleteHealthFacility(@PathVariable Long id) {
        log.debug("REST request to delete HealthFacility : {}", id);
        Integer[] status = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        // Nếu có các thành phần liên quan như đơn vị con, bác sĩ thuộc đơn vị, khoa thuộc đơn vị, DoctorAppointment thì không cho phép xóa
        // Lấy ra các đơn vị con
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findAllByParentAndStatusGreaterThan(id, Constants.ENTITY_STATUS.DELETED);
        if(!healthFacilitiesDTOList.isEmpty()) {
            throw new BadRequestAlertException("Health Facility have child", ENTITY_NAME, "excel.health_facility.delete.have_child");
        }

        // Các bác sĩ thuộc đơn vị
        List<DoctorDTO> doctorDTOList = doctorService.findAllDoctorByHealthFacilityId(id, status);
        if(!doctorDTOList.isEmpty()) {
            throw new BadRequestAlertException("Health Facility have doctor", ENTITY_NAME, "excel.health_facility.delete.doctor_in_healthFacility");
        }

        // Các khoa thuộc đơn vị
        List<MedicalSpecialityDTO> medicalSpecialityDTOS = medicalSpecialityService.findByHealthFacilityId(id, status);
        if(!medicalSpecialityDTOS.isEmpty()) {
            throw new BadRequestAlertException("Health Facility have MedicalSpeciality", ENTITY_NAME, "excel.health_facility.delete.medicalSpeciality_in_healthFacility");
        }
        // Doctor Appointment
        Integer[] statusDoctorAppointment = {Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE, Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED};
        List<DoctorAppointmentDTO> doctorAppointmentDTOList = doctorAppointmentService.findByHealthFacility(id, statusDoctorAppointment); // Danh sach lich kham trang thai Cho duyet, Da duyet
        if (!doctorAppointmentDTOList.isEmpty()) {
            throw new BadRequestAlertException("Health Facility have DoctorAppointment active", ENTITY_NAME, "excel.health_facility.delete.doctorAppointment_in_healthFacility");
        }
        healthFacilitiesService.delete(id);
        // Xóa config đặt lịch khám theo cơ sở y tế
        Optional<HealthFacilitiesDTO> optional = healthFacilitiesService.findOne(id);
        if (optional.isPresent()) {
            if (optional.get().getParent() == null) {
                doctorAppointmentConfigurationService.delete(optional.get().getId());
            }
        }
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, true,
                ENTITY_NAME, id.toString())).build();
    }

    @PutMapping("/health-facilities")
    public ResponseEntity<HealthFacilitiesDTO> update(
            @RequestParam(name = "formData") String formData,
            @RequestParam(name = "avatar", required = false) MultipartFile avatar,
            @RequestParam(name = "medicalprocess", required = false) MultipartFile medicalprocess) throws URISyntaxException {
        log.debug("REST request to update HealthFacility");
        ObjectMapper mapper = new ObjectMapper();
        HealthFacilitiesDTO healthFacilitiesDTO = null;
        try {
            healthFacilitiesDTO = mapper.readValue(formData, HealthFacilitiesDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        if (healthFacilitiesDTO == null || healthFacilitiesDTO.getId() == null) {
            throw new BadRequestAlertException("A new Health Facility cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Optional<HealthFacilitiesDTO> oldHealthFacilitiesOpt = healthFacilitiesService.findOne(healthFacilitiesDTO.getId());
        if(!oldHealthFacilitiesOpt.isPresent()) {
            throw new BadRequestAlertException("A new Health Facility cannot already", ENTITY_NAME, "idexists");
        }

        // Check dia chi - thanh pho
        String[] addressElement = healthFacilitiesDTO.getAddress().split(",");
        if (addressElement.length >= 2) {
            String cityName = addressElement[addressElement.length - 2];
            List<AreaDTO> cities = areaService.findByNameAndParentCodeAndStatus(cityName, Constants.COUNTRY_VN.CODE, Constants.ENTITY_STATUS.ACTIVE);

            if (!cities.isEmpty()) {
                healthFacilitiesDTO.setCityCode(cities.get(0).getAreaCode());
            }
        }

        if (StringUtils.isEmpty(healthFacilitiesDTO.getCityCode())) {
            healthFacilitiesDTO.setCityCode(Constants.SYT_DEFAULT.cityCode); // Set mac dinh cityCode cua Tinh Yen Bai
        }

        HealthFacilities oldHealthFacilities = healthFacilitiesMapper.toEntity(oldHealthFacilitiesOpt.get());
        // Todo Muốn ngừng hoạt động ĐƠn vị -> Tất cả các thành phần liên quan đều phải ngừng hoạt động. CÒn không không được phép ngừng hoạt động
        if (healthFacilitiesDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            // Don vi con
            List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findByParentId(healthFacilitiesDTO.getId()); // Danh sach Don vi dang hoat dong
            if (!healthFacilitiesDTOList.isEmpty()) {
                throw new BadRequestAlertException("Health Facility have child active", ENTITY_NAME, "excel.health_facility.update.child_active");
            }
            // Doctor
            List<DoctorDTO> doctorDTOList = doctorService.findAllDoctorByHealthFacilityId(healthFacilitiesDTO.getId()); // Danh sach Doctor dang hoat dong
            if (!doctorDTOList.isEmpty()) {
                throw new BadRequestAlertException("Health Facility have doctor active", ENTITY_NAME, "excel.health_facility.update.doctor_active_in_healthFacility");
            }
            // Chuyen khoa
            List<MedicalSpecialityDTO> medicalSpecialityDTOList = medicalSpecialityService.findByHealthFacilityId(healthFacilitiesDTO.getId()); //Danh sach Chuyen khoa dang hoat dong
            if (!medicalSpecialityDTOList.isEmpty()) {
                throw new BadRequestAlertException("Health Facility have MedicalSpeciality active", ENTITY_NAME, "excel.health_facility.update.medicalSpeciality_active_in_healthFacility");
            }
            // Doctor Appointment
            Integer[] status = {Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE, Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED};
            List<DoctorAppointmentDTO> doctorAppointmentDTOList = doctorAppointmentService.findByHealthFacility(healthFacilitiesDTO.getId(), status); // Danh sach lich kham trang thai Cho duyet, Da duyet
            if (!doctorAppointmentDTOList.isEmpty()) {
                throw new BadRequestAlertException("Health Facility have DoctorAppointment active", ENTITY_NAME, "excel.health_facility.update.doctorAppointment_active_in_healthFacility");
            }
        }
        UploadedFile uploadedFile;
        UploadedFile medicalProcess;
        if (avatar != null) {
            try {
                uploadedFile = uploadedFileService.store(avatar);
                healthFacilitiesDTO.setImgPath(uploadedFile.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        if (medicalprocess != null) {
            try {
                medicalProcess = uploadedFileService.store(medicalprocess);
                healthFacilitiesDTO.setMedicalProcessPath(medicalProcess.getStoredName());
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        // Neu thay doi cha thi cung phai update lai parentCode
        if(healthFacilitiesDTO.getParent() != oldHealthFacilitiesOpt.get().getParent()) {
            Optional<HealthFacilitiesDTO> parentOpt = healthFacilitiesService.findOne(healthFacilitiesDTO.getParent());
            if(parentOpt.isPresent()) {
                healthFacilitiesDTO.setParentCode(parentOpt.get().getCode());
            }
        }
        // Neu thay doi code thi cung phai cap nhat lai parentCode cua thang con
        HealthFacilitiesDTO result = healthFacilitiesService.save(healthFacilitiesDTO);
        if(!result.getCode().equalsIgnoreCase(oldHealthFacilitiesOpt.get().getCode())) {
            List<HealthFacilitiesDTO> chidren = healthFacilitiesService.findAllByParentAndStatusGreaterThan(result.getId(), Constants.ENTITY_STATUS.DELETED);
            if(chidren.size() > 0) {
                for(HealthFacilitiesDTO h : chidren) {
                    h.setParentCode(result.getCode());
                    healthFacilitiesService.save(h);
                }
            }
        }
        activityLogService.update(Constants.CONTENT_TYPE.HEALTH_FACILITY, oldHealthFacilities, healthFacilitiesMapper.toEntity(result));
        return ResponseEntity.created(new URI("/api/health-facilities/" + healthFacilitiesDTO.getId())).body(healthFacilitiesDTO);
    }

    @GetMapping("/health-facilities/exist/{code}")
    public ResponseEntity<Boolean> checkExistCode(@PathVariable String code) {
        log.debug("Check exist code");
        Boolean check = healthFacilitiesService.checkExistCode(code);
        return ResponseEntity.ok().body(check);
    }

    @GetMapping("/health-facilities")
    public ResponseEntity<List<HealthFacilitiesDTO>> getAllHealthFacilities(@RequestParam(name = "appointmentOption", required = false) Integer appointmentOption) {
        log.debug("REST request to get all HealthFacilities have parentCode is SYT");
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findAllHealthFacilities(appointmentOption);
        return ResponseEntity.ok().body(healthFacilitiesDTOList);
    }

    @GetMapping("/health-facilities/user")
    public ResponseEntity<List<HealthFacilitiesDTO>> getAllHealthFacilitiesByUser(@RequestParam(name = "appointmentOption", required = false) Integer appointmentOption) {
        log.debug("REST request to get all HealthFacilities have parentCode is SYT");

        User user = userService.getCurrentUser();
        if(user == null){
            return ResponseEntity.noContent().build();
        }
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findAllHealthFacilitiesByUser(appointmentOption, user);
        return ResponseEntity.ok().body(healthFacilitiesDTOList);
    }

    @GetMapping("/health-facilities/parent-health-facility/{id}")
    public ResponseEntity<List<HealthFacilitiesDTO>> getAll(@PathVariable(name = "id") Long parentId) {
        log.debug("REST request to get all active Health facilities with parent id: {}", parentId);
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesService.findByParentId(parentId);
        return ResponseEntity.ok().body(healthFacilitiesDTOList);
    }

    @GetMapping("/health-facilities/exist-except-this/{code}")
    public ResponseEntity<Boolean> checkExistCodeExistExceptThis(@PathVariable String code, @RequestParam(name = "id") Long id) {
        log.debug("Check exist code");
        Optional<HealthFacilitiesDTO> healthFacilitiesOpt = healthFacilitiesService.findOne(id);
        if (healthFacilitiesOpt.isPresent()) {
            HealthFacilitiesDTO healthFacilitiesDTO = healthFacilitiesOpt.get();
            if (healthFacilitiesDTO.getCode().equalsIgnoreCase(code)) return ResponseEntity.ok().body(false);
        }
        Boolean check = healthFacilitiesService.checkExistCode(code);
        return ResponseEntity.ok().body(check);
    }

    @GetMapping("/health-facilities/{id}/history")
    public ResponseEntity<List<HealthFacilitiesHistoryDTO>> getHealthFacilitiesHistory(@PathVariable Long id) {
        List<HealthFacilitiesHistoryDTO> list = healthFacilitiesService.getHealthFacilitiesHistoryById(id);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/health-facilities/parent-and-this/{id}")
    public ResponseEntity<List<HealthFacilitiesDTO>> getHealthFacilitiesParentAndThis(@PathVariable(name = "id") Long id) {
        List<HealthFacilitiesDTO> list = healthFacilitiesService.getHealthFacilitiesParentAndThis(id);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/public/health-facilities/download/template-excel")
    public void downloadTemplateExcelHealthFacilities(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=" + "health_facilities.xlsx");
        IOUtils.copy(storageService.downloadExcelTemplateFromResource("health_facilities.xlsx"), response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/health-facilities/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadHealthFacilities(@RequestParam("file") MultipartFile file) {
        log.debug("REST request upload health facilities");
        List<HealthFacilitiesDTO> healthFacilitiesDTOS;
        List<ErrorExcel> details = new ArrayList<>();
        ResultExcel resultExcel = new ResultExcel();
        try {
            healthFacilitiesDTOS = healthFacilitiesService.excelToHealthFacilities(file.getInputStream(), details);
            if(details.isEmpty()) {
                if(!healthFacilitiesDTOS.isEmpty()) {
                    for(HealthFacilitiesDTO dto : healthFacilitiesDTOS) {
                        if (dto.getParentCode() != null) {
                            HealthFacilitiesDTO parent = healthFacilitiesService.findByCodeAndStatusGreaterThan(dto.getParentCode(), Constants.ENTITY_STATUS.DELETED);
                            if (parent != null) {
                                dto.setParent(parent.getId());
                            }
                        }
                        // Set lat, lng
                        String urlGeocode = this.googleUrlGeocode + "?"
                                + "address=" + dto.getAddress()
                                + "&key=" + this.googleApiKey;
                        GeocodeResult googleResult = restTemplateHelper.execute(urlGeocode, HttpMethod.GET, null, GeocodeResult.class);
                        if (Objects.nonNull(googleResult)) {
                            List<GeocodeObject> geocodeObjects = googleResult.getResults();
                            if (!geocodeObjects.isEmpty()) {
                                GeocodeGeometry geocodeGeometry = geocodeObjects.get(0).getGeometry();
                                if (Objects.nonNull(geocodeGeometry)) {
                                    GeocodeLocation geocodeLocation = geocodeGeometry.getGeocodeLocation();
                                    Double lat = Double.valueOf(geocodeLocation.getLatitude());
                                    Double lng = Double.valueOf(geocodeLocation.getLongitude());
                                    dto.setLatitude(lat);
                                    dto.setLongitude(lng);
                                }

                                // Check dia chi - thanh pho
                                String[] addressElement = geocodeObjects.get(0).getFormattedAddress().split(",");
                                if (addressElement.length >= 2) {
                                    String cityName = addressElement[addressElement.length - 2];
                                    List<AreaDTO> cities = areaService.findByNameAndParentCodeAndStatus(cityName, Constants.COUNTRY_VN.CODE, Constants.ENTITY_STATUS.ACTIVE);

                                    if (!cities.isEmpty()) {
                                        dto.setCityCode(cities.get(0).getAreaCode());
                                    }
                                }
                                if (StringUtils.isEmpty(dto.getCityCode())) {
                                    dto.setCityCode(Constants.SYT_DEFAULT.cityCode); // Set mac dinh cityCode cua Tinh Yen Bai
                                }
                            }
                        }
                        healthFacilitiesService.save(dto);
                    }
                }
                resultExcel.setSucess(true);
            } else{
                resultExcel.setSucess(false);
            }
            resultExcel.setErrorExcels(details);
            return ResponseEntity.ok().body(resultExcel);

        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        return ResponseEntity.ok().body(null);
    }
}
