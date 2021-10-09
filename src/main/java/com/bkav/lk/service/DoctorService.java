package com.bkav.lk.service;

import com.bkav.lk.domain.CategoryConfigField;
import com.bkav.lk.dto.CategoryConfigFieldDTO;
import com.bkav.lk.dto.CategoryConfigValueDTO;
import com.bkav.lk.dto.DoctorCustomConfigDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.vm.HisDoctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface DoctorService {

    List<DoctorDTO> findAllByTimeSelected(Long healthFacilitiesId, Long medicalSpecialityId, Long clinicId, String date, Instant startTime, Instant endTime);

    List<DoctorDTO> findAllByTimeSelected(Long healthFacilitiesId, String date, Instant startTime, Instant endTime);

    Page<DoctorDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Page<DoctorDTO> findAllDoctorsWithin30Days(MultiValueMap<String, String> queryParams, Pageable pageable);

    DoctorDTO findById(Long id) throws EntityNotFoundException;

    DoctorDTO findByDoctorId(Long id);

    DoctorDTO findByIdAndStatus(Long doctor, Integer status);

    List<DoctorDTO> findAll();

    DoctorDTO create(DoctorDTO doctor);

    DoctorDTO update(DoctorDTO doctor) throws EntityNotFoundException;

    void delete(Long id) throws EntityNotFoundException;

    void deleteAll(List<Long> id);

    List<DoctorDTO> findAllDoctorByHealthFacilityId(Long id);

    List<DoctorDTO> findByMainHealthFacilityId(Long id);

    DoctorDTO findByCode(String code);

    boolean isDoctorCodeExist(String code);

    List<DoctorDTO> createAll(List<DoctorDTO> doctors);

    List<DoctorDTO> findAllByPosition(Long positionId);

    List<DoctorDTO> findByCodesAndHealthFacilityId(List<String> codes, int status, Long healthFacilityId);

    List<DoctorDTO> findByDoctorIdsAndExistsAppointment(List<Long> ids);

    List<DoctorDTO> addDoctorsByExcelFile(InputStream inputStream, List<ErrorExcel> errorDetails);

    ByteArrayInputStream exportDoctorToExcel(List<DoctorDTO> doctorDTOs, InputStream file);

    boolean isAcademicIdExist(Long id);

    List<DoctorDTO> findAllDoctorByHealthFacilityId(Long id, Integer[] status);

    List<HisDoctor> getListDoctorFromHis(String healthFacilityCode);

    /**
     * Find all custom config by doctor id list.
     *
     * @param doctorId the doctor id
     * @return the list
     */
    List<DoctorCustomConfigDTO> findAllCustomConfigByDoctorId(Long doctorId);

    boolean existsByPositionId(Long positionId);

    List<DoctorDTO> findByClincAndStatus(Long clinicId, Integer[] status);

    List<DoctorDTO> findAllByIds(List<Long> ids);
}
