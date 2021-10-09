package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Department;
import com.bkav.lk.dto.DepartmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper extends EntityMapper<DepartmentDTO, Department> {

    Department toEntity(DepartmentDTO departmentDTO);

    DepartmentDTO toDto(Department department);

    default Department fromId(Long id) {
        if (id == null) {
            return null;
        }
        Department department = new Department();
        department.setId(id);
        return department;
    }

}
