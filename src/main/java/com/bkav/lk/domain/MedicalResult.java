package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "his_ketquakham")
public class MedicalResult extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "his_makham")
    private String doctorAppointmentCode;

    @Column(name = "his_tenchuyenkhoa")
    private String medicalSpecialityName;

    @Column(name = "his_tenbenhvien")
    private String healthFacilityName;

    @Column(name = "his_ngaykham")
    private String examinationDate;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Override
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
