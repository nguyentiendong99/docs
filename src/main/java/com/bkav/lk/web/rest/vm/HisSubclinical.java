package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HisSubclinical {

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
}
