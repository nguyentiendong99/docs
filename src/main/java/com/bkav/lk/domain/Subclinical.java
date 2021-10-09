package com.bkav.lk.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "his_cls")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Doctor.class
)
public class Subclinical extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "his_makham")
    private String doctorAppointmentCode;

    @Column(name = "cls_madichvu")
    private String code;

    @Column(name = "cls_tendichvu")
    private String name;

    @Column(name = "cls_kithuatvien")
    private String technician;

    @Column(name = "cls_phongthuchien")
    private String room;

    @Column(name = "status")
    private Integer status;

    @Column(name = "health_facility_id")
    private Long healthFacilityId;

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


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }
}
