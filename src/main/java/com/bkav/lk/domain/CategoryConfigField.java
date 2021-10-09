package com.bkav.lk.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "category_config_field")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = CategoryConfigField.class
)
public class CategoryConfigField extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "health_facility_id", nullable = false)
    private HealthFacilities healthFacilities;

    @NotEmpty
    @Column(name = "name")
    private String name;

    @NotEmpty
    @Column(name = "datatype")
    private String dataType;

    @Column(name = "required")
    private Integer required;

    @Column(name = "value")
    private String value;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "status")
    private Integer status;

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryConfigValue> categoryConfigValues;

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public List<CategoryConfigValue> getCategoryConfigValues() {
        return categoryConfigValues;
    }

    public void setCategoryConfigValues(List<CategoryConfigValue> categoryConfigValues) {
        this.categoryConfigValues = categoryConfigValues;
    }

    public HealthFacilities getHealthFacilities() {
        return healthFacilities;
    }

    public void setHealthFacilities(HealthFacilities healthFacilities) {
        this.healthFacilities = healthFacilities;
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
}
