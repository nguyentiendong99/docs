package com.bkav.lk.web.rest.vm;

public class OtpVM {

    private String reference_id;

    private OtpStatusVM status;

    private String external_id;

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }

    public OtpStatusVM getStatus() {
        return status;
    }

    public void setStatus(OtpStatusVM status) {
        this.status = status;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

}
