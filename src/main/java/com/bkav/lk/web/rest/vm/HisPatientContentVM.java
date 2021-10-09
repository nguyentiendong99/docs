package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class HisPatientContentVM {

    @JsonProperty("his_mabenhnhan")
    private String connectionCode;

    @JsonProperty("his_tenbenhnhan")
    private String patientRecordName;

    @JsonProperty("his_sodienthoai")
    private String patientRecordPhone;

    @JsonProperty("his_mabenhvien")
    private String healthFacilityCode;

    @JsonProperty("his_tenbenhvien")
    private String healthFacilityName;

    @JsonProperty("his_danhsachketquakham")
    private List<ShortedMedicalResultVM> medicalResults;

    public HisPatientContentVM() {
    }

    public HisPatientContentVM(String connectionCode, String patientRecordName, String patientRecordPhone,
                               String healthFacilityCode, String healthFacilityName) {
        this.connectionCode = connectionCode;
        this.patientRecordName = patientRecordName;
        this.patientRecordPhone = patientRecordPhone;
        this.healthFacilityCode = healthFacilityCode;
        this.healthFacilityName = healthFacilityName;
    }

    public String getConnectionCode() {
        return connectionCode;
    }

    public void setConnectionCode(String connectionCode) {
        this.connectionCode = connectionCode;
    }

    public String getPatientRecordName() {
        return patientRecordName;
    }

    public void setPatientRecordName(String patientRecordName) {
        this.patientRecordName = patientRecordName;
    }

    public String getPatientRecordPhone() {
        return patientRecordPhone;
    }

    public void setPatientRecordPhone(String patientRecordPhone) {
        this.patientRecordPhone = patientRecordPhone;
    }

    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(String healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public List<ShortedMedicalResultVM> getMedicalResults() {
        return medicalResults;
    }

    public void setMedicalResults(List<ShortedMedicalResultVM> medicalResults) {
        this.medicalResults = medicalResults;
    }
}
