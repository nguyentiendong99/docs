package com.bkav.lk.web.rest.vm;

import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;

public class AppointmentDateConfig {
    private Long healthFacilitiesId;
    private Integer minutesPerAppointmentSchedule;
    private Integer startDayOfWeekMorning;
    private Integer endDayOfWeekMorning;
    private Integer startDayOfWeekAfternoon;
    private Integer endDayOfWeekAfternoon;
    private String startTimeMorning;
    private String endTimeMorning;
    private String startTimeAfternoon;
    private String endTimeAfternoon;

    public AppointmentDateConfig() {}

    public AppointmentDateConfig(DoctorAppointmentConfigurationDTO appointmentConfiguration) {
        this.healthFacilitiesId = appointmentConfiguration.getHealthFacilitiesId();
        this.minutesPerAppointmentSchedule = appointmentConfiguration.getMinutesPerAppointmentSchedule();
        this.startDayOfWeekMorning = appointmentConfiguration.getStartDayOfWeekMorning();
        this.endDayOfWeekMorning = appointmentConfiguration.getEndDayOfWeekMorning();
        this.startDayOfWeekAfternoon = appointmentConfiguration.getStartDayOfWeekAfternoon();
        this.endDayOfWeekAfternoon = appointmentConfiguration.getEndDayOfWeekAfternoon();
        this.startTimeMorning = appointmentConfiguration.getStartTimeMorning();
        this.endTimeMorning = appointmentConfiguration.getEndTimeMorning();
        this.startTimeAfternoon = appointmentConfiguration.getStartTimeAfternoon();
        this.endTimeAfternoon = appointmentConfiguration.getEndTimeAfternoon();
    }

    public Long getHealthFacilitiesId() {
        return healthFacilitiesId;
    }

    public void setHealthFacilitiesId(Long healthFacilitiesId) {
        this.healthFacilitiesId = healthFacilitiesId;
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
}
