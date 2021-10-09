package com.bkav.lk.web.rest;

import com.bkav.lk.domain.AppointmentCancelLog;
import com.bkav.lk.domain.Config;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.payment.vnpay.Utils;
import com.bkav.lk.service.payment.vnpay.VNPayPaymentService;
import com.bkav.lk.service.payment.vnpay.model.VNPayConfirmResult;
import com.bkav.lk.service.payment.vnpay.model.VNPayRequest;
import com.bkav.lk.service.payment.vnpay.model.VNPayResult;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class VNPayPaymentResource {
    private final Logger log = LoggerFactory.getLogger(VNPayPaymentResource.class);
    private static final String ENTITY_NAME = "VnpayPayment";


    private final VNPayPaymentService vnPayPaymentService;

    private final DoctorAppointmentService doctorAppointmentService;

    private final DoctorService doctorService;

    private final ClinicService clinicService;

    private final HealthFacilitiesService healthFacilitiesService;

    private final MedicalSpecialityService medicalSpecialityService;

    private final MedicalServiceService medicalServiceService;

    private final DoctorAppointmentConfigurationService appointmentConfigService;

    private final DoctorScheduleTimeService doctorScheduleTimeService;

    private final TransactionService transactionService;

    private final PatientRecordService patientRecordService;

    private final ConfigService configService;

    private final RestTemplateHelper restTemplateHelper;

    private final AppointmentCancelConfigService appointmentCancelConfigService;

    private final ConfigIntegratedService configIntegratedService;

    @Value("${social-insurance.insurance_code_check_url}")
    private String INSURANCE_CODE_CHECK_URL;

    @Value("${his.appointment_code_check_url}")
    private String APPOINTMENT_CODE_CHECK_URL;

    public VNPayPaymentResource(VNPayPaymentService vnPayPaymentService,
                                DoctorAppointmentService doctorAppointmentService,
                                DoctorService doctorService,
                                ClinicService clinicService, HealthFacilitiesService healthFacilitiesService, MedicalSpecialityService medicalSpecialityService, MedicalServiceService medicalServiceService, DoctorAppointmentConfigurationService appointmentConfigService,
                                DoctorScheduleTimeService doctorScheduleTimeService,
                                TransactionService transactionService,
                                PatientRecordService patientRecordService, ConfigService configService, RestTemplateHelper restTemplateHelper, AppointmentCancelConfigService appointmentCancelConfigService, ConfigIntegratedService configIntegratedService) {
        this.vnPayPaymentService = vnPayPaymentService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorService = doctorService;
        this.clinicService = clinicService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.medicalSpecialityService = medicalSpecialityService;
        this.medicalServiceService = medicalServiceService;
        this.appointmentConfigService = appointmentConfigService;
        this.doctorScheduleTimeService = doctorScheduleTimeService;
        this.transactionService = transactionService;
        this.patientRecordService = patientRecordService;
        this.configService = configService;
        this.restTemplateHelper = restTemplateHelper;
        this.appointmentCancelConfigService = appointmentCancelConfigService;
        this.configIntegratedService = configIntegratedService;
    }

    // app khi thanh toan se goi len, server goi qua vnpay, va tra ve url chua link thanh toan,.....
    // Create Temp DoctorAppointment and Transaction -> status = 0
    @PostMapping("/payment/vnpay/request")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VNPayResult> requestPayment(@RequestBody @Valid DoctorAppointmentDTO doctorAppointmentDTO, @RequestParam (name = "source") Integer source,
                                                      HttpServletRequest httpServletRequest) {
        // Check source: 1 or 2 (1: web, 2: mobile)
        if (!source.equals(Constants.SOURCE_DOCTOR_APPOINTMENT.WEB) && !source.equals(Constants.SOURCE_DOCTOR_APPOINTMENT.MOBILE)) {
            throw new BadRequestAlertException("Source only 1 or 2 (1: web, 2: mobile)", ENTITY_NAME, "source_only_1_2");
        }

        if (doctorAppointmentDTO.getId() != null) {
            throw new BadRequestAlertException("A new Appointment cannot already have an ID", ENTITY_NAME, "doctor_appointment.id_exists");
        }

        if (!checkCreateOverTime(doctorAppointmentDTO)) {
            throw new BadRequestAlertException("Đã hết thời gian đặt lịch khám vào ngày mai", ENTITY_NAME, "appointment.over_time_allowed_create_tomorrow");
        }

        if (!checkCreateInvalid(doctorAppointmentDTO)) {
            throw new BadRequestAlertException("Thời gian đặt lịch không hợp lệ", ENTITY_NAME, "appointment.time_invalid");
        }

        if (doctorAppointmentDTO.getType() != null && doctorAppointmentDTO.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DOCTOR)) {
            if (doctorAppointmentDTO.getDoctorId() == null) {
                throw new BadRequestAlertException("Invalid doctor id", ENTITY_NAME, "doctorIdnull");
            }
            DoctorDTO doctorDTOOptional = doctorService.findById(doctorAppointmentDTO.getDoctorId());
            if (doctorDTOOptional == null) {
                throw new BadRequestAlertException("Doctor Not Found", ENTITY_NAME, "doctorNotFound");
            }
        }

        // Kiểm tra lại các trường csyt, phòng khám, bác sỹ, chuyên khoa, dịch vụ khám xem còn hoạt động không?
        if (healthFacilitiesService.findById(doctorAppointmentDTO.getHealthFacilityId()).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Health facility is Inactived", ENTITY_NAME, "doctor_appointment.healthFacility_isInactived");
        }
        if (Objects.requireNonNull(clinicService.findOne(doctorAppointmentDTO.getClinicId()).orElse(null)).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Clinic is Inactived", ENTITY_NAME, "doctor_appointment.clinic_isInactived");
        }
        if (doctorService.findById(doctorAppointmentDTO.getDoctorId()).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Doctor is Inactived", ENTITY_NAME, "doctor_appointment.doctor_isInactived");
        }
        if (Objects.requireNonNull(medicalServiceService.findOne(doctorAppointmentDTO.getMedicalServiceId()).orElse(null)).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Medical service is Inactived", ENTITY_NAME, "doctor_appointment.medicalService_isInactived");
        }
        if (medicalSpecialityService.findOne(doctorAppointmentDTO.getMedicalSpecialityId()).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Medical speciality is Inactived", ENTITY_NAME, "doctor_appointment.medicalSpeciality_isInactived");
        }

        Long healthFacilityId = doctorAppointmentDTO.getHealthFacilityId();
        // Kiểm tra nếu là tái khám thì mã khám cũ có tồn tại trong hệ thống không (nếu không thì gọi sang HIS để kiểm tra)
        if (Constants.ENTITY_STATUS.ACTIVE.equals(doctorAppointmentDTO.getIsReExamination())) {
            boolean appointmentCodeExist = doctorAppointmentService.existsByAppointmentCode(doctorAppointmentDTO.getOldAppointmentCode());
            if (!appointmentCodeExist) {
                ConfigIntegratedDTO configIntegratedDTO = configIntegratedService
                        .findByConnectCodeAndHealthFacilityId(Constants.CONFIG_INTEGRATED.HIS_CONNECT_CODE, healthFacilityId);
                ErrorVM response = restTemplateHelper.execute(configIntegratedDTO.getConnectUrl() + APPOINTMENT_CODE_CHECK_URL + doctorAppointmentDTO.getOldAppointmentCode(),
                        HttpMethod.GET, null, ErrorVM.class);
                if (Objects.nonNull(response)) {
                    if (!Constants.HIS_STATUS_CODE.SUCCESS.equals(response.getErrorCode())) {
                        throw new BadRequestAlertException("Appointment code is not exist!", ENTITY_NAME, "appointmentcode.notexist");
                    }
                } else {
                    throw new BadRequestAlertException("Can't connect to HIS for checking appointment code", ENTITY_NAME, "his.failed_connection");
                }
            }
        }

        Optional<PatientRecordDTO> dtoOpt = patientRecordService.findOne(doctorAppointmentDTO.getPatientRecordId());

        // Kiểm tra người dùng hiện tại có bị block hay không
        if (dtoOpt.isPresent()) {
            AppointmentCancelLog acc = appointmentCancelConfigService.findByUserId(dtoOpt.get().getUserId());
            if (Objects.nonNull(acc) && Constants.BOOL_NUMBER.TRUE.equals(acc.getIsBlocked())) {
                throw new BadRequestAlertException("Current user has blocked - can't create new doctor appointment",
                        ENTITY_NAME, "doctor_appointment.user_is_blocked");
            }
        }

        // Kiểm tra số BHYT từ cổng BHXH
        if (Constants.BOOL_NUMBER.TRUE.equals(doctorAppointmentDTO.getHaveHealthInsurance())) {
            if (dtoOpt.isPresent()) {
                ConfigIntegratedDTO configIntegratedDTO = configIntegratedService
                        .findByConnectCodeAndHealthFacilityId(Constants.CONFIG_INTEGRATED.SOCIAL_INSURANCE_CONNECT_CODE, healthFacilityId);
                if (dtoOpt.get().getHealthInsuranceCode() == null) {
                    throw new BadRequestAlertException("Health Insurance Code", ENTITY_NAME, "insurance_code.empty");
                }
                String url = configIntegratedDTO.getConnectUrl()
                        + this.INSURANCE_CODE_CHECK_URL + "?"
                        + "maThe=" + dtoOpt.get().getHealthInsuranceCode()
                        + "&hoTen=" + dtoOpt.get().getName()
                        + "&ngaySinh=" + DateUtils.convertFromInstantToString(dtoOpt.get().getDob());
                Map<String, String> rs = restTemplateHelper.execute(url, HttpMethod.GET, null, Map.class);
                if (rs != null) {
                    if (rs.get("code").equals("wrong_information")) {
                        throw new BadRequestAlertException("Health Insurance wrong information",
                                ENTITY_NAME, "insurance_code.wrong_information");
                    }
                    if (rs.get("code").equals("expired")) {
                        throw new BadRequestAlertException("Health Insurance Code Expired",
                                ENTITY_NAME, "insurance_code.expired");
                    }
                    if (rs.get("code").equals("invalid")) {
                        throw new BadRequestAlertException("Health Insurance Code Invalid",
                                ENTITY_NAME, "insurance_code.invalid");
                    }
                    if (rs.get("code").equals("valid")) {
                        // Kiem tra startTime va endDate cua The BHYT
                        Date endDate = DateUtils.parse(rs.get("ngayKT"), "dd/MM/yyyy");
                        Date startTime = Date.from(doctorAppointmentDTO.getStartTime().atZone(DateUtils.getZoneHCM()).toInstant());
                        if (startTime.compareTo(endDate) > -1) {
                            throw new BadRequestAlertException("Start time is greater enDate of HealthInsurance",
                                    ENTITY_NAME, "insurance_code.startTime_greater_endDate");
                        }
                    }
                }

            }
        }

        DoctorAppointmentConfigurationDTO configAppointment = appointmentConfigService.findOne(doctorAppointmentDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE);
        if (configAppointment == null) {
            configAppointment = appointmentConfigService.getDefaultConfig();
            if (configAppointment == null) {
                throw new BadRequestAlertException("Cấu hình Cơ sở y tế mặc định hiện không hoạt động", ENTITY_NAME, "doctor_appointment.health_facility_deactivate");
            }
        }
        // Loai bo tat ca nhung lich TAM THOI vi pham: status = 0 && now - createAt > 30 phut && (start_time, end_time) && doctor_id && health_facility_id
        // 1. Find DoctorAppointment voi cac dieu kien tren.
        List<DoctorAppointmentDTO> listDoctorAppointmentTemp = null;
        if (Objects.nonNull(doctorAppointmentDTO.getDoctorId())) {
            listDoctorAppointmentTemp = doctorAppointmentService
                    .findTempDoctorAppointmentInvalidWithDoctor(Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, doctorAppointmentDTO.getHealthFacilityId()
                            , doctorAppointmentDTO.getDoctorId(), doctorAppointmentDTO.getStartTime()
                            , doctorAppointmentDTO.getEndTime(), Constants.TIME_OUT_VNPAY.TIME_OUT_MINUTE);
        } else {
            listDoctorAppointmentTemp = doctorAppointmentService
                    .findTempDoctorAppointmentInvalidNotWithDoctor(Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, doctorAppointmentDTO.getHealthFacilityId()
                    ,doctorAppointmentDTO.getStartTime(), doctorAppointmentDTO.getEndTime(), Constants.TIME_OUT_VNPAY.TIME_OUT_MINUTE);
        }
        // 2. Xoa Lich Tam thoi invalid DoctorAppointment -> status = -1
        if (Objects.nonNull(listDoctorAppointmentTemp) && listDoctorAppointmentTemp.size() > 0) {
            List<Long> ids = listDoctorAppointmentTemp.stream().map(DoctorAppointmentDTO::getId).collect(Collectors.toList());
            if (Objects.nonNull(ids)) {
                doctorAppointmentService.deleteTempDoctorAppointment(ids);
            }
        }
        // 3. Xoa Transaction Tam thoi -> status = -1
        if (Objects.nonNull(listDoctorAppointmentTemp) && listDoctorAppointmentTemp.size() > 0) {
            List<String> bookingCodes = listDoctorAppointmentTemp.stream().map(DoctorAppointmentDTO::getBookingCode).collect(Collectors.toList());
            if (Objects.nonNull(bookingCodes)) {
                transactionService.deleteTempTransactions(bookingCodes);
            }
        }

        // 4. Tru so luot voi cac dieu kien: (start_time, end_time) && health_facility_id && doctor_id.
        doctorScheduleTimeService.minusSubscriptions(doctorAppointmentDTO.getDoctorId(), doctorAppointmentDTO.getStartTime(),
                doctorAppointmentDTO.getEndTime(), listDoctorAppointmentTemp.size(), doctorAppointmentDTO.getHealthFacilityId());

        // Kiem tra con cho trong khung gio khong
        boolean available = doctorScheduleTimeService.appointmentTimeAvailable(doctorAppointmentDTO.getHealthFacilityId(),
                doctorAppointmentDTO.getDoctorId(), doctorAppointmentDTO.getStartTime(), doctorAppointmentDTO.getEndTime(), 1, true, true);

        if (available) {
            doctorAppointmentDTO.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
            doctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.REQUEST);
            doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST);
            // Luu doctorAppointment tam
            DoctorAppointmentDTO result = doctorAppointmentService.save(doctorAppointmentDTO);
            if (result == null) {
                throw new BadRequestAlertException("Create request DoctorAppointment Failed", ENTITY_NAME, "paymentFailed");
            }
            // Luu transaction tam
            TransactionDTO transactionDTO = new TransactionDTO();
            Optional<PatientRecordDTO> patientRecordDTO = patientRecordService.findOne(doctorAppointmentDTO.getPatientRecordId());
            if (!patientRecordDTO.isPresent()) {
                throw new BadRequestAlertException("Patient Record is not found", ENTITY_NAME, "null");
            } else {
                transactionDTO.setUserId(patientRecordDTO.get().getUserId());
            }
            transactionDTO.setHealthFacilityId(result.getHealthFacilityId());
            transactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.REQUEST);
            transactionDTO.setBookingCode(result.getBookingCode());
            transactionDTO.setTotalAmount(doctorAppointmentDTO.getAmount());
            transactionDTO.setAmount(doctorAppointmentDTO.getAmount());
            transactionDTO.setContent(result.getMedicalServiceName());
            transactionDTO.setTypeCode(Constants.TRANSACTION_TYPE_CODE.DEPOSIT);
            transactionDTO.setPaymentMethod(doctorAppointmentDTO.getPaymentMethod());
            transactionDTO.setBankCode(doctorAppointmentDTO.getBankCode());
            transactionService.save(transactionDTO);
            VNPayRequest vnPayRequest = new VNPayRequest();
            try {
                vnPayRequest.setAmount(doctorAppointmentDTO.getAmount());
                vnPayRequest.setBankCode(doctorAppointmentDTO.getBankCode());
                vnPayRequest.setLangCode("vn");
                vnPayRequest.setOrderType("topup");
                vnPayRequest.setOrderInfo(result.getBookingCode());
                vnPayRequest.setClientIp(Utils.getIpAddress(httpServletRequest));
                return ResponseEntity.ok().body(vnPayPaymentService.process(vnPayRequest, doctorAppointmentDTO.getHealthFacilityId(), source));
            } catch (ServletException | IOException e) {
                log.error("Error: ", e);
            }
            return ResponseEntity.notFound().build();
        }
        throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "notAvailableAppointment");

    }

    // link vnpay callback lai
    @GetMapping("/public/payment/vnpay/ipn")
    public ResponseEntity<VNPayConfirmResult> notifyPayment(@RequestParam MultiValueMap<String, String> queryParams) {
        log.debug("REST request to notify vnpay");
        return ResponseEntity.ok().body(vnPayPaymentService.processResult(queryParams));
    }

    @PutMapping("/payment/vnpay/request/update-doctor-appointment")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VNPayResult> requestPaymentUpdate(@RequestBody @Valid DoctorAppointmentDTO dto, @RequestParam (name = "source") Integer source,
                                                      HttpServletRequest httpServletRequest) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("id_null", ENTITY_NAME, "id_null");
        }

        Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findOne(dto.getId());
        if (!optional.isPresent()) {
            throw new BadRequestAlertException("id_not_found", ENTITY_NAME, "id_not_found");
        }

        if (!checkCreateInvalid(dto)) {
            throw new BadRequestAlertException("Thời gian đặt lịch không hợp lệ", ENTITY_NAME, "appointment.time_invalid");
        }

        // Kiểm tra nếu là tái khám thì mã khám cũ có tồn tại trong hệ thống không (nếu không thì gọi sang HIS để kiểm tra)
        if (Constants.ENTITY_STATUS.ACTIVE.equals(dto.getIsReExamination())) {
            boolean appointmentCodeExist = doctorAppointmentService.existsByAppointmentCode(dto.getOldAppointmentCode());
            if (!appointmentCodeExist) {
                Long healthFacilityId = dto.getHealthFacilityId();
                ConfigIntegratedDTO configIntegratedDTO = configIntegratedService
                        .findByConnectCodeAndHealthFacilityId(Constants.CONFIG_INTEGRATED.HIS_CONNECT_CODE, healthFacilityId);
                ErrorVM response = restTemplateHelper.execute(configIntegratedDTO.getConnectUrl() + APPOINTMENT_CODE_CHECK_URL + dto.getOldAppointmentCode(),
                        HttpMethod.GET, null, ErrorVM.class);
                if (Objects.nonNull(response)) {
                    if (!Constants.HIS_STATUS_CODE.SUCCESS.equals(response.getErrorCode())) {
                        throw new BadRequestAlertException("Appointment code is not exist!", ENTITY_NAME, "appointmentcode.notexist");
                    }
                } else {
                    throw new BadRequestAlertException("Can't connect to HIS for checking appointment code", ENTITY_NAME, "his.failed_connection");
                }
            }
        }

        dto.setStatus(optional.get().getStatus());
        checkOverTimeAllowed(dto, "update");
        if (!dto.getStartTime().equals(optional.get().getStartTime())) {
            boolean available = doctorScheduleTimeService.appointmentTimeAvailable(dto.getHealthFacilityId(),
                    dto.getDoctorId(), dto.getStartTime(), dto.getEndTime(), 1, true, true);

            if (!available) {
                throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "notAvailableAppointment");
            }
        }
        // Luu tam 1 lich va 1 transaction moi
        dto.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
        dto.setPaymentStatus(Constants.PAYMENT_STATUS.REQUEST);
        dto.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST);
        dto.setOldBookingCode(optional.get().getBookingCode());
        dto.setId(null); // set lai null de tao ban ghi moi
        dto.setBookingCode(null); // set lai null de tao ban ghi moi
        // Luu doctorAppointment tam
        DoctorAppointmentDTO result = doctorAppointmentService.save(dto);
        if (result == null) {
            throw new BadRequestAlertException("Create request DoctorAppointment Failed", ENTITY_NAME, "paymentFailed");
        }
        // Luu transaction tam
        TransactionDTO transactionDTO = new TransactionDTO();
        Optional<PatientRecordDTO> patientRecordDTO = patientRecordService.findOne(dto.getPatientRecordId());
        if (!patientRecordDTO.isPresent()) {
            throw new BadRequestAlertException("Patient Record is not found", ENTITY_NAME, "null");
        } else {
            transactionDTO.setUserId(patientRecordDTO.get().getUserId());
        }
        transactionDTO.setHealthFacilityId(result.getHealthFacilityId());
        transactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.REQUEST);
        transactionDTO.setBookingCode(result.getBookingCode());
        transactionDTO.setTotalAmount(dto.getAmount());
        transactionDTO.setAmount(dto.getAmount());
        transactionDTO.setContent(result.getMedicalServiceName());
        transactionDTO.setTypeCode(Constants.TRANSACTION_TYPE_CODE.DEPOSIT);
        transactionDTO.setPaymentMethod(dto.getPaymentMethod());
        transactionDTO.setBankCode(dto.getBankCode());
        transactionService.save(transactionDTO);
        VNPayRequest vnPayRequest = new VNPayRequest();
        try {
            vnPayRequest.setAmount(dto.getAmount());
            vnPayRequest.setBankCode(dto.getBankCode());
            vnPayRequest.setLangCode("vn");
            vnPayRequest.setOrderType("topup");
            vnPayRequest.setOrderInfo(result.getBookingCode());
            vnPayRequest.setClientIp(Utils.getIpAddress(httpServletRequest));
            return ResponseEntity.ok().body(vnPayPaymentService.process(vnPayRequest, dto.getHealthFacilityId(), source));
        } catch (ServletException | IOException e) {
            log.error("Error: ", e);
        }
        return ResponseEntity.ok(null);
    }

    private boolean checkCreateOverTime(DoctorAppointmentDTO dto) {
        ZonedDateTime now = ZonedDateTime.now(DateUtils.getZoneHCM()); // DateTime hiện tại
        Config configOther = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.TIME_ALLOW_BEFORE_BOOKING);
        LocalTime localTimeNow = now.toLocalTime();
        LocalTime localTimeDefault = LocalTime.parse(configOther.getPropertyValue());

        // Nếu thời gian hiện tại mà >= 16:30 thì không cho phép đặt lịch khám vào ngày hôm sau
        if (localTimeNow.isAfter(localTimeDefault) || localTimeNow.equals(localTimeDefault)) {
            ZonedDateTime startTime = dto.getStartTime().atZone(DateUtils.getZoneHCM());
            long days = ChronoUnit.DAYS.between(now.toLocalDate(), startTime.toLocalDate());
            // if days == 1 => is tomorrow
            return days != 1;
        }
        return true;
    }

    private boolean checkCreateInvalid(DoctorAppointmentDTO dto) {
        ZonedDateTime now = ZonedDateTime.now(DateUtils.getZoneHCM()); // DateTime hiện tại
        if (dto.getStartTime().isBefore(now.with(LocalTime.MAX).toInstant())) {
            return false;
        }
        return true;
    }

    private boolean checkOverTimeAllowed(DoctorAppointmentDTO dto, String funcName) {
        if (dto.getStatus().equals(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE)
                || dto.getStatus().equals(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED)) {
            ZonedDateTime now = ZonedDateTime.now(DateUtils.getZoneHCM());
            ZonedDateTime dtoTime = dto.getStartTime().atZone(DateUtils.getZoneHCM());
            long days = ChronoUnit.DAYS.between(now, dtoTime);
            if (days == 0) {
                Config configOther = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.TIME_ALLOW_BEFORE_BOOKING);
                LocalTime localTimeNow = now.toLocalTime();
                LocalTime localTimeDefault = LocalTime.parse(configOther.getPropertyValue());
                if (localTimeNow.isBefore(localTimeDefault) || localTimeNow.equals(localTimeDefault)) {
                    return true;
                }
            } else if (days > 0) {
                return true;
            }
            if (funcName.equals("cancel")) {
                throw new BadRequestAlertException("Đã quá thời gian cho phép hủy lịch", ENTITY_NAME, "over_time_allowed_cancel");
            } else {
                throw new BadRequestAlertException("Thời gian đặt lại lịch đã quá khung giờ cho phép", ENTITY_NAME, "over_time_allowed_update");
            }
        }
        return true;
    }
}
