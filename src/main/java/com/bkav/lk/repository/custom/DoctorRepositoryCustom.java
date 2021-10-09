package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.Doctor;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface DoctorRepositoryCustom {

    List<Doctor> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    List<Doctor> findByMainHealthFacilityIdAndStatus(Long parentId, Integer status);

    List<Doctor> findAllDoctorsWithin30Days(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long countDoctorsWithin30Days(MultiValueMap<String, String> queryParams);
}
