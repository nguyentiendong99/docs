package com.bkav.lk.repository;

import com.bkav.lk.domain.Subclinical;
import com.bkav.lk.repository.custom.SubclinicalRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubclinicalRepository extends JpaRepository<Subclinical, Long> , SubclinicalRepositoryCustom {

    List<Subclinical> findByStatus(Integer status);

    boolean existsByDoctorAppointmentCode(String doctorAppointmentCode);
}
