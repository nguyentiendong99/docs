package com.bkav.lk.web.errors;

import java.util.Map;

public class ErrorExcel {

    private String errorCode;

    private Map<String, String> params;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public ErrorExcel(String errorCode, Map<String, String> params) {
        this.errorCode = errorCode;
        this.params = params;
    }
}
