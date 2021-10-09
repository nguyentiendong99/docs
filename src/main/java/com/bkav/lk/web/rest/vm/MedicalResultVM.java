package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MedicalResultVM {

    @JsonProperty("his_makham")
    private String doctorAppointmentCode;

    @JsonProperty("his_mabenhnhan")
    private String patientCode;

    @JsonProperty("his_tenbenhnhan")
    private String patientName;

    @JsonProperty("his_mabenh")
    private String diseaseCode;

    @JsonProperty("his_tenbenh")
    private String diseaseName;

    @JsonProperty("his_mabenhkhac")
    private String otherDiseaseCode;

    @JsonProperty("his_dienbien")
    private String evolutions;

    @JsonProperty("his_hoichan")
    private String consultation;

    @JsonProperty("his_phauthuat")
    private String surgery;

    @JsonProperty("his_tenchuyenkhoa")
    private String medicalSpecialityName;

    @JsonProperty("his_tenbenhvien")
    private String healthFacilityName;

    @JsonProperty("his_ngaykham")
    private String examinationDate;

    @JsonProperty("his_tenhinhanh")
    private String imageNames;

    @JsonProperty("his_hinhanh")
    private String imageUrls;

    @JsonProperty("his_thuoc")
    private List<MedicineVM> medicines;

    @JsonProperty("his_cls")
    private List<SubclinicalVM> subclinicals;

    public String getDoctorAppointmentCode() {
        return doctorAppointmentCode;
    }

    public void setDoctorAppointmentCode(String doctorAppointmentCode) {
        this.doctorAppointmentCode = doctorAppointmentCode;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getDiseaseCode() {
        return diseaseCode;
    }

    public void setDiseaseCode(String diseaseCode) {
        this.diseaseCode = diseaseCode;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getOtherDiseaseCode() {
        return otherDiseaseCode;
    }

    public void setOtherDiseaseCode(String otherDiseaseCode) {
        this.otherDiseaseCode = otherDiseaseCode;
    }

    public String getEvolutions() {
        return evolutions;
    }

    public void setEvolutions(String evolutions) {
        this.evolutions = evolutions;
    }

    public String getConsultation() {
        return consultation;
    }

    public void setConsultation(String consultation) {
        this.consultation = consultation;
    }

    public String getSurgery() {
        return surgery;
    }

    public void setSurgery(String surgery) {
        this.surgery = surgery;
    }

    public String getMedicalSpecialityName() {
        return medicalSpecialityName;
    }

    public void setMedicalSpecialityName(String medicalSpecialityName) {
        this.medicalSpecialityName = medicalSpecialityName;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public String getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(String examinationDate) {
        this.examinationDate = examinationDate;
    }

    public String getImageNames() {
        return imageNames;
    }

    public void setImageNames(String imageNames) {
        this.imageNames = imageNames;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<MedicineVM> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<MedicineVM> medicines) {
        this.medicines = medicines;
    }

    public List<SubclinicalVM> getSubclinicals() {
        return subclinicals;
    }

    public void setSubclinicals(List<SubclinicalVM> subclinicals) {
        this.subclinicals = subclinicals;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @Override
    public String toString() {
        return "{doctorAppointmentCode='" + doctorAppointmentCode + '\'' +
                ", patientCode='" + patientCode + '\'' +
                ", diseaseCode='" + diseaseCode + '\'' +
                ", diseaseName='" + diseaseName + '\'' +
                ", otherDiseaseCode='" + otherDiseaseCode + '\'' +
                ", evolutions='" + evolutions + '\'' +
                ", consultation='" + consultation + '\'' +
                ", surgery='" + surgery + '\'' +
                '}';
    }
}
