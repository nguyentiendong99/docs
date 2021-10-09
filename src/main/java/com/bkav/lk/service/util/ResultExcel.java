package com.bkav.lk.service.util;

import com.bkav.lk.web.errors.ErrorExcel;

import java.util.ArrayList;
import java.util.List;

public class ResultExcel {
    private Boolean sucess;
    private List<String> detail = new ArrayList();
    private List<ErrorExcel> errorExcels = new ArrayList();

    public Boolean getSucess() {
        return sucess;
    }

    public void setSucess(Boolean sucess) {
        this.sucess = sucess;
    }

    public List<String> getDetail() {
        return detail;
    }

    public void setDetail(List<String> detail) {
        this.detail = detail;
    }

    public List<ErrorExcel> getErrorExcels() {
        return errorExcels;
    }

    public void setErrorExcels(List<ErrorExcel> errorExcels) {
        this.errorExcels = errorExcels;
    }
}

