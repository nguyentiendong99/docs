package com.bkav.lk.web.rest.vm;

import javax.validation.constraints.NotNull;

public class GoogleLoginVM {
    @NotNull
    private String idToken;

    private Boolean rememberMe;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
