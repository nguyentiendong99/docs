package com.bkav.lk.service.payment.vnpay.model;
public class VNPayConfirmResult {
    private String RspCode;
    private String Message;

    public String getRspCode() {
        return RspCode;
    }

    public void setRspCode(String rspCode) {
        RspCode = rspCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
