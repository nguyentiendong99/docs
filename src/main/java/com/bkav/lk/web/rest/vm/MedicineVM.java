package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MedicineVM {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @JsonProperty("his_makham")
    private String doctorAppointmentCode;

    @JsonProperty("his_mathuoc")
    private String code;

    @JsonProperty("his_tenthuoc")
    private String name;

    @JsonProperty("his_donvitinh")
    private String unitOfCalculation;

    @JsonProperty("his_hamluong")
    private String concentration;

    @JsonProperty("his_duongdung")
    private String drugAdministration;

    @JsonProperty("his_lieudung")
    private String dosage;

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

    public String getUnitOfCalculation() {
        return unitOfCalculation;
    }

    public void setUnitOfCalculation(String unitOfCalculation) {
        this.unitOfCalculation = unitOfCalculation;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public String getDrugAdministration() {
        return drugAdministration;
    }

    public void setDrugAdministration(String drugAdministration) {
        this.drugAdministration = drugAdministration;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    @Override
    public String toString() {
        return "{id=" + id +
                ", doctorAppointmentCode='" + doctorAppointmentCode + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", unitOfCalculation='" + unitOfCalculation + '\'' +
                ", concentration='" + concentration + '\'' +
                ", drugAdministration='" + drugAdministration + '\'' +
                ", dosage='" + dosage + '\'' +
                '}';
    }
}
