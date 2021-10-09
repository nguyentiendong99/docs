package com.bkav.lk.web.rest.vm;

import com.bkav.lk.util.StrUtil;

import java.util.List;

public class SmsVM {

    private String bookingCode;

    private String phoneNumber;

    private String message;

    private String healthFacilityName;

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public SmsVM() {
    }

    public SmsVM(String phoneNumber, boolean isApproveAuto, List<String> params) {
        String typeMessage = isApproveAuto ? "sms_message_booking_code" : "sms_message_booking";
        this.phoneNumber = "84" + phoneNumber.substring(1);
        this.message = StrUtil.getStringFromBundle(typeMessage, com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, params);
    }
}
