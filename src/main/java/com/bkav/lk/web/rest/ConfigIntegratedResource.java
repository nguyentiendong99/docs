package com.bkav.lk.web.rest;
import com.bkav.lk.domain.Config;
import com.bkav.lk.dto.ConfigIntegratedDTO;
import com.bkav.lk.service.ConfigIntegratedService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ConfigIntegratedResource {

    private static final Logger log = LoggerFactory.getLogger(ConfigResource.class);

    private static final String ENTITY_NAME = "ConfigIntegrated";

    private final ConfigIntegratedService configIntegratedService;

    public ConfigIntegratedResource(ConfigIntegratedService configIntegratedService) {
        this.configIntegratedService = configIntegratedService;
    }

    @PutMapping("/config-integrated")
    public ResponseEntity<ConfigIntegratedDTO> update(@RequestBody ConfigIntegratedDTO configIntegratedDTO){
        log.debug("REST request to save Config Integrated : {}", configIntegratedDTO);
        configIntegratedDTO = configIntegratedService.save(configIntegratedDTO);
        return ResponseEntity.ok().body(configIntegratedDTO);
    }

    @GetMapping("/config-integrated/healthfacility-id")
    public ResponseEntity<List<ConfigIntegratedDTO>> findByHealthFacilityId(@RequestParam (name = "healthFacilityId") Long healthFacilityId) {
        log.debug("REST request to find Config Integrated by : healthFacilityId {}", healthFacilityId);
        List<ConfigIntegratedDTO> configIntegratedDTOs = configIntegratedService.findByHealthFacilityId(healthFacilityId);
        // Chua co se tao mac dinh theo benh vien
        if (configIntegratedDTOs.isEmpty()) {
            configIntegratedDTOs = configIntegratedService.saveConfigIntegratedDefault(healthFacilityId);
        }
        return ResponseEntity.ok().body(configIntegratedDTOs);
    }

    @GetMapping("/config-integrated/shared")
    public ResponseEntity<ConfigIntegratedDTO> getConfigIntegratedShared(){
        List<ConfigIntegratedDTO> configIntegratedDTOS = configIntegratedService.findConfigIntegratedShared(Constants.CONFIG_INTEGRATED.SOCIAL_INSURANCE_CONNECT_CODE);
        if(configIntegratedDTOS.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(configIntegratedDTOS.get(0));
    }
}
