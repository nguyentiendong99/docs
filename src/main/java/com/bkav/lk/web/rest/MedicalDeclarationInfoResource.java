package com.bkav.lk.web.rest;

import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DeclarationQuestionDTO;
import com.bkav.lk.dto.DetailMedicalDeclarationInfoDTO;
import com.bkav.lk.dto.MedicalDeclarationInfoDTO;
import com.bkav.lk.dto.MedicalDeclarationInfoVM;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.DetailMedicalDeclarationInfoService;
import com.bkav.lk.service.MedicalDeclarationInfoService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.MedicalDeclarationInfoMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MedicalDeclarationInfoResource {

    private final Logger log = LoggerFactory.getLogger(MedicalDeclarationInfoResource.class);

    private static final String ENTITY_NAME = "Medical Declaration Info";

    private final MedicalDeclarationInfoService medicalDeclarationInfoService;

    private final DetailMedicalDeclarationInfoService detailMedicalDeclarationInfoService;

    private final UserService userService;

    private final MedicalDeclarationInfoMapper medicalDeclarationInfoMapper;

    private final ActivityLogService activityLogService;

    public MedicalDeclarationInfoResource(MedicalDeclarationInfoService medicalDeclarationInfoService,
                                          DetailMedicalDeclarationInfoService detailMedicalDeclarationInfoService, UserService userService, MedicalDeclarationInfoMapper medicalDeclarationInfoMapper,
                                          ActivityLogService activityLogService) {
        this.medicalDeclarationInfoService = medicalDeclarationInfoService;
        this.detailMedicalDeclarationInfoService = detailMedicalDeclarationInfoService;
        this.userService = userService;
        this.medicalDeclarationInfoMapper = medicalDeclarationInfoMapper;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/medical-declaration-infos")
    public ResponseEntity<List<MedicalDeclarationInfoDTO>> search(@RequestParam MultiValueMap<String, String> queryParams,
                                                  Pageable pageable) {
        if (!StrUtil.isBlank(queryParams.get("ageFrom").get(0)) && !StrUtil.isBlank(queryParams.get("ageTo").get(0))) {
            int ageFrom = Integer.parseInt(queryParams.get("ageFrom").get(0));
            int ageTo = Integer.parseInt(queryParams.get("ageTo").get(0));
            if (ageTo > 0 && ageFrom > ageTo) {
                throw new BadRequestAlertException("AgeFrom is no more than AgeTo",
                        ENTITY_NAME, "validate.minAgeFrom");
            }
        }
        Page<MedicalDeclarationInfoDTO> page = medicalDeclarationInfoService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/medical-declaration-infos/user")
    public ResponseEntity<List<MedicalDeclarationInfoDTO>> searchByUser(Pageable pageable){
        Optional<String> optionalLogin = SecurityUtils.getCurrentUserLogin();
        if (!optionalLogin.isPresent()) {
            return ResponseEntity.noContent().build();
        }
        Optional<User> user = userService.findByLogin(optionalLogin.get());
        if (!user.isPresent()) {
            return ResponseEntity.noContent().build();
        }
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("userId", String.valueOf(user.get().getId()));
        queryParams.add("notManager", "true"); // Nguoi dan
        Page<MedicalDeclarationInfoDTO> page = medicalDeclarationInfoService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/medical-declaration-infos")
    public ResponseEntity<MedicalDeclarationInfoDTO> create(@RequestBody MedicalDeclarationInfoDTO medicalDeclarationInfoDTO){
        if (medicalDeclarationInfoDTO.getId() != null) {
            throw new BadRequestAlertException("A new Medical Declaration Info cannot already have an ID",
                    ENTITY_NAME, "idexists");
        }

        medicalDeclarationInfoDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        MedicalDeclarationInfo medicalDeclarationInfo = medicalDeclarationInfoService.save(medicalDeclarationInfoDTO);
        if(medicalDeclarationInfo == null){
            throw new BadRequestAlertException("Error", ENTITY_NAME, "error");
        }
        Optional<MedicalDeclarationInfoDTO> optionalMedicalDeclarationInfoDTO = medicalDeclarationInfoService.findOne(medicalDeclarationInfo.getId());
        MedicalDeclarationInfoDTO returnDTO = optionalMedicalDeclarationInfoDTO.get();

        List<DetailMedicalDeclarationInfoDTO> details = medicalDeclarationInfoDTO.getDetails();
        for(DetailMedicalDeclarationInfoDTO detail: details){
            detail.setMedicalDeclarationInfoId(medicalDeclarationInfo.getId());
            detailMedicalDeclarationInfoService.save(detail);
        }

        activityLogService.create(Constants.CONTENT_TYPE.MEDICAL_DECLARATION_INFO, medicalDeclarationInfo);

        details = medicalDeclarationInfoService.findMedicalDeclarationInfoDetail(medicalDeclarationInfo.getId());
        returnDTO.setDetails(details);

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert
                        ("", true, ENTITY_NAME, returnDTO.getId().toString()))
                .body(returnDTO);
    }

    /**
     * findOne
     * Trả về một đối tượng tờ khai y tế
     *
     * @param medicalDeclarationDeclarationId id
     * @return MedicalDeclarationInfoDTO
     */
    @GetMapping("/medical-declaration-infos/{medicalDeclarationDeclarationId}")
    public ResponseEntity<MedicalDeclarationInfoDTO> findOne(@PathVariable Long medicalDeclarationDeclarationId) {
        log.debug("REST request to get Medical Declaration Info : {}", medicalDeclarationDeclarationId);
        Optional<MedicalDeclarationInfoDTO> optional = medicalDeclarationInfoService.findOne(medicalDeclarationDeclarationId);
        if(!optional.isPresent()){
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "invalid_id");
        }
        MedicalDeclarationInfoDTO medicalDeclarationInfoDTO = optional.get();
        List<DetailMedicalDeclarationInfoDTO> list = medicalDeclarationInfoService.findMedicalDeclarationInfoDetail(medicalDeclarationDeclarationId);
        medicalDeclarationInfoDTO.setDetails(list);
        return ResponseEntity.ok().body(medicalDeclarationInfoDTO);
    }

    @DeleteMapping("/medical-declaration-infos/{medicalDeclarationDeclarationId}")
    public ResponseEntity<Void> delete(@PathVariable Long medicalDeclarationDeclarationId) {
        log.debug("REST request to delete Medical Declaration Info : {}", medicalDeclarationDeclarationId);
        medicalDeclarationInfoService.delete(medicalDeclarationDeclarationId);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, true, ENTITY_NAME, medicalDeclarationDeclarationId.toString()))
                .build();
    }

    @GetMapping("/public/medical-declaration-infos/questions")
    public ResponseEntity<List<DeclarationQuestionDTO>> getListQuestion(){
        List<DeclarationQuestionDTO> list = medicalDeclarationInfoService.getListQuestion();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/public/medical-declaration-infos/download")
    public ResponseEntity<String> getInfoToExcel(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable, HttpServletResponse response) throws IOException {



        Page<MedicalDeclarationInfoVM> page = medicalDeclarationInfoService.searchToExcel(queryParams, pageable);

        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/medical_declaration_info.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_medical_declaration_info_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", page.getContent());
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @GetMapping("/public/medical-declaration-infos/export")
    public void getFileExportCategory(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_medical_declaration_info_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_medical_declaration_info_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }
}
