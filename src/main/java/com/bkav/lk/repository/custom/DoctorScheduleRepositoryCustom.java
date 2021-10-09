package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.DoctorSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.List;

public interface DoctorScheduleRepositoryCustom {

    List<DoctorSchedule> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    List<DoctorSchedule> findAllByTimeSelected(Long healthFacilityId, Instant currentSelectDay);

    List<Instant> findAllAvailableInHospital(Long healthFacilityId, Instant startDate, Instant endDate);

}
