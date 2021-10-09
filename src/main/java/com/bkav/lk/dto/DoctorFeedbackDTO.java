package com.bkav.lk.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class DoctorFeedbackDTO implements Serializable {
    private Long id;

    private Long userId;

    private Long healthFacilityId;

    private String healthFacilityName;

    private String userName;

    private String userAvatar;

    private String feedbackContent;

    private Long doctorId;

    private String doctorName;

    private Integer rate;

    private String content;

    private Integer status;

    private Instant createdDate;

    private List<DoctorFeedbackCustomConfigDTO> doctorFeedbackCustomConfigDTOS;

    public String getFeedbackContent() {
        return feedbackContent;
    }

    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public List<DoctorFeedbackCustomConfigDTO> getDoctorFeedbackCustomConfigDTOS() {
        return doctorFeedbackCustomConfigDTOS;
    }

    public void setDoctorFeedbackCustomConfigDTOS(List<DoctorFeedbackCustomConfigDTO> doctorFeedbackCustomConfigDTOS) {
        this.doctorFeedbackCustomConfigDTOS = doctorFeedbackCustomConfigDTOS;
    }
}
