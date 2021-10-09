package com.bkav.lk.domain;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
@Entity
@Table(name = "doctor_feedback")
public class DoctorFeedback extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "rate is a required field")
    @Min(1) @Max(5)
    @Column(name = "rate")
    private Integer rate;

    @Column(name = "content")
    private String content;

    @Size(max = 1000, min = 1, message = "Max size 1000")
    @Column(name = "feedback_content")
    private String feedbackContent;

    @NotNull(message = "doctorId is a required field")
    @ManyToOne(optional=false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status")
    private Integer status;

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
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

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFeedbackContent() {
        return feedbackContent;
    }

    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
