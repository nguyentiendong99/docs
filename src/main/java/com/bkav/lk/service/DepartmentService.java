package com.bkav.lk.service;

import com.bkav.lk.dto.DepartmentDTO;
import com.bkav.lk.web.errors.ErrorExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DepartmentService {

    Page<DepartmentDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    List<DepartmentDTO> findAll(Integer status);

    List<DepartmentDTO> findAll(Integer[] status);

    DepartmentDTO findById(Long departmentId);

    DepartmentDTO findByCode(String departmentCode);

    DepartmentDTO save(DepartmentDTO departmentDTO);

    void delete(Long departmentId);

    boolean existByCode(String departmentCode);

    String generateDepartmentCode(String code, String name, boolean checkName);

    List<DepartmentDTO> addDepartmentsByExcelFile(InputStream file, List<ErrorExcel> details);

    List<DepartmentDTO> createAll(List<DepartmentDTO> departmentDTOs);

    Map<String, Object> handleTreeDepartment(List<DepartmentDTO> list, Map<String, String> params);

}
