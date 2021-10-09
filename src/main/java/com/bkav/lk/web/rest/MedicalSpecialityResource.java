package com.bkav.lk.web.rest;

import com.bkav.lk.domain.AbstractAuditingEntity;
import com.bkav.lk.dto.MedicalSpecialityDTO;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.MedicalSpecialityService;
import com.bkav.lk.service.mapper.MedicalSpecialityMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MedicalSpecialityResource {

    private static final Logger log = LoggerFactory.getLogger(MedicalServiceResource.class);
    private static final String ENTITY_NAME = "medical speciality";

    private final MedicalSpecialityService medicalSpecialityService;
    private final ActivityLogService activityLogService;
    private final MedicalSpecialityMapper medicalSpecialityMapper;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    public MedicalSpecialityResource(
            MedicalSpecialityService medicalSpecialityService,
            ActivityLogService activityLogService,
            MedicalSpecialityMapper medicalSpecialityMapper) {
        this.medicalSpecialityService = medicalSpecialityService;
        this.activityLogService = activityLogService;
        this.medicalSpecialityMapper = medicalSpecialityMapper;
    }

    @GetMapping("/medical-specialities")
    public ResponseEntity<List<MedicalSpecialityDTO>> search(
            @RequestHeader("healthFacilityId") Long healthFacilityId,
            @RequestParam MultiValueMap<String, String> queryParams,
            Pageable pageable) {
        log.debug("REST request to search for a page of medical specialities with health facility ID = {} and query: {}",
                healthFacilityId, queryParams);
        queryParams.set("healthFacilityId", String.valueOf(healthFacilityId));
        Page<MedicalSpecialityDTO> results = medicalSpecialityService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequestUri(), results);
        return ResponseEntity.ok().headers(headers).body(results.getContent());
    }

    @GetMapping("/medical-specialities/find-all")
    public ResponseEntity<List<MedicalSpecialityDTO>> findAll(
            @RequestHeader("healthFacilityId") Long healthFacilityId) {
        log.debug("REST request to find list medical speciality with health facility ID = {}");
        List<MedicalSpecialityDTO> results = medicalSpecialityService.findByHealthFacilityId(healthFacilityId);
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/medical-specialities/{id}")
    public ResponseEntity<MedicalSpecialityDTO> findById(@PathVariable("id") Long medicalSpecialityId) {
        log.debug("REST request to find a medical speciality with ID = {}");
        MedicalSpecialityDTO result = medicalSpecialityService.findById(medicalSpecialityId);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/medical-specialities")
    public ResponseEntity<MedicalSpecialityDTO> create(
            @RequestHeader("") Long healthFacilityId, @RequestBody MedicalSpecialityDTO medicalSpecialityDTO) {
        log.debug("REST request to create a medical speciality with data: {}", medicalSpecialityDTO);
        if (medicalSpecialityDTO.getId() != null) {
            throw new BadRequestAlertException("A new medical speciality cannot already has ID", ENTITY_NAME, "idexist");
        }
        medicalSpecialityDTO.setHealthFacilityId(healthFacilityId);
        MedicalSpecialityDTO result = medicalSpecialityService.save(medicalSpecialityDTO);
        activityLogService.create(Constants.CONTENT_TYPE.MEDICAL_SPECIALITY, medicalSpecialityMapper.toEntity(result));
        HttpHeaders headers = HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                String.valueOf(result.getId()));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PutMapping("/medical-specialities")
    public ResponseEntity<MedicalSpecialityDTO> update(
            @RequestHeader("") Long healthFacilityId, @RequestBody MedicalSpecialityDTO medicalSpecialityDTO) {
        log.debug("REST request to update a medical speciality with data: {}", medicalSpecialityDTO);
        if (medicalSpecialityDTO.getId() == null) {
            throw new BadRequestAlertException("A medical speciality cannot save has not ID", ENTITY_NAME, "idnull");
        }

        if(medicalSpecialityDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE) && !medicalSpecialityService.isDeactivable(medicalSpecialityDTO.getId())){
            throw new BadRequestAlertException("Chuyên khoa này đang được sử dụng", ENTITY_NAME, "medical-speciality.cant_deactivate");
        }

        medicalSpecialityDTO.setHealthFacilityId(healthFacilityId);
        MedicalSpecialityDTO oldData = medicalSpecialityService.findById(medicalSpecialityDTO.getId());
        MedicalSpecialityDTO newData = medicalSpecialityService.save(medicalSpecialityDTO);
        activityLogService.update(Constants.CONTENT_TYPE.MEDICAL_SPECIALITY,
                medicalSpecialityMapper.toEntity(oldData), medicalSpecialityMapper.toEntity(newData));
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                String.valueOf(newData.getId()));
        return ResponseEntity.ok().headers(headers).body(newData);
    }

    @DeleteMapping("/medical-specialities/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long medicalFacilityId) {
        log.debug("REST request to delete a medical speciality with ID = {}", medicalFacilityId);
        medicalSpecialityService.delete(medicalFacilityId);
        MedicalSpecialityDTO result = medicalSpecialityService.findById(medicalFacilityId);
        activityLogService.delete(Constants.CONTENT_TYPE.MEDICAL_SPECIALITY, medicalSpecialityMapper.toEntity(result));
        HttpHeaders headers = HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME,
                String.valueOf(medicalFacilityId));
        return ResponseEntity.noContent().headers(headers).build();
    }

    @DeleteMapping("/medical-specialities/delete-all")
    public ResponseEntity<Void> deleteAllByIds(@RequestParam(name = "ids") List<Long> ids) {
        log.info("List of medical speciality's id are prepared for deleting: {}", ids);
        medicalSpecialityService.deleteAll(ids);
        List<MedicalSpecialityDTO> medicalSpecialityDTOs = medicalSpecialityService.findAllByIds(ids);
        activityLogService.multipleDelete(
                Constants.CONTENT_TYPE.MEDICAL_SPECIALITY,
                medicalSpecialityMapper.toEntity(medicalSpecialityDTOs).stream()
                        .map(o -> (AbstractAuditingEntity) o).collect(Collectors.toList()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medical-specialities/exist-by-code/{code}")
    public ResponseEntity<Boolean> existByCode(@PathVariable("code") String code) {
        log.info("Check exist a medical speciality with code: {}", code);
        Boolean result = medicalSpecialityService.existByCode(code);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/medical-specialities/health-facility/{id}")
    public ResponseEntity<List<MedicalSpecialityDTO>> getByHealthFacility(
            @PathVariable Long id, @RequestParam(name = "hasClinic", required = false, defaultValue = "false") Boolean hasClinic) {
        List<MedicalSpecialityDTO> list = null;
        if (hasClinic) {
            list = medicalSpecialityService.findByHealthFacilityIdAndExistClinic(id);
        } else {
            list = medicalSpecialityService.findByHealthFacilityId(id);
        }
        return ResponseEntity.ok().body(list);
    }
}
