package com.bkav.lk.dto;

/**
 * The type Doctor custom config.
 */
public class DoctorCustomConfigDTO {

    private Long fieldId;
    private String fieldName;
    private String value;
    private String dataType;

    public DoctorCustomConfigDTO() {
    }

    public DoctorCustomConfigDTO(Long fieldId, String fieldName, String value, String dataType) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.value = value;
        this.dataType = dataType;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return "DoctorCustomConfigDTO{" +
                "fieldId=" + fieldId +
                ", fieldName='" + fieldName + '\'' +
                ", value='" + value + '\'' +
                ", dataType='" + dataType + '\'' +
                '}';
    }
}
