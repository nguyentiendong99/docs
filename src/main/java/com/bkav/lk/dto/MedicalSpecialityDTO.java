package com.bkav.lk.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class MedicalSpecialityDTO {

    private Long id;
    private String code;
    @NotEmpty
    private String name;
    @NotNull
    private Integer status;
    private String description;
    private Long healthFacilityId;
    private String healthFacilityCode;
    private String healthFacilityName;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(String healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    @Override
    public String toString() {
        return "{id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", healthFacilityId=" + healthFacilityId +'}';
    }
}
