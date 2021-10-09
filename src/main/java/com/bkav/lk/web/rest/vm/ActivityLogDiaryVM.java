package com.bkav.lk.web.rest.vm;

import java.time.Instant;

public class ActivityLogDiaryVM {

    private Long id;
    private String actionType;
    private Integer actionTypeValue;
    private String contentType;
    private Integer contentTypeValue;
    private String content;
    private String contentFormatter;
    private Instant createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getActionTypeValue() {
        return actionTypeValue;
    }

    public void setActionTypeValue(Integer actionTypeValue) {
        this.actionTypeValue = actionTypeValue;
    }

    public Integer getContentTypeValue() {
        return contentTypeValue;
    }

    public void setContentTypeValue(Integer contentTypeValue) {
        this.contentTypeValue = contentTypeValue;
    }

    public String getContentFormatter() {
        return contentFormatter;
    }

    public void setContentFormatter(String contentFormatter) {
        this.contentFormatter = contentFormatter;
    }
}
