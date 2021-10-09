package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SubclinicalDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @JsonProperty("his_makham")
    private String doctorAppointmentCode;

    @JsonProperty("cls_madichvu")
    private String code;

    @JsonProperty("cls_tendichvu")
    private String name;

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

    private Long healthFacilityId;

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

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    @Override
    public String toString() {
        return "{id=" + id +
                ", doctorAppointmentCode='" + doctorAppointmentCode + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
