package com.bkav.lk.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "doctor")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Doctor.class
)
public class Doctor extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(name = "code")
    private String code;

    @NotEmpty
    @Column(name = "name")
    private String name;

    @Column(name = "dob")
    private String dob;

    @Column(name = "education")
    private String education;

    @Column(name = "experience")
    private String experience;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "avatar")
    private String avatar;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "medical_speciality_id")
    private MedicalSpeciality medicalSpeciality;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "status")
    private Integer status;

    @Column(name = "is_different_facility")
    private Integer differentFacility;

    @Column(name = "gender")
    private String gender;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "health_facility_id")
    private Long healthFacilityId;

    @ManyToOne
    @JoinColumn(name = "academic_id", nullable = false)
    private Academic academic;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<DoctorSchedule> doctorSchedules;

    public MedicalSpeciality getMedicalSpeciality() {
        return medicalSpeciality;
    }

    public void setMedicalSpeciality(MedicalSpeciality medicalSpeciality) {
        this.medicalSpeciality = medicalSpeciality;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long jobTitleId) {
        this.positionId = jobTitleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<DoctorSchedule> getDoctorSchedules() {
        return doctorSchedules;
    }

    public void setDoctorSchedules(List<DoctorSchedule> doctorSchedules) {
        this.doctorSchedules = doctorSchedules;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public Academic getAcademic() {
        return academic;
    }

    public void setAcademic(Academic academic) {
        this.academic = academic;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
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
}
