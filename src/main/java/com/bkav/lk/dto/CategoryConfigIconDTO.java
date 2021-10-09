package com.bkav.lk.dto;


public class CategoryConfigIconDTO {

    private Long id;

    private String type;

    private Integer status;

    private Integer display;

    private String codeMethod;

    private Long healthFacilityId;

    private String icon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDisplay() {
        return display;
    }

    public void setDisplay(Integer display) {
        this.display = display;
    }

    public String getCodeMethod() {
        return codeMethod;
    }

    public void setCodeMethod(String codeMethod) {
        this.codeMethod = codeMethod;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
