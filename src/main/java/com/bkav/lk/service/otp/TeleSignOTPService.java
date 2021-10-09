package com.bkav.lk.service.otp;

import com.bkav.lk.domain.Otp;
import com.bkav.lk.dto.OtpDTO;
import com.bkav.lk.helper.OTPGenerator;
import com.bkav.lk.repository.OtpRepository;
import com.bkav.lk.service.OTPService;
import com.bkav.lk.service.mapper.OtpMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.DoctorResource;
import com.bkav.lk.web.rest.vm.OtpVM;
import com.bkav.lk.web.rest.vm.SmsVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telesign.MessagingClient;
import com.telesign.RestClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class TeleSignOTPService implements OTPService {

    @Autowired
    private MessagingClient messagingClient;

    @Autowired
    private OTPGenerator otpGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    private final OtpRepository repository;

    private final OtpMapper mapper;

    @Value("${otp.session-duration}")
    private Integer duration;

    @Value("${otp.turn-on-otp-default}")
    private Boolean isUseOtpDefault;

    @Value("${otp.code}")
    private String otpCodeDefault;

    private static final String MESSAGE_TYPE = "OTP";

    private static final Logger log = LoggerFactory.getLogger(DoctorResource.class);

    public TeleSignOTPService(OtpRepository repository, OtpMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Object init(String userId, String phoneNumber) {
        return null;
    }

    @Override
    public boolean verify(String userId, String phoneNumber, String code, String sessionId) {
        Optional<Otp> otp = repository.findByReferenceIdAndPhoneNumber(sessionId, phoneNumber);
        Instant now = Instant.now();
        if (!otp.isPresent()) {
            throw new BadRequestAlertException("Mã tham chiếu không tìm thấy, vui lòng thử lại", "OTP", "otp.not_found");
        }
        long seconds = ChronoUnit.SECONDS.between(
                otp.get().getTimeOut().atZone(DateUtils.getZoneHCM()),
                now.atZone(DateUtils.getZoneHCM()));
        if (seconds > duration || otp.get().getStatusCode().equals(Constants.ENTITY_STATUS.DELETED)) {
            throw new BadRequestAlertException("Mã OTP đã hết hạn, vui lòng thử lại", "OTP", "otp.expired");
        }
        if (!code.equals(otp.get().getOtpCode())) {
            return false;
        }
        otp.get().setStatusCode(Constants.ENTITY_STATUS.DELETED);
        return true;
    }

    @Override
    public OtpVM generator(Long userId, String phoneNumber) {
        try {
            String otpCode = "";
            if (isUseOtpDefault) {
                otpCode = otpCodeDefault;
            } else {
                otpCode = otpGenerator.generateOTPCode();
            }
            RestClient.TelesignResponse telesignResponse = messagingClient.message(phoneNumber, otpGenerator.generateOTPMessage(otpCode), MESSAGE_TYPE, null);
            OtpVM otpVM = objectMapper.readValue(telesignResponse.body, OtpVM.class);

            if (isUseOtpDefault) {
                if (!otpVM.getStatus().getCode().equals(290)) {
                    int length = 35;
                    boolean useLetters = true;
                    boolean useNumbers = true;
                    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
                    otpVM.setReference_id(generatedString);
                }
            } else {
                if (!otpVM.getStatus().getCode().equals(290)) { // statusCode = 290 - SUCCESS
                    throw new BadRequestAlertException(otpVM.getStatus().getDescription(), "OTP", "otp.error");
                }
            }
            OtpDTO otpDTO = otpVMConvert(otpVM, userId, phoneNumber, otpCode);
            repository.save(mapper.toEntity(otpDTO));
            return otpVM;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error: ", e);
        }
        return null;
    }

    @Override
    public void sendSMS(SmsVM smsVM) {
        try {
            RestClient.TelesignResponse telesignResponse = messagingClient.message(smsVM.getPhoneNumber(), smsVM.getMessage(), "ARN", null);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public OtpDTO otpVMConvert(OtpVM otpVM, Long userId, String phoneNumber, String code) {
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setReferenceId(otpVM.getReference_id());
        otpDTO.setOtpCode(code);
        otpDTO.setStatusCode(Constants.ENTITY_STATUS.ACTIVE);
        otpDTO.setPhoneNumber(phoneNumber);
        otpDTO.setUserId(userId);
        return otpDTO;
    }
}
