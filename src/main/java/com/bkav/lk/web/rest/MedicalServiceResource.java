package com.bkav.lk.web.rest;

import com.bkav.lk.dto.ClsDTO;
import com.bkav.lk.dto.MedicalServiceCustomConfigDTO;
import com.bkav.lk.dto.MedicalServiceDTO;
import com.bkav.lk.service.MedicalServiceService;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MedicalServiceResource {

    private final Logger log = LoggerFactory.getLogger(MedicalServiceResource.class);

    private static final String ENTITY_NAME = "Medical Service";

    private final MedicalServiceService service;
    private StorageService storageService;

    public MedicalServiceResource(MedicalServiceService service, StorageService storageService) {
        this.service = service;
        this.storageService = storageService;
    }

    /**
     * create
     *
     * @param medicalServiceDTO medicalServiceDTO
     * @return MedicalServiceDTO
     */
    @PostMapping("/medical-services")
    public ResponseEntity<MedicalServiceDTO> create(@Valid @RequestBody MedicalServiceDTO medicalServiceDTO) throws URISyntaxException {
        log.debug("REST request to save Medical Service : {}", medicalServiceDTO);
        if (medicalServiceDTO.getId() != null) {
            throw new BadRequestAlertException("A new Medical Service cannot already have an ID", ENTITY_NAME, "idexists");
        }
        medicalServiceDTO = service.save(medicalServiceDTO);
        return ResponseEntity.created(new URI("/api/medical-services/" + medicalServiceDTO.getId())).body(medicalServiceDTO);
    }

    /**
     * update
     *
     * @param medicalServiceDTO medicalServiceDTO
     * @return MedicalServiceDTO
     */
    @PutMapping("/medical-services")
    public ResponseEntity<MedicalServiceDTO> update(@RequestBody MedicalServiceDTO medicalServiceDTO) throws URISyntaxException {
        log.debug("REST request to save Medical Service : {}", medicalServiceDTO);
        if (medicalServiceDTO.getId() == null) {
            throw new BadRequestAlertException("A Medical Service cannot save has not ID", ENTITY_NAME, "idnull");
        }
        medicalServiceDTO = service.save(medicalServiceDTO);
        return ResponseEntity.created(new URI("/api/medical-services/" + medicalServiceDTO.getId())).body(medicalServiceDTO);
    }

    /**
     * findOne
     * Trả về một đối tượng dịch vụ khám/ y tế
     *
     * @param id id
     * @return MedicalServiceDTO
     */
    @GetMapping("/medical-services/{id}")
    public ResponseEntity<MedicalServiceDTO> findOne(@PathVariable Long id) {
        log.debug("REST request to get Medical Service : {}", id);
        Optional<MedicalServiceDTO> dto = service.findOne(id);
        if (!dto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(dto);
    }

    /**
     * search
     * Tìm kiếm danh mục dịch vụ khám/ y tế
     *
     * @param queryParams name, code
     * @param pageable    pageNumber, pageSize
     * @return List<MedicalServiceDTO> - Danh sách dịch vụ khám/ y tế
     */
    @GetMapping("/medical-services/search")
    public ResponseEntity<List<MedicalServiceDTO>> search(@RequestHeader("healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams,
                                                          Pageable pageable) {
        log.debug("REST request to search for a page of Medical Services for query {}", queryParams);
        if (healthFacilityId != null) {
            queryParams.set("healthFacilityId", healthFacilityId.toString());
        }
        Page<MedicalServiceDTO> page = service.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/medical-services")
    public ResponseEntity<List<MedicalServiceDTO>> findAllForMobile(@RequestHeader(value = "healthFacilityId", required = false) Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams,
                                                                    Pageable pageable) {
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            queryParams.set("healthFacilityId", queryParams.get("healthFacilityId").get(0));
        } else {
            if (healthFacilityId != null) {
                queryParams.set("healthFacilityId", healthFacilityId.toString());
            }
        }
        queryParams.set("pageIsNull", null);
        queryParams.set("status", Constants.ENTITY_STATUS.ACTIVE.toString());
        Page<MedicalServiceDTO> page = service.search(queryParams, pageable);
        List<MedicalServiceDTO> medicalServiceDTOS = page.getContent();
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(medicalServiceDTOS);
    }

    /**
     * delete
     *
     * @param id Xóa một dịch vụ khám/ y tế => status = 0
     * @return void
     */
    @DeleteMapping("/medical-services/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete Medical Service : {}", id);
        service.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert("", true, ENTITY_NAME, id.toString()))
                .build();
    }

    @PutMapping("/medical-services/deletes")
    public ResponseEntity<Boolean> deletes(@RequestBody List<Long> arrayIds) {
        log.debug("REST request to deletes list ids medical service : {}", arrayIds);
        arrayIds.forEach(item -> {
            Optional<MedicalServiceDTO> optional = service.findOne(item);
            if (optional.isPresent()) {
                if (optional.get().getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) || optional.get().getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
                    service.delete(optional.get().getId());
                } else {
                    throw new BadRequestAlertException("A record have status is DELETED, can not delete", ENTITY_NAME, "state_is_changed");
                }
            }
        });
        return ResponseEntity.ok().body(true);
    }

    /**
     * check exist
     *
     * @param code code only
     * @return true is exist/ otherwise
     */
    @GetMapping("/medical-services/exist/{code}")
    public ResponseEntity<Boolean> existMedicalServiceByCode(@PathVariable String code) {
        log.debug("REST request to get exist Medical Service : {}", code);
        return ResponseEntity.ok().body(service.existCode(code));
    }


    @GetMapping("/public/medical-services/download")
    public ResponseEntity<String> exportExcel(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable, HttpServletResponse response) throws IOException {

        queryParams.set("pageIsNull", null);
        List<MedicalServiceDTO> list = service.search(queryParams, pageable).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/medical_service-export.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_medical_service_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @PostMapping("/medical-services/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadMedicalService(@RequestHeader("healthFacilityId") Long healthFacilityId,
                                                                            @RequestParam("file") MultipartFile file) {
        log.debug("REST request upload medical Service");
        List<MedicalServiceDTO> medicalServiceDTOS = new ArrayList<>();
        List<ErrorExcel> details = new ArrayList<>();
        ResultExcel resultExcel = new ResultExcel();
        try {
            medicalServiceDTOS = service.excelToOBject(file.getInputStream(), details);
            if(details.isEmpty()) {
                if (!medicalServiceDTOS.isEmpty()) {
                    for (int i = 0; i < medicalServiceDTOS.size(); i++) {
                        medicalServiceDTOS.get(i).setHealthFacilityId(healthFacilityId);
                        service.save(medicalServiceDTOS.get(i));
                    }
                }
                resultExcel.setSucess(true);
            } else {
                resultExcel.setSucess(false);
            }
            resultExcel.setErrorExcels(details);
        } catch (IOException exception) {

        }
        return ResponseEntity.ok().body(resultExcel);
    }

    @GetMapping("/public/medical-services/download/template-excel")
    public void downloadTemplateExcelMedicalService(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=" + "medical_service.xlsx");
        IOUtils.copy(storageService.downloadExcelTemplateFromResource("medical_service.xlsx"), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/medical-services/custom-config/{id}")
    public ResponseEntity<List<MedicalServiceCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(service.findAllCustomConfigByMedicalServiceId(id));
    }

    @GetMapping("/public/medical-services/export")
    public void exportExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_medical_service_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_medical_service_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }
}
