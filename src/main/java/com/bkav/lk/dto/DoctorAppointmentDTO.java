package com.bkav.lk.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class DoctorAppointmentDTO implements Serializable {

    private Long id;

    private String bookingCode;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @NotBlank
    private String medicalReason;

    private String changeAppointmentReason;

    private String rejectReason;

    private Integer haveHealthInsurance;

    private Integer isReExamination;

    private String appointmentCode;

    private String oldAppointmentCode;

    private String approvedBy;

    private Integer status;

    private Integer isConfirmed;

    private Instant reExaminationDate;

    private String reExaminationDateFormat;

    private Long doctorId;

    private String doctorName;

    @NotNull
    private Long patientRecordId;

    private String patientName;

    private Instant patientDob;

    private String patientDobFormat;

    private String patientCode;

    private String patientAddress;

    private Long clinicId;

    private String clinicName;

    @NotNull
    private Long healthFacilityId;

    private Long medicalSpecialityId;

    private Long medicalServiceId;

    private Integer type;

    private String medicalServiceName;

    private BigDecimal medicalServicePrice;

    private String content;

    private Integer paymentStatus;

    private Long userId;

    private BigDecimal amount;

    private String bankCode;

    private String paymentMethod;

    private String academicCode;

    private String appointmentDate;

    private String appointmentTime;

    private String doctorGender;

    private Integer workingTime;

    private String healthInsuranceCode;

    private String medicalSpecialityName;

    private String oldBookingCode;

    public BigDecimal getMedicalServicePrice() {
        return medicalServicePrice;
    }

    public void setMedicalServicePrice(BigDecimal medicalServicePrice) {
        this.medicalServicePrice = medicalServicePrice;
    }

    public String getMedicalSpecialityName() {
        return medicalSpecialityName;
    }

    public void setMedicalSpecialityName(String medicalSpecialityName) {
        this.medicalSpecialityName = medicalSpecialityName;
    }

    public Integer getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(Integer workingTime) {
        this.workingTime = workingTime;
    }

    public String getHealthInsuranceCode() {
        return healthInsuranceCode;
    }

    public void setHealthInsuranceCode(String healthInsuranceCode) {
        this.healthInsuranceCode = healthInsuranceCode;
    }

    public String getDoctorGender() {
        return doctorGender;
    }

    public void setDoctorGender(String doctorGender) {
        this.doctorGender = doctorGender;
    }

    public String getPatientDobFormat() {
        return patientDobFormat;
    }

    public void setPatientDobFormat(String patientDobFormat) {
        this.patientDobFormat = patientDobFormat;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public Instant getPatientDob() {
        return patientDob;
    }

    public void setPatientDob(Instant patientDob) {
        this.patientDob = patientDob;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
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

    public String getMedicalReason() {
        return medicalReason;
    }

    public void setMedicalReason(String medicalReason) {
        this.medicalReason = medicalReason;
    }

    public String getChangeAppointmentReason() {
        return changeAppointmentReason;
    }

    public void setChangeAppointmentReason(String changeAppointmentReason) {
        this.changeAppointmentReason = changeAppointmentReason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getHaveHealthInsurance() {
        return haveHealthInsurance;
    }

    public void setHaveHealthInsurance(Integer haveHealthInsurance) {
        this.haveHealthInsurance = haveHealthInsurance;
    }

    public Integer getIsReExamination() {
        return isReExamination;
    }

    public void setIsReExamination(Integer isReExamination) {
        this.isReExamination = isReExamination;
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsConfirmed() {
        return isConfirmed;
    }

    public void setIsConfirmed(Integer isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public Instant getReExaminationDate() {
        return reExaminationDate;
    }

    public void setReExaminationDate(Instant reExaminationDate) {
        this.reExaminationDate = reExaminationDate;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getPatientRecordId() {
        return patientRecordId;
    }

    public void setPatientRecordId(Long patientRecordId) {
        this.patientRecordId = patientRecordId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public Long getMedicalSpecialityId() {
        return medicalSpecialityId;
    }

    public void setMedicalSpecialityId(Long medicalSpecialtyId) {
        this.medicalSpecialityId = medicalSpecialtyId;
    }

    public Long getMedicalServiceId() {
        return medicalServiceId;
    }

    public void setMedicalServiceId(Long medicalServiceId) {
        this.medicalServiceId = medicalServiceId;
    }

    public Integer getType() {
        return type;
    }

    public String getMedicalServiceName() {
        return medicalServiceName;
    }

    public void setMedicalServiceName(String medicalServiceName) {
        this.medicalServiceName = medicalServiceName;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAcademicCode() {
        return academicCode;
    }

    public void setAcademicCode(String academicCode) {
        this.academicCode = academicCode;
    }

    public String getOldBookingCode() {
        return oldBookingCode;
    }

    public void setOldBookingCode(String oldBookingCode) {
        this.oldBookingCode = oldBookingCode;
    }

    public String getReExaminationDateFormat() {
        return reExaminationDateFormat;
    }

    public void setReExaminationDateFormat(String reExaminationDateFormat) {
        this.reExaminationDateFormat = reExaminationDateFormat;
    }
}
