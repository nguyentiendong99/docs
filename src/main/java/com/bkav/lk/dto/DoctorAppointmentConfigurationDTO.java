package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class DoctorAppointmentConfigurationDTO implements Serializable {

    private Long id;

    private Long healthFacilitiesId;

    private Integer appointmentDaily;

    private Integer appointmentDoctor;

    private Integer minutesPerAppointmentSchedule;

    private Integer startDayOfWeekMorning;

    private Integer endDayOfWeekMorning;

    private Integer startDayOfWeekAfternoon;

    private Integer endDayOfWeekAfternoon;

    private String startTimeMorning;

    private String endTimeMorning;

    private String startTimeAfternoon;

    private String endTimeAfternoon;

    private Integer allowTimeDefault;

    private Integer maxRegisteredPatientsByDaily;

    private Integer maxRegisteredPatientsByDoctor;

    private Integer connectWithHis;

    private Integer prepaymentMedicalService;

    private Integer status;

    private Integer notiApproveAuto;

    private String timeConfig;

    private Integer periodConfig;

    private String dayConfig;

    private Integer timeConfigSubclinicalResults;

    private Integer applyConfigAfterDay;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instant createdDate;

    private List<String> listTimeConfig;

    public Integer getApplyConfigAfterDay() {
        return applyConfigAfterDay;
    }

    public void setApplyConfigAfterDay(Integer applyConfigAfterDay) {
        this.applyConfigAfterDay = applyConfigAfterDay;
    }

    public Integer getTimeConfigSubclinicalResults() {
        return timeConfigSubclinicalResults;
    }

    public void setTimeConfigSubclinicalResults(Integer timeConfigSubclinicalResults) {
        this.timeConfigSubclinicalResults = timeConfigSubclinicalResults;
    }

    public String getTimeConfig() {
        return timeConfig;
    }

    public String getDayConfig() {
        return dayConfig;
    }

    public void setDayConfig(String dayConfig) {
        this.dayConfig = dayConfig;
    }

    public void setTimeConfig(String timeConfig) {
        this.timeConfig = timeConfig;
    }

    public Integer getPeriodConfig() {
        return periodConfig;
    }

    public void setPeriodConfig(Integer periodConfig) {
        this.periodConfig = periodConfig;
    }

    public Integer getNotiApproveAuto() {
        return notiApproveAuto;
    }

    public void setNotiApproveAuto(Integer notiApproveAuto) {
        this.notiApproveAuto = notiApproveAuto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHealthFacilitiesId() {
        return healthFacilitiesId;
    }

    public void setHealthFacilitiesId(Long healthFacilitiesId) {
        this.healthFacilitiesId = healthFacilitiesId;
    }

    public Integer getAppointmentDaily() {
        return appointmentDaily;
    }

    public void setAppointmentDaily(Integer appointmentDaily) {
        this.appointmentDaily = appointmentDaily;
    }

    public Integer getAppointmentDoctor() {
        return appointmentDoctor;
    }

    public void setAppointmentDoctor(Integer appointmentDoctor) {
        this.appointmentDoctor = appointmentDoctor;
    }

    public Integer getMinutesPerAppointmentSchedule() {
        return minutesPerAppointmentSchedule;
    }

    public void setMinutesPerAppointmentSchedule(Integer minutesPerAppointmentSchedule) {
        this.minutesPerAppointmentSchedule = minutesPerAppointmentSchedule;
    }

    public Integer getStartDayOfWeekMorning() {
        return startDayOfWeekMorning;
    }

    public void setStartDayOfWeekMorning(Integer startDayOfWeekMorning) {
        this.startDayOfWeekMorning = startDayOfWeekMorning;
    }

    public Integer getEndDayOfWeekMorning() {
        return endDayOfWeekMorning;
    }

    public void setEndDayOfWeekMorning(Integer endDayOfWeekMorning) {
        this.endDayOfWeekMorning = endDayOfWeekMorning;
    }

    public Integer getStartDayOfWeekAfternoon() {
        return startDayOfWeekAfternoon;
    }

    public void setStartDayOfWeekAfternoon(Integer startDayOfWeekAfternoon) {
        this.startDayOfWeekAfternoon = startDayOfWeekAfternoon;
    }

    public Integer getEndDayOfWeekAfternoon() {
        return endDayOfWeekAfternoon;
    }

    public void setEndDayOfWeekAfternoon(Integer endDayOfWeekAfternoon) {
        this.endDayOfWeekAfternoon = endDayOfWeekAfternoon;
    }

    public String getStartTimeMorning() {
        return startTimeMorning;
    }

    public void setStartTimeMorning(String startTimeMorning) {
        this.startTimeMorning = startTimeMorning;
    }

    public String getEndTimeMorning() {
        return endTimeMorning;
    }

    public void setEndTimeMorning(String endTimeMorning) {
        this.endTimeMorning = endTimeMorning;
    }

    public String getStartTimeAfternoon() {
        return startTimeAfternoon;
    }

    public void setStartTimeAfternoon(String startTimeAfternoon) {
        this.startTimeAfternoon = startTimeAfternoon;
    }

    public String getEndTimeAfternoon() {
        return endTimeAfternoon;
    }

    public void setEndTimeAfternoon(String endTimeAfternoon) {
        this.endTimeAfternoon = endTimeAfternoon;
    }

    public Integer getAllowTimeDefault() {
        return allowTimeDefault;
    }

    public void setAllowTimeDefault(Integer allowTimeDefault) {
        this.allowTimeDefault = allowTimeDefault;
    }

    public Integer getMaxRegisteredPatientsByDaily() {
        return maxRegisteredPatientsByDaily;
    }

    public void setMaxRegisteredPatientsByDaily(Integer maxRegisteredPatientsByDaily) {
        this.maxRegisteredPatientsByDaily = maxRegisteredPatientsByDaily;
    }

    public Integer getMaxRegisteredPatientsByDoctor() {
        return maxRegisteredPatientsByDoctor;
    }

    public void setMaxRegisteredPatientsByDoctor(Integer maxRegisteredPatientsByDoctor) {
        this.maxRegisteredPatientsByDoctor = maxRegisteredPatientsByDoctor;
    }

    public Integer getConnectWithHis() {
        return connectWithHis;
    }

    public void setConnectWithHis(Integer connectWithHis) {
        this.connectWithHis = connectWithHis;
    }

    public Integer getPrepaymentMedicalService() {
        return prepaymentMedicalService;
    }

    public void setPrepaymentMedicalService(Integer prepaymentMedicalService) {
        this.prepaymentMedicalService = prepaymentMedicalService;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public List<String> getListTimeConfig() {
        return listTimeConfig;
    }

    public void setListTimeConfig(List<String> listTimeConfig) {
        this.listTimeConfig = listTimeConfig;
    }
}
