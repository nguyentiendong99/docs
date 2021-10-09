package com.bkav.lk.service.impl;

import com.bkav.lk.domain.CategoryConfigFieldMain;
import com.bkav.lk.dto.CategoryConfigFieldMainDTO;
import com.bkav.lk.repository.CategoryConfigFieldMainRepository;
import com.bkav.lk.service.CategoryConfigFieldMainService;
import com.bkav.lk.service.mapper.CategoryConfigFieldMainMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Service
public class CategoryConfigFieldMainServiceImpl implements CategoryConfigFieldMainService {

    public static final String ENTITY_NAME = "CategoryConfigFieldMain";
    private final CategoryConfigFieldMainRepository repository;
    private final CategoryConfigFieldMainMapper mapper;


    public CategoryConfigFieldMainServiceImpl(CategoryConfigFieldMainRepository repository, CategoryConfigFieldMainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<CategoryConfigFieldMainDTO> search(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        if (queryParams.containsKey("configType") && !StrUtil.isBlank(queryParams.get("configType").get(0))) {
            Integer configType = Integer.valueOf(queryParams.get("configType").get(0));
            String cType = Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            queryParams.put("configType", Arrays.asList(cType));
        }
        List<CategoryConfigFieldMainDTO> fieldMainDTOS = mapper.toDto(repository.search(queryParams, healthFacilityId));
        if(fieldMainDTOS.isEmpty()) {
            fieldMainDTOS = getFieldMainDefault(queryParams, healthFacilityId);
        }
        return fieldMainDTOS;
    }

    @Override
    public List<CategoryConfigFieldMainDTO> searchReport(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        if (queryParams.containsKey("configTypeId") && !StrUtil.isBlank(queryParams.get("configTypeId").get(0))) {
            Integer configType = Integer.valueOf(queryParams.get("configTypeId").get(0));
            String cType = Arrays.stream(Constants.CONFIG_REPORT_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            queryParams.put("configType", Arrays.asList(cType));
        }
        List<CategoryConfigFieldMainDTO> fieldMainDTOS = mapper.toDto(repository.search(queryParams, healthFacilityId));
        if(fieldMainDTOS.isEmpty()) {
            fieldMainDTOS = getFieldMainDefault(queryParams, healthFacilityId);
        }
        return fieldMainDTOS;
    }

    @Override
    public List<CategoryConfigFieldMainDTO> searchPatient(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        if (queryParams.containsKey("configTypeId") && !StrUtil.isBlank(queryParams.get("configTypeId").get(0))) {
            Integer configType = Integer.valueOf(queryParams.get("configTypeId").get(0));
            String cType = Arrays.stream(Constants.CONFIG_PATIENT_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            queryParams.put("configType", Arrays.asList(cType));
        }
        List<CategoryConfigFieldMainDTO> fieldMainDTOS = mapper.toDto(repository.search(queryParams, healthFacilityId));
        if(fieldMainDTOS.isEmpty()) {
            fieldMainDTOS = getFieldMainDefault(queryParams, healthFacilityId);
        }
        return fieldMainDTOS;
    }

    @Override
    public List<CategoryConfigFieldMainDTO> update(List<CategoryConfigFieldMainDTO> configFieldMainDTOs) {
        Set<Integer> setIndexPosition = new HashSet<>();
        for(CategoryConfigFieldMainDTO fieldMainDTO : configFieldMainDTOs) {
            // Check chi duoc thay doi 2 thuoc tinh: indexPosition and display
            if(fieldMainDTO.getId() != null) {
                Optional<CategoryConfigFieldMain> fieldMainOpt = repository.findById(fieldMainDTO.getId());
                if(fieldMainOpt.isPresent()) {
                    if(!(fieldMainDTO.getColumnName().equals(fieldMainOpt.get().getColumnName()))
                            || fieldMainDTO.getHealthFacilityId() != fieldMainOpt.get().getHealthFacilities().getId()
                            || !(fieldMainDTO.getType().equals(fieldMainOpt.get().getType()))) {
                        throw new BadRequestAlertException("Only change indexPosition, display", ENTITY_NAME, "config.only_change");
                    }
                }
            } else {
                // Xu ly loi 2 tab
                List<CategoryConfigFieldMain> fieldMains = repository.findByColumnNameAndTypeAndHealthFacilitiesId(fieldMainDTO.getColumnName(), fieldMainDTO.getType(), fieldMainDTO.getHealthFacilityId());
                if(fieldMains.size() > 0) {
                    fieldMainDTO.setId(fieldMains.get(0).getId());
                }
            }
            setIndexPosition.add(fieldMainDTO.getIndexPosition());
        }
        // Check trung vi tri
        if(setIndexPosition.size() != configFieldMainDTOs.size()) {
            throw new BadRequestAlertException("Index position duplicate", ENTITY_NAME, "config.index_position_duplicate");
        }
        List<CategoryConfigFieldMain> result = repository.saveAll(mapper.toEntity(configFieldMainDTOs));
        return mapper.toDto(result);
    }

    List<CategoryConfigFieldMainDTO> getFieldMainDefault(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        List<CategoryConfigFieldMainDTO> listFieldMain = new ArrayList<>();
        if(queryParams.containsKey("configType") && !StrUtil.isBlank(queryParams.get("configType").get(0))){
            String cType = queryParams.get("configType").get(0);
            if(cType.equals("DOCTOR")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_DOCTOR.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_DOCTOR[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("DOCTOR");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }else if(cType.equals("CLINIC")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_CLINIC.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_CLINIC[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("CLINIC");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }else if(cType.equals("MEDICAL_SERVICE")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_MEDICAL_SERVICE.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_MEDICAL_SERVICE[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("MEDICAL_SERVICE");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }else if(cType.equals("CLS")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_CLS.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_CLS[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("CLS");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }else if(cType.equals("TOPIC")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_TOPIC.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_TOPIC[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("TOPIC");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }else if(cType.equals("ACADEMIC")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_ACADEMIC.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_ACADEMIC[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("ACADEMIC");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }else if(cType.equals("MEDICAL_SPECIALITY")) {
                for(int i = 0; i < Constants.CATEGORY_FIELD_MAIN_CONFIG_MEDICAL_SPECIALITY.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.CATEGORY_FIELD_MAIN_CONFIG_MEDICAL_SPECIALITY[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("MEDICAL_SPECIALITY");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            } else if(cType.equals("FEEDBACK")) {
                for(int i = 0; i < Constants.REPORT_FIELD_MAIN_CONFIG_FEEDBACK.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.REPORT_FIELD_MAIN_CONFIG_FEEDBACK[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("FEEDBACK");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            } else if(cType.equals("DOCTOR_FEEDBACK")) {
                for(int i = 0; i < Constants.REPORT_FIELD_MAIN_CONFIG_DOCTOR_FEEDBACK.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.REPORT_FIELD_MAIN_CONFIG_DOCTOR_FEEDBACK[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("DOCTOR_FEEDBACK");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            } else if(cType.equals("PATIENT_RECORD")) {
                for(int i = 0; i < Constants.REPORT_FIELD_MAIN_CONFIG_PATIENT_RECORD.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.REPORT_FIELD_MAIN_CONFIG_PATIENT_RECORD[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("PATIENT_RECORD");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            } else if(cType.equals("SUBCLINICAL_RESULT")) {
                for(int i = 0; i < Constants.REPORT_FIELD_MAIN_CONFIG_SUBCLINICAL_RESULT.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.REPORT_FIELD_MAIN_CONFIG_SUBCLINICAL_RESULT[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("SUBCLINICAL_RESULT");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            } else if(cType.equals("RE_EXAMINATION")) {
                for(int i = 0; i < Constants.REPORT_FIELD_MAIN_CONFIG_RE_EXAMINATION.length; i++) {
                    CategoryConfigFieldMainDTO dto = new CategoryConfigFieldMainDTO();
                    dto.setHealthFacilityId(Long.valueOf(healthFacilityId));
                    dto.setColumnName(Constants.REPORT_FIELD_MAIN_CONFIG_RE_EXAMINATION[i]);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setType("RE_EXAMINATION");
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setIndexPosition(i+1);
                    listFieldMain.add(dto);
                }
            }
        }
        return listFieldMain;
    }
}
