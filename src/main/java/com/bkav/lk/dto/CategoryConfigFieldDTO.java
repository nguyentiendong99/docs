package com.bkav.lk.dto;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * @author hieu.daominh
 *
 * The type Category config field dto.
 */
public class CategoryConfigFieldDTO implements Serializable {

    private Long id;

    @NotNull
    private Long healthFacilityId;

    @NotEmpty
    private String name;

    private Integer status;

    @NotNull
    private String dataType;

    private Integer required;

    private String value;

    private String type;

    private String description;

    private String configType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    @Override
    public String toString() {
        return "CategoryConfigFieldDTO{" +
                "id=" + id +
                ", healthFacilityId=" + healthFacilityId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", dataType='" + dataType + '\'' +
                ", type='" + type + '\'' +
                ", required=" + required +
                ", configType=" + configType +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
