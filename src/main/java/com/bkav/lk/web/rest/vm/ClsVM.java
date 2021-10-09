package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClsVM {

    @JsonProperty("cls_madichvu")
    private String clsCode;

    @JsonProperty("cls_tendichvu")
    private String clsName;

    @JsonProperty("cls_soluong")
    private String clsNumber;

    @JsonProperty("cls_phongban")
    private String clsDepartment;

    @JsonProperty("cls_bacsy")
    private String clsDoctor;

    @JsonProperty("cls_thutu")
    private int clsPriority;

    @JsonProperty("cls_thutuhientai")
    private int clsCurrentPriority;

    @JsonProperty("cls_thoigiandk")
    private String scheduledTime;

    public String getClsCode() {
        return clsCode;
    }

    public void setClsCode(String clsCode) {
        this.clsCode = clsCode;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    public String getClsNumber() {
        return clsNumber;
    }

    public void setClsNumber(String clsNumber) {
        this.clsNumber = clsNumber;
    }

    public String getClsDepartment() {
        return clsDepartment;
    }

    public void setClsDepartment(String clsDepartment) {
        this.clsDepartment = clsDepartment;
    }

    public String getClsDoctor() {
        return clsDoctor;
    }

    public void setClsDoctor(String clsDoctor) {
        this.clsDoctor = clsDoctor;
    }

    public int getClsPriority() {
        return clsPriority;
    }

    public void setClsPriority(int clsPriority) {
        this.clsPriority = clsPriority;
    }

    public int getClsCurrentPriority() {
        return clsCurrentPriority;
    }

    public void setClsCurrentPriority(int clsCurrentPriority) {
        this.clsCurrentPriority = clsCurrentPriority;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
