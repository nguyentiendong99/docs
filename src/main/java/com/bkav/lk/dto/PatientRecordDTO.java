package com.bkav.lk.dto;

import com.bkav.lk.web.errors.validation.DobConstraint;
import com.bkav.lk.web.errors.validation.EmailConstraint;
import com.bkav.lk.web.errors.validation.PhoneNumberConstraint;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class PatientRecordDTO implements Serializable {

    private Long id;

    private String patientRecordCode;

    @NotBlank
    private String name;

    @NotEmpty
    private String gender;

    @NotNull
    private Instant dob;

    private String healthInsuranceCode;

    private BigDecimal height;

    private BigDecimal weight;

    private String address;

    @NotEmpty
    @PhoneNumberConstraint
    private String phone;

    @EmailConstraint
    private String email;

    private String avatar;

    private Long userId;

    private String cityName;

    @NotEmpty
    private String cityCode;

    private String districtName;

    @NotEmpty
    private String districtCode;

    private String wardName;

    @NotEmpty
    private String wardCode;

    @NotEmpty
    private String relationship;

    private Integer status;

    private String hisPatientCode;

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

    public Instant getDob() {
        return dob;
    }

    public void setDob(Instant dob) {
        this.dob = dob;
    }

    public String getHealthInsuranceCode() {
        return healthInsuranceCode;
    }

    public void setHealthInsuranceCode(String healthInsuranceCode) {
        this.healthInsuranceCode = healthInsuranceCode;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHisPatientCode() {
        return hisPatientCode;
    }

    public void setHisPatientCode(String hisPatientCode) {
        this.hisPatientCode = hisPatientCode;
    }
}
