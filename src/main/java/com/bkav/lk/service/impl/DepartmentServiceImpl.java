package com.bkav.lk.service.impl;

import com.bkav.lk.domain.AbstractAuditingEntity;
import com.bkav.lk.domain.Department;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DepartmentDTO;
import com.bkav.lk.repository.DepartmentRepository;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.DepartmentService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.DepartmentMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.google.api.client.util.Lists;
import com.google.common.base.Functions;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private static final String ENTITY_NAME = "departments";

    private static final String SHEET_DEPARTMENTS = "Departments";

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final ActivityLogService activityLogService;
    private final UserService userService;

    @Autowired
    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            DepartmentMapper departmentMapper,
            ActivityLogServiceImpl activityLogService, UserService userService) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
        this.activityLogService = activityLogService;
        this.userService = userService;
    }

    @Override
    public Page<DepartmentDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.info("Search list of department with conditions: {}", queryParams);
        List<DepartmentDTO> result = departmentMapper.toDto(departmentRepository.search(queryParams, pageable));
        this.stripDeletedDataFromList(result);
        return new PageImpl<>(result, pageable, departmentRepository.count(queryParams));
    }

    @Override
    public List<DepartmentDTO> findAll(Integer status) {
        List<Department> result = null;
        if (Objects.nonNull(status))
            result = departmentRepository.findByStatus(status);
        else
            result = departmentRepository.findAll();
        return departmentMapper.toDto(result);
    }

    @Override
    public List<DepartmentDTO> findAll(Integer[] status) {
        List<Department> result = departmentRepository.findByStatusIn(status);
        return departmentMapper.toDto(result);
    }

    @Override
    public DepartmentDTO findById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        return departmentMapper.toDto(department);
    }

    @Override
    public DepartmentDTO findByCode(String departmentCode) {
        Department department = departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new BadRequestAlertException("Invalid code", ENTITY_NAME, "codenull"));
        return departmentMapper.toDto(department);
    }

    @Override
    public DepartmentDTO save(DepartmentDTO departmentDTO) {
        log.info("Save department information with data: {}", departmentDTO);
        if (departmentDTO.getName() != null) {
            if (StringUtils.isEmpty(departmentDTO.getId())) {
                // Case: Create
                departmentDTO.setCode(generateDepartmentCode(departmentDTO.getCode(), departmentDTO.getName(), false));

            } else {
                // Case : Update
                // Neu tat ca cac thanh phan lien quan deu ngung hoat dong -> ngung hoat dong. Con lai khong duoc ngung hoat dong
                Optional<Department> department = departmentRepository.findById(departmentDTO.getId());
                departmentDTO.setChildDepartments(departmentMapper.toDto(department.get().getChildDepartments()));
                if (departmentDTO.getStatus() == Constants.ENTITY_STATUS.DEACTIVATE) {
                    List<Long> idChildDepartment = new ArrayList<>();
                    idChildDepartment.add(department.get().getId());

                    getChildDepartmentIdsIsActive(departmentDTO, idChildDepartment); // Tim tat ca id Department con dang hoat dong
                    if (idChildDepartment.size() > 1) { // neu size > 1 thi khong duoc ngung hoat dong
                        throw new BadRequestAlertException("Department have child active",ENTITY_NAME, "excel.department.update.child_active");
                    } else { // Neu size = 1
                        Integer[] status = {Constants.ENTITY_STATUS.ACTIVE};
                        List<User> users = userService.findByDepartment(departmentDTO.getId(), status); // Kiem tra User: Neu Con user dang hoat dong thi k duoc ngung hoat dong
                        if(!users.isEmpty()) {
                            throw new BadRequestAlertException("Department have user active",ENTITY_NAME, "excel.department.update.have_user_active");
                        }
                    }
                }
                if (!department.get().getCode().equals(departmentDTO.getCode()) || !department.get().getName().equals(departmentDTO.getName())) {
                    departmentDTO.setCode(generateDepartmentCode(departmentDTO.getCode(), departmentDTO.getName(), !department.get().getName().equals(departmentDTO.getName())));
                }
            }
        }
        Department department = departmentMapper.toEntity(departmentDTO);
        return departmentMapper.toDto(departmentRepository.save(department));
    }

    private void getChildDepartmentIdsIsActive(DepartmentDTO departmentDTO, List<Long> idChildDepartment) {
        if (!departmentDTO.getChildDepartments().isEmpty()) {
            departmentDTO.getChildDepartments().forEach(item -> {
                if (item.getStatus() == Constants.ENTITY_STATUS.ACTIVE) {
                    idChildDepartment.add(item.getId());
                    getChildDepartmentIdsIsActive(item, idChildDepartment);
                }
            });
        }
        return;
    }

    @Override
    public void delete(Long departmentId) {
        log.info("Delete department by ID: {}", departmentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        // Khong cho phep xoa neu co cac thanh phan lien quan: co Department con hoac User thuoc phong ban
        // Kiem tra phong ban con
        if (!department.getChildDepartments().isEmpty()) {
            List<Department> list = department.getChildDepartments().stream()
                    .filter(item -> item.getStatus() != Constants.ENTITY_STATUS.DELETED).collect(Collectors.toList());
            if (!list.isEmpty()) {
                throw new BadRequestAlertException("Department have children", ENTITY_NAME, "excel.department.delete.child_exist_in_department");
            }
        }
        // Kiem tra User thuoc phong ban
        Integer[] status = {Constants.ENTITY_STATUS.ACTIVE, Constants.ENTITY_STATUS.DEACTIVATE};
        List<User> users = userService.findByDepartment(department.getId(), status);
        if (!users.isEmpty()) {
            throw new BadRequestAlertException("Department have user", ENTITY_NAME, "excel.department.delete.user_exist_in_department");
        }

        department.setStatus(Constants.ENTITY_STATUS.DELETED);
        departmentRepository.save(department);
    }

    @Override
    public boolean existByCode(String departmentCode) {
        log.info("Check department exist by code: {}", departmentCode);
        return departmentRepository.existsByCode(departmentCode);
    }

    @Override
    public List<DepartmentDTO> addDepartmentsByExcelFile(InputStream file, List<ErrorExcel> errorDetails) {
        List<DepartmentDTO> departmentDTOs = new ArrayList<>();
        DepartmentDTO departmentDTO = null;
        int rowIndex = 0;
        int index = 1;
        String tempCode = null;
        Integer tempStatus = 0;
        DepartmentDTO tempDepartment = null;
        boolean correctData;
        try (Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheet(SHEET_DEPARTMENTS);
            if (Objects.isNull(sheet) || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestAlertException("format template file invalid", "", "excel.formatTemplate");
            }
            List<ErrorExcel> excelList = new ArrayList<>();
            
            // delete all row empty of excel file
            int rowLastNumber = sheet.getLastRowNum();
            Row rowFinal = sheet.getRow(rowLastNumber);
            while(checkIfRowIsEmpty(rowFinal)){
                sheet.removeRow(rowFinal);
                rowLastNumber = sheet.getLastRowNum();
                rowFinal = sheet.getRow(rowLastNumber);
            }

            for (Row row : sheet) {
                if (rowIndex < 6) {
                    rowIndex++;
                    continue;
                }
                departmentDTO = new DepartmentDTO();
                correctData = true;
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        case 0:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempCode = cell.getRichStringCellValue().getString().trim();
                                if(tempCode.length() <2){
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("department.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() > 10){
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("department.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if (this.existByCode(tempCode)) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("department.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    departmentDTO.setCode(tempCode);
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("department.codeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 1:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                departmentDTO.setName(cell.getRichStringCellValue().getString().trim());
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("department.nameNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 2:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempCode = cell.getRichStringCellValue().getString().trim();
                                if (!this.existByCode(tempCode)) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("department.parentCodeNotExist", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    tempDepartment = this.findByCode(tempCode);
                                    if (Objects.nonNull(tempDepartment)) {
                                        departmentDTO.setParentId(tempDepartment.getId());
                                    }
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("department.parentCodeNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 3:
                            if (CellType.NUMERIC.equals(cell.getCellType())) {
                                tempStatus = (int) cell.getNumericCellValue();
                                if (tempStatus >= Constants.ENTITY_STATUS.DELETED && tempStatus <= Constants.ENTITY_STATUS.DEACTIVATE) {
                                    departmentDTO.setStatus(tempStatus);
                                } else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("department.statusNotExist", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("department.statusNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        default:
                    }
                }
                if (departmentDTO.getName() != null || departmentDTO.getStatus() != null || departmentDTO.getCode() != null || departmentDTO.getParentId() != null) {
                    if (correctData && this.isEmpty(departmentDTO)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("department.lackOfDataField", mapError);
                        excelList.add(errorExcels);
                    }

                    if (departmentDTO.getName() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("department.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (departmentDTO.getStatus() == null) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("department.statusIsBlank", mapError);
                        excelList.add(errorExcels);
                    }

                    if (correctData && this.isDuplicateCode(departmentDTO.getCode(), departmentDTOs)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("department.codeIsDuplicated", mapError);
                        excelList.add(errorExcels);
                    }
                }

                if (departmentDTO.getCode() == null && departmentDTO.getName() == null && departmentDTO.getParentId() == null &&
                        departmentDTO.getStatus() == null) {
                    excelList.clear();
                } else {
                    errorDetails.addAll(excelList);
                    excelList.clear();
                }

                if (correctData) {
                    departmentDTOs.add(departmentDTO);
                }
                index++;
            }
            workbook.close();
            if (departmentDTOs.isEmpty() && errorDetails.isEmpty()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", "");
                ErrorExcel errorExcels = new ErrorExcel("department.fileIsBlank", mapError);
                errorDetails.add(errorExcels);
            }
        } catch (IOException e) {
            log.error("Error: ", e);
        }
        return departmentDTOs;
    }

    @Override
    public List<DepartmentDTO> createAll(List<DepartmentDTO> departmentDTOs) {
        log.info("REST request to save departments: {}", departmentDTOs);
        List<Department> newDepartments = departmentMapper.toEntity(departmentDTOs);
        List<Department> result = departmentRepository.saveAll(newDepartments);
        activityLogService.multipleCreate(Constants.CONTENT_TYPE.DEPARTMENT,
                result.stream().map(o -> (AbstractAuditingEntity) o).collect(Collectors.toList()));
        return departmentMapper.toDto(result);
    }

    private void stripDeletedDataFromList(List<DepartmentDTO> departments) {
        List<DepartmentDTO> markedAsDeletedDepartments = new ArrayList<>();
        for (DepartmentDTO department : departments) {
            if (Constants.ENTITY_STATUS.DELETED.equals(department.getStatus())) {
                markedAsDeletedDepartments.add(department);
            } else {
                if (!department.getChildDepartments().isEmpty()) {
                    stripDeletedDataFromList(department.getChildDepartments());
                }
            }
        }
        departments.removeAll(markedAsDeletedDepartments);
    }

    private boolean isEmpty(DepartmentDTO departmentDTO) {
        boolean isEmpty = false;
        for (Field field : departmentDTO.getClass().getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase("name")
                    || field.getName().equalsIgnoreCase("status")) {
                field.setAccessible(true);
                try {
                    if (field.get(departmentDTO) == null) {
                        isEmpty = true;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return isEmpty;
    }

    private boolean isDuplicateCode(String code, List<DepartmentDTO> departmentDTOs) {
        boolean isDuplicate = false;
        if( code != null){
            for (DepartmentDTO departmentDTO : departmentDTOs) {
                if (departmentDTO.getCode() != null) {
                    if (code.trim().equalsIgnoreCase(departmentDTO.getCode().trim())) {
                        isDuplicate = true;
                        break;
                    }
                }
            }
        }
        return isDuplicate;
    }

    @Override
    public String generateDepartmentCode(String code, String name, boolean checkName) {
        int count = 0;
        Department department = null;
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
                department = departmentRepository.findByCodeAndStatusGreaterThan(newCode, Constants.ENTITY_STATUS.DELETED).orElse(null);
                if (Objects.isNull(department)) {
                    break;
                }
                count++;
            }
        } else {
            newCode = code.trim();
            department = departmentRepository.findByCodeAndStatusGreaterThan(newCode, Constants.ENTITY_STATUS.DELETED).orElse(null);
            if (Objects.nonNull(department)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }

    @Override
    public Map<String, Object> handleTreeDepartment(List<DepartmentDTO> list, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        if (list.isEmpty()) {
            result.put("data", list);
            result.put("total", list.size());
            return result;
        }
        String name = params.getOrDefault("name", null);
        Integer status = (params.containsKey("status") && !params.get("status").equals("")) ? Integer.parseInt(params.get("status")) : null;

        Map<Long, DepartmentDTO> mapId2Department = list.stream().collect(Collectors.toMap(DepartmentDTO::getId, Functions.identity()));
        Map<Long, DepartmentDTO> res = new LinkedHashMap<>();

        list.stream()
                .filter(departmentDTO -> filterByNameAndStatus(departmentDTO, name, status))
                .forEach(departmentDTO -> {
                    DepartmentDTO root = parentMapping(departmentDTO, mapId2Department);

                    if (!res.containsKey(root.getId())) {
                        res.put(root.getId(), root);
                    }
                });

        int totalRecords = res.values()
                .stream()
                .map(departmentDTO -> countTreeByStatus(departmentDTO, status))
                .reduce(Integer::sum)
                .orElse(0);

        List<DepartmentDTO> data = Lists.newArrayList(res.values());
        data.sort(Comparator.comparing(DepartmentDTO::getStatus).thenComparing(DepartmentDTO::getLastModifiedDate, Comparator.reverseOrder()));
        result.put("data", data);
        result.put("total", totalRecords);

        return result;
    }

    private boolean filterByNameAndStatus(DepartmentDTO departmentDTO, String name, Integer status) {
        if (status != null) {
            return (name == null
                    || Utils.removeAccent(departmentDTO.getName().toLowerCase()).contains(Utils.removeAccent(name.trim().toLowerCase())))
                    && departmentDTO.getStatus().equals(status);
        }
        return (name == null || Utils.removeAccent(departmentDTO.getName().toLowerCase()).contains(Utils.removeAccent(name.trim().toLowerCase())));
    }

    private int countTreeByStatus(DepartmentDTO root, Integer status) {
        int count = 0;

        if (status != null) {
            if (root.getStatus().equals(status)) {
                count = 1;
            }

            for (DepartmentDTO departmentDTO : root.getChildDepartments()) {
                count += countTreeByStatus(departmentDTO, status);
            }
        } else {
            count = 1;
            for (DepartmentDTO departmentDTO : root.getChildDepartments()) {
                count += countTreeByStatus(departmentDTO, status);
            }
        }

        return count;
    }

    private DepartmentDTO parentMapping(DepartmentDTO current, Map<Long, DepartmentDTO> mapId2Department) {
        if (current.getParentId() != null) {
            DepartmentDTO parent = mapId2Department.get(current.getParentId());
            if (!parent.getChildDepartments().contains(current)) {
                parent.getChildDepartments().add(current);
            }
            return parentMapping(parent, mapId2Department);
        }
        return current;
    }
    
    private boolean checkIfRowIsEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK && !StringUtils.isEmpty(cell.toString())) {
                return false;
            }
        }
        return true;
    }

}
