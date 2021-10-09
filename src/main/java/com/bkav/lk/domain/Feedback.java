package com.bkav.lk.domain;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "feedback")
    public class Feedback extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "topic_id")
    private Long topicId;

    @NotNull
    @Column(name = "content")
    private String content;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "feedbacked_unit_id", nullable = false)
    private HealthFacilities feedbackedUnit;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "processing_unit_id", nullable = false)
    private HealthFacilities processingUnit;

    @Column(name = "processed_by")
    private String processedBy;

    @NotNull
    @Column(name = "status")
    private Integer status;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public HealthFacilities getFeedbackedUnit() {
        return feedbackedUnit;
    }

    public void setFeedbackedUnit(HealthFacilities feedbackedUnit) {
        this.feedbackedUnit = feedbackedUnit;
    }

    public HealthFacilities getProcessingUnit() {
        return processingUnit;
    }

    public void setProcessingUnit(HealthFacilities processingUnit) {
        this.processingUnit = processingUnit;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
