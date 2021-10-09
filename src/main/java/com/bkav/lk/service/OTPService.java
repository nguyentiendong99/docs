package com.bkav.lk.service;

import com.bkav.lk.web.rest.vm.OtpVM;
import com.bkav.lk.web.rest.vm.SmsVM;

public interface OTPService {

    Object init(String userId, String phoneNumber);

    boolean verify(String userId, String phoneNumber, String otp, String sessionId);

    OtpVM generator(Long userId, String phoneNumber);

    void sendSMS(SmsVM smsVM);

}
