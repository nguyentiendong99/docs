package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.*;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigValueService;
import com.bkav.lk.service.ClinicService;
import com.bkav.lk.service.mapper.ClinicMapper;
import com.bkav.lk.service.mapper.HealthFacilitiesMapper;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClinicServiceImpl implements ClinicService {

    private final Logger log = LoggerFactory.getLogger(ClinicService.class);

    private static final String ENTITY_NAME = "clinic";

    private final ClinicRepository clinicRepository;

    private final HealthFacilitiesRepository healthFacilitiesRepository;

    private final HealthFacilitiesMapper healthFacilitiesMapper;

    private final DoctorAppointmentRepository doctorAppointmentRepository;

    private final DoctorScheduleRepository doctorScheduleRepository;

    private final ClinicMapper clinicMapper;

    private final MedicalSpecialityRepository medicalSpecialityRepository;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private final CategoryConfigValueService categoryConfigValueService;

    public ClinicServiceImpl(ClinicRepository clinicRepository, HealthFacilitiesRepository healthFacilitiesRepository, HealthFacilitiesMapper healthFacilitiesMapper, ClinicMapper clinicMapper,
                             MedicalSpecialityRepository medicalSpecialityRepository,
                             CategoryConfigFieldService categoryConfigFieldService,
                             CategoryConfigValueService categoryConfigValueService,
                             DoctorAppointmentRepository doctorAppointmentRepository,
                             DoctorScheduleRepository doctorScheduleRepository) {
        this.healthFacilitiesRepository = healthFacilitiesRepository;
        this.healthFacilitiesMapper = healthFacilitiesMapper;
        this.clinicMapper = clinicMapper;
        this.clinicRepository = clinicRepository;
        this.medicalSpecialityRepository = medicalSpecialityRepository;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigValueService = categoryConfigValueService;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    @Override
    public Optional<Clinic> findOne(Long id) {
        return clinicRepository.findById(id);
    }

    @Override
    public List<ClinicDTO> findAll() {
        return clinicMapper.toDto(clinicRepository.findAll());
    }

    @Override
    public ClinicDTO findByCode(String code) {
        return clinicMapper.toDto(clinicRepository.findByCode(code));
    }

    @Override
    public ClinicDTO findByIdAndStatus(Long id) {
        Clinic clinic = clinicRepository.findByIdAndStatusIsNot(id, Constants.ENTITY_STATUS.DELETED)
                .orElseThrow(() -> new BadRequestAlertException("Clinic has been deleted", ENTITY_NAME, "clinic.beenDeleted"));
        return clinicMapper.toDto(clinic);
    }

    @Override
    public Page<ClinicDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<ClinicDTO> clinicDTOList = clinicMapper.toDto(clinicRepository.search(queryParams, pageable));
        HealthFacilitiesDTO healthFacilitiesDTO = healthFacilitiesMapper.toDto(healthFacilitiesRepository.
                findById(Long.valueOf(queryParams.get("healthFacilityId").get(0))).orElse(new HealthFacilities()));
        clinicDTOList.forEach(item -> {
            item.setHealthFacilityCode(healthFacilitiesDTO.getCode());
        });
        return new PageImpl<>(clinicDTOList, pageable, clinicRepository.count(queryParams));
    }

    @Override
    public Clinic save(ClinicDTO clinicDTO) {
        if (Objects.nonNull(clinicDTO) && Objects.nonNull(clinicDTO.getId())) {
            List<DoctorSchedule> doctorSchedules = doctorScheduleRepository.findByClinicAndStatus(clinicDTO.getId(), Constants.ENTITY_STATUS.ACTIVE);
            List<DoctorAppointment> doctorAppointments = doctorAppointmentRepository.findByClinicAndStatusNot(clinicDTO.getId(), Constants.ENTITY_STATUS.DELETED);

            if ((!doctorSchedules.isEmpty() || !doctorAppointments.isEmpty())
                    && Constants.ENTITY_STATUS.DEACTIVATE.equals(clinicDTO.getStatus())) {
                throw new BadRequestAlertException("Clinic is already used", ENTITY_NAME, "clinic.cant_deactivate");
            }
        }
        try {
            Clinic clinic = clinicMapper.toEntity(clinicDTO);
            clinicRepository.save(clinic);
            //Check config
            //Create
            if (clinicDTO.getId() == null) {
                if (!CollectionUtils.isEmpty(clinicDTO.getClinicCustomConfigDTOS())) {
                    List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

                    clinicDTO.getClinicCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(clinicCustomConfigDTO -> {
                        CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                        configValueDTO.setValue(clinicCustomConfigDTO.getValue());
                        configValueDTO.setFieldId(clinicCustomConfigDTO.getFieldId());
                        configValueDTO.setObjectId(clinic.getId());
                        configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                        configValueDTOS.add(configValueDTO);
                    });
                    categoryConfigValueService.createAll(configValueDTOS);
                }
            } else if (clinicDTO.getId() != null) {
                // Update
                if (!CollectionUtils.isEmpty(clinicDTO.getClinicCustomConfigDTOS())) {
                    List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
                    List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
                    clinicDTO.getClinicCustomConfigDTOS().forEach(clinicCustomConfigDTO -> {
                        CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(clinic.getId(), clinicCustomConfigDTO.getFieldId());
                        configValueDTO.setValue(clinicCustomConfigDTO.getValue());
                        if (configValueDTO.getFieldId() != null) {
                            configValueUpdateDTOS.add(configValueDTO);
                        } else {
                            configValueDTO.setObjectId(clinic.getId());
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
            return clinic;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        List<DoctorSchedule> doctorSchedules = doctorScheduleRepository.findByClinicAndStatusNot(id, Constants.ENTITY_STATUS.DELETED);
        List<DoctorAppointment> doctorAppointments = doctorAppointmentRepository.findByClinicAndStatusNot(id, Constants.ENTITY_STATUS.DELETED);

        if (!doctorSchedules.isEmpty() || !doctorAppointments.isEmpty()) {
            throw new BadRequestAlertException("Clinic is already used", ENTITY_NAME, "clinic.is_used");
        }
        clinicRepository.deleteTechnically(id, Constants.ENTITY_STATUS.DELETED);
    }

    @Override
    public ByteArrayInputStream exportClinicToExcel(List<ClinicDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 9;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (ClinicDTO clinicDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(clinicDTO.getCode());
                row.createCell(1).setCellValue(clinicDTO.getName());
                row.createCell(3).setCellValue(convertStatus(clinicDTO.getStatus()));
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

    @Override
    public List<ClinicDTO> findByHealthFacilityId(Long healthFacilityId) {
        List<Clinic> results = clinicRepository.findByHealthFacilitiesIdAndStatus(
                healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
        return clinicMapper.toDto(results);
    }

    @Override
    public String generateClinicCode(String code, String name, boolean checkName) {
        int count = 0;
        Clinic clinic;
        String generateCode;
        String newCode;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCodeForClinic(name);
            while (true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                clinic = clinicRepository.findByCodeAndStatusIsNot(newCode, Constants.ENTITY_STATUS.DELETED)
                        .orElse(null);

                if (Objects.isNull(clinic)) {
                    break;
                }
                count++;
            }
            return newCode;
        }
        newCode = code.trim();
        clinic = clinicRepository.findByCodeAndStatusIsNot(code, Constants.ENTITY_STATUS.DELETED)
                .orElse(null);
        if (Objects.nonNull(clinic)) {
            return null;

        }
        return newCode;
    }

    private static String convertStatus(Integer status) {
        if (status.equals(Constants.ENTITY_STATUS.ACTIVE)) {
            return "Đang hoạt động";
        }
        if (status.equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            return "Dừng hoạt động";
        }
        return "Đã xóa";
    }

    @Override
    public List<ClinicDTO> excelToClinic(InputStream inputStream, List<ErrorExcel> errorDetails) {
        List<ClinicDTO> clinicDTOS = new ArrayList<>();
        ClinicDTO clinicDTO;
        int index = 1;
        String tempCode;
        int tempStatus;
        boolean correctData;
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("Clinic");

            if (sheet == null) {
                throw new BadRequestAlertException("Định dạng excel không đúng", "", "formatExcel");
            }
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            List<ErrorExcel> excelList = new ArrayList<>();
            for (Row row : sheet) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber < 9) {
                    rowNumber++;
                    continue;
                }
                clinicDTO = new ClinicDTO();
                correctData = true;
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {

                        case 0:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                tempCode = String.valueOf(cell.getNumericCellValue()).trim();
                                if (this.isClsCodeExist((cell.getDateCellValue()).toString())) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    clinicDTO.setCode(tempCode);
                                }
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempCode = cell.getStringCellValue();
                                if (this.isClsCodeExist(cell.getStringCellValue().trim())) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    clinicDTO.setCode(tempCode);
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("clinic.codeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 1:
                            if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                clinicDTO.setName(cell.getRichStringCellValue().getString().trim());
                            } else if (cell.getCellType() == CellType.NUMERIC) {
                                clinicDTO.setName(String.valueOf(cell.getNumericCellValue()).trim());
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("clinic.nameNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                tempStatus = (int) cell.getNumericCellValue();
                                if (tempStatus > Constants.ENTITY_STATUS.DELETED && tempStatus <= Constants.ENTITY_STATUS.DEACTIVATE) {
                                    clinicDTO.setStatus(tempStatus);
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("clinic.statusNotExist", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else if (cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                                clinicDTO.setStatus(Integer.valueOf(cell.getStringCellValue()));
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("clinic.statusNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        default:
                            break;
                    }
                }
                // chỉ check các trường bắt buộc phải nhập.
                if (clinicDTO.getStatus() != null || clinicDTO.getName() != null || clinicDTO.getCode() != null) {
                    if (correctData && this.isEmpty(clinicDTO)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("clinic.lackOfDataField", mapError);
                        excelList.add(errorExcels);
                    }
                    if (clinicDTO.getName() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("clinic.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (clinicDTO.getStatus() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("clinic.statusIsBlank", mapError);
                        excelList.add(errorExcels);
                    }

                    if (correctData && this.isDuplicateCode(clinicDTO.getCode(), clinicDTOS)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("clinic.codeIsDuplicated", mapError);
                        excelList.add(errorExcels);
                    }
                }

                if (clinicDTO.getCode() == null && clinicDTO.getName() == null && clinicDTO.getStatus() == null) {
                    excelList.clear();
                } else {
                    errorDetails.addAll(excelList);
                    excelList.clear();
                }

                if (correctData) {
                    clinicDTOS.add(clinicDTO);
                }

                index++;
            }
            workbook.close();
            if (clinicDTOS.isEmpty() && errorDetails.isEmpty()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", "");
                ErrorExcel errorExcels = new ErrorExcel("fileIsBlank", mapError);
                errorDetails.add(errorExcels);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
        return clinicDTOS;
    }

    @Override
    public List<ClinicCustomConfigDTO> findAllCustomConfigByClinicId(Long clinicId) {
        log.debug("Find all custom config of clinic Start");
        ClinicDTO clinicDTO = clinicMapper.toDto(clinicRepository.findById(clinicId).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllByHealthFacilityIdAndStatusAndConfigType(
                clinicDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE,
                Constants.CONFIG_CATEGORY_TYPE.CLINIC.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(clinicId)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of doctor End");
        return configFieldDTOS.stream()
                .map(field -> new ClinicCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDeactivable(Long clinicId) {
        MultiValueMap<String, String> querySearch = new LinkedMultiValueMap<>();
        querySearch.set("clinicId", clinicId.toString());
        querySearch.set("status", Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE + "," + Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
        List<DoctorAppointment> appointmentList = doctorAppointmentRepository.search(querySearch, null);
        return appointmentList.size() == 0;
    }

    @Override
    public List<ClinicDTO> findAllByIds(List<Long> ids) {
        return clinicMapper.toDto(clinicRepository.findAllById(ids));
    }

    private boolean isClsCodeExist(String code) {
        ClinicDTO result = null;
        try {
            result = this.findByCode(code);
        } catch (BadRequestAlertException ex) {
            log.error(ex.getMessage());
        }
        return result != null;
    }

    private boolean isEmpty(ClinicDTO clinicDTO) {
        boolean isEmpty = false;
        for (Field field : clinicDTO.getClass().getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase("name")
                    || field.getName().equalsIgnoreCase("medicalSpecialtyId")
                    || field.getName().equalsIgnoreCase("status")) {
                field.setAccessible(true);
                try {
                    if (field.get(clinicDTO) == null) {
                        isEmpty = true;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return isEmpty;
    }

    private boolean isDuplicateCode(String code, List<ClinicDTO> clinicDTOS) {
        boolean isDuplicate = false;
        if(code != null){
            for (ClinicDTO clinicDTO : clinicDTOS) {
                if (clinicDTO.getCode() != null) {
                    if (code.trim().equalsIgnoreCase(clinicDTO.getCode().trim())) {
                        isDuplicate = true;
                        break;
                    }
                }
            }
        }
        return isDuplicate;
    }
}
