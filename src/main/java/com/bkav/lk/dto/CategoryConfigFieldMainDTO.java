package com.bkav.lk.dto;

public class CategoryConfigFieldMainDTO {

    private Long id;

    private String type;

    private Integer status;

    private Integer display;

    private Integer indexPosition;

    private String columnName;

    private Long healthFacilityId;

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

    public Integer getIndexPosition() {
        return indexPosition;
    }

    public void setIndexPosition(Integer indexPosition) {
        this.indexPosition = indexPosition;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }
}
