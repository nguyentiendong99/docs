package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "medical_declaration_info")
public class MedicalDeclarationInfo extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private Integer status;

    @ManyToOne
    @JoinColumn(name = "patient_record_id")
    private PatientRecord patientRecord;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public PatientRecord getPatientRecord() {
        return patientRecord;
    }

    public void setPatientRecord(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
    }
}
