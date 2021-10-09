package com.bkav.lk.dto;

import java.time.Instant;
import java.util.List;

public class DoctorAppointmentHistoryDTO {
    private Instant createdDate;

    private String createdBy;

    private List<String> content;

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
