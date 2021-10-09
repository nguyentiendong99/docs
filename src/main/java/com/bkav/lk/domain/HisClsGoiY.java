package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "his_cls_goi_y")
public class HisClsGoiY extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cls_madichvu")
    private String clsCode;

    @Column(name = "cls_tendichvu")
    private String clsName;

    @Column(name = "cls_soluong")
    private String clsNumber;

    @Column(name = "cls_phongban")
    private String clsDepartment;

    @Column(name = "cls_bacsy")
    private String clsDoctor;

    @Column(name = "cls_thutu")
    private Integer clsPriority;

    @Column(name = "cls_thutuhientai")
    private Integer clsCurrentPriority;

    @Column(name = "cls_thoigiandk")
    private String scheduledTime;

    @Column(name = "his_makham")
    private String doctorAppointmentCode;

    @Column(name = "madatlich")
    private String bookingCode;

    @Column(name = "patient_record_id")
    private Long patientRecordId;

    @Column(name = "user_id")
    private Long userId;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClsCode() {
        return clsCode;
    }

    public void setClsCode(String clsCode) {
        this.clsCode = clsCode;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    public String getClsNumber() {
        return clsNumber;
    }

    public void setClsNumber(String clsNumber) {
        this.clsNumber = clsNumber;
    }

    public String getClsDepartment() {
        return clsDepartment;
    }

    public void setClsDepartment(String clsDepartment) {
        this.clsDepartment = clsDepartment;
    }

    public String getClsDoctor() {
        return clsDoctor;
    }

    public void setClsDoctor(String clsDoctor) {
        this.clsDoctor = clsDoctor;
    }

    public Integer getClsPriority() {
        return clsPriority;
    }

    public void setClsPriority(Integer clsPriority) {
        this.clsPriority = clsPriority;
    }

    public Integer getClsCurrentPriority() {
        return clsCurrentPriority;
    }

    public void setClsCurrentPriority(Integer clsCurrentPriority) {
        this.clsCurrentPriority = clsCurrentPriority;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getDoctorAppointmentCode() {
        return doctorAppointmentCode;
    }

    public void setDoctorAppointmentCode(String doctorAppointmentCode) {
        this.doctorAppointmentCode = doctorAppointmentCode;
    }

    public Long getPatientRecordId() {
        return patientRecordId;
    }

    public void setPatientRecordId(Long patientRecordId) {
        this.patientRecordId = patientRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
}
