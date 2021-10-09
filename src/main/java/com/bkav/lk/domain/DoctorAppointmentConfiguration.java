package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "doctor_appointment_configuration")
public class DoctorAppointmentConfiguration extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "health_facilities_id")
    private Long healthFacilitiesId;

    @Column(name = "appointment_daily")
    private Integer appointmentDaily;

    @Column(name = "appointment_doctor")
    private Integer appointmentDoctor;

    @Column(name = "minutes_per_appointment_schedule")
    private Integer minutesPerAppointmentSchedule;

    @Column(name = "start_day_of_week_morning")
    private Integer startDayOfWeekMorning;

    @Column(name = "end_day_of_week_morning")
    private Integer endDayOfWeekMorning;

    @Column(name = "start_day_of_week_afternoon")
    private Integer startDayOfWeekAfternoon;

    @Column(name = "end_day_of_week_afternoon")
    private Integer endDayOfWeekAfternoon;

    @Column(name = "start_time_morning")
    private String startTimeMorning;

    @Column(name = "end_time_morning")
    private String endTimeMorning;

    @Column(name = "start_time_afternoon")
    private String startTimeAfternoon;

    @Column(name = "end_time_afternoon")
    private String endTimeAfternoon;

    @Column(name = "allow_time_default")
    private Integer allowTimeDefault;

    @Column(name = "max_registered_patients_by_daily")
    private Integer maxRegisteredPatientsByDaily;

    @Column(name = "max_registered_patients_by_doctor")
    private Integer maxRegisteredPatientsByDoctor;

    @Column(name = "connect_with_his")
    private Integer connectWithHis;

    @Column(name = "prepayment_medical_service")
    private Integer prepaymentMedicalService;

    @Column(name = "status")
    private Integer status;

    @Column(name = "noti_approve_auto")
    private Integer notiApproveAuto;

    @Column(name = "time_config")
    private String timeConfig;

    @Column(name = "time_config_subclinical_results")
    private Integer timeConfigSubclinicalResults;

    @Column(name = "day_config")
    private String dayConfig;

    @Column(name = "period_config")
    private Integer periodConfig;

    @Column(name = "apply_config_after_day")
    private Integer applyConfigAfterDay;

    public Integer getApplyConfigAfterDay() {
        return applyConfigAfterDay;
    }

    public void setApplyConfigAfterDay(Integer applyConfigAfterDay) {
        this.applyConfigAfterDay = applyConfigAfterDay;
    }

    public Integer getNotiApproveAuto() {
        return notiApproveAuto;
    }

    public void setNotiApproveAuto(Integer notiApproveAuto) {
        this.notiApproveAuto = notiApproveAuto;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public String getTimeConfig() {
        return timeConfig;
    }

    public void setTimeConfig(String timeConfig) {
        this.timeConfig = timeConfig;
    }

    public String getDayConfig() {
        return dayConfig;
    }

    public void setDayConfig(String dayConfig) {
        this.dayConfig = dayConfig;
    }

    public Integer getPeriodConfig() {
        return periodConfig;
    }

    public void setPeriodConfig(Integer periodConfig) {
        this.periodConfig = periodConfig;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTimeConfigSubclinicalResults() {
        return timeConfigSubclinicalResults;
    }

    public void setTimeConfigSubclinicalResults(Integer timeConfigSubclinicalResults) {
        this.timeConfigSubclinicalResults = timeConfigSubclinicalResults;
    }
}
