package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "area")
public class Area implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "area_code")
    private String areaCode;

    @Column(name = "alias")
    private String alias;

    @Column(name = "parent_code")
    private String parentCode;

    @Column(name = "type")
    private String type;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "level")
    private Integer level;

    @Column(name = "status")
    private Integer status;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "short_name")
    private String shortName;

    public Area() {
    }
    public Area(Long id, String name,  String areaCode,  String alias,  String parentCode,
                 String type, Integer level, String postalCode, Integer status, Integer priority,
                Double latitude, Double longitude, String shortName) {
        this.id = id;
        this.name = name;
        this.areaCode = areaCode;
        this.alias = alias;
        this.parentCode = parentCode;
        this.type = type;
        this.level = level;
        this.postalCode = postalCode;
        this.status = status;
        this.priority = priority;
        this.latitude = latitude;
        this.longitude = longitude;
        this.shortName = shortName;
    }

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

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
