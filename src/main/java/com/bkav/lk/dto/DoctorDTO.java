package com.bkav.lk.dto;

import com.bkav.lk.web.rest.vm.HisDoctor;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class DoctorDTO implements Serializable {

    private Long id;

    private String code;

    @NotEmpty
    private String name;

    private String dob;

    private String education;

    private String experience;

    private String phone;

    private String email;

    private Long clinicId;

    private String clinicName;

    private String clinicCode;

    @NotNull
    private Integer status;

    private String avatar;

    private Long medicalSpecialityId;

    private String medicalSpecialityName;

    private String medicalSpecialityCode;

    private String description;

    private Integer differentFacility;

    private String gender;

    private Long positionId;

    private String positionName;

    private Long healthFacilityId;

    private String healthFacilityName;

    private Long academicId;

    private String academicName;

    private String academicCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer workingTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String workingTimeAvailable;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String averagePoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalFeedback;

    private List<DoctorCustomConfigDTO> doctorCustomConfigDTOS;

    public DoctorDTO() {
    }

    public DoctorDTO(HisDoctor hisDoctor) {
        this.code = hisDoctor.getDoctorCode();
        this.name = hisDoctor.getDoctorName();
        this.medicalSpecialityCode = hisDoctor.getMedicalSpeciality();
        this.academicCode = hisDoctor.getAcademic();
        this.avatar = hisDoctor.getAvatar();
        this.gender = hisDoctor.getGender();
        this.description = hisDoctor.getDescription();
    }

    public String getWorkingTimeAvailable() {
        return workingTimeAvailable;
    }

    public void setWorkingTimeAvailable(String workingTimeAvailable) {
        this.workingTimeAvailable = workingTimeAvailable;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
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

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public String getClinicName() {
        return clinicName;
    }

    public String getClinicCode() {
        return clinicCode;
    }

    public void setClinicCode(String clinicCode) {
        this.clinicCode = clinicCode;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getMedicalSpecialityId() {
        return medicalSpecialityId;
    }

    public void setMedicalSpecialityId(Long medicalSpecialityId) {
        this.medicalSpecialityId = medicalSpecialityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(Integer workingTime) {
        this.workingTime = workingTime;
    }

    public String getMedicalSpecialityName() {
        return medicalSpecialityName;
    }

    public void setMedicalSpecialityName(String medicalSpecialityName) {
        this.medicalSpecialityName = medicalSpecialityName;
    }

    public Long getAcademicId() {
        return academicId;
    }

    public void setAcademicId(Long academicId) {
        this.academicId = academicId;
    }

    public String getAcademicName() {
        return academicName;
    }

    public void setAcademicName(String academicName) {
        this.academicName = academicName;
    }

    public Integer getDifferentFacility() {
        return differentFacility;
    }

    public void setDifferentFacility(Integer differentFacility) {
        this.differentFacility = differentFacility;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalSpecialityCode() {
        return medicalSpecialityCode;
    }

    public void setMedicalSpecialityCode(String medicalSpecialityCode) {
        this.medicalSpecialityCode = medicalSpecialityCode;
    }

    public String getAcademicCode() {
        return academicCode;
    }

    public void setAcademicCode(String academicCode) {
        this.academicCode = academicCode;
    }

    public String getAveragePoint() {
        return averagePoint;
    }

    public void setAveragePoint(String averagePoint) {
        this.averagePoint = averagePoint;
    }

    public Integer getTotalFeedback() {
        return totalFeedback;
    }

    public void setTotalFeedback(Integer totalFeedback) {
        this.totalFeedback = totalFeedback;
    }

    public List<DoctorCustomConfigDTO> getDoctorCustomConfigDTOS() {
        return doctorCustomConfigDTOS;
    }

    public void setDoctorCustomConfigDTOS(List<DoctorCustomConfigDTO> doctorCustomConfigDTOS) {
        this.doctorCustomConfigDTOS = doctorCustomConfigDTOS;
    }

    @Override
    public String toString() {
        return "{code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", avatar='" + avatar + '\'' +
                ", description='" + description + '\'' +
                ", differentFacility=" + differentFacility +
                ", gender=" + gender + '}';
    }
}
