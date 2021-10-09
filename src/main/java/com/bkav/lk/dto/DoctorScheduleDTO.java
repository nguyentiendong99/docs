package com.bkav.lk.dto;

import java.io.Serializable;
import java.time.Instant;

public class DoctorScheduleDTO implements Serializable {

    private Long id;

    private Long doctorId;

    private String doctorCode;

    private String doctorName;

    private String clinicName;

    private Boolean isValid;

    private Instant workingDate;

    private String workingDateFormat;

    private Integer workingTime;

    private Integer status;

    private String groupByParentId;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private String morningWork;

    private String afternoonWork;

    private Instant lastModifiedDate;

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

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public Instant getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(Instant workingDate) {
        this.workingDate = workingDate;
    }

    public String getWorkingDateFormat() {
        return workingDateFormat;
    }

    public void setWorkingDateFormat(String workingDateFormat) {
        this.workingDateFormat = workingDateFormat;
    }

    public Integer getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(Integer workingTime) {
        this.workingTime = workingTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGroupByParentId() {
        return groupByParentId;
    }

    public void setGroupByParentId(String groupByParentId) {
        this.groupByParentId = groupByParentId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getMorningWork() {
        return morningWork;
    }

    public void setMorningWork(String morningWork) {
        this.morningWork = morningWork;
    }

    public String getAfternoonWork() {
        return afternoonWork;
    }

    public void setAfternoonWork(String afternoonWork) {
        this.afternoonWork = afternoonWork;
    }
}
