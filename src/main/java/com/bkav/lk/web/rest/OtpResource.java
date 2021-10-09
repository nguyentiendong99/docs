package com.bkav.lk.web.rest;

import com.bkav.lk.dto.DoctorAppointmentDTO;
import com.bkav.lk.dto.OtpDTO;
import com.bkav.lk.service.DoctorAppointmentService;
import com.bkav.lk.service.OTPService;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.vm.OtpVM;
import com.bkav.lk.web.rest.vm.SmsVM;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OtpResource {

    private final OTPService otpService;

    private final DoctorAppointmentService doctorAppointmentService;

    @Value("${otp.num-digits}")
    private Integer numberDigits;

    public OtpResource(OTPService otpService, DoctorAppointmentService doctorAppointmentService) {
        this.otpService = otpService;
        this.doctorAppointmentService = doctorAppointmentService;
    }

    @PostMapping("/public/otp/generator")
    public ResponseEntity<OtpVM> generator(@RequestBody OtpDTO otpDTO) {
        if (otpDTO.getId() != null) {
            throw new BadRequestAlertException("Dữ liệu không hợp lệ, vui lòng thử lại", "OTP", "otp.data_invalid");
        }
        if (otpDTO.getPhoneNumber() == null || otpDTO.getPhoneNumber().equals("")) {
            throw new BadRequestAlertException("Số điện thoại không được để trống, vui lòng thử lại", "OTP", "otp.phone_empty");
        }
        if (otpDTO.getPhoneNumber().length() != 10) {
            throw new BadRequestAlertException("Số điện thoại không hợp lệ, vui lòng thử lại", "OTP", "otp.phone_invalid");
        }
        String phoneNumber = otpDTO.getPhoneNumber();
        phoneNumber = "84" + phoneNumber.substring(1);
        OtpVM otpVM = otpService.generator(otpDTO.getUserId(), phoneNumber);
        return ResponseEntity.ok().body(otpVM);
    }

    @PostMapping("/public/otp/verify")
    public ResponseEntity<Boolean> verify(@RequestBody OtpDTO otpDTO) {
        if (otpDTO.getReferenceId() == null) {
            throw new BadRequestAlertException("Mã tham chiếu không được để trống, vui lòng thử lại", "OTP", "otp.referenceId_null");
        }
        if (otpDTO.getOtpCode() == null || otpDTO.getOtpCode().equals("") || otpDTO.getOtpCode().length() != numberDigits) {
            throw new BadRequestAlertException("Mã OTP không hợp lệ, vui lòng thử lại", "OTP", "otp.code_invalid");
        }
        if (otpDTO.getPhoneNumber() == null || otpDTO.getPhoneNumber().equals("")) {
            throw new BadRequestAlertException("Số điện thoại không được để trống, vui lòng thử lại", "OTP", "otp.phone_empty");
        }
        if (otpDTO.getPhoneNumber().length() != 10) {
            throw new BadRequestAlertException("Số điện thoại không hợp lệ, vui lòng thử lại", "OTP", "otp.phone_invalid");
        }
        String phoneNumber = otpDTO.getPhoneNumber();
        phoneNumber = "84" + phoneNumber.substring(1);
        boolean status = otpService.verify(null, phoneNumber, otpDTO.getOtpCode(), otpDTO.getReferenceId());
        return ResponseEntity.ok().body(status);
    }

    @PostMapping("/public/send-sms")
    public ResponseEntity<Void> sendSMS(@RequestBody SmsVM body, @RequestParam boolean isApproveAuto) {
        DoctorAppointmentDTO dto = doctorAppointmentService.findTopByBookingCode(body.getBookingCode());
        List<String> params = new ArrayList<>();
        if (isApproveAuto) {
            params.add(dto.getAppointmentDate());
            params.add(body.getBookingCode());
        } else {
            params.add(body.getHealthFacilityName());
        }
        SmsVM smsVM = new SmsVM(body.getPhoneNumber(), isApproveAuto, params);
        otpService.sendSMS(smsVM);
        return ResponseEntity.ok().build();
    }

}
