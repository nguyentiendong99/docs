package com.bkav.lk.domain;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "category_config_field_main")
public class CategoryConfigFieldMain extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    private Integer status;

    @Column(name = "display")
    private Integer display;

    @Column(name = "index_position")
    private Integer indexPosition;

    @Column(name = "column_name")
    private String columnName;

    @ManyToOne
    @JoinColumn(name = "health_facility_id", nullable = false)
    private HealthFacilities healthFacilities;

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

    public HealthFacilities getHealthFacilities() {
        return healthFacilities;
    }

    public void setHealthFacilities(HealthFacilities healthFacilities) {
        this.healthFacilities = healthFacilities;
    }
}
