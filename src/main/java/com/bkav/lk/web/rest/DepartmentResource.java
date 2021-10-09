package com.bkav.lk.web.rest;

import com.bkav.lk.dto.DepartmentDTO;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.DepartmentService;
import com.bkav.lk.service.impl.ActivityLogServiceImpl;
import com.bkav.lk.service.impl.DepartmentServiceImpl;
import com.bkav.lk.service.mapper.DepartmentMapper;
import com.bkav.lk.service.storage.FileSystemStorageService;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DepartmentResource {

    private static final Logger log = LoggerFactory.getLogger(DepartmentResource.class);

    private static final String ENTITY_NAME = "departments";
    private static final String DEPARTMENT_EXCEL_TEMPLATE_NAME = "departments.xlsx";

    private final DepartmentService departmentService;
    private final ActivityLogService activityLogService;
    private final StorageService storageService;
    private final DepartmentMapper departmentMapper;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    public DepartmentResource(
            DepartmentServiceImpl departmentService,
            ActivityLogServiceImpl activityLogService,
            FileSystemStorageService storageService,
            DepartmentMapper departmentMapper) {
        this.departmentService = departmentService;
        this.activityLogService = activityLogService;
        this.storageService = storageService;
        this.departmentMapper = departmentMapper;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> search(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("REST request to search for a page of Departments for query {}", queryParams);
        Page<DepartmentDTO> result = departmentService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), result);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @GetMapping("/departments/search-tree")
    public ResponseEntity<Map<String, Object>> searchTree(@RequestParam Map<String, String> queryParams) {
        log.debug("REST request to search for a page of Departments for query {}", queryParams);
        Integer[] status = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        List<DepartmentDTO> list = departmentService.findAll(status);
        list.forEach(departmentDTO -> {
            departmentDTO.setChildDepartments(new ArrayList<>());
        });
        Collections.sort(list, Comparator.comparing(DepartmentDTO::getStatus).thenComparing(DepartmentDTO::getLastModifiedDate, Comparator.reverseOrder()));
        Map<String, Object> departments = departmentService.handleTreeDepartment(list, queryParams);
        return ResponseEntity.ok().body(departments);
    }


    @GetMapping("/departments/findAll")
    public  ResponseEntity<List<DepartmentDTO>> findAll(@RequestParam(name = "status", required = false) Integer status) {
        List<DepartmentDTO> result = departmentService.findAll(status);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/departments")
    public ResponseEntity<DepartmentDTO> create(@RequestBody @Valid DepartmentDTO departmentDTO) {
        log.debug("REST request to create new department with data: {}", departmentDTO);
        if (departmentDTO.getId() != null) {
            throw new BadRequestAlertException("A new department cannot already has ID", ENTITY_NAME, "idexist");
        }
        DepartmentDTO result = departmentService.save(departmentDTO);
        activityLogService.create(Constants.CONTENT_TYPE.DEPARTMENT, departmentMapper.toEntity(result));
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @PutMapping("/departments")
    public ResponseEntity<DepartmentDTO> update(@RequestBody @Valid DepartmentDTO departmentDTO) {
        log.debug("REST request to update department with data: {}", departmentDTO);
        if (departmentDTO.getId() == null) {
            throw new BadRequestAlertException("A department cannot save has not ID", ENTITY_NAME, "idnull");
        }
        DepartmentDTO oldResult = departmentService.findById(departmentDTO.getId());
        DepartmentDTO result = departmentService.save(departmentDTO);
        activityLogService.update(Constants.CONTENT_TYPE.DEPARTMENT,
                departmentMapper.toEntity(oldResult), departmentMapper.toEntity(result));
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long departmentId) {
        log.debug("REST request to delete department with id = {}", departmentId);
        departmentService.delete(departmentId);
        DepartmentDTO result = departmentService.findById(departmentId);
        activityLogService.delete(Constants.CONTENT_TYPE.DEPARTMENT, departmentMapper.toEntity(result));
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME,
                        departmentId.toString())).build();
    }

    @GetMapping("/departments/existByCode/{code}")
    public ResponseEntity<Boolean> existByCode(@PathVariable("code") String departmentCode) {
        log.debug("REST request to check department exist by code = {}", departmentCode);
        Boolean result = departmentService.existByCode(departmentCode);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/departments/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadDepartment(@RequestParam("file") MultipartFile file) {
        log.info("REST request upload department by file: {}", file.getOriginalFilename());
        List<DepartmentDTO> departmentDTOs = null;

        ResultExcel resultExcel = new ResultExcel();
        List<ErrorExcel> details = new ArrayList<>();

        try {
            departmentDTOs = departmentService.addDepartmentsByExcelFile(file.getInputStream(), details);
            if (details.isEmpty()) {
                resultExcel.setSucess(true);
                if (departmentDTOs.size() > 0) {
                    for (DepartmentDTO departmentDTO : departmentDTOs) {
                        if (departmentDTO.getName() != null && departmentDTO.getStatus() != null) {
                            String newPotionCode = departmentService.generateDepartmentCode(departmentDTO.getCode(), departmentDTO.getName(), false);
                            departmentDTO.setCode(newPotionCode);
                        }
                    }
                    departmentService.createAll(departmentDTOs);
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

    @GetMapping("/public/departments/download/template-excel")
    public void downloadTemplateExcelDoctor(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment; filename=" + this.DEPARTMENT_EXCEL_TEMPLATE_NAME);
        IOUtils.copy(storageService.downloadExcelTemplateFromResource(this.DEPARTMENT_EXCEL_TEMPLATE_NAME), response.getOutputStream());
        response.flushBuffer();
    }

}
