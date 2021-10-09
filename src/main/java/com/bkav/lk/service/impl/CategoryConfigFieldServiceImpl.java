package com.bkav.lk.service.impl;

import com.bkav.lk.domain.CategoryConfigField;
import com.bkav.lk.dto.CategoryConfigFieldDTO;
import com.bkav.lk.dto.CategoryConfigValueDTO;
import com.bkav.lk.repository.CategoryConfigFieldRepository;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigValueService;
import com.bkav.lk.service.HealthFacilitiesService;
import com.bkav.lk.service.mapper.CategoryConfigFieldMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hieu.daominh
 */
@Service
public class CategoryConfigFieldServiceImpl implements CategoryConfigFieldService {

    private static final Logger log = LoggerFactory.getLogger(CategoryConfigFieldService.class);

    private static final String ENTITY_NAME = "categoryConfigField";

    private final CategoryConfigFieldRepository categoryConfigFieldRepository;
    private final CategoryConfigValueService categoryConfigValueService;
    private final CategoryConfigFieldMapper categoryConfigFieldMapper;
    private final HealthFacilitiesService healthFacilitiesService;

    public CategoryConfigFieldServiceImpl(CategoryConfigFieldRepository categoryConfigFieldRepository, @Lazy CategoryConfigValueService categoryConfigValueService, CategoryConfigFieldMapper categoryConfigFieldMapper, HealthFacilitiesService healthFacilitiesService) {
        this.categoryConfigFieldRepository = categoryConfigFieldRepository;
        this.categoryConfigValueService = categoryConfigValueService;
        this.categoryConfigFieldMapper = categoryConfigFieldMapper;
        this.healthFacilitiesService = healthFacilitiesService;
    }

