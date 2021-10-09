package com.bkav.lk.dto;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class FeedbackDTO implements Serializable {

    private Long id;

    private Long topicId;

    private String topicName;

    @Size(min = 1, max = 1000)
    private String content;

    private String contentFeedback;

    private Long feedbackedUnitId;

    private Long processingUnitId;

    private String processedBy;

    private Integer status;

    private String feedbackedUnitName;

    private String processingUnitName;

    private Instant createdDate;

    private String createdBy;

    private Long userId;

    private String userAvatar;

    private String feedbackedUnitAvatar;

    private List<FeedbackContentDTO> feedbackContentDTOList;

    private List<UploadedFileDTO> uploadedFileDTOList;

    private List<FeedbackCustomConfigDTO> feedbackCustomConfigDTOS;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private Instant lastModifiedDate;

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

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getFeedbackedUnitId() {
        return feedbackedUnitId;
    }

    public String getContentFeedback() {
        return contentFeedback;
    }

    public void setContentFeedback(String contentFeedback) {
        this.contentFeedback = contentFeedback;
    }

    public void setFeedbackedUnitId(Long feedbackedUnitId) {
        this.feedbackedUnitId = feedbackedUnitId;
    }

    public Long getProcessingUnitId() {
        return processingUnitId;
    }

    public void setProcessingUnitId(Long processingUnitId) {
        this.processingUnitId = processingUnitId;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public List<FeedbackContentDTO> getFeedbackContentDTOList() {
        return feedbackContentDTOList;
    }

    public void setFeedbackContentDTOList(List<FeedbackContentDTO> feedbackContentDTOList) {
        this.feedbackContentDTOList = feedbackContentDTOList;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFeedbackedUnitName() {
        return feedbackedUnitName;
    }

    public void setFeedbackedUnitName(String feedbackedUnitName) {
        this.feedbackedUnitName = feedbackedUnitName;
    }

    public String getFeedbackedUnitAvatar() {
        return feedbackedUnitAvatar;
    }

    public void setFeedbackedUnitAvatar(String feedbackedUnitAvatar) {
        this.feedbackedUnitAvatar = feedbackedUnitAvatar;
    }

    public String getProcessingUnitName() {
        return processingUnitName;
    }

    public void setProcessingUnitName(String processingUnitName) {
        this.processingUnitName = processingUnitName;
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

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<UploadedFileDTO> getUploadedFileDTOList() {
        return uploadedFileDTOList;
    }

    public void setUploadedFileDTOList(List<UploadedFileDTO> uploadedFileDTOList) {
        this.uploadedFileDTOList = uploadedFileDTOList;
    }

    public List<FeedbackCustomConfigDTO> getFeedbackCustomConfigDTOS() {
        return feedbackCustomConfigDTOS;
    }

    public void setFeedbackCustomConfigDTOS(List<FeedbackCustomConfigDTO> feedbackCustomConfigDTOS) {
        this.feedbackCustomConfigDTOS = feedbackCustomConfigDTOS;
    }
}
