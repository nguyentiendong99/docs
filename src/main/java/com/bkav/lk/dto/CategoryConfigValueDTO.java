package com.bkav.lk.dto;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * @author hieu.daominh
 *
 * The type Category config value dto.
 */
public class CategoryConfigValueDTO implements Serializable {

    private Long id;

    @NotEmpty
    private String value;

    private Integer status;

    private Long objectId;

    @NotNull
    private Long fieldId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "CategoryConfigValueDTO{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", status=" + status +
                ", objectId=" + objectId +
                ", fieldId=" + fieldId +
                '}';
    }
}
