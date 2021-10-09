package com.bkav.lk.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "doctor_appointment")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = DoctorAppointment.class
)
public class DoctorAppointment extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code")
    private String bookingCode;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "medical_reason")
    private String medicalReason;

    @Column(name = "change_appointment_reason", length = 1000)
    private String changeAppointmentReason;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "have_health_insurance")
    private Integer haveHealthInsurance;

    @Column(name = "is_reexamination")
    private Integer isReExamination;

    @Column(name = "appointment_code")
    private String appointmentCode;

    @Column(name = "old_appointment_code")
    private String oldAppointmentCode;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "status")
    private Integer status;

    @Column(name = "is_confirmed")
    private Integer isConfirmed;

    @Column(name = "reexamination_date")
    private Instant reExaminationDate;

    @Column(name = "type")
    private Integer type;

    @ManyToOne
    @JoinColumn(name = "medical_speciality_id")
    private MedicalSpeciality medicalSpeciality;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @ManyToOne
    @JoinColumn(name = "patient_record_id")
    private PatientRecord patientRecord;

    @Column(name = "health_facility_id")
    private Long healthFacilityId;

    @ManyToOne
    @JoinColumn(name = "medical_service_id")
    private MedicalService medicalService;

    @Column(name = "payment_status")
    private Integer paymentStatus;

    @Column(name = "old_booking_code")
    private String oldBookingCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getMedicalReason() {
        return medicalReason;
    }

    public void setMedicalReason(String medicalReason) {
        this.medicalReason = medicalReason;
    }

    public String getChangeAppointmentReason() {
        return changeAppointmentReason;
    }

    public void setChangeAppointmentReason(String changeAppointmentReason) {
        this.changeAppointmentReason = changeAppointmentReason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getHaveHealthInsurance() {
        return haveHealthInsurance;
    }

    public void setHaveHealthInsurance(Integer haveHealthInsurance) {
        this.haveHealthInsurance = haveHealthInsurance;
    }

    public Integer getIsReExamination() {
        return isReExamination;
    }

    public void setIsReExamination(Integer isReExamination) {
        this.isReExamination = isReExamination;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getOldAppointmentCode() {
        return oldAppointmentCode;
    }

    public void setOldAppointmentCode(String oldAppointmentCode) {
        this.oldAppointmentCode = oldAppointmentCode;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsConfirmed() {
        return isConfirmed;
    }

    public void setIsConfirmed(Integer isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public Instant getReExaminationDate() {
        return reExaminationDate;
    }

    public void setReExaminationDate(Instant reExaminationDate) {
        this.reExaminationDate = reExaminationDate;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public MedicalSpeciality getMedicalSpeciality() {
        return medicalSpeciality;
    }

    public void setMedicalSpeciality(MedicalSpeciality medicalSpeciality) {
        this.medicalSpeciality = medicalSpeciality;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public PatientRecord getPatientRecord() {
        return patientRecord;
    }

    public void setPatientRecord(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOldBookingCode() {
        return oldBookingCode;
    }

    public void setOldBookingCode(String oldBookingCode) {
        this.oldBookingCode = oldBookingCode;
    }
}
