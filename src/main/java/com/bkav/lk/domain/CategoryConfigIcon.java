package com.bkav.lk.domain;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "category_config_icon")
public class CategoryConfigIcon extends AbstractAuditingEntity implements Serializable {
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

    @Column(name = "code_method")
    private String codeMethod;

    @ManyToOne
    @JoinColumn(name = "health_facility_id", nullable = false)
    private HealthFacilities healthFacilities;

    @Column(name = "icon")
    private String icon;


    @Override
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

    public HealthFacilities getHealthFacilities() {
        return healthFacilities;
    }

    public void setHealthFacilities(HealthFacilities healthFacilities) {
        this.healthFacilities = healthFacilities;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
