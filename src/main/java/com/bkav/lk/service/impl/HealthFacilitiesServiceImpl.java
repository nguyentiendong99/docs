package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.DoctorAppointmentConfiguration;
import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.CategoryConfigFieldDTO;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import com.bkav.lk.dto.HealthFacilitiesHistoryDTO;
import com.bkav.lk.repository.ActivityLogRepository;
import com.bkav.lk.repository.DoctorAppointmentConfigurationRepository;
import com.bkav.lk.repository.HealthFacilitiesRepository;
import com.bkav.lk.security.AuthoritiesConstants;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.DoctorAppointmentConfigurationService;
import com.bkav.lk.service.HealthFacilitiesService;
import com.bkav.lk.service.mapper.DoctorAppointmentConfigurationMapper;
import com.bkav.lk.service.mapper.HealthFacilitiesMapper;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.google.api.client.util.Lists;
import com.google.common.base.Functions;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class HealthFacilitiesServiceImpl implements HealthFacilitiesService {
    private final Logger log = LoggerFactory.getLogger(HealthFacilitiesService.class);

    private static final String SHEET_HEALTH_FACILITIES = "Health_facilities";

    private final HealthFacilitiesRepository healthFacilitiesRepository;

    private final HealthFacilitiesMapper healthFacilitiesMapper;

    private final ActivityLogRepository activityLogRepository;

    private final DoctorAppointmentConfigurationRepository doctorAppointmentConfigurationRepository;

    private final DoctorAppointmentConfigurationMapper doctorAppointmentConfigurationMapper;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    public HealthFacilitiesServiceImpl(HealthFacilitiesRepository healthFacilitiesRepository, HealthFacilitiesMapper healthFacilitiesMapper, ActivityLogRepository activityLogRepository, DoctorAppointmentConfigurationRepository doctorAppointmentConfigurationRepository, DoctorAppointmentConfigurationMapper doctorAppointmentConfigurationMapper, @Lazy CategoryConfigFieldService categoryConfigFieldService, @Lazy DoctorAppointmentConfigurationService doctorAppointmentConfigurationService) {
        this.healthFacilitiesRepository = healthFacilitiesRepository;
        this.healthFacilitiesMapper = healthFacilitiesMapper;
        this.activityLogRepository = activityLogRepository;
        this.doctorAppointmentConfigurationRepository = doctorAppointmentConfigurationRepository;
        this.doctorAppointmentConfigurationMapper = doctorAppointmentConfigurationMapper;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
    }

    /**
     * Tạo đơn vị SYT yên bái nếu chưa tồn tai
     */
    @PostConstruct
    public void init() {
        Optional<HealthFacilities> healthFacilitiesOptional = healthFacilitiesRepository.findByCodeAndStatusGreaterThan(Constants.SYT_DEFAULT.code, Constants.ENTITY_STATUS.DELETED);
        if (!healthFacilitiesOptional.isPresent()) {
            HealthFacilities healthFacilities = new HealthFacilities();
            healthFacilities.setName(Constants.SYT_DEFAULT.name);
            healthFacilities.setCode(Constants.SYT_DEFAULT.code);
            healthFacilities.setCityCode(Constants.SYT_DEFAULT.cityCode);
            healthFacilities.setStatus(Constants.ENTITY_STATUS.ACTIVE);
            healthFacilities.setDistrictCode(Constants.SYT_DEFAULT.districtCode);
            healthFacilities.setWardCode(Constants.SYT_DEFAULT.wardCode);
            healthFacilities.setAddress(Constants.SYT_DEFAULT.address);
            healthFacilitiesRepository.save(healthFacilities);
        }
    }

    @Override
    public Optional<HealthFacilitiesDTO> findOne(Long id) {
        log.debug("Request to get healthFacilities : {}", id);
        return healthFacilitiesRepository.findById(id).map(healthFacilitiesMapper::toDto);
    }

    @Override
    public Page<HealthFacilitiesDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("Request to search healthFacilities with multi query {}", queryParams);
        queryParams.set("parentCode", Constants.SYT_DEFAULT.code);
        List<HealthFacilitiesDTO> healthFacilitiesDTOList = healthFacilitiesMapper.toDto(healthFacilitiesRepository.search(queryParams, pageable));
        healthFacilitiesDTOList.forEach(item -> item.setConfig(doctorAppointmentConfigurationService.findOneByHealthFacilitiesId(item.getId())));
        return new PageImpl<>(healthFacilitiesDTOList, pageable, healthFacilitiesRepository.count(queryParams));
    }

    @Override
    public HealthFacilitiesDTO save(HealthFacilitiesDTO healthFacilitiesDTO) {
        log.debug("Request to save healthFacilities: {}", healthFacilitiesDTO);
        //Update
        if (healthFacilitiesDTO.getId() != null) {
            Optional<HealthFacilities> hfOpt = healthFacilitiesRepository.findById(healthFacilitiesDTO.getId());
            if (hfOpt.isPresent()) {
                String code;
                if (healthFacilitiesDTO.getCode() == null) {
                    code = hfOpt.get().getCode();
                    healthFacilitiesDTO.setCode(code);
                } else if (!healthFacilitiesDTO.getCode().trim().equalsIgnoreCase(hfOpt.get().getCode()) || !healthFacilitiesDTO.getName().trim().equalsIgnoreCase(hfOpt.get().getName())){
                    code = this.generateHealthFacilitiesCode(healthFacilitiesDTO.getCode(), healthFacilitiesDTO.getName(), !healthFacilitiesDTO.getName().trim().equalsIgnoreCase(hfOpt.get().getName()));
                    healthFacilitiesDTO.setCode(code);
                }
            } else {
                new BadRequestAlertException("Health facilites not found", "health facilities", "not exist");
            }
        } else {
            // Create
            healthFacilitiesDTO.setCode(this.generateHealthFacilitiesCode(healthFacilitiesDTO.getCode(), healthFacilitiesDTO.getName(), false));
        }
        HealthFacilities healthFacilities = healthFacilitiesMapper.toEntity(healthFacilitiesDTO);
        healthFacilities = healthFacilitiesRepository.save(healthFacilities);
        return healthFacilitiesMapper.toDto(healthFacilities);
    }

    @Override
    public List<HealthFacilitiesDTO> findByStatusActiveOrUnActive() {
        return healthFacilitiesMapper.toDto(healthFacilitiesRepository.findByStatusActiveOrUnActive());
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete HealthFacility : {}", id);
        // set status delete for config field - value of HealthFacility
        categoryConfigFieldService.deleteAll(categoryConfigFieldService.findAllByHealthFacilityIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE).stream().map(CategoryConfigFieldDTO::getId).collect(Collectors.toList()));
        healthFacilitiesRepository.findById(id).ifPresent(healthFacility -> healthFacility.setStatus(Constants.ENTITY_STATUS.DELETED));
    }

    @Override
    public List<HealthFacilitiesDTO> findAllChildrenByParentId(Long parent) {
        return healthFacilitiesMapper.toDto(healthFacilitiesRepository.findAllChildrenByParent(parent));
    }

    @Override
    public List<HealthFacilitiesDTO> findByStatus(List<Integer> status) {
        return healthFacilitiesMapper.toDto(healthFacilitiesRepository.findByStatus(status));
    }

    @Override
    public HealthFacilitiesDTO findById(Long id) {
        Optional<HealthFacilities> optional = healthFacilitiesRepository.findById(id);
        return optional.map(healthFacilitiesMapper::toDto).orElse(null);
    }

    @Override
    public Boolean checkExistCode(String code) {
        return healthFacilitiesRepository.existsByCodeAndStatusGreaterThan(code, Constants.ENTITY_STATUS.DELETED);
    }

    @Override
    public List<HealthFacilitiesDTO> findAllHealthFacilities(Integer appointmentOption) {
        List<HealthFacilitiesDTO> list = healthFacilitiesMapper.toDto(healthFacilitiesRepository.findAllByParentCodeAndStatus(Constants.SYT_DEFAULT.code, Constants.ENTITY_STATUS.ACTIVE));
        for (HealthFacilitiesDTO healthFacilitiesDTO : list) {
            Optional<DoctorAppointmentConfiguration> config =
                    doctorAppointmentConfigurationRepository.findByHealthFacilitiesIdAndStatus(healthFacilitiesDTO.getId(), Constants.ENTITY_STATUS.ACTIVE);
            if (config.isPresent()) {
                healthFacilitiesDTO.setConfig(config.map(doctorAppointmentConfigurationMapper::toDto).get());
            } else {
                // Neu chua co config thi mac dinh day lay config default
                Optional<DoctorAppointmentConfiguration> configDefault = doctorAppointmentConfigurationRepository.findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
                if (configDefault.isPresent()) {
                    healthFacilitiesDTO.setConfig(configDefault.map(doctorAppointmentConfigurationMapper::toDto).get());
                }
            }
        }
        if (appointmentOption == Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE) {
            list = list.stream()
                    .filter(x -> x.getConfig().getAppointmentDaily() == Constants.DOCTOR_APPOINTMENT_TYPE.TYPE_ACTIVE)
                    .collect(Collectors.toList());
        } else if (appointmentOption == Constants.DOCTOR_APPOINTMENT_TYPE.BY_DOCTOR) {
            list = list.stream()
                    .filter(x -> x.getConfig().getAppointmentDoctor() == Constants.DOCTOR_APPOINTMENT_TYPE.TYPE_ACTIVE)
                    .collect(Collectors.toList());
        } else if (appointmentOption == Constants.DOCTOR_APPOINTMENT_TYPE.BOTH) {
            list = list.stream()
                    .filter(x -> x.getConfig().getAppointmentDoctor() == Constants.DOCTOR_APPOINTMENT_TYPE.TYPE_ACTIVE && x.getConfig().getAppointmentDaily() == Constants.DOCTOR_APPOINTMENT_TYPE.TYPE_ACTIVE)
                    .collect(Collectors.toList());
        }
        return list;
    }

    // api lấy list cơ sở y tế đã phân quyền
    @Override
    public List<HealthFacilitiesDTO> findAllHealthFacilitiesByUser(Integer appointmentOption, User user) {
        List<HealthFacilitiesDTO> list = healthFacilitiesMapper.toDto(healthFacilitiesRepository.findAllByParentCodeAndStatus(Constants.SYT_DEFAULT.code, Constants.ENTITY_STATUS.ACTIVE));

        // check if user has role super admin
        if(user.getAuthorities().stream().filter(item -> item.getName().equals(AuthoritiesConstants.SUPER_ADMIN)).collect(Collectors.toList()).size() == 0){
            list = list.stream().filter(item -> item.getId().equals(user.getHealthFacilityId()))
                    .collect(Collectors.toList());
            if(list.size() == 0){
                Optional<HealthFacilities> optionalHealthFacilities = healthFacilitiesRepository.findByCode(Constants.SYT_DEFAULT.code);
                list.add(healthFacilitiesMapper.toDto(optionalHealthFacilities.get()));
            }
        }
        return list;
    }

    @Override
    public List<HealthFacilitiesDTO> findByParentId(Long parentId) {
        List<HealthFacilities> result = healthFacilitiesRepository.findByParentAndStatus(parentId, Constants.ENTITY_STATUS.ACTIVE);
        return healthFacilitiesMapper.toDto(result);

    }

    @Override
    public Map<String, Object> handleListToTree(List<HealthFacilitiesDTO> list, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        if (list.isEmpty()) {
            result.put("data", list);
            result.put("total", 0);
            return result;
        }
        String name = params.getOrDefault("name", null);
        Integer status = (params.containsKey("status") && !params.get("status").equals("9")) ? Integer.parseInt(params.get("status")) : null;
        Map<Long, HealthFacilitiesDTO> mapId2Position = list.stream().collect(Collectors.toMap(HealthFacilitiesDTO::getId, Functions.identity()));
        Map<Long, HealthFacilitiesDTO> res = new HashMap<>();

        list.stream().filter(p -> filterByNameAndStatusAndAddress(p, name, status))
                .forEach(p -> {
                    HealthFacilitiesDTO root = parentMapping(p, mapId2Position);
                    if (!res.containsKey(root.getId())) {
                        res.put(root.getId(), root);
                    }
                });
        int totalRecords = res.values()
                .stream()
                .map(p -> countTreeByStatus(p, status))
                .reduce(Integer::sum)
                .orElse(0);
        result.put("total", totalRecords);
        List<HealthFacilitiesDTO> list1 = Lists.newArrayList(res.values());
        list1.sort(Comparator.comparing(HealthFacilitiesDTO::getStatus).thenComparing(HealthFacilitiesDTO::getLastModifiedDate, Comparator.reverseOrder()));
        result.put("data", list1);
        return result;
    }

    private boolean filterByNameAndStatusAndAddress(HealthFacilitiesDTO h, String keyword, Integer status) {
        if (status != null) {
            return (keyword == null
                    || Utils.removeAccent(h.getName().toLowerCase()).contains(Utils.removeAccent(keyword.trim().toLowerCase()))
                    || Utils.removeAccent(h.getAddress().toLowerCase()).contains(Utils.removeAccent(keyword.trim().toLowerCase())))
                    && h.getStatus().equals(status);
        }
        return (keyword == null
                || Utils.removeAccent(h.getName().toLowerCase()).contains(Utils.removeAccent(keyword.trim().toLowerCase()))
                || Utils.removeAccent(h.getAddress().toLowerCase()).contains(Utils.removeAccent(keyword.trim().toLowerCase())));
    }

    private int countTreeByStatus(HealthFacilitiesDTO root, Integer status) {
        int count = 0;

        if (status != null) {
            if (root.getStatus().equals(status)) {
                count = 1;
            }

            for (HealthFacilitiesDTO p : root.getChildren()) {
                count += countTreeByStatus(p, status);
            }
        } else {
            count = 1;
            for (HealthFacilitiesDTO p : root.getChildren()) {
                count += countTreeByStatus(p, status);
            }
        }
        return count;
    }

    private HealthFacilitiesDTO parentMapping(HealthFacilitiesDTO current, Map<Long, HealthFacilitiesDTO> mapId2Position) {
        if (current.getParent() != null) {
            HealthFacilitiesDTO parent = mapId2Position.get(current.getParent());
            if (!parent.getChildren().contains(current)) {
                parent.getChildren().add(current);
            }
            return parentMapping(parent, mapId2Position);
        }
        return current;
    }

    @Override
    public List<HealthFacilitiesHistoryDTO> getHealthFacilitiesHistoryById(Long id) {
        List<HealthFacilitiesHistoryDTO> listHFHistory = new ArrayList<>();
        List<ActivityLog> listActivityLog = activityLogRepository.findByContentIdAndContentTypeOrderByCreatedDateDesc(id, Constants.CONTENT_TYPE.HEALTH_FACILITY);
        for (ActivityLog activityLog : listActivityLog) {
            HealthFacilitiesHistoryDTO history = new HealthFacilitiesHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            List<String> newContentList = new ArrayList<>();
            List<String> oldContentList = new ArrayList<>();
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                newContentList.add("Thêm mới");
                history.setNewContents(newContentList);
                listHFHistory.add(history);
                continue;
            } else if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                HealthFacilities oldH = convertToHealthFacilities(activityLog.getOldContent());
                HealthFacilities newH = convertToHealthFacilities(activityLog.getContent());
                createContentList(oldH, newH, oldContentList, newContentList);
            }
            if (oldContentList.size() == 0 && newContentList.size() == 0) {
                continue;
            }
            history.setOldContents(oldContentList);
            history.setNewContents(newContentList);
            listHFHistory.add(history);
        }
        return listHFHistory;
    }

    @Override
    public List<HealthFacilitiesDTO> getHealthFacilitiesParentAndThis(Long id) {
        List<HealthFacilitiesDTO> list = new ArrayList<>();
        Optional<HealthFacilitiesDTO> child = healthFacilitiesRepository.findById(id).map(healthFacilitiesMapper::toDto);
        if (child.isPresent()) {
            Long parentId = child.get().getParent();
            if (parentId != null) {
                Optional<HealthFacilitiesDTO> parent = healthFacilitiesRepository.findById(parentId).map(healthFacilitiesMapper::toDto);
                parent.ifPresent(list::add);
            }
            list.add(child.get());
        }
        return list;
    }

    private HealthFacilities convertToHealthFacilities(String input) {
        if (input.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(input, HealthFacilities.class);
    }

    private void createContentList(HealthFacilities oldH, HealthFacilities newH, List<String> oldContentList, List<String> newContentList) {
        if (oldH == null) {
            return;
        }
        if (oldH.getName() != null && !oldH.getName().equals(newH.getName())) {
            oldContentList.add("Tên đơn vị: " + oldH.getName());
            newContentList.add("Tên đơn vị: " + newH.getName());
        }
        if (oldH.getCode() != null && !oldH.getCode().equals(newH.getCode())) {
            oldContentList.add("Mã đơn vị: " + oldH.getCode());
            newContentList.add("Mã đơn vị: " + newH.getCode());
        }
        if (!(oldH.getParentCode() == null && newH.getParentCode() == null)) {
            if (oldH.getParentCode() == null && newH.getParentCode() != null) {
                newContentList.add("Mã chức vụ cha: " + newH.getParentCode());
            }
            if (oldH.getParentCode() != null && newH.getParentCode() == null) {
                oldContentList.add("Mã chức vụ cha: " + oldH.getParentCode());
            }
            if (oldH.getParentCode() != null && !oldH.getParentCode().equals(newH.getParentCode())) {
                oldContentList.add("Mã chức vụ cha: " + oldH.getParentCode());
                newContentList.add("Mã chức vụ cha: " + newH.getParentCode());
            }
        }
        if (!(oldH.getPhone() == null && newH.getPhone() == null)) {
            if (oldH.getPhone() == null && newH.getPhone() != null) {
                newContentList.add("Số điện thoại: " + newH.getPhone());
            } else if (oldH.getPhone() != null && newH.getPhone() == null) {
                oldContentList.add("Số điện thoại: " + oldH.getPhone());
            } else if (!oldH.getPhone().equals(newH.getPhone())) {
                oldContentList.add("Số điện thoại: " + oldH.getPhone());
                newContentList.add("Số điện thoại: " + newH.getPhone());
            }
        }
        if (!(oldH.getFax() == null && newH.getFax() == null)) {
            if (oldH.getFax() == null && newH.getFax() != null) {
                newContentList.add("Số Fax: " + newH.getFax());
            } else if (oldH.getFax() != null && newH.getFax() == null) {
                oldContentList.add("Số Fax: " + oldH.getFax());
            } else if (!oldH.getFax().equals(newH.getFax())) {
                oldContentList.add("Số Fax: " + oldH.getFax());
                newContentList.add("Số Fax: " + newH.getFax());
            }
        }
        if (!(oldH.getEmail() == null && newH.getEmail() == null)) {
            if (oldH.getEmail() == null && newH.getEmail() != null) {
                newContentList.add("Email: " + newH.getEmail());
            } else if (oldH.getEmail() != null && newH.getEmail() == null) {
                oldContentList.add("Email: " + oldH.getEmail());
            } else if (!oldH.getEmail().equals(newH.getEmail())) {
                oldContentList.add("Email: " + oldH.getEmail());
                newContentList.add("Email: " + newH.getEmail());
            }
        }
        if (!(oldH.getManager() == null && newH.getManager() == null)) {
            if (oldH.getManager() == null && newH.getManager() != null) {
                newContentList.add("Giám đốc cơ sở: " + newH.getManager());
            } else if (oldH.getManager() != null && newH.getManager() == null) {
                oldContentList.add("Giám đốc cơ sở: " + oldH.getManager());
            } else if (!oldH.getManager().equals(newH.getManager())) {
                oldContentList.add("Giám đốc cơ sở: " + oldH.getManager());
                newContentList.add("Giám đốc cơ sở: " + newH.getManager());
            }
        }
        if (oldH.getAddress() != null && !oldH.getAddress().equals(newH.getAddress())) {
            oldContentList.add("Địa chỉ: " + oldH.getAddress());
            newContentList.add("Địa chỉ: " + newH.getAddress());
        }
        if (oldH.getStatus() != null && !oldH.getStatus().equals(newH.getStatus())) {
            oldContentList.add("Trạng thái: " +
                    (oldH.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
            newContentList.add("Trạng thái: " +
                    (newH.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
        }
    }

    @Override
    public List<HealthFacilitiesDTO> excelToHealthFacilities(InputStream inputStream, List<ErrorExcel> details) {
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(SHEET_HEALTH_FACILITIES);
            if (Objects.isNull(sheet) || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestAlertException("format template file invalid", "", "excel.formatTemplate");
            }
            List<HealthFacilitiesDTO> list = new ArrayList<>();
            int rowNumber = 0;
            int index = 1;
            boolean pass = true;
            Set<String> setCodes = new HashSet<>();
            List<ErrorExcel> excelList = new ArrayList<>();
            for (Row row : sheet) {
                // skip header
                if (rowNumber < 8) {
                    rowNumber++;
                    continue;
                }
                HealthFacilitiesDTO healthFacilitiesDTO = new HealthFacilitiesDTO();
                for (Cell currentCell : row) {
                    switch (currentCell.getColumnIndex()) {
                        case 0:
                            if (currentCell.getCellType() == CellType.STRING && !StrUtil.isBlank(currentCell.getStringCellValue())) {
                                String tempParentCode = currentCell.getStringCellValue().trim();
                                if (!healthFacilitiesRepository.existsByCodeAndStatusGreaterThan(tempParentCode, Constants.ENTITY_STATUS.DELETED)) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.parentCodeIsNotExisted", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    healthFacilitiesDTO.setParentCode(tempParentCode);
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                String tempParentCode = String.valueOf(currentCell.getNumericCellValue()).trim();
                                if (!healthFacilitiesRepository.existsByCodeAndStatusGreaterThan(tempParentCode, Constants.ENTITY_STATUS.DELETED)) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.parentCodeIsNotExisted", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    healthFacilitiesDTO.setParentCode(tempParentCode);
                                }
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("health_facility.parentCodeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 1:
                            if (currentCell.getCellType() == CellType.STRING && !StrUtil.isBlank(currentCell.getStringCellValue())) {
                                String tempCode = currentCell.getStringCellValue().trim();
                                if (healthFacilitiesRepository.existsByCodeAndStatusGreaterThan(tempCode, Constants.ENTITY_STATUS.DELETED)) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    healthFacilitiesDTO.setCode(tempCode);
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                String tempCode = String.valueOf(currentCell.getNumericCellValue());
                                if (healthFacilitiesRepository.existsByCodeAndStatusGreaterThan(tempCode, Constants.ENTITY_STATUS.DELETED)) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    healthFacilitiesDTO.setCode(tempCode);
                                }
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("health_facility.codeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 2:
                            if (currentCell.getCellType() == CellType.STRING && !StrUtil.isBlank(currentCell.getStringCellValue())) {
                                healthFacilitiesDTO.setName(currentCell.getStringCellValue().trim());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                healthFacilitiesDTO.setName(String.valueOf(currentCell.getNumericCellValue()).trim());
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("health_facility.nameNotSuitable", mapError);
                                excelList.add(errorExcels);;
                            }
                            break;
                        case 3:
                            if (currentCell.getCellType() == CellType.STRING && !StrUtil.isBlank(currentCell.getStringCellValue())) {
                                healthFacilitiesDTO.setAddress(currentCell.getStringCellValue().trim());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                healthFacilitiesDTO.setAddress(String.valueOf(currentCell.getNumericCellValue()).trim());
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("health_facility.addressNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 4:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                int status = (int) currentCell.getNumericCellValue();
                                if (status != Constants.ENTITY_STATUS.ACTIVE && status != Constants.ENTITY_STATUS.DEACTIVATE) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.statusIs1or2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    healthFacilitiesDTO.setStatus(status);
                                }
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                try {
                                    int status = Integer.parseInt(currentCell.getStringCellValue());
                                    if (status != Constants.ENTITY_STATUS.ACTIVE && status != Constants.ENTITY_STATUS.DEACTIVATE) {
                                        pass = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("health_facility.statusIs1or2", mapError);
                                        excelList.add(errorExcels);
                                    } else {
                                        healthFacilitiesDTO.setStatus(status);
                                    }
                                }catch (NumberFormatException numberFormatException) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("health_facility.statusNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("health_facility.statusNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (healthFacilitiesDTO.getStatus() != null || healthFacilitiesDTO.getName() != null || healthFacilitiesDTO.getAddress() != null || healthFacilitiesDTO.getParentCode() != null || healthFacilitiesDTO.getCode() != null) {
                    if (healthFacilitiesDTO.getName() == null) {
                        pass = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("health_facility.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (healthFacilitiesDTO.getAddress() == null) {
                        pass = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("health_facility.addressIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (healthFacilitiesDTO.getStatus() == null) {
                        pass = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("health_facility.statusMustIs1or2", mapError);
                        excelList.add(errorExcels);
                    }
                }
                if (pass) {
                    list.add(healthFacilitiesDTO);
                }
                if (healthFacilitiesDTO.getParentCode() == null && healthFacilitiesDTO.getName() == null && healthFacilitiesDTO.getCode() == null &&
                        healthFacilitiesDTO.getAddress() == null && healthFacilitiesDTO.getStatus() == null) {
                    excelList.clear();
                } else {
                    details.addAll(excelList);
                    excelList.clear();
                }
                index++;
            }
            // Check kiem tra ma don vi phai la duy nhat
            List<HealthFacilitiesDTO> listCodeNotNull = list.stream().filter(item -> item.getCode() != null).collect(Collectors.toList());
            listCodeNotNull.forEach(item -> setCodes.add(item.getCode().toUpperCase()));
            if(setCodes.size() != listCodeNotNull.size()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", String.valueOf(index));
                ErrorExcel errorExcels = new ErrorExcel("health_facility.codeIsOnlyOne", mapError);
                details.add(errorExcels);
            }
            workbook.close();
            if (list.isEmpty() && details.isEmpty()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", "");
                ErrorExcel errorExcels = new ErrorExcel("fileIsBlank", mapError);
                details.add(errorExcels);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResultExcel bulkUploadHealthFacilities(List<HealthFacilitiesDTO> list) {
        return null;
    }

    String generateHealthFacilitiesCode(String code, String name, boolean checkName) {
        String newCode;
        String generateCode;
        int count = 0;
        Optional<HealthFacilities> hfOpt;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCodeForHealthFacilities(name.trim());
            while (true) {
                if (count >= 1) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                hfOpt = healthFacilitiesRepository.findByCodeAndStatusGreaterThan(newCode, Constants.ENTITY_STATUS.DELETED);
                if (!hfOpt.isPresent()) {
                    break;
                }
                count++;
            }

        } else {
            newCode = code.trim();
            hfOpt = healthFacilitiesRepository.findByCodeAndStatusGreaterThan(newCode, Constants.ENTITY_STATUS.DELETED);
            if (hfOpt.isPresent()) {
                throw new BadRequestAlertException("Code already exist", "health facilities", "code exist");
            }
        }
        return newCode;
    }

    @Override
    public List<HealthFacilitiesDTO> findAllByParentAndStatusGreaterThan(Long parentId, Integer status) {
        return healthFacilitiesMapper.toDto(healthFacilitiesRepository.findAllByParentAndStatusGreaterThan(parentId, status));
    }

    @Override
    public boolean checkExitsByIdAndStatus(Long id, Integer status) {
        return healthFacilitiesRepository.existsByIdAndStatus(id, status);
    }

    public HealthFacilitiesDTO findByCodeAndStatusGreaterThan(String code, Integer status) {
        Optional<HealthFacilities> healthFacilities = healthFacilitiesRepository.findByCodeAndStatusGreaterThan(code, status);
        return healthFacilities.map(healthFacilitiesMapper::toDto).orElse(null);
    }
}
