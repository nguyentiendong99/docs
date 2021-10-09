package com.bkav.lk.service;

import com.bkav.lk.domain.AppointmentCancelLog;

import java.util.List;

public interface AppointmentCancelConfigService {

    AppointmentCancelLog save();

    AppointmentCancelLog update(AppointmentCancelLog appointmentCancelLog);

    List<AppointmentCancelLog> updateAll(List<AppointmentCancelLog> appointmentCancelLogs);

    AppointmentCancelLog findByUserId(Long userId);

    AppointmentCancelLog findByCurrentUser();

    List<AppointmentCancelLog> findAll();

    void deleteAll(List<Long> ids);

    Boolean hasBlocked();
}
