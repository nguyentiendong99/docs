package com.bkav.lk.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class MedicalServiceDTO implements Serializable {

    private Long id;

    private String code;

    private String name;

    private BigDecimal price;

    private Integer status;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Long healthFacilityId;

    private List<MedicalServiceCustomConfigDTO> medicalServiceCustomConfigDTOS;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public List<MedicalServiceCustomConfigDTO> getMedicalServiceCustomConfigDTOS() {
        return medicalServiceCustomConfigDTOS;
    }

    public void setMedicalServiceCustomConfigDTOS(List<MedicalServiceCustomConfigDTO> medicalServiceCustomConfigDTOS) {
        this.medicalServiceCustomConfigDTOS = medicalServiceCustomConfigDTOS;
    }
}