    @Override
    public Page<CategoryConfigFieldDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        return null;
    }

    @Override
    public CategoryConfigFieldDTO findById(Long fieldId) throws EntityNotFoundException {
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findById(fieldId).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
    }

    @Override
    public CategoryConfigFieldDTO findByIdAndStatus(Long fieldId, Integer status) {
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findByIdAndStatus(fieldId, status).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllByStatus(Integer status) {
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByStatus(status).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAll() {
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CategoryConfigFieldDTO> create(List<CategoryConfigFieldDTO> categoryConfigFieldDTOs) throws BadRequestAlertException {
        log.debug("Create category config field Start");
        categoryConfigFieldDTOs.forEach(categoryConfigFieldDTO -> {
            // check HealthFacility exist
            if (!healthFacilitiesService.checkExitsByIdAndStatus(categoryConfigFieldDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE)) {
                throw new BadRequestAlertException("Health Facility Not Exist", ENTITY_NAME, "config.health_facility_not_exist");
            }
            // check duplicate field name
            String cType = Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> categoryConfigFieldDTO.getType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            if (categoryConfigFieldRepository.existsByNameIgnoreCaseAndStatusNot(categoryConfigFieldDTO.getHealthFacilityId(), categoryConfigFieldDTO.getName(), cType, Constants.ENTITY_STATUS.DELETED) > 0) {
                throw new BadRequestAlertException("Duplicate config field name", ENTITY_NAME, "config.field_name_duplicate");
            }
            categoryConfigFieldDTO.setStatus(categoryConfigFieldDTO.getStatus());
            // set config type
            if (categoryConfigFieldDTO.getConfigType().equals(Constants.CONFIG_REPORT_TYPE_STRING)) {
                categoryConfigFieldDTO.setType(Arrays.stream(Constants.CONFIG_REPORT_TYPE.values()).filter(type -> categoryConfigFieldDTO.getType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value);
            } else if (categoryConfigFieldDTO.getConfigType().equals(Constants.CONFIG_PATIENT_TYPE_STRING)) {
                categoryConfigFieldDTO.setType(Arrays.stream(Constants.CONFIG_PATIENT_TYPE.values()).filter(type -> categoryConfigFieldDTO.getType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value);
            } else {
                categoryConfigFieldDTO.setType(Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> categoryConfigFieldDTO.getType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value);
            }
            // set datatype
            categoryConfigFieldDTO.setDataType(Arrays.stream(Constants.CONFIG_DATA_TYPE.values()).filter(type -> categoryConfigFieldDTO.getDataType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.datatype_invalid")).value);
        });
        List<CategoryConfigField> configFields = categoryConfigFieldRepository.saveAll(categoryConfigFieldMapper.toEntity(categoryConfigFieldDTOs));
        log.debug("Create category config field End");
        return categoryConfigFieldMapper.toDto(configFields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CategoryConfigFieldDTO> update(List<CategoryConfigFieldDTO> categoryConfigFieldDTOs) throws EntityNotFoundException {
        log.debug("Update category config field Start");
        List<CategoryConfigField> configFields = new ArrayList<>();
        List<CategoryConfigValueDTO> valueDTOS = new ArrayList<>();
        Integer[] arrStatus = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        categoryConfigFieldDTOs.forEach(categoryConfigFieldDTO -> {
            // check field exited
            CategoryConfigField configField = categoryConfigFieldRepository.findByIdAndStatusIn(categoryConfigFieldDTO.getId(), arrStatus).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
            // check duplicate field name
            String cType = Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> categoryConfigFieldDTO.getType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.datatype_invalid")).value;
            if ((categoryConfigFieldRepository.existsByNameIgnoreCaseAndIdNotAndStatusNot(categoryConfigFieldDTO.getName(), categoryConfigFieldDTO.getId(), Constants.ENTITY_STATUS.DELETED, categoryConfigFieldDTO.getHealthFacilityId(), cType)) > 0) {
                throw new BadRequestAlertException("Duplicate config field name", ENTITY_NAME, "config.field_name_duplicate");
            }
            // check HealthFacility exist
            if (!healthFacilitiesService.checkExitsByIdAndStatus(categoryConfigFieldDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE)) {
                throw new BadRequestAlertException("Health Facility Not Exist", ENTITY_NAME, "config.health_facility_not_exist");
            }
            // set datatype
            configField.setDataType(Arrays.stream(Constants.CONFIG_DATA_TYPE.values()).filter(type -> categoryConfigFieldDTO.getDataType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value);
            // set config type
            configField.setType(Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> categoryConfigFieldDTO.getType().equals(type.code.toString())).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.datatype_invalid")).value);
            configField.setName(categoryConfigFieldDTO.getName());
            configField.setDescription(categoryConfigFieldDTO.getDescription());
            configField.setRequired(categoryConfigFieldDTO.getRequired());
            configField.setStatus(categoryConfigFieldDTO.getStatus());
            configField.setValue(categoryConfigFieldDTO.getValue());

            // set status all value af field
            valueDTOS.addAll(categoryConfigValueService.findAllByFieldId(categoryConfigFieldDTO.getId()));

            configFields.add(configField);
        });
        categoryConfigFieldRepository.saveAll(configFields);
        categoryConfigValueService.updateAll(valueDTOS);
        log.debug("Update category config field End");
        return categoryConfigFieldMapper.toDto(configFields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) throws EntityNotFoundException {
        log.debug("Delete category config field Start");
        CategoryConfigField configField = categoryConfigFieldRepository.findById(id).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        // set delete status all value af field
        categoryConfigValueService.deleteAll(categoryConfigValueService.findAllByFieldIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE).stream().map(CategoryConfigValueDTO::getId).collect(Collectors.toList()));
        configField.setStatus(Constants.ENTITY_STATUS.DELETED);
        categoryConfigFieldRepository.save(configField);
        log.debug("Delete category config field End");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(List<Long> ids) {
        log.debug("Delete multi category config field Start");
        List<CategoryConfigField> configFields = new ArrayList<>();
        List<Long> configValueIds = new ArrayList<>();
        for (Long id : ids) {
            CategoryConfigField configField = categoryConfigFieldRepository.findByIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
            // set delete status all value af field
            configValueIds.addAll(categoryConfigValueService.findAllByFieldIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE).stream().map(CategoryConfigValueDTO::getId).collect(Collectors.toList()));
            configField.setStatus(Constants.ENTITY_STATUS.DELETED);
            configFields.add(configField);
        }
        categoryConfigValueService.deleteAll(configValueIds);
        categoryConfigFieldRepository.saveAll(configFields);
        log.debug("Delete multi category config field End");
    }

    @Override
    public boolean checkExitsByIdAndStatusNot(Long id, Integer status) {
        return categoryConfigFieldRepository.existsByIdAndStatusNot(id, status);
    }

    @Override
    public boolean checkExitsById(Long id) {
        return categoryConfigFieldRepository.existsById(id);
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllByHealthFacilityIdAndStatus(Long healthFacilityId, Integer status) {
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndStatus(healthFacilityId, status).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, Integer configType) {
        String cType = Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, status, cType).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, Integer configType, Integer[] status) {
        String cType = Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndConfigTypeAndStatusIn(healthFacilityId, cType, status).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllReportByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, Integer configType) {
        String cType = Arrays.stream(Constants.CONFIG_REPORT_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, status, cType).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllPatientByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, Integer configType) {
        String cType = Arrays.stream(Constants.CONFIG_PATIENT_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, status, cType).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllReportByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, Integer configType, Integer[] status) {
        String cType = Arrays.stream(Constants.CONFIG_REPORT_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndConfigTypeAndStatusIn(healthFacilityId, cType, status).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigFieldDTO> findAllPatientByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, Integer configType, Integer[] status) {
        String cType = Arrays.stream(Constants.CONFIG_PATIENT_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
        return categoryConfigFieldMapper.toDto(categoryConfigFieldRepository.findAllByHealthFacilityIdAndConfigTypeAndStatusIn(healthFacilityId, cType, status).orElseGet(ArrayList::new));
    }
}
