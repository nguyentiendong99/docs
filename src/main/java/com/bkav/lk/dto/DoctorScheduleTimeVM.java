package com.bkav.lk.dto;

import java.io.Serializable;
import java.time.Instant;

public class DoctorScheduleTimeVM implements Serializable {

    private Instant startTime;

    private Instant endTime;

    private String time;

    private boolean available;

    private boolean isMorning;

    private Integer peopleRegistered;

    public Integer getPeopleRegistered() {
        return peopleRegistered;
    }

    public void setPeopleRegistered(Integer peopleRegistered) {
        this.peopleRegistered = peopleRegistered;
    }

    public boolean isMorning() {
        return isMorning;
    }

    public void setMorning(boolean morning) {
        isMorning = morning;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
