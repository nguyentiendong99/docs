package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShortedMedicalResultVM {

    @JsonProperty("his_makham")
    private String doctorAppointmentCode;

    @JsonProperty("his_tenchuyenkhoa")
    private String medicalSpecialityName;

    @JsonProperty("his_ngaykham")
    private String examinationDate;

    public ShortedMedicalResultVM() {}

    public ShortedMedicalResultVM(String doctorAppointmentCode, String medicalSpecialityName, String examinationDate) {
        this.doctorAppointmentCode = doctorAppointmentCode;
        this.medicalSpecialityName = medicalSpecialityName;
        this.examinationDate = examinationDate;
    }

    public String getDoctorAppointmentCode() {
        return doctorAppointmentCode;
    }

    public void setDoctorAppointmentCode(String doctorAppointmentCode) {
        this.doctorAppointmentCode = doctorAppointmentCode;
    }

    public String getMedicalSpecialityName() {
        return medicalSpecialityName;
    }

    public void setMedicalSpecialityName(String medicalSpecialityName) {
        this.medicalSpecialityName = medicalSpecialityName;
    }

    public String getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(String examinationDate) {
        this.examinationDate = examinationDate;
    }
}
