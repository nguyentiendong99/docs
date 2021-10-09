package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.Position;
import com.bkav.lk.dto.PositionDTO;
import com.bkav.lk.dto.PositionHistoryDTO;
import com.bkav.lk.repository.PositionRepository;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.PositionService;
import com.bkav.lk.service.mapper.PositionMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.util.Lists;
import com.google.common.base.Functions;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    Gson gson = new Gson();

    private final Logger log = LoggerFactory.getLogger(PositionService.class);

    private final PositionRepository positionRepository;

    private final PositionMapper positionMapper;

    private final ActivityLogService activityLogService;

    private static final String ENTITY_NAME = "position";

    public PositionServiceImpl(PositionRepository positionRepository, ActivityLogService activityLogService, PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
        this.activityLogService = activityLogService;
    }

    @Override
    public Page<PositionDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("Request to search for a page of positions with multi query {}", queryParams);
        if (queryParams.containsKey("pageable")) {
            pageable = null;
        }
        List<PositionDTO> positionDTOS = positionMapper.toDto(positionRepository.search(queryParams, pageable));
        if (pageable == null) {
            return new PageImpl<>(positionDTOS);
        }
        return new PageImpl<>(positionDTOS, pageable, positionRepository.count(queryParams));
    }

    @Override
    public Optional<PositionDTO> findOne(Long id) {
        log.debug("Request to get position : {}", id);
        return positionRepository.findById(id).map(positionMapper::toDto);
    }

    @Override
    public Position findById(Long id) {
        return positionRepository.findByIdAndStatus(id, 1);
    }

    @Override
    public PositionDTO save(PositionDTO positionDTO) {
        log.debug("Request to save position : {}", positionDTO);
        if (positionDTO.getName() != null) {
            if (StringUtils.isEmpty(positionDTO.getId())) {
                positionDTO.setCode(generatePositionCode(positionDTO.getCode(), positionDTO.getName(), false));

            } else {
                Optional<Position> position = positionRepository.findById(positionDTO.getId());
                if (positionDTO.getCode() == null) {
                    positionDTO.setCode(position.get().getCode());
                }
                if ((!position.get().getCode().equals(positionDTO.getCode()) || !position.get().getName().equals(positionDTO.getName())) && positionDTO.getCode() != null) {
                    positionDTO.setCode(generatePositionCode(positionDTO.getCode(), positionDTO.getName(), !position.get().getName().equals(positionDTO.getName())));
                }
            }
        }
        Position position = positionMapper.toEntity(positionDTO);
        position = positionRepository.save(position);
        return positionMapper.toDto(position);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete position : {}", id);
        positionRepository.findById(id).ifPresent(item -> {
            item.setStatus(Constants.ENTITY_STATUS.DELETED);
            log.debug("Deleted position id = {}", id);
        });
    }

    @Override
    public boolean existCode(String code) {
        return positionRepository.existsByCode(code.toUpperCase());
    }

    @Override
    public Map<String, Object> handleTreePosition(List<PositionDTO> list, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        if (list.isEmpty()) {
            result.put("data", list);
            result.put("total", list.size());
            return result;
        }
        String name = params.getOrDefault("name", null);
        Integer status = (params.containsKey("status") && !params.get("status").equals("")) ? Integer.parseInt(params.get("status")) : null;

        Map<Long, PositionDTO> mapId2Position = list.stream().collect(Collectors.toMap(PositionDTO::getId, Functions.identity()));
        Map<Long, PositionDTO> res = new LinkedHashMap<>();

        list.stream()
                .filter(p -> filterByNameAndStatus(p, name, status))
                .forEach(p -> {
                    PositionDTO root = parentMapping(p, mapId2Position);

                    if (!res.containsKey(root.getId())) {
                        res.put(root.getId(), root);
                    }
                });

        int totalRecords = res.values()
                .stream()
                .map(p -> countTreeByStatus(p, status))
                .reduce(Integer::sum)
                .orElse(0);

        List<PositionDTO> data = Lists.newArrayList(res.values());
        data.sort(Comparator.comparing(PositionDTO::getStatus).thenComparing(PositionDTO::getLastModifiedDate, Comparator.reverseOrder()));
        result.put("data", data);
        result.put("total", totalRecords);

        return result;
    }

    private boolean filterByNameAndStatus(PositionDTO p, String name, Integer status) {
        if (status != null) {
            return (name == null
                    || Utils.removeAccent(p.getName().toLowerCase()).contains(Utils.removeAccent(name.trim().toLowerCase())))
                    && p.getStatus().equals(status);
        }
        return (name == null || Utils.removeAccent(p.getName().toLowerCase()).contains(Utils.removeAccent(name.trim().toLowerCase())));
    }

    private int countTreeByStatus(PositionDTO root, Integer status) {
        int count = 0;

        if (status != null) {
            if (root.getStatus().equals(status)) {
                count = 1;
            }

            for (PositionDTO p : root.getChildren()) {
                count += countTreeByStatus(p, status);
            }
        } else {
            count = 1;
            for (PositionDTO p : root.getChildren()) {
                count += countTreeByStatus(p, status);
            }
        }

        return count;
    }

    private PositionDTO parentMapping(PositionDTO current, Map<Long, PositionDTO> mapId2Position) {
        if (current.getParentId() != null) {
            PositionDTO parent = mapId2Position.get(current.getParentId());
            parent.getChildren().add(current);
            return parentMapping(parent, mapId2Position);
        }
        return current;
    }


    @Override
    public List<PositionDTO> findAll() {
        return positionMapper.toDto(positionRepository.findAllOrderByLastModifiedDateDesc());
    }

    @Override
    public List<PositionDTO> findAllChildrenByParentId(Long parentId) {
        return positionMapper.toDto(positionRepository.findAllChildrenByParentId(parentId));
    }

    @Override
    public List<PositionDTO> excelToPositions(InputStream inputStream, List<ErrorExcel> errorDetails) {
        List<PositionDTO> positionDTOS = new ArrayList<>();
        PositionDTO positionDTO;
        int index = 1;
        String tempCode;
        String tempParentCode;
        int tempStatus;
        boolean correctData;
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("Position");
            if (Objects.isNull(sheet) || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestAlertException("format template file invalid", ENTITY_NAME, "excel.formatTemplate");
            }

            int rowNumber = 0;
            List<ErrorExcel> excelList = new ArrayList<>();
            for (Row row : sheet) {
                // skip header
                if (rowNumber < 9) {
                    rowNumber++;
                    continue;
                }
                positionDTO = new PositionDTO();
                correctData = true;
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        case 0:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                tempParentCode = cell.getRichStringCellValue().getString().trim();
                                if (!this.isPositionCodeExist((cell.getDateCellValue()).toString())) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.parentCodeNotExist", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    PositionDTO result = this.findByCode(tempParentCode);
                                    positionDTO.setParentId(result.getId());
                                }
                            } else if (cell.getCellType() == CellType.STRING && cell.getStringCellValue() != null) {
                                tempParentCode = cell.getStringCellValue();
                                if (!this.isPositionCodeExist(cell.getStringCellValue())) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.parentCodeNotExist", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    PositionDTO result = this.findByCode(tempParentCode);
                                    positionDTO.setParentId(result.getId());
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("position.parentCodeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 1:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                tempCode = cell.getRichStringCellValue().getString().trim();
                                if (this.isPositionCodeExist((cell.getDateCellValue()).toString())) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    positionDTO.setCode(tempCode);
                                }
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempCode = cell.getStringCellValue().trim();
                                if (this.isPositionCodeExist(cell.getStringCellValue())) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    positionDTO.setCode(tempCode);
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("position.codeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                positionDTO.setName((String.valueOf(cell.getNumericCellValue()).trim()));
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                positionDTO.setName(cell.getStringCellValue().trim());
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("position.nameNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                tempStatus = (int) cell.getNumericCellValue();
                                if (tempStatus >= Constants.ENTITY_STATUS.DELETED && tempStatus <= Constants.ENTITY_STATUS.DEACTIVATE) {
                                    positionDTO.setStatus(tempStatus);
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("position.statusNotExist", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("position.statusNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        default:
                            break;
                    }
                }
                // chỉ có tên và status là trường bắt buộc phải nhập nhưng nếu 2 trường đồng thời null nên check full trường
                if (positionDTO.getName() != null || positionDTO.getStatus() != null || positionDTO.getCode() != null || positionDTO.getParentId() != null) {
                    if (correctData && this.isEmpty(positionDTO)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("position.lackOfDataField", mapError);
                        excelList.add(errorExcels);
                    }
                    if (positionDTO.getName() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("position.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (positionDTO.getStatus() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("position.statusIsBlank", mapError);
                        excelList.add(errorExcels);
                    }

                    if (correctData && this.isDuplicateCode(positionDTO.getCode(), positionDTOS)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("position.codeIsDuplicated", mapError);
                        excelList.add(errorExcels);
                    }
                }
                if (positionDTO.getCode() == null && positionDTO.getName() == null && positionDTO.getParentId() == null &&
                        positionDTO.getStatus() == null) {
                    excelList.clear();
                } else {
                    errorDetails.addAll(excelList);
                    excelList.clear();
                }

                if (correctData) {
                    positionDTOS.add(positionDTO);
                }
                index++;
            }
            workbook.close();
            if (positionDTOS.isEmpty() && errorDetails.isEmpty()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", "");
                ErrorExcel errorExcels = new ErrorExcel("fileIsBlank", mapError);
                errorDetails.add(errorExcels);
            }
            return positionDTOS;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }


    private PositionDTO convert(PositionDTO item) {
        PositionDTO positionDTO = new PositionDTO();
        positionDTO.setId(item.getId());
        positionDTO.setCode(item.getCode());
        positionDTO.setName(item.getName());
        positionDTO.setDescription(item.getDescription());
        positionDTO.setParentId(item.getParentId());
        positionDTO.setStatus(item.getStatus());
        positionDTO.setCreatedBy(item.getCreatedBy());
        positionDTO.setCreatedDate(item.getCreatedDate());
        positionDTO.setLastModifiedBy(item.getLastModifiedBy());
        positionDTO.setLastModifiedDate(item.getLastModifiedDate());
        return positionDTO;
    }

    @Override
    public List<PositionHistoryDTO> getPositionHistory(MultiValueMap<String, String> queryParam) {
        List<ActivityLog> activityLogs = activityLogService.search(queryParam);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<PositionHistoryDTO> positionHistoryDTOList = new ArrayList<>();


        for (ActivityLog activityLog : activityLogs) {
            PositionHistoryDTO history = new PositionHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            List<String> newContentList = new ArrayList<>();
            List<String> oldContentList = new ArrayList<>();
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                newContentList.add("Thêm mới");
                history.setNewContents(newContentList);
                positionHistoryDTOList.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                Position oldP = convertToPosition(activityLog.getOldContent());
                Position newP = convertToPosition(activityLog.getContent());
                createContentList(oldP, newP, oldContentList, newContentList);
            }
            if (oldContentList.size() == 0 || newContentList.size() == 0) {
                continue;
            }
            history.setOldContents(oldContentList);
            history.setNewContents(newContentList);
            positionHistoryDTOList.add(history);
        }

        return positionHistoryDTOList;
    }

    @Override
    public List<PositionDTO> findAllActiveStatus() {
        return positionMapper.toDto(positionRepository.findByStatus(Constants.ENTITY_STATUS.ACTIVE));
    }


    private Position convertToPosition(String input) {
        if (input.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(input, Position.class);
    }

    private void createContentList(Position oldP, Position newP, List<String> oldContentList, List<String> newContentList) {
        if (oldP == null) {
            return;
        }
        if (!oldP.getName().equals(newP.getName())) {
            oldContentList.add("Tên chức vụ: " + oldP.getName());
            newContentList.add("Tên chức vụ: " + newP.getName());
        }
        if (!oldP.getCode().equals(newP.getCode())) {
            oldContentList.add("Mã chức vụ: " + oldP.getCode());
            newContentList.add("Mã chức vụ: " + newP.getCode());
        }
        if (!(oldP.getParentId() == null && newP.getParentId() == null)) {
            if (oldP.getParentId() == null && newP.getParentId() != null) {
                newContentList.add("Mã chức vụ cha: " + newP.getParentId());
            }

            if (oldP.getParentId() != null && newP.getParentId() == null) {
                oldContentList.add("Mã chức vụ cha: " + oldP.getParentId());
            }

            if (oldP.getParentId() != null && !oldP.getParentId().equals(newP.getParentId())) {
                oldContentList.add("Mã chức vụ cha: " + oldP.getParentId());
                newContentList.add("Mã chức vụ cha: " + newP.getParentId());
            }
        }

        if (!oldP.getStatus().equals(newP.getStatus())) {
            oldContentList.add("Trạng thái: " +
                    (oldP.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
            newContentList.add("Trạng thái: " +
                    (newP.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
        }
    }

    @Override
    public String generatePositionCode(String code, String name, boolean checkName) {
        int count = 0;
        Position position;
        String generateCode;
        String newCode;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCode(name);
            while (true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                position = positionRepository.findByCodeAndStatusIsNot(newCode, Constants.ENTITY_STATUS.DELETED)
                        .orElse(null);

                if (Objects.isNull(position)) {
                    break;
                }
                count++;
            }
            return newCode;
        }
        newCode = code.trim();
        position = positionRepository.findByCodeAndStatusIsNot(code, Constants.ENTITY_STATUS.DELETED)
                .orElse(null);
        if (Objects.nonNull(position)) {
            return null;

        }
        return newCode;
    }

    public PositionDTO findByCode(String code) {
        return positionMapper.toDto(positionRepository.findByCodeAndStatusIsNot(code, Constants.ENTITY_STATUS.DELETED).orElse(null));
    }

    public boolean isPositionCodeExist(String code) {
        PositionDTO result = null;
        try {
            result = this.findByCode(code);
        } catch (BadRequestAlertException ex) {
            log.error(ex.getMessage());
        }
        return result != null;
    }

    private boolean isEmpty(PositionDTO positionDTO) {
        boolean isEmpty = false;
        for (Field field : positionDTO.getClass().getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase("name") || field.getName().equalsIgnoreCase("status")) {
                field.setAccessible(true);
                try {
                    if (field.get(positionDTO) == null) {
                        isEmpty = true;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return isEmpty;
    }


    private boolean isDuplicateCode(String code, List<PositionDTO> positionDTOS) {
        boolean isDuplicate = false;
        if (!StringUtils.isEmpty(code)) {
            for (PositionDTO positionDTO : positionDTOS) {
                if (code.trim().equalsIgnoreCase(positionDTO.getCode().trim())) {
                    isDuplicate = true;
                    break;
                }
            }
        }
        return isDuplicate;
    }
}
