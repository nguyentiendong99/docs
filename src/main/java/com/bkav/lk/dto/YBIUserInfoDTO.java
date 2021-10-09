package com.bkav.lk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YBIUserInfoDTO {

    private String sub;

    @JsonProperty("emails.work")
    private String email;

    private String ddcd;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDdcd() {
        return ddcd;
    }

    public void setDdcd(String ddcd) {
        this.ddcd = ddcd;
    }
}
