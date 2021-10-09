package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MedicalResultDTO {

    @JsonIgnore
    private Long id;

    private String doctorAppointmentCode;

    private String medicalSpecialityName;

    private String healthFacilityName;

    private String examinationDate;

    private Long patientId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoctorAppointmentCode() {
        return doctorAppointmentCode;
    }

    public void setDoctorAppointmentCode(String doctorAppointmentCode) {
        this.doctorAppointmentCode = doctorAppointmentCode;
    }

    public String getMedicalSpecialityName() {
        return medicalSpecialityName;
    }

    public void setMedicalSpecialityName(String medicalSpecialityName) {
        this.medicalSpecialityName = medicalSpecialityName;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public String getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(String examinationDate) {
        this.examinationDate = examinationDate;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
}
