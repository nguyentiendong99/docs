package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

public class MedicalDeclarationInfoVM {
    private Long id;

    private String patientRecordCode;

    private String patientRecordName;

    private String gender;

    private String dob;

    private BigDecimal height;

    private BigDecimal weight;

    private String address;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdDate;

    private String healthInsuranceCode;

    private String phoneNumber;

    private String email;

    private String nationsGoTo;

    private String fever;

    private String dyspnoeic;

    private String soreThroat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getHealthInsuranceCode() {
        return healthInsuranceCode;
    }

    public void setHealthInsuranceCode(String healthInsuranceCode) {
        this.healthInsuranceCode = healthInsuranceCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationsGoTo() {
        return nationsGoTo;
    }

    public void setNationsGoTo(String nationsGoTo) {
        this.nationsGoTo = nationsGoTo;
    }

    public String getFever() {
        return fever;
    }

    public void setFever(String fever) {
        this.fever = fever;
    }

    public String getDyspnoeic() {
        return dyspnoeic;
    }

    public void setDyspnoeic(String dyspnoeic) {
        this.dyspnoeic = dyspnoeic;
    }

    public String getSoreThroat() {
        return soreThroat;
    }

    public void setSoreThroat(String soreThroat) {
        this.soreThroat = soreThroat;
    }
}
