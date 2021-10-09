package com.bkav.lk.dto;

import java.io.Serializable;
import java.time.Instant;

public class DoctorScheduleTimeDTO implements Serializable {

    private Long id;

    private Long doctorId;

    private Instant startTime;

    private Instant endTime;

    private Integer peopleRegistered;

    private boolean isValid;

    private Long healthFacilityId;

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Integer getPeopleRegistered() {
        return peopleRegistered;
    }

    public void setPeopleRegistered(Integer peopleRegistered) {
        this.peopleRegistered = peopleRegistered;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
