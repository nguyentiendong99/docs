package com.bkav.lk.service.impl;

import com.bkav.lk.domain.CategoryConfigIcon;
import com.bkav.lk.dto.CategoryConfigIconDTO;
import com.bkav.lk.repository.CategoryConfigIconRepository;
import com.bkav.lk.service.CategoryConfigIconService;
import com.bkav.lk.service.mapper.CategoryConfigIconMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CategoryConfigIconServiceImpl implements CategoryConfigIconService {

    public static final String ENTITY_NAME = "CategoryConfigIcon";
    private final CategoryConfigIconRepository repository;
    private final CategoryConfigIconMapper mapper;

    public CategoryConfigIconServiceImpl(CategoryConfigIconRepository repository, CategoryConfigIconMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<CategoryConfigIconDTO> search(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        if (queryParams.containsKey("configType") && !StrUtil.isBlank(queryParams.get("configType").get(0))) {
            Integer configType = Integer.valueOf(queryParams.get("configType").get(0));
            String cType = Arrays.stream(Constants.CONFIG_CATEGORY_TYPE.values()).filter(type -> type.code.equals(configType)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            queryParams.put("configType", Arrays.asList(cType));
        }
        List<CategoryConfigIconDTO> fieldMainDTOS = mapper.toDto(repository.search(queryParams, healthFacilityId));
        if(fieldMainDTOS.isEmpty()) {
            fieldMainDTOS = getIconDefault(queryParams, healthFacilityId);
        }
        return fieldMainDTOS;
    }

    @Override
    public List<CategoryConfigIconDTO> searchReport(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        if (queryParams.containsKey("configTypeId") && !StrUtil.isBlank(queryParams.get("configTypeId").get(0))) {
            Integer configTypeId = Integer.valueOf(queryParams.get("configTypeId").get(0));
            String cType = Arrays.stream(Constants.CONFIG_REPORT_TYPE.values()).filter(type -> type.code.equals(configTypeId)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            queryParams.put("configType", Arrays.asList(cType));
        }
        List<CategoryConfigIconDTO> fieldMainDTOS = mapper.toDto(repository.search(queryParams, healthFacilityId));
        if(fieldMainDTOS.isEmpty()) {
            fieldMainDTOS = getIconDefault(queryParams, healthFacilityId);
        }
        return fieldMainDTOS;
    }

    @Override
    public List<CategoryConfigIconDTO> searchPatient(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        if (queryParams.containsKey("configTypeId") && !StrUtil.isBlank(queryParams.get("configTypeId").get(0))) {
            Integer configTypeId = Integer.valueOf(queryParams.get("configTypeId").get(0));
            String cType = Arrays.stream(Constants.CONFIG_PATIENT_TYPE.values()).filter(type -> type.code.equals(configTypeId)).findFirst().orElseThrow(() -> new BadRequestAlertException("Invalid data type", ENTITY_NAME, "config.type_invalid")).value;
            queryParams.put("configType", Arrays.asList(cType));
        }
        List<CategoryConfigIconDTO> fieldMainDTOS = mapper.toDto(repository.search(queryParams, healthFacilityId));
        if(fieldMainDTOS.isEmpty()) {
            fieldMainDTOS = getIconDefault(queryParams, healthFacilityId);
        }
        return fieldMainDTOS;
    }

    @Override
    public List<CategoryConfigIconDTO> update(List<CategoryConfigIconDTO> configIconDTOs) {
        for(CategoryConfigIconDTO iconDTO: configIconDTOs) {
            // Check TH 2 tab
            List<CategoryConfigIcon> icons = repository.findByCodeMethodAndTypeAndHealthFacilitiesId(iconDTO.getCodeMethod(), iconDTO.getType(), iconDTO.getHealthFacilityId());
            if(icons.size() > 0) {
                iconDTO.setId(icons.get(0).getId());
            }
        }
        return mapper.toDto(repository.saveAll(mapper.toEntity(configIconDTOs)));
    }

    private List<CategoryConfigIconDTO> getIconDefault(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        List<CategoryConfigIconDTO> iconDTOList = new ArrayList<>();
        if (queryParams.containsKey("configType") && !StrUtil.isBlank(queryParams.get("configType").get(0))) {
            String cType = queryParams.get("configType").get(0);
            if (cType.equals("DOCTOR")) {
                for(String code : Constants.CATEGORY_ICON_DOCTOR_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("add")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD);
                    }else if(code.equals("update")) {
                        dto.setIcon(Constants.ICON_DEFAULT.UPDATE);
                    }else if(code.equals("delete")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DELETE);
                    }else if(code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    }else if(code.equals("add_by_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD_BY_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("DOCTOR");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("CLINIC")) {
                for(String code : Constants.CATEGORY_ICON_CLINIC_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("add")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD);
                    }else if(code.equals("update")) {
                        dto.setIcon(Constants.ICON_DEFAULT.UPDATE);
                    }else if(code.equals("delete")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DELETE);
                    }else if(code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    }else if(code.equals("add_by_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD_BY_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("CLINIC");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("MEDICAL_SERVICE")) {
                for(String code : Constants.CATEGORY_ICON_MEDICAL_SERVICE_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("add")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD);
                    }else if(code.equals("update")) {
                        dto.setIcon(Constants.ICON_DEFAULT.UPDATE);
                    }else if(code.equals("delete")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DELETE);
                    }else if(code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    }else if(code.equals("add_by_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD_BY_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("MEDICAL_SERVICE");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("CLS")) {
                for(String code : Constants.CATEGORY_ICON_CLS_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("add")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD);
                    }else if(code.equals("update")) {
                        dto.setIcon(Constants.ICON_DEFAULT.UPDATE);
                    }else if(code.equals("delete")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DELETE);
                    }else if(code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    }else if(code.equals("add_by_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD_BY_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("CLS");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("TOPIC")) {
                for(String code : Constants.CATEGORY_ICON_CLS_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if (code.equals("add")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD);
                    } else if (code.equals("update")) {
                        dto.setIcon(Constants.ICON_DEFAULT.UPDATE);
                    } else if (code.equals("delete")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DELETE);
                    } else if (code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    } else if (code.equals("add_by_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.ADD_BY_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("TOPIC");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("FEEDBACK")) {
                for(String code : Constants.REPORT_ICON_FEEDBACK_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("detail")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DETAIL);
                    }else if(code.equals("history")) {
                        dto.setIcon(Constants.ICON_DEFAULT.HISTORY);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("FEEDBACK");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("DOCTOR_FEEDBACK")) {
                for(String code : Constants.REPORT_ICON_DOCTOR_FEEDBACK_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("detail")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DETAIL);
                    }else if(code.equals("history")) {
                        dto.setIcon(Constants.ICON_DEFAULT.HISTORY);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("DOCTOR_FEEDBACK");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("PATIENT_RECORD")) {
                for(String code : Constants.REPORT_ICON_PATIENT_RECORD_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("detail")) {
                        dto.setIcon(Constants.ICON_DEFAULT.DETAIL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("PATIENT_RECORD");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("SUBCLINICAL_RESULT")) {
                for(String code : Constants.REPORT_ICON_SUBCLINICAL_RESULT_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("notify")) {
                        dto.setIcon(Constants.ICON_DEFAULT.NOTIFY);
                    } else if(code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("SUBCLINICAL_RESULT");
                    iconDTOList.add(dto);
                }
            } else if (cType.equals("RE_EXAMINATION")) {
                for(String code : Constants.REPORT_ICON_RE_EXAMINATION_METHOD) {
                    CategoryConfigIconDTO dto = new CategoryConfigIconDTO();
                    if(code.equals("re_examination")) {
                        dto.setIcon(Constants.ICON_DEFAULT.RE_EXAMINATION);
                    } else if(code.equals("export_excel")) {
                        dto.setIcon(Constants.ICON_DEFAULT.EXPORT_EXCEL);
                    }
                    dto.setCodeMethod(code);
                    dto.setDisplay(Constants.BOOL_NUMBER.TRUE);
                    dto.setHealthFacilityId(healthFacilityId);
                    dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    dto.setType("RE_EXAMINATION");
                    iconDTOList.add(dto);
                }
            }
        }
        return iconDTOList;
    }

}
