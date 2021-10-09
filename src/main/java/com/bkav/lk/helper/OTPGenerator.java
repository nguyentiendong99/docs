package com.bkav.lk.helper;


import com.telesign.Util;

public class OTPGenerator {
    // the verify code's length
    private int defaultNumDigits;

    private String defaultMessageTemplate;

    public OTPGenerator() {

    }

    public OTPGenerator(int defaultNumDigits, String defaultMessageTemplate) {
        this.defaultNumDigits = defaultNumDigits;
        this.defaultMessageTemplate = defaultMessageTemplate;
    }

    public String generateOTPCode(int numDigits) {
        return Util.randomWithNDigits(numDigits);
    }

    public String generateOTPCode() {
        return Util.randomWithNDigits(defaultNumDigits);
    }

    public String generateOTPMessage(String code) {
        return String.format(defaultMessageTemplate, code);
    }
}
