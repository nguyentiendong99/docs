package com.bkav.lk.dto;

import java.math.BigDecimal;
import java.util.List;

public class ClsDTO {

    private Long id;

    private String clsCode;

    private String clsName;

    private BigDecimal clsPrice;

    private String note;

    private Integer status;

    private Long healthFacilityId;

    private List<ClsCustomConfigDTO> clsCustomConfigDTOS;

    public ClsDTO() {
    }

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

    public List<ClsCustomConfigDTO> getClsCustomConfigDTOS() {
        return clsCustomConfigDTOS;
    }

    public void setClsCustomConfigDTOS(List<ClsCustomConfigDTO> clsCustomConfigDTOS) {
        this.clsCustomConfigDTOS = clsCustomConfigDTOS;
    }
}
