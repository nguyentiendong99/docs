package com.bkav.lk.web.rest;

import com.bkav.lk.dto.DoctorAppointmentDTO;
import com.bkav.lk.dto.PatientRecordDTO;
import com.bkav.lk.dto.SubclinicalDTO;
import com.bkav.lk.service.NotificationService;
import com.bkav.lk.service.PatientRecordService;
import com.bkav.lk.service.SubclinicalService;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
public class SubclinicalResultResource {

    private static final String ENTITY_NAME = "subclinical result";

    private static final Logger log = LoggerFactory.getLogger(SubclinicalResultResource.class);

    private final SubclinicalService subclinicalService;

    private final NotificationService notificationService;

    private final PatientRecordService patientRecordService;

    private final ObjectMapper objectMapper;

    private StorageService storageService;

    public SubclinicalResultResource(SubclinicalService subclinicalService, NotificationService notificationService, PatientRecordService patientRecordService, ObjectMapper objectMapper, StorageService storageService) {
        this.subclinicalService = subclinicalService;
        this.notificationService = notificationService;
        this.patientRecordService = patientRecordService;

        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    @GetMapping("/subclinical-results")
    public ResponseEntity<List<SubclinicalDTO>> search(@RequestHeader(name = "healthFacilityId") Long healthFacilityId,
                                                       @RequestParam MultiValueMap<String, String> queryParams,
                                                       Pageable pageable) {
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        Page<SubclinicalDTO> page = subclinicalService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PutMapping("/subclinical-results")
    public ResponseEntity<List<SubclinicalDTO>> update(@RequestBody List<SubclinicalDTO> listSubclinicalDTO) {
        if (listSubclinicalDTO.size() == 0) {
            throw new BadRequestAlertException("Empty list", ENTITY_NAME, "listempty");
        }
        List<SubclinicalDTO> result = subclinicalService.update(listSubclinicalDTO);
        for (SubclinicalDTO item : listSubclinicalDTO) {
            FirebaseData firebaseData = new FirebaseData();
            firebaseData.setType(String.valueOf(Constants.NotificationConstants.GET_RESULT_CLS.id));
            firebaseData.setObjectId(item.getId().toString());
            try {
                firebaseData.setObject(objectMapper.writeValueAsString(item).replaceAll("\\n|\\r", ""));
            } catch (JsonProcessingException e) {
                log.error("Error: ", e);
            }
            String template = Constants.NotificationConstants.GET_RESULT_CLS.template;;
            List<String> paramsBody = new ArrayList<>();
            paramsBody.add(item.getName());
            if(item.getRoom() == null || item.getRoom().isEmpty()){
                paramsBody.add("thực hiện cận lâm sàng");
            }else{
                paramsBody.add(item.getRoom());
            }
            Optional<PatientRecordDTO> optional = patientRecordService.findOne(item.getPatientRecordId());
            if(optional.isPresent()){
                notificationService.pushNotification(template, firebaseData, null, paramsBody, optional.get().getUserId());
            }
        }
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/public/subclinical-results/download")
    public ResponseEntity<String> exportExcel(@RequestParam MultiValueMap<String, String> queryParams,
                                              Pageable pageable, HttpServletResponse response) throws IOException {
        queryParams.set("pageIsNull", null);
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            queryParams.set("healthFacilityId", queryParams.get("healthFacilityId").get(0));
        }
        List<SubclinicalDTO> list = subclinicalService.search(queryParams, pageable).getContent();

        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/subclinical-result-export.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_subclinical_result_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);

    }


    @GetMapping("/public/subclinical-results/export")
    public void exportExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_subclinical_result_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_subclinical_result_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/public/subclinical-results/download/selected")
    public ResponseEntity<String> exportExcelSelected(@RequestBody List<SubclinicalDTO> listExcel) throws IOException{
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/subclinical-result-export.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_subclinical_result_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", listExcel);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }
}
