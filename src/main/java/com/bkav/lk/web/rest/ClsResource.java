package com.bkav.lk.web.rest;

import com.bkav.lk.dto.ClinicDTO;
import com.bkav.lk.dto.ClsCustomConfigDTO;
import com.bkav.lk.dto.ClsDTO;
import com.bkav.lk.service.ClsService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ClsResource {

    private static final Logger log = LoggerFactory.getLogger(ClsResource.class);


    private static final String ENTITY_NAME = "Cls";

    private final ClsService clsService;

    private StorageService storageService;

    @Value("${spring.application.name}")
    private String applicationName;

    public ClsResource(ClsService clsService, StorageService storageService) {
        this.clsService = clsService;
        this.storageService = storageService;
    }

    @PostMapping("/cls")
    public ResponseEntity<ClsDTO> create(@RequestBody ClsDTO clsDTO) throws URISyntaxException {
        ClsDTO cls = clsService.save(clsDTO);
        return ResponseEntity.created(new URI("/api/cls/" + cls.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        cls.getId().toString()))
                .body(cls);
    }

    @GetMapping("/cls/search")
    public ResponseEntity<List<ClsDTO>> search(@RequestHeader("healthFacilityId") Long healthFacilityId ,@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        if (healthFacilityId != null) {
            queryParams.set("healthFacilityId", healthFacilityId.toString());
        }
        Page<ClsDTO> page = clsService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PutMapping("/cls")
    public ResponseEntity<ClsDTO> update(@RequestBody ClsDTO clsDTO) throws URISyntaxException {
        ClsDTO clsByCode = clsService.findByIdAndStatus(clsDTO.getId());
        clsDTO = clsService.save(clsDTO);
        return ResponseEntity.created(new URI("/api/cls/" + clsDTO.getId())).body(clsDTO);
    }

    @DeleteMapping("/cls/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        clsService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }

    @PutMapping("/cls/deletes")
    public ResponseEntity<Boolean> deletes(@RequestBody List<Long> arrayIds) {
        arrayIds.forEach(item -> {
            Optional<ClsDTO> optional = clsService.findOne(item);
            if (optional.isPresent()) {
                if (optional.get().getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) || optional.get().getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
                    clsService.delete(optional.get().getId());
                } else {
                    throw new BadRequestAlertException("A record have status is DELETED, can not delete", ENTITY_NAME, "state_is_changed");
                }
            }
        });
        return ResponseEntity.ok().body(true);
    }

    @GetMapping("/public/cls/download/template-excel")
    public void downloadTemplateExcelMedicalService(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=" + "cls.xlsx");
        IOUtils.copy(storageService.downloadExcelTemplateFromResource("cls.xlsx"), response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/cls/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadMedicalService(@RequestHeader("healthFacilityId") Long healthFacilityId ,@RequestParam("file") MultipartFile file) {
        List<ClsDTO> clsDTOS;
        ResultExcel resultExcel = new ResultExcel();
        List<ErrorExcel> details = new ArrayList<>();
        try {
            clsDTOS = clsService.excelToObject(file.getInputStream(), details);
            if (details.isEmpty()) {
                resultExcel.setSucess(true);
                if (!clsDTOS.isEmpty()) {
                    for (ClsDTO clsDTO : clsDTOS) {
                        if (clsDTO.getClsName() != null && clsDTO.getClsPrice() != null && clsDTO.getStatus() != null) {
                            clsDTO.setHealthFacilityId(healthFacilityId);
                            clsService.save(clsDTO);
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

    @GetMapping("/public/cls/export")
    public void exportExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_cls_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_cls_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/cls/custom-config/{id}")
    public ResponseEntity<List<ClsCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id) {
        log.info("REST request to get list custom config by cls id input: {}", id);
        return ResponseEntity.ok().body(clsService.findAllCustomConfigByClsId(id));
    }

    @GetMapping("/public/cls/download")
    public ResponseEntity<String> exportExcel(@RequestParam MultiValueMap<String, String> queryParams,
                                              Pageable pageable) throws IOException {
        queryParams.set("pageIsNull", null);
        List<ClsDTO> list = clsService.search(queryParams, pageable).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/cls-export.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_cls_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }
}
