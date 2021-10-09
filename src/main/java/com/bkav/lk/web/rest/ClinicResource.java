package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.dto.ClinicCustomConfigDTO;
import com.bkav.lk.dto.ClinicDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.ClinicService;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.service.mapper.ClinicMapper;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ClinicResource {

    private static final String ENTITY_NAME = "clinic";

    private final ClinicService clinicService;

    private final ClinicMapper clinicMapper;

    private StorageService storageService;

    private final ActivityLogService activityLogService;

    private final DoctorService doctorService;

    public ClinicResource(ClinicService clinicService, ClinicMapper clinicMapper, ActivityLogService activityLogService, StorageService storageService, DoctorService doctorService) {
        this.clinicService = clinicService;
        this.clinicMapper = clinicMapper;
        this.activityLogService = activityLogService;
        this.storageService = storageService;
        this.doctorService = doctorService;
    }

    @RequestMapping(value = "/clinics/{id}", method = RequestMethod.GET)
    public ResponseEntity<ClinicDTO> getClinic(@PathVariable Long id) {
        Optional<Clinic> optional = clinicService.findOne(id);
        if (!optional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(clinicMapper.toDto(optional.get()));
    }

    @GetMapping("/clinics/search")
    public ResponseEntity<List<ClinicDTO>> search(@RequestParam MultiValueMap<String, String> queryParams,
                                                  @RequestHeader("healthFacilityId") Long healthFacilityId,
                                                  Pageable pageable) {
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        Page<ClinicDTO> page = clinicService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/clinics")
    public ResponseEntity<ClinicDTO> createClinic(@RequestBody ClinicDTO newClinicDTO) {
        if (newClinicDTO.getId() != null) {
            throw new BadRequestAlertException("A new clinic cannot already has ID", ENTITY_NAME, "idexist");
        }
        String newClinicCode = clinicService.generateClinicCode(newClinicDTO.getCode(), newClinicDTO.getName(), false);

        if (newClinicCode == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "clinic", "duplicateCode", "Duplicate code for clinic"))
                    .body(null);
        }
        newClinicDTO.setCode(newClinicCode);
        Clinic result = clinicService.save(newClinicDTO);
        if (result == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "clinic", "", "Exception in creating new clinic"))
                    .body(null);
        }
        activityLogService.create(Constants.CONTENT_TYPE.CLINIC, result);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert("", true, "clinic", result.getId().toString()))
                .body(clinicMapper.toDto(result));
    }

    @PutMapping("/clinics/{id}")
    public ResponseEntity<ClinicDTO> updateClinic(@PathVariable Long id, @RequestBody ClinicDTO updatedClinicDTO) {
        // Không giữ nguyên mã code cũ khi update mà mã code để trống nữa, thay vào đó sinh mã theo quy tắc
//        if (updatedClinicDTO.getCode() == null || updatedClinicDTO.getCode().equals("")) {
//            Optional<Clinic> clinicById = clinicService.findOne(id);
//            updatedClinicDTO.setCode(clinicById.orElseGet(Clinic::new).getCode());
//        }

        Optional<Clinic> clinicDTO = clinicService.findOne(updatedClinicDTO.getId());
        String updatedClinicCode  = clinicService.generateClinicCode(updatedClinicDTO.getCode(), updatedClinicDTO.getName(), !clinicDTO.get().getName().equals(updatedClinicDTO.getName()));
        if(updatedClinicCode == null){
            updatedClinicCode = updatedClinicDTO.getCode();
        }

        if (updatedClinicDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE) && !clinicService.isDeactivable(id)) {
            throw new BadRequestAlertException("Đã có lịch đặt khám sử dụng phòng này", ENTITY_NAME, "clinic.cant_deactivate");
        }

        // Check Bac si(Hoat dong) lam viec tai phong kham
        if (updatedClinicDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            Integer[] status = {Constants.ENTITY_STATUS.ACTIVE};
            List<DoctorDTO> doctorDTOList = doctorService.findByClincAndStatus(id, status);
            if (!doctorDTOList.isEmpty())
                throw new BadRequestAlertException("Đang có bác sĩ hoạt động tại phòng khám này", ENTITY_NAME, "clinic.doctor_is_active_in_clinic");
        }

        ClinicDTO clinicByCode = clinicService.findByCode(updatedClinicCode);
        if (clinicByCode != null && updatedClinicCode.equals(clinicByCode.getCode()) && !clinicByCode.getId().equals(id)) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "clinic", "duplicateCode", "Duplicate Code"))
                    .body(null);
        }
        Clinic oldClinic = new Clinic();
        Optional<Clinic> optional = clinicService.findOne(id);
        if (optional.isPresent()) {
            oldClinic = optional.get();
        }

        updatedClinicDTO.setId(id);
        updatedClinicDTO.setCode(updatedClinicCode);
        ClinicDTO clinicByStatus = clinicService.findByIdAndStatus(updatedClinicDTO.getId());
        if (!CollectionUtils.isEmpty(updatedClinicDTO.getClinicCustomConfigDTOS())) {
            clinicByStatus.setClinicCustomConfigDTOS(updatedClinicDTO.getClinicCustomConfigDTOS());
        }
        Clinic result = clinicService.save(updatedClinicDTO);

        activityLogService.update(Constants.CONTENT_TYPE.CLINIC, oldClinic, result);
        if (result == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "clinic", "", "Exception in updating clinic"))
                    .body(null);
        }
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert("", true, "clinic", result.getId().toString()))
                .body(clinicMapper.toDto(result));
    }

    @DeleteMapping("/clinics/{id}")
    public ResponseEntity<Boolean> deleteClinic(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "clinic", "idNull", "Null Id")).body(false);
        }

        // Check Lich dat kham dang ton tai o Phong kham
        if (!clinicService.isDeactivable(id)) {
            throw new BadRequestAlertException("Đã có lịch đặt khám sử dụng phòng này", ENTITY_NAME, "clinic.cant_delete");
        }

        // Check Bac si (Hoat dong, Khong hoat dong) lam viec tai Phong kham
        Integer[] status = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        List<DoctorDTO> doctorDTOList = doctorService.findByClincAndStatus(id, status);
        if (!doctorDTOList.isEmpty())
            throw new BadRequestAlertException("Đang có bác sĩ tại phòng khám này, không thể xóa", ENTITY_NAME, "clinic.doctor_in_clinic");

        Optional<Clinic> optional = clinicService.findOne(id);
        if (optional.isPresent()) {
            activityLogService.delete(Constants.CONTENT_TYPE.CLINIC, optional.get());
        }
        clinicService.delete(id);
        return ResponseEntity.ok().body(true);
    }

    @PutMapping("/clinics/delete-many")
    public ResponseEntity<Boolean> deleteMany(@RequestBody List<Long> listId) {
        if (listId == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "clinic", "idNull", "Null Ids")).body(false);
        }
        for (Long id : listId) {
            clinicService.delete(id);
        }
        return ResponseEntity.ok().body(true);
    }

    @GetMapping("/clinics/exist/{code}")
    public ResponseEntity<Boolean> existClinicByCode(@PathVariable String code) {
        ClinicDTO clinicDTO = clinicService.findByCode(code);
        return ResponseEntity.ok().body(clinicDTO != null);
    }

    @GetMapping("/public/clinics/download")
    public ResponseEntity<String> exportExcel(@RequestParam MultiValueMap<String, String> queryParams, @RequestHeader("healthFacilityId") Long healthFacilityId,
                                                           Pageable pageable) throws IOException {
        queryParams.set("pageIsNull", null);
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        List<ClinicDTO>  list = clinicService.search(queryParams, pageable).getContent();

        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/clinic-export.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_clinic_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @GetMapping("/clinics/health-facility/{id}")
    public ResponseEntity<List<ClinicDTO>> findByHealthFacilityId(@PathVariable("id") Long healthFacilityId) {
        List<ClinicDTO> results = clinicService.findByHealthFacilityId(healthFacilityId);
        return ResponseEntity.ok().body(results);
    }

    @PostMapping("/clinics/bulk-upload/{id}")
    public ResponseEntity<ResultExcel> bulkUpload(@RequestParam("file") MultipartFile file, @PathVariable("id") Long healthFacilityId) {
        List<ClinicDTO> clinicDTOS;
        ResultExcel resultExcel = new ResultExcel();
        List<ErrorExcel> details = new ArrayList<>();
        try {
            clinicDTOS = clinicService.excelToClinic(file.getInputStream(), details);
            if (details.isEmpty()) {
                resultExcel.setSucess(true);
                if (!clinicDTOS.isEmpty()) {
                    for (ClinicDTO clinicDTO : clinicDTOS) {
                        if (clinicDTO.getName() != null && clinicDTO.getStatus() != null) {
                            String newClinicCode = clinicService.generateClinicCode(clinicDTO.getCode(), clinicDTO.getName(), false);
                            clinicDTO.setCode(newClinicCode);
                            clinicDTO.setHealthFacilityId(healthFacilityId);
                            clinicService.save(clinicDTO);
                        }
                    }
                }
            } else {
                resultExcel.setSucess(false);
            }
            resultExcel.setErrorExcels(details);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return ResponseEntity.ok().body(resultExcel);
    }

    @GetMapping("/public/clinics/export")
    public void downloadTemplateExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_clinic_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_clinic_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/public/clinics/download/template-excel")
    public void downloadTemplateExcel(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=" + "clinic.xlsx");
        IOUtils.copy(storageService.downloadExcelTemplateFromResource("clinic.xlsx"), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/clinics/custom-config/{id}")
    public ResponseEntity<List<ClinicCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clinicService.findAllCustomConfigByClinicId(id));
    }

}
