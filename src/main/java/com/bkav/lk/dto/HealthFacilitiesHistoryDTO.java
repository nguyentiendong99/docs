package com.bkav.lk.dto;

import java.time.Instant;
import java.util.List;

public class HealthFacilitiesHistoryDTO {
    private Instant createdDate;

    private String createdBy;

    private List<String> oldContents;

    private List<String> newContents;

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

    public List<String> getOldContents() {
        return oldContents;
    }

    public void setOldContents(List<String> oldContents) {
        this.oldContents = oldContents;
    }

    public List<String> getNewContents() {
        return newContents;
    }

    public void setNewContents(List<String> newContents) {
        this.newContents = newContents;
    }
}
