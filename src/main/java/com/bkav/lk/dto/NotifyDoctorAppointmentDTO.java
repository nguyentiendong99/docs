package com.bkav.lk.dto;

import java.time.Instant;

public class NotifyDoctorAppointmentDTO {
    private String bookingCode;
    private Long userId;
    private String doctorName;
    private String clinicName;
    private String healthFacilitiesName;
    private String rejectReason;
    private Instant startTime;
    private Instant endTime;
    private Long idDoctorAppointment;
    private String changeAppointmentReason;
    private String friendlyDateTimeFormat;

    public String getFriendlyDateTimeFormat() {
        return friendlyDateTimeFormat;
    }

    public void setFriendlyDateTimeFormat(String friendlyDateTimeFormat) {
        this.friendlyDateTimeFormat = friendlyDateTimeFormat;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getHealthFacilitiesName() {
        return healthFacilitiesName;
    }

    public void setHealthFacilitiesName(String healthFacilitiesName) {
        this.healthFacilitiesName = healthFacilitiesName;
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

    public String getRejectReason() {
        return rejectReason;
    }

    public Long getIdDoctorAppointment() {
        return idDoctorAppointment;
    }

    public void setIdDoctorAppointment(Long idDoctorAppointment) {
        this.idDoctorAppointment = idDoctorAppointment;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getChangeAppointmentReason() {
        return changeAppointmentReason;
    }

    public void setChangeAppointmentReason(String changeAppointmentReason) {
        this.changeAppointmentReason = changeAppointmentReason;
    }
}
