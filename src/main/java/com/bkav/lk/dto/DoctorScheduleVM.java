package com.bkav.lk.dto;

import java.io.Serializable;
import java.time.Instant;

public class DoctorScheduleVM implements Serializable {

    private Instant workingDate;

    private String date;

    private boolean available;

    public Instant getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(Instant workingDate) {
        this.workingDate = workingDate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
