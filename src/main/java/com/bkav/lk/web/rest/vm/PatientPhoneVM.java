package com.bkav.lk.web.rest.vm;

public class PatientPhoneVM {
    private String phone;

    public PatientPhoneVM() {
    }

    public PatientPhoneVM(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
