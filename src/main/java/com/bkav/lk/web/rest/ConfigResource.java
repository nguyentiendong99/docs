package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Config;
import com.bkav.lk.dto.CategoryConfigFieldDTO;
import com.bkav.lk.dto.CategoryConfigFieldMainDTO;
import com.bkav.lk.dto.CategoryConfigIconDTO;
import com.bkav.lk.service.CategoryConfigFieldMainService;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigIconService;
import com.bkav.lk.service.ConfigService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ConfigResource {

    private static final Logger log = LoggerFactory.getLogger(ConfigResource.class);

    private static final String ENTITY_NAME = "config";

    private ConfigService configService;
    private final CategoryConfigFieldService categoryConfigFieldService;
    private final CategoryConfigFieldMainService categoryConfigFieldMainService;
    private final CategoryConfigIconService categoryConfigIconService;

    private static String TERM_OF_USE = "term_of_use";
    public ConfigResource(ConfigService configService, CategoryConfigFieldService categoryConfigFieldService, CategoryConfigFieldMainService categoryConfigFieldMainService,
                          CategoryConfigIconService categoryConfigIconService) {
        this.configService = configService;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigFieldMainService = categoryConfigFieldMainService;
        this.categoryConfigIconService = categoryConfigIconService;
    }

    @GetMapping("/public/configs/relationship")
    public ResponseEntity<List<HashMap<String, String>>> geRelationship() {
        List<HashMap<String, String>> relationships = new ArrayList<>();
        for (Constants.RelationShipConstant r : Constants.RelationShipConstant.values()) {
            HashMap<String, String> relationship = new HashMap<>();
            relationship.put("code", r.name());
            relationship.put("name", r.value);
            relationship.put("gender", r.gender);
            relationships.add(relationship);
        }
        return ResponseEntity.ok(relationships);
    }

    @GetMapping("/public/configs/term-of-use")
    public ResponseEntity<Config> getTermOfUse() {
        Config config = configService.findByPropertyCode(TERM_OF_USE);
        if(config == null){
            config = configService.createNewTermOfUseConfig();
        }
        return ResponseEntity.ok(config);
    }

    @GetMapping("/public/configs/social-network-signin")
    public ResponseEntity<List<Map<String, String>>> listOfSocialNetworks() {
        List<Map<String, String>> mapList = new ArrayList<>();
        List<Config> configs = configService.findByPropertyGroup(Constants.CONFIG_PROPERTY.SOCIAL_NETWORK_SIGNIN_GROUP);
        configs.forEach(config -> {
            Map<String, String> values = new LinkedHashMap<>();
            String status = config.getPropertyValue().equals(Constants.ENTITY_STATUS.ACTIVE.toString()) ? "ACTIVE" : "DEACTIVATE";
            values.put("code", config.getPropertyCode());
            values.put("value", config.getPropertyValue());
            values.put("status", status);
            mapList.add(values);
        });
        return ResponseEntity.ok(mapList);
    }

    @PutMapping("/configs/term-of-use")
    public ResponseEntity<Config> updateTermOfUse(@RequestBody Config config) {
        return ResponseEntity.ok(configService.save(config));
    }

    @PutMapping("/configs/integrated")
    public ResponseEntity<List<Config>> updateIntegrated(@RequestBody List<Config> configs) {
        for(Config config : configs) {
            configService.save(config);
        }
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/configs/integrated/{propGroup}")
    public ResponseEntity<List<Config>> getConfigByPropGroup(@PathVariable(name = "propGroup") String propGroup) {
        return ResponseEntity.ok(configService.findByPropertyGroup(propGroup));
    }

    @GetMapping("/public/configs/integrated/{propCode}")
    public ResponseEntity<Config> getPublicConfigByPropCode(@PathVariable(name = "propCode") String propCode) {
        return ResponseEntity.ok(configService.findByPropertyCode(propCode));
    }

    @GetMapping("/configs/category-custom-field")
    public ResponseEntity<List<CategoryConfigFieldDTO>> getCustomCategoryFields(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam Integer configType) {
        return ResponseEntity.ok(categoryConfigFieldService.findAllByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE, configType));
    }

    @GetMapping("/configs/report-custom-field")
    public ResponseEntity<List<CategoryConfigFieldDTO>> getCustomReportFields(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam Integer configType) {
        return ResponseEntity.ok(categoryConfigFieldService.findAllReportByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE, configType));
    }

    @GetMapping("/configs/patient-custom-field")
    public ResponseEntity<List<CategoryConfigFieldDTO>> getCustomPatientFields(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam Integer configType) {
        return ResponseEntity.ok(categoryConfigFieldService.findAllPatientByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE, configType));
    }

    @GetMapping("/configs/category-custom-field/active-unactive")
    public ResponseEntity<List<CategoryConfigFieldDTO>> getCustomCategoryFieldsActiveUnActive(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam Integer configType) {
        Integer[] arrStatus = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        return ResponseEntity.ok(categoryConfigFieldService.findAllByHealthFacilityIdAndConfigTypeAndStatusIn(healthFacilityId, configType, arrStatus));
    }

    @GetMapping("/configs/report-custom-field/active-unactive")
        public ResponseEntity<List<CategoryConfigFieldDTO>> getCustomReportFieldsActiveUnActive(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam Integer configType) {
            Integer[] arrStatus = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
            return ResponseEntity.ok(categoryConfigFieldService.findAllReportByHealthFacilityIdAndConfigTypeAndStatusIn(healthFacilityId, configType, arrStatus));
    }

    @GetMapping("/configs/patient-custom-field/active-unactive")
    public ResponseEntity<List<CategoryConfigFieldDTO>> getCustomPatientFieldsActiveUnActive(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam Integer configType) {
        Integer[] arrStatus = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        return ResponseEntity.ok(categoryConfigFieldService.findAllPatientByHealthFacilityIdAndConfigTypeAndStatusIn(healthFacilityId, configType, arrStatus));
    }

    @PostMapping("/configs/category-custom-field")
    public ResponseEntity<List<CategoryConfigFieldDTO>> createCategoryCustomField(@RequestBody @Valid List<CategoryConfigFieldDTO> configFieldDTOs) {
        log.info("REST request to save category custom field input: {}", configFieldDTOs);
        configFieldDTOs.forEach(categoryConfigFieldDTO -> {
            if (categoryConfigFieldDTO.getId() != null) {
                throw new BadRequestAlertException("A new category custom filed cannot already has ID", ENTITY_NAME, "idexist");
            }
        });
        List<CategoryConfigFieldDTO> fieldDTOs = categoryConfigFieldService.create(configFieldDTOs);
        return ResponseEntity.ok(fieldDTOs);
    }

    @PutMapping("/configs/category-custom-field")
    public ResponseEntity<List<CategoryConfigFieldDTO>> updateCategoryCustomField(@RequestBody @Valid List<CategoryConfigFieldDTO> configFieldDTOs) {
        log.info("REST request to save category custom field input: {}", configFieldDTOs);
        configFieldDTOs.forEach(categoryConfigFieldDTO -> {
            if (categoryConfigFieldDTO.getId() == null) {
                throw new BadRequestAlertException("A category custom filed cannot save when not has ID", ENTITY_NAME, "idnull");
            }
        });
        List<CategoryConfigFieldDTO> result = categoryConfigFieldService.update(configFieldDTOs);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/configs/category-custom-field")
    public ResponseEntity<Object> deleteCategoryCustomField(@RequestParam(name = "ids") List<Long> ids) {
        log.info("REST request to delete category custom field input: {}", ids);
        ids.forEach(id -> {
            if (!categoryConfigFieldService.checkExitsByIdAndStatusNot(id, Constants.ENTITY_STATUS.DELETED)) {
                throw new BadRequestAlertException("A category custom filed cannot save when not has ID", ENTITY_NAME, "idnull");
            }
        });
        categoryConfigFieldService.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/configs/category-field-main")
    public ResponseEntity<List<CategoryConfigFieldMainDTO>> searchCategoryMainFields(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams) {
        return ResponseEntity.ok().body(categoryConfigFieldMainService.search(queryParams, healthFacilityId));
    }

    @GetMapping("/configs/report-field-main")
    public ResponseEntity<List<CategoryConfigFieldMainDTO>> searchReportMainFields(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams) {
        return ResponseEntity.ok().body(categoryConfigFieldMainService.searchReport(queryParams, healthFacilityId));
    }

    @GetMapping("/configs/patient-field-main")
    public ResponseEntity<List<CategoryConfigFieldMainDTO>> searchPatientMainFields(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams) {
        return ResponseEntity.ok().body(categoryConfigFieldMainService.searchPatient(queryParams, healthFacilityId));
    }

    @PutMapping("configs/category-field-main")
    public ResponseEntity<List<CategoryConfigFieldMainDTO>> save(@RequestBody List<CategoryConfigFieldMainDTO> configFieldMainDTOs ) {
        log.info("REST request to save category field main input: {}", configFieldMainDTOs);
        List<CategoryConfigFieldMainDTO> result = categoryConfigFieldMainService.update(configFieldMainDTOs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("configs/category-icon")
    public ResponseEntity<List<CategoryConfigIconDTO>> searchCategoryIcon(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams){
        return ResponseEntity.ok().body(categoryConfigIconService.search(queryParams, healthFacilityId));
    }

    @GetMapping("configs/report-icon")
        public ResponseEntity<List<CategoryConfigIconDTO>> searchReportIcon(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams){
            return ResponseEntity.ok().body(categoryConfigIconService.searchReport(queryParams, healthFacilityId));
    }

    @GetMapping("configs/patient-icon")
    public ResponseEntity<List<CategoryConfigIconDTO>> searchPatientIcon(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams){
        return ResponseEntity.ok().body(categoryConfigIconService.searchPatient(queryParams, healthFacilityId));
    }

    @PutMapping("configs/category-icon")
    public ResponseEntity<List<CategoryConfigIconDTO>> saveCategoryIcon(@RequestBody List<CategoryConfigIconDTO> configIconDTOs ) {
        log.info("REST request to save category icon input: {}", configIconDTOs);
        List<CategoryConfigIconDTO> result = categoryConfigIconService.update(configIconDTOs);
        return ResponseEntity.ok(result);
    }

    @PutMapping("configs/category")
    public ResponseEntity<Object> update(@RequestBody Map<String, List<Object>> mapConfig) {
        log.info("REST request to save category config input: {}", mapConfig);
        Boolean result = configService.updateConfigCategory(mapConfig);
        return ResponseEntity.ok(result);
    }

    @PutMapping("configs/report")
    public ResponseEntity<Object> updateReport(@RequestBody Map<String, List<Object>> mapConfig) {
        log.info("REST request to save report config input: {}", mapConfig);
        Boolean result = configService.updateConfigCategory(mapConfig);
        return ResponseEntity.ok(result);
    }

    @PutMapping("configs/patient")
    public ResponseEntity<Object> updatePatient(@RequestBody Map<String, List<Object>> mapConfig) {
        log.info("REST request to save patient config input: {}", mapConfig);
        Boolean result = configService.updateConfigCategory(mapConfig);
        return ResponseEntity.ok(result);
    }

}
