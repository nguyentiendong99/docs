package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HisDoctor {

    @JsonProperty("bacsy_id")
    private String doctorCode;

    @JsonProperty("bacsy_hoten")
    private String doctorName;

    @JsonProperty("bacsy_avatar")
    private String avatar;

    @JsonProperty("bacsy_chucdanh")
    private String academic;

    @JsonProperty("bacsy_chuyenkhoa")
    private String medicalSpeciality;

    @JsonProperty("bacsy_gioitinh")
    private String gender;

    @JsonProperty("bacsy_mota")
    private String description;

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAcademic() {
        return academic;
    }

    public void setAcademic(String academic) {
        this.academic = academic;
    }

    public String getMedicalSpeciality() {
        return medicalSpeciality;
    }

    public void setMedicalSpeciality(String medicalSpeciality) {
        this.medicalSpeciality = medicalSpeciality;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
