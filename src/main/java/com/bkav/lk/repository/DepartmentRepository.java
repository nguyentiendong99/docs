package com.bkav.lk.repository;

import com.bkav.lk.domain.Department;
import com.bkav.lk.repository.custom.DepartmentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, DepartmentRepositoryCustom {

    boolean existsByCode(String departmentCode);

    List<Department> findByStatus(Integer active);

    List<Department> findByStatusIn(Integer[] status);

    Optional<Department> findByCode(String departmentCode);

    Optional<Department> findByCodeAndStatusGreaterThan(String code, Integer status);
}
