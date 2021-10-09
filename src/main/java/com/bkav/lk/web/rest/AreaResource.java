package com.bkav.lk.web.rest;

import com.bkav.lk.dto.AreaDTO;
import com.bkav.lk.service.AreaService;
import com.bkav.lk.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AreaResource {
    private final Logger log = LoggerFactory.getLogger(AreaResource.class);

    private static final String ENTITY_NAME = "area";

    private final AreaService areaService;

    public AreaResource(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping("/areas/{areaCode}/children")
    public ResponseEntity<List<AreaDTO>>
    getChildrenOfArea(@PathVariable String areaCode) {
        log.debug("REST request to get all children of Area: {}", areaCode);
        List<AreaDTO> result = areaService.findByParentCode(areaCode);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/public/areas/{areaCode}/children")
    public ResponseEntity<List<AreaDTO>>
    getChildrenOfAreaMobile(@PathVariable String areaCode) {
        log.debug("REST request to get all children of Area: {}", areaCode);
        List<AreaDTO> result = areaService.findByParentCode(areaCode);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/public/areas/cities")
    public ResponseEntity<List<AreaDTO>> getAllCity() {
        log.debug("REST request to get all City");
        List<AreaDTO> cities = areaService.findByLevelAndStatus(Constants.AREA_LEVEL.CITY,Constants.ENTITY_STATUS.ACTIVE );
        return ResponseEntity
                .ok()
                .body(cities);
    }

    @GetMapping("/public/areas/districts")
    public ResponseEntity<List<AreaDTO>> getAllDistrict() {
        log.debug("REST request to get all District");
        List<AreaDTO> districts = areaService.findByLevelAndStatus(Constants.AREA_LEVEL.DISTRICT,Constants.ENTITY_STATUS.ACTIVE );
        return ResponseEntity
                .ok()
                .body(districts);
    }

    @GetMapping("/public/areas/wards")
    public ResponseEntity<List<AreaDTO>> getAllWard() {
        log.debug("REST request to get all Ward");
        List<AreaDTO> wards = areaService.findByLevelAndStatus(Constants.AREA_LEVEL.WARD,Constants.ENTITY_STATUS.ACTIVE );
        return ResponseEntity
                .ok()
                .body(wards);
    }
}
