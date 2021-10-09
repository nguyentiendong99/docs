package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SubclinicalVM {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @JsonProperty("his_makham")
    private String doctorAppointmentCode;

    @JsonProperty("cls_madichvu")
    private String code;

    @JsonProperty("cls_tendichvu")
    private String name;

    @JsonProperty("cls_machiso")
    private String medicalTestIndexCode;

    @JsonProperty("cls_tenchiso")
    private String medicalTestIndexName;

    @JsonProperty("cls_giatri")
    private String medicalTestIndexValue;

    @JsonProperty("cls_mamay")
    private String machineCode;

    @JsonProperty("cls_mota")
    private String description;

    @JsonProperty("cls_ketluan")
    private String conclusion;

    @JsonProperty("cls_ngaykq")
    private String resultDate;

    @JsonProperty("cls_kithuatvien")
    private String technician;

    @JsonProperty("cls_phongthuchien")
    private String room;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long patientRecordId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String patientRecordCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String patientRecordName;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMedicalTestIndexCode() {
        return medicalTestIndexCode;
    }

    public void setMedicalTestIndexCode(String medicalTestIndexCode) {
        this.medicalTestIndexCode = medicalTestIndexCode;
    }

    public String getMedicalTestIndexName() {
        return medicalTestIndexName;
    }

    public void setMedicalTestIndexName(String medicalTestIndexName) {
        this.medicalTestIndexName = medicalTestIndexName;
    }

    public String getMedicalTestIndexValue() {
        return medicalTestIndexValue;
    }

    public void setMedicalTestIndexValue(String medicalTestIndexValue) {
        this.medicalTestIndexValue = medicalTestIndexValue;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getResultDate() {
        return resultDate;
    }

    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getPatientRecordName() {
        return patientRecordName;
    }

    public void setPatientRecordName(String patientRecordName) {
        this.patientRecordName = patientRecordName;
    }

    @Override
    public String toString() {
        return "{id=" + id +
                ", doctorAppointmentCode='" + doctorAppointmentCode + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", medicalTestIndexCode='" + medicalTestIndexCode + '\'' +
                ", medicalTestIndexName='" + medicalTestIndexName + '\'' +
                ", medicalTestIndexValue='" + medicalTestIndexValue + '\'' +
                ", machineCode='" + machineCode + '\'' +
                ", description='" + description + '\'' +
                ", conclusion='" + conclusion + '\'' +
                ", resultDate='" + resultDate + '\'' +
                '}';
    }
}
