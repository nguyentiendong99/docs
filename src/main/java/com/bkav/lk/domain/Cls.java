package com.bkav.lk.domain;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cls")
public class Cls extends AbstractAuditingEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(name = "cls_code")
    private String clsCode;

    @NotEmpty
    @Column(name = "cls_name")
    private String clsName;

    @NotNull
    @Column(name = "cls_price")
    private BigDecimal clsPrice;

    @Column(name = "note")
    private String note;

    @Column(name = "status")
    private Integer status;

    @NotNull
    @Column(name = "health_facility_id")
    private Long healthFacilityId;

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

    public BigDecimal getClsPrice() {
        return clsPrice;
    }

    public void setClsPrice(BigDecimal clsPrice) {
        this.clsPrice = clsPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }
}
