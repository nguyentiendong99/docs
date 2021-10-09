package com.bkav.lk.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class HisReExaminationVM {

    @NotNull
    @JsonProperty("thoi_gian_bat_dau")
    private String startTime;

    @NotNull
    @JsonProperty("thoi_gian_ket_thuc")
    private String endTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
