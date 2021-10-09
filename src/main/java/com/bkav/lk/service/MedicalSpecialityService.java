package com.bkav.lk.service;

import com.bkav.lk.dto.MedicalSpecialityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface MedicalSpecialityService {

    Page<MedicalSpecialityDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    MedicalSpecialityDTO findById(Long medicalSpecialityId);

    MedicalSpecialityDTO findOne(Long medicalSpecialityId);

    List<MedicalSpecialityDTO> findByHealthFacilityId(Long healthFacilityId);

    List<MedicalSpecialityDTO> findAllByIds(List<Long> ids);

    MedicalSpecialityDTO save(MedicalSpecialityDTO medicalSpecialityDTO);

    void delete(Long healthFacilityId);

    void deleteAll(List<Long> ids);

    boolean existByCode(String medicalSpecialityCode);

    List<MedicalSpecialityDTO> findByHealthFacilityId(Long healthFacilityId, Integer[] status);

    List<MedicalSpecialityDTO> findByHealthFacilityIdAndExistClinic(Long healthFacilityId);

    boolean isDeactivable(Long id);

}
