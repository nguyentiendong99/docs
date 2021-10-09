package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public class MedicalDeclarationInfoDTO {
    private Long id;

    private Integer status;

    private Long patientRecordId;

    private String patientRecordCode;

    private String patientRecordName;

    private String gender;

    private Instant dob;

    private String address;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instant createdDate;

    private List<DetailMedicalDeclarationInfoDTO> details;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientRecordId() {
        return patientRecordId;
    }

    public void setPatientRecordId(Long patientRecordId) {
        this.patientRecordId = patientRecordId;
    }

    public String getPatientRecordCode() {
        return patientRecordCode;
    }

    public void setPatientRecordCode(String patientRecordCode) {
        this.patientRecordCode = patientRecordCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPatientRecordName() {
        return patientRecordName;
    }

    public void setPatientRecordName(String patientRecordName) {
        this.patientRecordName = patientRecordName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Instant getDob() {
        return dob;
    }

    public void setDob(Instant dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public List<DetailMedicalDeclarationInfoDTO> getDetails() {
        return details;
    }

    public void setDetails(List<DetailMedicalDeclarationInfoDTO> details) {
        this.details = details;
    }
}
