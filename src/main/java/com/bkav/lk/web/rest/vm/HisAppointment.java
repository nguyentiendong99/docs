package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HisAppointment {

    @JsonProperty("dangkykham_id")
    private String bookingCode;

    @JsonProperty("his_makham")
    private String appointmentCode;

    @JsonProperty("his_mabenhnhan")
    private String patientCode;

    @JsonProperty("his_tenbenhnhan")
    private String patientName;

    @JsonProperty("his_sodienthoai")
    private String phone;

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
