package com.bkav.lk.web.rest.vm;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SocialLoginVM {

    @NotNull
    @Size(min = 1, max = 255)
    private String accessToken;

    private Boolean rememberMe;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "SocialLoginVM{" +
                "accessToken='" + accessToken + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
