package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HisClsGoiYDTO {

    private Long id;

    private String clsCode;

    private String clsName;

    private String clsNumber;

    private String clsDepartment;

    private String clsDoctor;

    private Integer clsPriority;

    private Integer clsCurrentPriority;

    private String scheduledTime;

    private String doctorAppointmentCode;

    private String bookingCode;

    private Long patientRecordId;

    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClsCode() {
        return clsCode;
    }

    public void setClsCode(String clsCode) {
        this.clsCode = clsCode;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    public String getClsNumber() {
        return clsNumber;
    }

    public void setClsNumber(String clsNumber) {
        this.clsNumber = clsNumber;
    }

    public String getClsDepartment() {
        return clsDepartment;
    }

    public void setClsDepartment(String clsDepartment) {
        this.clsDepartment = clsDepartment;
    }

    public String getClsDoctor() {
        return clsDoctor;
    }

    public void setClsDoctor(String clsDoctor) {
        this.clsDoctor = clsDoctor;
    }

    public Integer getClsPriority() {
        return clsPriority;
    }

    public void setClsPriority(Integer clsPriority) {
        this.clsPriority = clsPriority;
    }

    public Integer getClsCurrentPriority() {
        return clsCurrentPriority;
    }

    public void setClsCurrentPriority(Integer clsCurrentPriority) {
        this.clsCurrentPriority = clsCurrentPriority;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getDoctorAppointmentCode() {
        return doctorAppointmentCode;
    }

    public void setDoctorAppointmentCode(String doctorAppointmentCode) {
        this.doctorAppointmentCode = doctorAppointmentCode;
    }

    public Long getPatientRecordId() {
        return patientRecordId;
    }

    public void setPatientRecordId(Long patientRecordId) {
        this.patientRecordId = patientRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
}
