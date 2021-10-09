package com.bkav.lk.web.rest.vm;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class HisPatientRecord {

    @JsonProperty("hososuckhoe_id")
    private String patientRecordCode;

    @JsonProperty("benhnhan_hoten")
    private String name;

    @JsonProperty("benhnhan_gioitinh")
    private String gender;

    @JsonProperty("benhnhan_ngaysinh")
    private String dob;

    @JsonProperty("benhnhan_dienthoai")
    private String phone;

    @JsonProperty("benhnhan_diachi")
    private String address;

    @JsonProperty("diachi_phuongxa")
    private String ward;

    @JsonProperty("diachi_quanhuyen")
    private String district;

    @JsonProperty("diachi_tinh")
    private String city;

    @JsonProperty("benhnhan_thebhyt")
    private String healthInsuranceCode;

    @JsonProperty("benhnhan_chieucao")
    private String height;

    @JsonProperty("benhnhan_cannang")
    private String weight;

    @JsonProperty("dangkykham_id")
    private String bookingCode;

    @JsonProperty("dangkykham_ngaydk")
    private String bookingDate;

    @JsonProperty("dangkykham_giodk")
    private String bookingTime;

    @JsonProperty("dangkykham_bacsyid")
    private String doctorCode;

    @JsonProperty("dangkykham_phongkhamid")
    private String clinicCode;

    @JsonProperty("dangkykham_lydo")
    private String reason;

    @JsonProperty("his_makham")
    private String appointmentCode;

    @JsonProperty("his_makhamcu")
    private String oldAppointmentCode;

    @JsonProperty("his_mabenhnhan")
    private String patientCode;

    public HisPatientRecord() {
    }

    public HisPatientRecord(DoctorAppointment doctorAppointment) {
        if (Objects.nonNull(doctorAppointment)) {
            if (Objects.nonNull(doctorAppointment.getPatientRecord())) {
                this.patientRecordCode = doctorAppointment.getPatientRecord().getPatientRecordCode();
                this.name = doctorAppointment.getPatientRecord().getName();
                this.gender = doctorAppointment.getPatientRecord().getGender();
                this.dob = Objects.nonNull(doctorAppointment.getPatientRecord().getDob()) ?
                        DateUtils.convertFromInstantToString(doctorAppointment.getPatientRecord().getDob()) : null;
                this.phone = doctorAppointment.getPatientRecord().getPhone();
                this.address = doctorAppointment.getPatientRecord().getAddress();
                this.ward = Objects.nonNull(doctorAppointment.getPatientRecord().getWard()) ? doctorAppointment.getPatientRecord().getWard().getName() : null;
                this.district = Objects.nonNull(doctorAppointment.getPatientRecord().getDistrict()) ? doctorAppointment.getPatientRecord().getDistrict().getName() : null;
                this.city = Objects.nonNull(doctorAppointment.getPatientRecord().getCity()) ? doctorAppointment.getPatientRecord().getCity().getName() : null;
                this.healthInsuranceCode = doctorAppointment.getPatientRecord().getHealthInsuranceCode();
                this.height = Objects.nonNull(doctorAppointment.getPatientRecord().getHeight()) ? doctorAppointment.getPatientRecord().getHeight().toString() : null;
                this.weight = Objects.nonNull(doctorAppointment.getPatientRecord().getWeight()) ? doctorAppointment.getPatientRecord().getWeight().toString() : null;
            }
            this.bookingCode = doctorAppointment.getBookingCode();
            this.bookingDate = Objects.nonNull(doctorAppointment.getStartTime()) ? DateUtils.convertFromInstantToString(doctorAppointment.getStartTime()) : null;
            if (Objects.nonNull(doctorAppointment.getStartTime()) &&  Objects.nonNull(doctorAppointment.getEndTime())) {
                this.bookingTime = DateUtils.convertFromInstantToHour(doctorAppointment.getStartTime()) + '-' + DateUtils.convertFromInstantToHour(doctorAppointment.getEndTime());
            }
            this.reason = doctorAppointment.getMedicalReason();
            this.doctorCode = Objects.nonNull(doctorAppointment.getDoctor()) ? doctorAppointment.getDoctor().getCode() : null;
            this.clinicCode = Objects.nonNull(doctorAppointment.getClinic()) ? doctorAppointment.getClinic().getCode() : null;
        }
    }

    public String getPatientRecordCode() {
        return patientRecordCode;
    }

    public void setPatientRecordCode(String patientRecordCode) {
        this.patientRecordCode = patientRecordCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHealthInsuranceCode() {
        return healthInsuranceCode;
    }

    public void setHealthInsuranceCode(String healthInsuranceCode) {
        this.healthInsuranceCode = healthInsuranceCode;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public String getClinicCode() {
        return clinicCode;
    }

    public void setClinicCode(String clinicCode) {
        this.clinicCode = clinicCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getOldAppointmentCode() {
        return oldAppointmentCode;
    }

    public void setOldAppointmentCode(String oldAppointmentCode) {
        this.oldAppointmentCode = oldAppointmentCode;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }
}
