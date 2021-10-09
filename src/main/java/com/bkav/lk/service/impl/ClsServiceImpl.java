package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Cls;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.ClsRepository;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigValueService;
import com.bkav.lk.service.ClsService;
import com.bkav.lk.service.mapper.ClsMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClsServiceImpl implements ClsService {
    private static final Logger log = LoggerFactory.getLogger(ClsService.class);

    private final ClsRepository clsRepository;

    private final ClsMapper clsMapper;

    private final CategoryConfigValueService categoryConfigValueService;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private static final String ENTITY_NAME = "cls";

    public ClsServiceImpl(ClsRepository clsRepository, ClsMapper clsMapper, CategoryConfigValueService categoryConfigValueService, CategoryConfigFieldService categoryConfigFieldService) {
        this.clsRepository = clsRepository;
        this.clsMapper = clsMapper;
        this.categoryConfigValueService = categoryConfigValueService;
        this.categoryConfigFieldService = categoryConfigFieldService;
    }

    @Override
    public ClsDTO save(ClsDTO clsDTO) {
        if (clsDTO.getClsName() != null && clsDTO.getClsPrice() != null) {
            if (StringUtils.isEmpty(clsDTO.getId())) {
                clsDTO.setClsCode(generateClsCode(clsDTO.getClsCode(), clsDTO.getClsName(), false));

            } else {
                Optional<Cls> cls2 = clsRepository.findById(clsDTO.getId());
                if (!cls2.get().getClsCode().equals(clsDTO.getClsCode()) || !cls2.get().getClsName().equals(clsDTO.getClsName())) {
                    clsDTO.setClsCode(generateClsCode(clsDTO.getClsCode(), clsDTO.getClsName(), !cls2.get().getClsName().equals(clsDTO.getClsName())));
                }

                if (!CollectionUtils.isEmpty(clsDTO.getClsCustomConfigDTOS())) {
                    List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
                    List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
                    clsDTO.getClsCustomConfigDTOS().forEach(clsCustomConfigDTO -> {
                        CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(cls2.get().getId(), clsCustomConfigDTO.getFieldId());
                        configValueDTO.setValue(clsCustomConfigDTO.getValue());
                        if (configValueDTO.getFieldId() != null) {
                            configValueUpdateDTOS.add(configValueDTO);
                        } else {
                            configValueDTO.setObjectId(cls2.get().getId());
                            configValueDTO.setFieldId(clsCustomConfigDTO.getFieldId());
                            configValueDTO.setValue(clsCustomConfigDTO.getValue());
                            configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                            configValueCreateDTOS.add(configValueDTO);
                        }
                    });
                    if (configValueUpdateDTOS.size() > 0) {
                        categoryConfigValueService.updateAll(configValueUpdateDTOS);
                    }
                    if (configValueCreateDTOS.size() > 0) {
                        categoryConfigValueService.createAll(configValueCreateDTOS);
                    }
                }
            }
        }
        Cls cls = clsRepository.save(clsMapper.toEntity(clsDTO));
        if (StringUtils.isEmpty(clsDTO.getId())) {
            if (!CollectionUtils.isEmpty(clsDTO.getClsCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

                clsDTO.getClsCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(doctorCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                    configValueDTO.setValue(doctorCustomConfigDTO.getValue());
                    configValueDTO.setFieldId(doctorCustomConfigDTO.getFieldId());
                    configValueDTO.setObjectId(cls.getId());
                    configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    configValueDTOS.add(configValueDTO);
                });
                categoryConfigValueService.createAll(configValueDTOS);
            }
        }
        return clsMapper.toDto(cls);
    }

    @Override
    public Page<ClsDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        if (queryParams.containsKey("pageable")) {
            pageable = null;
        }
        List<ClsDTO> clsDTOList = clsMapper.toDto(clsRepository.search(queryParams, pageable));
        if (pageable == null) {
            return new PageImpl<>(clsDTOList);
        }
        return new PageImpl<>(clsDTOList, pageable, clsRepository.count(queryParams));
    }

    @Override
    public void delete(Long id) {
        clsRepository.findById(id).ifPresent(item -> item.setStatus(Constants.ENTITY_STATUS.DELETED));
    }

    @Override
    public Optional<ClsDTO> findOne(Long id) {
        return clsRepository.findById(id).map(clsMapper::toDto);
    }

    public ClsDTO findByCode(String code) {
        Cls cls = clsRepository.findByClsCodeAndStatusIsGreaterThanEqual(code, Constants.ENTITY_STATUS.ACTIVE)
                .orElseThrow(() -> new BadRequestAlertException("Invalid code", ENTITY_NAME, "codenull"));
        return clsMapper.toDto(cls);
    }

    @Override
    public ClsDTO findByIdAndStatus(Long id) {
        Cls cls = clsRepository.findByIdAndStatusIsNot(id, Constants.ENTITY_STATUS.DELETED)
                .orElseThrow(() -> new BadRequestAlertException("Cls has been deleted", ENTITY_NAME, "cls.beenDeleted"));
        return clsMapper.toDto(cls);
    }


    public boolean isClsCodeExist(String code) {
        ClsDTO result = null;
        try {
            result = this.findByCode(code);
        } catch (BadRequestAlertException ex) {
            log.error(ex.getMessage());
        }
        return result != null;
    }

    @Override
    public List<ClsDTO> excelToObject(InputStream inputStream, List<ErrorExcel> errorDetails) {
        List<ClsDTO> clsDTOS = new ArrayList<>();
        ClsDTO clsDTO;
        int index = 1;
        String tempCode;
        int tempStatus;
        boolean correctData;

        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("CLS");

            if (sheet == null) {
                throw new BadRequestAlertException("format template file invalid", "", "excel.formatTemplate");
            }
            int rowNumber = 0;
            List<ErrorExcel> excelList = new ArrayList<>();
            for (Row row : sheet) {
                // skip header
                if (rowNumber < 8) {
                    rowNumber++;
                    continue;
                }
                clsDTO = new ClsDTO();
                correctData = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        switch (cell.getColumnIndex()) {
                            case 0:
                                if (cell.getCellType() == CellType.NUMERIC) {
                                    tempCode = cell.getRichStringCellValue().getString().trim();
                                    if (Objects.nonNull(clsRepository.findTopByClsCode(tempCode))) {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.codeIsExisted", mapError);
                                        excelList.add(errorExcels);
                                    } else if (tempCode.length() > 10) {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.codeMax10", mapError);
                                        excelList.add(errorExcels);
                                    }else if(tempCode.length() <2){
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.codeMin2", mapError);
                                        excelList.add(errorExcels);
                                    } else {
                                        clsDTO.setClsCode(tempCode);
                                    }
                                } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                    tempCode = cell.getStringCellValue().trim();
                                    Cls cls = clsRepository.findTopByClsCode(tempCode);
                                    if (cls != null) {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.codeIsExisted", mapError);
                                        excelList.add(errorExcels);
                                    } else if (tempCode.length() > 10) {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.codeMax10", mapError);
                                        excelList.add(errorExcels);
                                    }else if(tempCode.length() <2){
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.codeMin2", mapError);
                                        excelList.add(errorExcels);
                                    } else {
                                        clsDTO.setClsCode(tempCode);
                                    }
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("cls.codeNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                                break;
                            case 1:
                                if (cell.getStringCellValue() != null && !StrUtil.isBlank(cell.getStringCellValue())) {
                                    clsDTO.setClsName(cell.getStringCellValue());
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("cls.nameNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                                break;
                            case 2:
                                if (cell.getCellType() == CellType.NUMERIC ) {
                                    clsDTO.setClsPrice(BigDecimal.valueOf(cell.getNumericCellValue()));
                                } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                    try {
                                        String price = cell.getStringCellValue().replace(",", "").replace(".", "");
                                        clsDTO.setClsPrice(BigDecimal.valueOf(Long.parseLong(price)));
                                    } catch (NumberFormatException e) {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.priceNotSuitable", mapError);
                                        excelList.add(errorExcels);
                                    }
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("cls.priceNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                                break;
                            case 3:
                                if (cell.getCellType() == CellType.NUMERIC) {
                                    tempStatus = (int) cell.getNumericCellValue();
                                    if (tempStatus >= Constants.ENTITY_STATUS.DELETED && tempStatus <= Constants.ENTITY_STATUS.DEACTIVATE) {
                                        clsDTO.setStatus(tempStatus);
                                    } else {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.statusNotExist", mapError);
                                        excelList.add(errorExcels);
                                    }
                                } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                    try {
                                        clsDTO.setStatus(Integer.valueOf(cell.getStringCellValue()));
                                    }
                                    catch (NumberFormatException e) {
                                        correctData = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("cls.statusNotSuitable", mapError);
                                        excelList.add(errorExcels);
                                    }
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("cls.statusNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                                break;
                            case 4:
                                if (cell.getStringCellValue() != null && !StrUtil.isBlank(cell.getStringCellValue())) {
                                    clsDTO.setNote(cell.getStringCellValue());
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    else {correctData = false;}
                }
                // chỉ check các trường bắt buộc không được để trống khi tạo mới
                if (clsDTO.getClsPrice() != null || clsDTO.getStatus() != null || clsDTO.getClsName() != null) {
                    if (correctData && this.isEmpty(clsDTO)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("cls.lackOfDataField", mapError);
                        excelList.add(errorExcels);
                    }
                    if (clsDTO.getClsName() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("cls.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (clsDTO.getClsPrice() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("cls.priceIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (clsDTO.getStatus() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("cls.statusIsBlank", mapError);
                        excelList.add(errorExcels);
                    }

                    if (correctData && this.isDuplicateCode(clsDTO.getClsCode(), clsDTOS)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("cls.codeIsDuplicated", mapError);
                        excelList.add(errorExcels);
                    }

                    if (clsDTO.getClsCode() == null && clsDTO.getClsName() == null && clsDTO.getClsPrice() == null &&
                            clsDTO.getStatus() == null) {
                        excelList.clear();
                    } else {
                        errorDetails.addAll(excelList);
                        excelList.clear();
                    }

                    if (correctData) {
                        clsDTOS.add(clsDTO);
                    }
                }
                index++;
            }
            workbook.close();
            if (clsDTOS.isEmpty() && errorDetails.isEmpty()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", "");
                ErrorExcel errorExcels = new ErrorExcel("fileIsBlank", mapError);
                errorDetails.add(errorExcels);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
        return clsDTOS;
    }

    private boolean isEmpty(ClsDTO clsDTO) {
        boolean isEmpty = false;
        for (Field field : clsDTO.getClass().getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase("clsName")
                    || field.getName().equalsIgnoreCase("status")
                    || field.getName().equalsIgnoreCase("clsPrice")) {
                field.setAccessible(true);
                try {
                    if (field.get(clsDTO) == null) {
                        isEmpty = true;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return isEmpty;
    }

    @Override
    public ByteArrayInputStream exportToExcel(List<ClsDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 8;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (ClsDTO dsDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(dsDTO.getClsCode());
                row.createCell(1).setCellValue(dsDTO.getClsName());
                String convertPrice = NumberFormat.getInstance(Locale.US).format(dsDTO.getClsPrice());
                row.createCell(2).setCellValue(convertPrice);
                row.createCell(3).setCellValue(dsDTO.getStatus());
                row.createCell(4).setCellValue(dsDTO.getNote());
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private String generateClsCode (String code, String name, boolean checkName) {
        int count = 0;
        Cls cls;
        String generateCode;
        String newCode;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCode(name.trim());
            while(true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                cls = clsRepository.findTopByClsCode(newCode);
                if (Objects.isNull(cls)) {
                    break;
                }
                count++;
            }
        } else {
            newCode = code.trim();
            cls = clsRepository.findTopByClsCode(code);
            if (Objects.nonNull(cls)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }

    @Override
    public List<ClsCustomConfigDTO> findAllCustomConfigByClsId(Long clsId) {
        log.debug("Find all custom config of cls Start");
        ClsDTO clsDTO = clsMapper.toDto(clsRepository.findById(clsId).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllByHealthFacilityIdAndStatusAndConfigType(clsDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE, Constants.CONFIG_CATEGORY_TYPE.CLS.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(clsId)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of cls End");
        return configFieldDTOS.stream()
                .map(field -> new ClsCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClsDTO> findAllByIds(List<Long> ids) {
        return clsMapper.toDto(clsRepository.findAllById(ids));
    }

    private boolean isDuplicateCode(String code, List<ClsDTO> clsDTOS) {
        boolean isDuplicate = false;
        if (code != null) {
            for (ClsDTO clsDTO: clsDTOS) {
                if (clsDTO.getClsCode() != null) {
                    if (code.trim().equalsIgnoreCase(clsDTO.getClsCode().trim())) {
                        isDuplicate = true;
                        break;
                    }
                }
            }
        }
        return isDuplicate;
    }
}
