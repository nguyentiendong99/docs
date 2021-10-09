package com.bkav.lk.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "patient_record")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = PatientRecord.class
)
public class PatientRecord extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_record_code")
    private String patientRecordCode;

    @Column(name = "name")
    private String name;

    @Column(name = "gender")
    private String gender; // male / female / other

    @Column(name = "dob")
    private Instant dob;

    @Column(name = "health_insurance_code")
    private String healthInsuranceCode;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "his_mabenhnhan")
    private String hisPatientCode;

    @ManyToOne
    @JoinColumn(name = "city_code", referencedColumnName = "area_code")
    private Area city;

    @ManyToOne
    @JoinColumn(name = "district_code", referencedColumnName = "area_code")
    private Area district;

    @ManyToOne
    @JoinColumn(name = "ward_code", referencedColumnName = "area_code")
    private Area ward;

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

    public Area getCity() {
        return city;
    }

    public void setCity(Area city) {
        this.city = city;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getWard() {
        return ward;
    }

    public void setWard(Area ward) {
        this.ward = ward;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getHisPatientCode() {
        return hisPatientCode;
    }

    public void setHisPatientCode(String hisPatientCode) {
        this.hisPatientCode = hisPatientCode;
    }
}
