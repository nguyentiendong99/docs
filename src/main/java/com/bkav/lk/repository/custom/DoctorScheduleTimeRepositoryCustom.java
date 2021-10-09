package com.bkav.lk.repository.custom;

import org.springframework.util.MultiValueMap;

public interface DoctorScheduleTimeRepositoryCustom {

    Integer totalSUMPeopleRegisteredCustom(MultiValueMap<String, String> queryParams);

}
