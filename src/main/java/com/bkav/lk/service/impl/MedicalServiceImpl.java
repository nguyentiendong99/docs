package com.bkav.lk.service.impl;

import com.bkav.lk.domain.MedicalService;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.MedicalServiceRepository;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigValueService;
import com.bkav.lk.service.MedicalServiceService;
import com.bkav.lk.service.mapper.MedicalServiceMapper;
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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicalServiceImpl implements MedicalServiceService {

    private static final String ENTITY_NAME = "MedicalService";

    private final Logger log = LoggerFactory.getLogger(MedicalServiceService.class);

    private final MedicalServiceRepository medicalServiceRepository;

    private final MedicalServiceMapper medicalServiceMapper;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private final CategoryConfigValueService categoryConfigValueService;

    public MedicalServiceImpl(MedicalServiceRepository medicalServiceRepository, MedicalServiceMapper medicalServiceMapper,
                              CategoryConfigFieldService categoryConfigFieldService, CategoryConfigValueService categoryConfigValueService) {
        this.medicalServiceRepository = medicalServiceRepository;
        this.medicalServiceMapper = medicalServiceMapper;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigValueService = categoryConfigValueService;
    }

    @Override
    public Optional<MedicalServiceDTO> findOne(Long id) {
        log.debug("Request to get medical service : {}", id);
        return medicalServiceRepository.findById(id).map(medicalServiceMapper::toDto);
    }

    @Override
    public MedicalServiceDTO save(MedicalServiceDTO medicalServiceDTO) {
        log.debug("Request to save Medical Service : {}", medicalServiceDTO);
        if (medicalServiceDTO.getName() != null && medicalServiceDTO.getPrice() != null) {
            if (StringUtils.isEmpty(medicalServiceDTO.getId())) {
                medicalServiceDTO.setCode(generateMedicalServiceCode(medicalServiceDTO.getCode(), medicalServiceDTO.getName(), false));

            } else {
                Optional<MedicalService> medicalService = medicalServiceRepository.findById(medicalServiceDTO.getId());
                if (!medicalService.get().getCode().equals(medicalServiceDTO.getCode()) || !medicalService.get().getName().equals(medicalServiceDTO.getName())) {
                    medicalServiceDTO.setCode(generateMedicalServiceCode(medicalServiceDTO.getCode(), medicalServiceDTO.getName(), !medicalService.get().getName().equals(medicalServiceDTO.getName())));
                }
            }
        }
        MedicalService medicalService = medicalServiceRepository.save(medicalServiceMapper.toEntity(medicalServiceDTO));

        //Check config
        //Create
        if(medicalServiceDTO.getId() == null) {
            if (!CollectionUtils.isEmpty(medicalServiceDTO.getMedicalServiceCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

                medicalServiceDTO.getMedicalServiceCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(medicalServiceCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                    configValueDTO.setValue(medicalServiceCustomConfigDTO.getValue());
                    configValueDTO.setFieldId(medicalServiceCustomConfigDTO.getFieldId());
                    configValueDTO.setObjectId(medicalService.getId());
                    configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    configValueDTOS.add(configValueDTO);
                });
                categoryConfigValueService.createAll(configValueDTOS);
            }
        } else if(medicalServiceDTO.getId() != null) {
            // Update
            if (!CollectionUtils.isEmpty(medicalServiceDTO.getMedicalServiceCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
                List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
                medicalServiceDTO.getMedicalServiceCustomConfigDTOS().forEach(clinicCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(medicalService.getId(), clinicCustomConfigDTO.getFieldId());
                    configValueDTO.setValue(clinicCustomConfigDTO.getValue());
                    if (configValueDTO.getFieldId() != null) {
                        configValueUpdateDTOS.add(configValueDTO);
                    } else {
                        configValueDTO.setObjectId(medicalService.getId());
                        configValueDTO.setFieldId(clinicCustomConfigDTO.getFieldId());
                        configValueDTO.setValue(clinicCustomConfigDTO.getValue());
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
        return medicalServiceMapper.toDto(medicalService);
    }

    @Override
    public List<MedicalServiceDTO> findAll() {
        List<MedicalService> list = medicalServiceRepository.findAllByStatus(Constants.ENTITY_STATUS.ACTIVE);
        return medicalServiceMapper.toDto(list);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Medical Service : {}", id);
        medicalServiceRepository.findById(id).ifPresent(item -> {
            item.setStatus(Constants.ENTITY_STATUS.DELETED);
            log.debug("Deleted Medical Service id = {}", id);
        });
    }

    @Override
    public Page<MedicalServiceDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("Request to search for a page of MedicalServices with multi query {}", queryParams);
        List<MedicalServiceDTO> medicalServiceDTOList = medicalServiceMapper.toDto(medicalServiceRepository.search(queryParams, pageable));
        return new PageImpl<>(medicalServiceDTOList, pageable, medicalServiceRepository.count(queryParams));
    }

    @Override
    public boolean existCode(String code) {
        return medicalServiceRepository.existsByCode(code.toUpperCase());
    }

    @Override
    public ByteArrayInputStream exportToExcel(List<MedicalServiceDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 8;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (MedicalServiceDTO dsDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(dsDTO.getCode());
                row.createCell(1).setCellValue(dsDTO.getName());
                String convertPrice = NumberFormat.getInstance(Locale.US).format(dsDTO.getPrice());
                row.createCell(2).setCellValue(convertPrice);
                row.createCell(3).setCellValue(dsDTO.getStatus());
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception exception) {
            log.error("Error: ", exception);
            return null;
        }
    }


    public List<MedicalServiceDTO> excelToOBject(InputStream inputStream, List<ErrorExcel> details) {
        boolean pass;
        List<MedicalServiceDTO> list = new ArrayList<>();
        int index = 1;
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("DVK");

            if (Objects.isNull(sheet) || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestAlertException("format template file invalid", "", "excel.formatTemplate");
            }

            int rowNumber = 0;
            Set<String> setCodes = new HashSet<>();
            List<ErrorExcel> excelList = new ArrayList<>();
            for (Row row : sheet) {
                // skip header ( bằng 6 là check row có dữ liệu)
                if (rowNumber < 6) {
                    rowNumber++;
                    continue;
                }
                MedicalServiceDTO medicalServiceDTO = new MedicalServiceDTO();
                pass = true;
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        // Ma dich vu la khong bat buoc
                        case 0:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                String tempCode = String.valueOf(cell.getNumericCellValue()).trim();
                                if (medicalServiceRepository.existsByCodeAndStatusGreaterThan(tempCode, Constants.ENTITY_STATUS.DELETED)) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    medicalServiceDTO.setCode(tempCode);
                                }
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue()) ) {
                                String tempCode = cell.getStringCellValue().trim();
                                if (medicalServiceRepository.existsByCodeAndStatusGreaterThan(tempCode, Constants.ENTITY_STATUS.DELETED)) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    medicalServiceDTO.setCode(tempCode);
                                }
                            } else if (cell.getCellType() == CellType.BLANK){
                                medicalServiceDTO.setCode(null);
                            } else {
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("medicalService.codeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 1:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                medicalServiceDTO.setName((String.valueOf(cell.getNumericCellValue()).trim()));
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                medicalServiceDTO.setName(cell.getStringCellValue().trim());
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("medicalService.nameNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                medicalServiceDTO.setPrice(BigDecimal.valueOf(cell.getNumericCellValue()));
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                String price = cell.getStringCellValue().replace(",", "").replace(".", "");
                                try {
                                    medicalServiceDTO.setPrice(BigDecimal.valueOf(Long.parseLong(price)));
                                }catch (NumberFormatException numberFormatException) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.priceNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                int status = (int) cell.getNumericCellValue();
                                if (status != Constants.ENTITY_STATUS.ACTIVE && status != Constants.ENTITY_STATUS.DEACTIVATE) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.statusIs1or2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    medicalServiceDTO.setStatus(status);
                                }
                            } else if (cell.getCellType() == CellType.STRING) {
                                try {
                                    int status = Integer.parseInt(cell.getStringCellValue());
                                    if (status != Constants.ENTITY_STATUS.ACTIVE && status != Constants.ENTITY_STATUS.DEACTIVATE) {
                                        pass = false;
                                        Map<String, String> mapError = new HashMap<>();
                                        mapError.put("row", String.valueOf(index));
                                        ErrorExcel errorExcels = new ErrorExcel("medicalService.statusIs1or2", mapError);
                                        excelList.add(errorExcels);
                                    } else {
                                        medicalServiceDTO.setStatus(status);
                                    }
                                }catch (NumberFormatException numberFormatException) {
                                    pass = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("medicalService.statusNotSuitable", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                pass = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("medicalService.statusNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        default:
                            break;
                    }
                }
                // không check pass = true vì BE đọc file sẽ chỉ đọc những cột có giá trị tương ứng trong 1 hàng nên nếu dùng pass = true sẽ check thiếu TH
                if (medicalServiceDTO.getName() != null || medicalServiceDTO.getPrice() != null || medicalServiceDTO.getStatus() != null) {
                    if (medicalServiceDTO.getName() == null) {
                        pass = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("medicalService.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (medicalServiceDTO.getPrice() == null) {
                        pass = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("medicalService.priceIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (medicalServiceDTO.getStatus() == null) {
                        pass = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("medicalService.statusIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                }
                // Vi sao phai check != null: vi neu xoa 1 hang ơ trong excel no van hieu hang day co gia tri
                if (medicalServiceDTO.getName() == null && medicalServiceDTO.getPrice() == null && medicalServiceDTO.getStatus() == null) {
                    pass = false;
                    excelList.clear();
                } else {
                    details.addAll(excelList);
                    excelList.clear();
                }
                if (pass) {
                    list.add(medicalServiceDTO);
                }
                index++;
            }
            // Check kiem tra ma don vi phai la duy nhat
            List<MedicalServiceDTO> listCodesNotNull = list.stream().filter(item -> item.getCode() != null).collect(Collectors.toList());
            listCodesNotNull.forEach(item -> {
                setCodes.add(item.getCode().toUpperCase());
            });
            if(setCodes.size() != listCodesNotNull.size()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", String.valueOf(index));
                ErrorExcel errorExcels = new ErrorExcel("medicalService.codeIsOnlyOne", mapError);
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
    private String generateMedicalServiceCode (String code, String name, boolean checkName) {
        int count = 0;
        MedicalService medicalService = null;
        String generateCode = null;
        String newCode = null;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCode(name.trim());
            while(true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                medicalService = medicalServiceRepository.findTopByCode(newCode);
                if (Objects.isNull(medicalService)) {
                    break;
                }
                count++;
            }
        } else {
            newCode = code.trim();
            medicalService = medicalServiceRepository.findTopByCode(code);
            if (Objects.nonNull(medicalService)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }

    @Override
    public List<MedicalServiceCustomConfigDTO> findAllCustomConfigByMedicalServiceId(Long medicalServiceId) {
        log.debug("Find all custom config of clinic Start");
        MedicalServiceDTO medicalServiceDTO = medicalServiceMapper.toDto(medicalServiceRepository.findById(medicalServiceId).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllByHealthFacilityIdAndStatusAndConfigType(medicalServiceDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE, Constants.CONFIG_CATEGORY_TYPE.MEDICAL_SERVICE.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(medicalServiceId)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of doctor End");
        return configFieldDTOS.stream()
                .map(field -> new MedicalServiceCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalServiceDTO> findAllByIds(List<Long> ids) {
       return  medicalServiceMapper.toDto(medicalServiceRepository.findAllById(ids));
    }
}
