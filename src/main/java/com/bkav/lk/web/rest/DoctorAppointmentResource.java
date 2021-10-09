package com.bkav.lk.web.rest;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.*;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorAppointmentMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorVM;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.bkav.lk.web.rest.vm.AppointmentStatisticsVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.bkav.lk.util.Constants.PAYMENT_STATUS.PAID_SUCCESS;

@RestController
@RequestMapping("/api")
public class DoctorAppointmentResource {

    private final DoctorAppointmentService doctorAppointmentService;

    private static final Logger log = LoggerFactory.getLogger(DoctorAppointmentResource.class);

    private static final String ENTITY_NAME = "doctor_appointment";

    private final DoctorScheduleTimeService doctorScheduleTimeService;

    private final NotificationService notificationService;

    private final ObjectMapper objectMapper;

    private final DoctorService doctorService;

    private final DoctorAppointmentMapper doctorAppointmentMapper;

    private final DoctorScheduleService doctorScheduleService;

    private final PatientRecordService patientRecordService;

    private final MedicalServiceService medicalServiceService;

    private final ClinicService clinicService;

    private final MedicalSpecialityService medicalSpecialityService;

    private final UserService userService;

    private final ActivityLogService activityLogService;

    private final StorageService storageService;

    private final TransactionService transactionService;

    private final DoctorAppointmentConfigurationService appointmentConfigService;

    private final HealthFacilitiesService healthFacilitiesService;

    private final RestTemplateHelper restTemplateHelper;

    private final ConfigService configService;

    private final AppointmentCancelConfigService appointmentCancelConfigService;

    private final ConfigIntegratedService configIntegratedService;

    @Value("${social-insurance.insurance_code_check_url}")
    private String INSURANCE_CODE_CHECK_URL;

    @Value("${his.appointment_code_check_url}")
    private String APPOINTMENT_CODE_CHECK_URL;

    public DoctorAppointmentResource(DoctorAppointmentService doctorAppointmentService, DoctorScheduleTimeService doctorScheduleTimeService,
                                     NotificationService notificationService, ObjectMapper objectMapper, DoctorService doctorService,
                                     DoctorAppointmentMapper doctorAppointmentMapper, DoctorScheduleService doctorScheduleService, PatientRecordService patientRecordService, MedicalServiceService medicalServiceService, ClinicService clinicService, MedicalSpecialityService medicalSpecialityService, UserService userService,
                                     ActivityLogService activityLogService, StorageService storageService, TransactionService transactionService,
                                     DoctorAppointmentConfigurationService appointmentConfigService, HealthFacilitiesService healthFacilitiesService,
                                     RestTemplateHelper restTemplateHelper, ConfigService configService, AppointmentCancelConfigService appointmentCancelConfigService, ConfigIntegratedService configIntegratedService) {
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorScheduleTimeService = doctorScheduleTimeService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.doctorService = doctorService;
        this.doctorAppointmentMapper = doctorAppointmentMapper;
        this.doctorScheduleService = doctorScheduleService;
        this.patientRecordService = patientRecordService;
        this.medicalServiceService = medicalServiceService;
        this.clinicService = clinicService;
        this.medicalSpecialityService = medicalSpecialityService;
        this.userService = userService;
        this.activityLogService = activityLogService;
        this.storageService = storageService;
        this.transactionService = transactionService;
        this.appointmentConfigService = appointmentConfigService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.restTemplateHelper = restTemplateHelper;
        this.configService = configService;
        this.appointmentCancelConfigService = appointmentCancelConfigService;
        this.configIntegratedService = configIntegratedService;
    }

    @GetMapping("/doctor-appointments")
    public ResponseEntity<List<DoctorAppointmentDTO>> search(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams,
                                                             Pageable pageable) {
        queryParams.set("healthFacilityId", healthFacilityId.toString());

        User user = getCurrentUser();
        if (user == null) {
            throw new BadRequestAlertException("Invalid User", ENTITY_NAME, "invalid_user");
        }

        Page<DoctorAppointmentDTO> page = doctorAppointmentService.search(queryParams, pageable, user);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/doctor-appointments/user")
    public ResponseEntity<List<DoctorAppointmentDTO>> findByUserMobile(@RequestParam MultiValueMap<String, String> queryParams,
                                                                       Pageable pageable) {
        User user = getCurrentUser();
        if (user == null) {
            throw new BadRequestAlertException("Invalid User", ENTITY_NAME, "invalid_user");
        }
        queryParams.add("userId", String.valueOf(user.getId()));
        if (!queryParams.containsKey("status")) {
            List<String> arrStatus = Arrays.asList(
                    String.valueOf(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE),
                    String.valueOf(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED),
                    String.valueOf(Constants.DOCTOR_APPOINTMENT_STATUS.DONE),
                    String.valueOf(Constants.DOCTOR_APPOINTMENT_STATUS.DENY),
                    String.valueOf(Constants.DOCTOR_APPOINTMENT_STATUS.WAIT_CONFIRM)
            );
            String status = arrStatus.toString().substring(1, arrStatus.toString().length() - 1);
            queryParams.add("status", status);
        }
        Page<DoctorAppointmentDTO> result = doctorAppointmentService.search(queryParams, pageable, null);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), result);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @GetMapping("/doctor-appointments/{id}")
    public ResponseEntity<DoctorAppointmentDTO> findOne(@PathVariable Long id, @RequestHeader(name = "healthFacilityId", required = false) Long healthFacilityId) {
        Optional<DoctorAppointmentDTO> dto = doctorAppointmentService.findOne(id);
        if (!dto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        // Check tren web
        if (healthFacilityId != null && !healthFacilityId.equals(dto.get().getHealthFacilityId())) {
            return ResponseEntity.ok().body(null);
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (dto.get().getPatientDob() != null) {
            dto.get().setPatientDobFormat(dto.get().getPatientDob().atZone(DateUtils.getZoneHCM()).toLocalDate().format(dateTimeFormatter));
        }
        if (dto.get().getDoctorId() != null) {
            DoctorDTO doctor = doctorService.findByIdAndStatus(dto.get().getDoctorId(), Constants.ENTITY_STATUS.ACTIVE);
            dto.get().setDoctorGender(Objects.nonNull(doctor) ? doctor.getGender() : Constants.GENDER.OTHER);
            dto.get().setMedicalSpecialityName(Objects.nonNull(doctor.getMedicalSpecialityName()) ? doctor.getMedicalSpecialityName() : null);
            dto.get().setAcademicCode(Objects.nonNull(doctor.getAcademicCode()) ? doctor.getAcademicCode() : null);
            Instant workingDate = ZonedDateTime.of(dto.get().getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate(),
                    LocalTime.MIN, DateUtils.getZoneHCM()).toInstant();
            List<DoctorScheduleDTO> doctorScheduleDTOS = doctorScheduleService.findAllByWorkingDateAndStatus(Collections.singletonList(dto.get().getDoctorId()), workingDate, Constants.ENTITY_STATUS.ACTIVE);
            if (!doctorScheduleDTOS.isEmpty()) {
                dto.get().setWorkingTime(doctorScheduleDTOS.get(0).getWorkingTime());
            }

        }
        if (dto.get().getPatientRecordId() != null) {
            Optional<PatientRecordDTO> patientRecordDTO = patientRecordService.findOne(dto.get().getPatientRecordId());
            patientRecordDTO.ifPresent(recordDTO -> dto.get().setHealthInsuranceCode(recordDTO.getHealthInsuranceCode()));
        }
        if (dto.get().getMedicalServiceId() != null) {
            Optional<MedicalServiceDTO> medicalService = medicalServiceService.findOne(dto.get().getMedicalServiceId());
            if (medicalService.isPresent()) {
                dto.get().setMedicalServicePrice(medicalService.get().getPrice());
            } else {
                dto.get().setMedicalServicePrice(BigDecimal.ZERO);
            }
        }
        dto.get().setAppointmentDate(dto.get().getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate().format(dateTimeFormatter));
        dto.get().setAppointmentTime(DateUtils.getFriendlyTimeFormat(dto.get().getStartTime(), dto.get().getEndTime()) + " " + DateUtils.friendlyTimeOfDayFormat(dto.get().getStartTime()));
        return ResponseEntity.of(dto);
    }

    @PutMapping("/doctor-appointments/approve")
    public ResponseEntity<Void> approve(@RequestBody Map<String, String> queryParams) {
        String ids = null;
        if (queryParams.containsKey("ids") && !StrUtil.isBlank(queryParams.get("ids"))) {
            ids = queryParams.get("ids");
        }
        if (StrUtil.isBlank(ids)) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        doctorAppointmentService.approve(ids);

        List<Long> listIds = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
        listIds.forEach(id -> {
            Optional<DoctorAppointmentDTO> result = doctorAppointmentService.findOne(id);
            if (result.isPresent()) {
                DoctorAppointmentDTO doctorAppointmentDTO = result.get();
                DoctorAppointment doctorAppointment = doctorAppointmentMapper.toEntity(doctorAppointmentDTO);
                activityLogService.approve(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointment);
                // Todo - Gửi thông báo
                FirebaseData firebaseData = new FirebaseData();
                firebaseData.setObjectId(doctorAppointmentDTO.getId().toString());
                try {
                    firebaseData.setObject(objectMapper.writeValueAsString(doctorAppointmentDTO).replaceAll("\\n|\\r", ""));
                } catch (JsonProcessingException e) {
                    log.error("Error: ", e);
                }
                String template;
                List<String> paramsBody = new ArrayList<>();
                if (result.get().getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE)) {
                    firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_DATE_SUCCESS.id));
                    template = Constants.NotificationConstants.APPOINTMENT_DATE_SUCCESS.template;
                    paramsBody.add(DateUtils.convertFromInstantToString(doctorAppointmentDTO.getStartTime()));
                } else {
                    firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_DOCTOR_SUCCESS.id));
                    template = Constants.NotificationConstants.APPOINTMENT_DOCTOR_SUCCESS.template;
                    paramsBody.add(doctorAppointmentDTO.getAcademicCode() + '.' + doctorAppointmentDTO.getDoctorName());
                }
                notificationService.pushNotification(template, firebaseData, null, paramsBody, result.get().getUserId());
            }
        });

        //Push notify
        /*List<NotifyDoctorAppointmentDTO> listNotify = doctorAppointmentService.getDoctorAppointments(listIds);
        if (!listNotify.isEmpty()) {
                notificationFireBaseService.pushNotiApprove(listNotify);
        }*/
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/doctor-appointments/deny")
    public ResponseEntity<Void> deny(@RequestBody Map<String, String> queryParams) {
        String ids = null;
        String rejectReason = "";
        if (queryParams.containsKey("ids") && !StrUtil.isBlank(queryParams.get("ids"))) {
            ids = queryParams.get("ids");
        }
        if (StrUtil.isBlank(ids)) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (queryParams.containsKey("rejectReason")) {
            rejectReason = queryParams.get("rejectReason");
        }
        doctorAppointmentService.deny(ids, rejectReason);
        // Push notify
        List<Long> listIds = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
        List<DoctorAppointmentDTO> listDto = doctorAppointmentService.findAllByIds(listIds);
        String finalRejectReason = rejectReason;
        listDto.forEach(item -> {
            // nếu lịch đặt đã được thanh toán thì sẽ tạo lịch sử thanh toán refunded và đổi trạng thái sang hoàn tiền
            if (Constants.PAYMENT_STATUS.PAID_SUCCESS.equals(item.getPaymentStatus())) {
                transactionService.refund(item.getPatientRecordId(),
                        item.getBookingCode(),
                        "Hoàn tiền - Từ chối lịch đặt khám: " + finalRejectReason);
                doctorAppointmentService.updatePayStatus(item.getId());
            }
            DoctorScheduleTimeDTO scheduleTimeDTO;
            if (item.getDoctorId() != null) {
                scheduleTimeDTO = doctorScheduleTimeService.findOne(item.getDoctorId(), item.getStartTime(), item.getEndTime(), item.getHealthFacilityId());
            } else {
                scheduleTimeDTO = doctorScheduleTimeService.findOne(item.getStartTime(), item.getEndTime(), item.getHealthFacilityId());
            }

            if (scheduleTimeDTO != null) {
                scheduleTimeDTO.setPeopleRegistered(scheduleTimeDTO.getPeopleRegistered() - 1);
                doctorScheduleTimeService.save(scheduleTimeDTO);
            }
            DoctorAppointment doctorAppointment = doctorAppointmentMapper.toEntity(item);
            activityLogService.deny(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointment);
            // Todo - Gửi thông báo
            FirebaseData firebaseData = new FirebaseData();
            firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_FAILED.id));
            firebaseData.setObjectId(doctorAppointment.getId().toString());
            try {
                firebaseData.setObject(objectMapper.writeValueAsString(item).replaceAll("\\n|\\r", ""));
            } catch (JsonProcessingException e) {
                log.error("Error: ", e);
            }
            notificationService.pushNotification(Constants.NotificationConstants.APPOINTMENT_FAILED.template,
                    firebaseData, null, null, item.getUserId());

        });

        return ResponseEntity.noContent().build();
    }

    // User confirm doctor appointment change
    @PutMapping("/doctor-appointments/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long id) throws JsonProcessingException {
        DoctorAppointmentDTO result = doctorAppointmentService.confirm(id);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        activityLogService.confirm(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointmentMapper.toEntity(result));
        String type = "";
        String template = "";
        if (result.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE)) {
            type = String.valueOf(Constants.NotificationConstants.APPOINTMENT_DATE_SUCCESS.id);
            template = String.valueOf(Constants.NotificationConstants.APPOINTMENT_DATE_SUCCESS.template);
        } else {
            type = String.valueOf(Constants.NotificationConstants.APPOINTMENT_DOCTOR_SUCCESS.id);
            template = String.valueOf(Constants.NotificationConstants.APPOINTMENT_DOCTOR_SUCCESS.template);
        }
        sendNotification(result, type, template, "confirm");
        return ResponseEntity.noContent().build();
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

    // User actively cancel
    @PutMapping("/doctor-appointments/{id}/cancel")
    @Transactional
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        Optional<DoctorAppointmentDTO> dto = doctorAppointmentService.findOne(id);
        if (!dto.isPresent()) {
            throw new BadRequestAlertException("Not exist", ENTITY_NAME, "not_exist");
        }
        checkOverTimeAllowed(dto.get(), "cancel");
        DoctorAppointmentDTO result = doctorAppointmentService.cancel(id);
        if (Constants.PAYMENT_STATUS.PAID_SUCCESS.equals(result.getPaymentStatus())) {
            transactionService.refund(result.getPatientRecordId(), result.getBookingCode(), "Hoàn tiền - Hủy lịch đặt khám");
            doctorAppointmentService.updatePayStatus(id);
        }
        if (result.getDoctorId() != null) {
            doctorScheduleTimeService.minusSubscriptions(result.getDoctorId(), result.getStartTime(), result.getEndTime(), 1, result.getHealthFacilityId());
        } else {
            doctorScheduleTimeService.minusSubscriptions(null, result.getStartTime(), result.getEndTime(), 1, result.getHealthFacilityId());
        }
        activityLogService.cancel(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointmentMapper.toEntity(result));

        //cập nhật số lượt hủy lịch khám
        AppointmentCancelLog appointmentCancelLog = appointmentCancelConfigService.findByCurrentUser();
        if (Objects.isNull(appointmentCancelLog)) {
            appointmentCancelConfigService.save();
        } else {
            appointmentCancelLog.setMaxDayCanceled(appointmentCancelLog.getMaxDayCanceled() > 0 ? appointmentCancelLog.getMaxDayCanceled() - 1 : 0);
            appointmentCancelLog.setMaxWeekCanceled(appointmentCancelLog.getMaxWeekCanceled() > 0 ? appointmentCancelLog.getMaxWeekCanceled() - 1 : 0);
            if (appointmentCancelLog.getMaxDayCanceled() <= 0 || appointmentCancelLog.getMaxWeekCanceled() <= 0) {
                appointmentCancelLog.setIsBlocked(Constants.BOOL_NUMBER.TRUE);
                appointmentCancelLog.setStartBlockedDate(DateUtils.nowInstant());
            }
            appointmentCancelConfigService.update(appointmentCancelLog);
        }
        String type = String.valueOf(Constants.NotificationConstants.APPOINTMENT_CANCEL.id);
        String template = String.valueOf(Constants.NotificationConstants.APPOINTMENT_CANCEL.template);
        sendNotification(result, type, template, "cancel");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/doctor-appointments")
    @Transactional
    public ResponseEntity<Boolean> update(@RequestBody List<DoctorAppointmentDTO> listDoctorAppointmentDTO) {
        if (listDoctorAppointmentDTO.size() == 0) {
            throw new BadRequestAlertException("Empty list", ENTITY_NAME, "listempty");
        }
        DoctorAppointmentDTO dto = listDoctorAppointmentDTO.get(0);
        boolean available = doctorScheduleTimeService.appointmentTimeAvailable(dto.getHealthFacilityId(),
                dto.getDoctorId(), dto.getStartTime(), dto.getEndTime(), listDoctorAppointmentDTO.size(), true, true);
        if (!available) {
            throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "notAvailableAppointment");
        }
        List<Long> ids = listDoctorAppointmentDTO
                .stream()
                .map(DoctorAppointmentDTO::getId)
                .collect(Collectors.toList());

        List<DoctorAppointment> listOldDoctorAppointment = getListByIds(ids);
        listOldDoctorAppointment.forEach(item -> {
            if (item.getDoctor() == null) {
                doctorScheduleTimeService.minusSubscriptions(null, item.getStartTime(), item.getEndTime(), 1, item.getHealthFacilityId());
            } else {
                doctorScheduleTimeService.minusSubscriptions(item.getDoctor().getId(), item.getStartTime(), item.getEndTime(), 1, item.getHealthFacilityId());
            }
        });

        Object result = doctorAppointmentService.update(listDoctorAppointmentDTO);
        if (result instanceof Boolean && !(Boolean)result) {
            throw new BadRequestAlertException("Chuyển lịch thất bại", ENTITY_NAME, "appointment.update.fail");
        }

        // Save log and Push notify
        saveLogAndSendNotification(listOldDoctorAppointment);

        return ResponseEntity.ok().body(true);
    }

    @PutMapping("/doctor-appointments/update")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> updateDoctorAppointment(@RequestBody DoctorAppointmentDTO dto) {
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

        // Kiểm tra lại các trường csyt, phòng khám, bác sỹ, chuyên khoa, dịch vụ khám xem còn hoạt động không?
        if (healthFacilitiesService.findById(dto.getHealthFacilityId()).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Health facility is Inactived", ENTITY_NAME, "doctor_appointment.healthFacility_isInactived");
        }
        if (Objects.requireNonNull(clinicService.findOne(dto.getClinicId()).orElse(null)).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Clinic is Inactived", ENTITY_NAME, "doctor_appointment.clinic_isInactived");
        }
        if (doctorService.findById(dto.getDoctorId()).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Doctor is Inactived", ENTITY_NAME, "doctor_appointment.doctor_isInactived");
        }
        if (Objects.requireNonNull(medicalServiceService.findOne(dto.getMedicalServiceId()).orElse(null)).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Medical service is Inactived", ENTITY_NAME, "doctor_appointment.medicalService_isInactived");
        }
        if (medicalSpecialityService.findOne(dto.getMedicalSpecialityId()).getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Medical speciality is Inactived", ENTITY_NAME, "doctor_appointment.medicalSpeciality_isInactived");
        }

        // Kiểm tra nếu là tái khám thì mã khám cũ có tồn tại trong hệ thống không (nếu không thì gọi sang HIS để kiểm tra)
        if (Constants.ENTITY_STATUS.ACTIVE.equals(dto.getIsReExamination())) {
            Long healthFacilityId = dto.getHealthFacilityId();
            boolean appointmentCodeExist = doctorAppointmentService.existsByAppointmentCode(dto.getOldAppointmentCode());
            if (!appointmentCodeExist) {
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
        List<DoctorAppointment> listOld = getListByIds(Collections.singletonList(dto.getId()));

        // Là thay đổi Thời gian khám bệnh hoặc DoctorId
        boolean isChange = false;

        // Từ request => nếu dto == null && optional == null => Không thay đổi
        if (Objects.nonNull(dto.getDoctorId()) && Objects.isNull(optional.get().getDoctorId()) && !Constants.DOCTOR_APPOINTMENT_STATUS.DENY.equals(dto.getStatus())) {
            isChange = true;
        } else if (Objects.isNull(dto.getDoctorId()) && Objects.nonNull(optional.get().getDoctorId()) && !Constants.DOCTOR_APPOINTMENT_STATUS.DENY.equals(dto.getStatus())) {
            isChange = true;
        } else if (Objects.nonNull(dto.getDoctorId()) && Objects.nonNull(optional.get().getDoctorId()) && !Constants.DOCTOR_APPOINTMENT_STATUS.DENY.equals(dto.getStatus())) {
            if (!dto.getDoctorId().equals(optional.get().getDoctorId())) {
                isChange = true;
            }
        } else {
            // Thay đổi thời gian khám
            if (!dto.getStartTime().equals(optional.get().getStartTime()) && !Constants.DOCTOR_APPOINTMENT_STATUS.DENY.equals(dto.getStatus())) {
                isChange = true;
            }
        }

        if (isChange) {
            boolean available = doctorScheduleTimeService.appointmentTimeAvailable(dto.getHealthFacilityId(),
                    dto.getDoctorId(), dto.getStartTime(), dto.getEndTime(), 1, true, true);

            if (!available) {
                throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "notAvailableAppointment");
            }

            listOld.forEach(item -> {
                if (Objects.isNull(item.getDoctor()) || item.getDoctor().getId() == null) {
                    doctorScheduleTimeService.minusSubscriptions(null, item.getStartTime(), item.getEndTime(), 1, item.getHealthFacilityId());
                } else {
                    doctorScheduleTimeService.minusSubscriptions(item.getDoctor().getId(), item.getStartTime(), item.getEndTime(), 1, item.getHealthFacilityId());
                }
            });
        }

        if (Constants.DOCTOR_APPOINTMENT_STATUS.DENY.equals(dto.getStatus())) {
            if (Objects.isNull(dto.getDoctorId())) {
                doctorScheduleTimeService.plusSubscriptions(null, dto.getStartTime(), dto.getEndTime(), dto.getHealthFacilityId(), false);
            } else {
                doctorScheduleTimeService.plusSubscriptions(dto.getDoctorId(), dto.getStartTime(), dto.getEndTime(), dto.getHealthFacilityId(), false);
            }
        }

        Object result = doctorAppointmentService.update(Collections.singletonList(dto));
        if (result instanceof Boolean && !(Boolean)result) {
            throw new BadRequestAlertException("Cập nhật thất bại", ENTITY_NAME, "appointment.update.fail");
        }

        // Save log
        saveLogUpdate(listOld);

        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/doctor-appointments/calculate-cost-medical")
    public ResponseEntity<Map<String, String>> calculateCostOfMedicalExamination(@RequestBody DoctorAppointmentDTO dto) {
        // Tính lại chi phí khám bệnh - khi trường hợp đã thanh toán
        Map<String, String> map = new LinkedHashMap<>();

        if (dto.getId() == null) {
            throw new BadRequestAlertException("id_null", ENTITY_NAME, "id_null");
        }
        Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findOne(dto.getId());
        if (!optional.isPresent()) {
            throw new BadRequestAlertException("id_not_found", ENTITY_NAME, "id_not_found");
        }
        if (optional.get().getPaymentStatus().equals(PAID_SUCCESS)) {
            if (!dto.getMedicalServiceId().equals(optional.get().getMedicalServiceId())) { // Thay đổi dịch vụ khám
                Optional<MedicalServiceDTO> medicalService = medicalServiceService.findOne(dto.getMedicalServiceId());
                TransactionDTO transaction = transactionService
                        .findTopByBookingCodeAndTypeCodeAndPaymentStatus(
                                optional.get().getBookingCode(),
                                Constants.TRANSACTION_TYPE_CODE.DEPOSIT,
                                PAID_SUCCESS
                        );
                if (medicalService.isPresent()) {
                    BigDecimal medicalPriceOld = transaction.getTotalAmount();
                    BigDecimal medicalPriceNew = medicalService.get().getPrice();
                    if (medicalPriceOld.compareTo(medicalPriceNew) == 0) { // equal
                        map.put("amount", BigDecimal.ZERO.toString());
                        map.put("type", "");
                        map.put("message", "Medical service price (old) = Medical service price (new)");
                    } else if (medicalPriceOld.compareTo(medicalPriceNew) > 0) { // medicalPriceOld > medicalPriceNew => dư
                        map.put("amount", medicalPriceOld.subtract(medicalPriceNew).toString());
                        map.put("type", Constants.TRANSACTION_TYPE_CODE.WITHDRAW);
                        map.put("message", "Medical service price (old) > Medical service price (new)");
                    } else { // medicalPriceOld < medicalPriceNew
                        map.put("amount", medicalPriceNew.subtract(medicalPriceOld).toString());
                        map.put("type", Constants.TRANSACTION_TYPE_CODE.DEPOSIT);
                        map.put("message", "Medical service price (old) < Medical service price (new)");
                    }
                    map.put("medicalServicePriceOld", medicalPriceOld.toString());
                    map.put("medicalServicePriceNew", medicalPriceNew.toString());
                    return ResponseEntity.ok().body(map);
                } else {
                    throw new BadRequestAlertException("Medical Service not found", "DoctorAppointment", "medical_service_not_found");
                }
            }
        }
        throw new BadRequestAlertException("This api only applies check for online payments - when the previous payment was made", "DoctorAppointment", "api_only_check_online_payments");
    }

    private List<DoctorAppointment> getListByIds(List<Long> ids) {
        List<DoctorAppointment> listOldDoctorAppointment = new ArrayList<>();
        for (Long id : ids) {
            Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findOne(id);
            if (optional.isPresent()) {
                DoctorAppointment doctorAppointment = doctorAppointmentMapper.toEntity(optional.get());
                Doctor doctor = new Doctor();
                if (optional.get().getDoctorId() != null) {
                    doctor.setId(optional.get().getDoctorId());
                    doctorAppointment.setDoctor(doctor);
                }
                listOldDoctorAppointment.add(doctorAppointment);
            }
        }
        return listOldDoctorAppointment;
    }

    private void sendNotification(DoctorAppointmentDTO doctorAppointment, String type, String template, String typeNotification) {
        FirebaseData firebaseData = new FirebaseData();
        firebaseData.setObjectId(doctorAppointment.getId().toString());
        try {
            firebaseData.setObject(objectMapper.writeValueAsString(doctorAppointment).replaceAll("\\n|\\r", ""));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        firebaseData.setType(type);

        List<String> paramsBody = new ArrayList<>();
        if (typeNotification.equals("confirm")) {
            String body = DateUtils.convertFromInstantToString(doctorAppointment.getStartTime());
            if (doctorAppointment.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DOCTOR)) {
                body = doctorAppointment.getAcademicCode() + "." + doctorAppointment.getDoctorName();
            }
            paramsBody.add(body);
        }
        Optional<PatientRecordDTO> patientRecordOptional = patientRecordService.findOne(doctorAppointment.getPatientRecordId());
        patientRecordOptional.ifPresent(patientRecordDTO -> {
            if (typeNotification.equals("confirm")) {
                notificationService.pushNotification(template, firebaseData, null, paramsBody, patientRecordDTO.getUserId());
            } else {
                notificationService.saveNotification(template, firebaseData, null, paramsBody, patientRecordDTO.getUserId());
            }
        });
    }

    private void saveLogAndSendNotification(List<DoctorAppointment> listOldDoctorAppointment) {
        for (DoctorAppointment oldDoctorAppointment : listOldDoctorAppointment) {
            Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findOne(oldDoctorAppointment.getId());
            if (optional.isPresent()) {
                DoctorAppointment newDoctorAppointment = doctorAppointmentMapper.toEntity(optional.get());
                Doctor doctor = new Doctor();
                if (optional.get().getDoctorId() != null) {
                    doctor.setId(optional.get().getDoctorId());
                    newDoctorAppointment.setDoctor(doctor);
                }
                FirebaseData firebaseData = new FirebaseData();
                firebaseData.setObjectId(oldDoctorAppointment.getId().toString());
                try {
                    firebaseData.setObject(objectMapper.writeValueAsString(doctorAppointmentMapper.toDto(newDoctorAppointment)).replaceAll("\\n|\\r", ""));
                } catch (JsonProcessingException e) {
                    log.error("Error: ", e);
                }
                List<String> paramsBody = new ArrayList<>();
                paramsBody.add(newDoctorAppointment.getChangeAppointmentReason());
                String strOldAppointment = generateMessage(oldDoctorAppointment);
                String strNewAppointment = generateMessage(newDoctorAppointment);
                paramsBody.add(strOldAppointment);
                paramsBody.add(strNewAppointment);

                firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_CHANGE.id));
                String template = Constants.NotificationConstants.APPOINTMENT_CHANGE.template;

                Optional<PatientRecordDTO> patientRecordOptional = patientRecordService.findOne(newDoctorAppointment.getPatientRecord().getId());
                // send notification to user through firebase
                patientRecordOptional.ifPresent(patientRecordDTO -> notificationService.pushNotification(template, firebaseData, null, paramsBody, patientRecordDTO.getUserId()));
                // save activity log
                activityLogService.update(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, oldDoctorAppointment, newDoctorAppointment);
            }
        }
    }

    private void saveLogUpdate(List<DoctorAppointment> listOldDoctorAppointment) {
        for (DoctorAppointment oldDoctorAppointment : listOldDoctorAppointment) {
            Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findOne(oldDoctorAppointment.getId());
            if (optional.isPresent()) {
                DoctorAppointment newDoctorAppointment = doctorAppointmentMapper.toEntity(optional.get());
                Doctor doctor = new Doctor();
                if (optional.get().getDoctorId() != null) {
                    doctor.setId(optional.get().getDoctorId());
                    newDoctorAppointment.setDoctor(doctor);
                }
                // save activity log
                activityLogService.update(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, oldDoctorAppointment, newDoctorAppointment);
            }
        }
    }

    private String generateMessage(DoctorAppointment doctorAppointment) {
        String str = "Lịch khám ngày " + DateUtils.convertFromInstantToString(doctorAppointment.getStartTime());
        if (doctorAppointment.getDoctor() != null) {
            DoctorDTO doctorDTO = doctorService.findById(doctorAppointment.getDoctor().getId());
            str += " bởi bác sĩ " + doctorDTO.getName();
        }
        return str;
    }

    @GetMapping("/doctor-appointments/statistics")
    public ResponseEntity<AppointmentStatisticsVM> report(@RequestHeader Long healthFacilityId) {
        AppointmentStatisticsVM appointmentStatisticsVM = new AppointmentStatisticsVM();
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new BadRequestAlertException("Not found current login user", "user", "user.notfound"));
        Integer waiting = 0;
        Integer approved = 0;
        Integer done = 0;
        Integer canceled = 0;
        if (Objects.nonNull(currentUser.getDoctorId())) {
            waiting = doctorAppointmentService.countByStatus(currentUser.getDoctorId(), Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE, healthFacilityId);
            approved = doctorAppointmentService.countByStatus(currentUser.getDoctorId(), Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED, healthFacilityId);
            done = doctorAppointmentService.countByStatus(currentUser.getDoctorId(), Constants.DOCTOR_APPOINTMENT_STATUS.DONE, healthFacilityId);
            canceled = doctorAppointmentService.countByStatus(currentUser.getDoctorId(), Constants.DOCTOR_APPOINTMENT_STATUS.CANCEL, healthFacilityId);
        } else {
            waiting = doctorAppointmentService.countByStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE, healthFacilityId);
            approved = doctorAppointmentService.countByStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED, healthFacilityId);
            done = doctorAppointmentService.countByStatus(Constants.DOCTOR_APPOINTMENT_STATUS.DONE, healthFacilityId);
            canceled = doctorAppointmentService.countByStatus(Constants.DOCTOR_APPOINTMENT_STATUS.CANCEL, healthFacilityId);
        }
        appointmentStatisticsVM.setWaiting(waiting);
        appointmentStatisticsVM.setApproved(approved);
        appointmentStatisticsVM.setDone(done);
        appointmentStatisticsVM.setCanceled(canceled);
        return ResponseEntity.ok().body(appointmentStatisticsVM);
    }

    @PostMapping("/doctor-appointments")
    public ResponseEntity<DoctorAppointmentDTO> create(@RequestBody @Valid DoctorAppointmentDTO doctorAppointmentDTO) throws URISyntaxException {
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
                throw new BadRequestAlertException("Invalid doctor id", ENTITY_NAME, "doctor_appointment.doctor_id_null");
            }
            DoctorDTO doctorDTOOptional = doctorService.findById(doctorAppointmentDTO.getDoctorId());
            if (doctorDTOOptional == null) {
                throw new BadRequestAlertException("Doctor Not Found", ENTITY_NAME, "doctor_appointment.doctor_not_found");
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
//            throw new BadRequestAlertException("Cơ sở y tế hiện không hoạt động", ENTITY_NAME, "doctor_appointment.health_facility_deactivate");
        }

        boolean available = doctorScheduleTimeService.appointmentTimeAvailable(doctorAppointmentDTO.getHealthFacilityId(),
                doctorAppointmentDTO.getDoctorId(), doctorAppointmentDTO.getStartTime(), doctorAppointmentDTO.getEndTime(), 1, true, false);

        if (available) {
            if (Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC.equals(configAppointment.getConnectWithHis())) {
                doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
            } else {
                doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
            }

            doctorAppointmentDTO.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
            doctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_WATING);
            DoctorAppointmentDTO result = doctorAppointmentService.save(doctorAppointmentDTO);
            activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointmentMapper.toEntity(result));
            return ResponseEntity.created(new URI("/api/doctor-appointments/" + result.getId()))
                    .body(result);
        }
        throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "doctor_appointment.not_available_appointment");

    }

    // Dung cho Mobile
    @PostMapping("/doctor-appointments-mobile")
    public ResponseEntity<DoctorAppointmentDTO> createForMobile(@RequestBody @Valid DoctorAppointmentDTO doctorAppointmentDTO) throws URISyntaxException {
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
                throw new BadRequestAlertException("Invalid doctor id", ENTITY_NAME, "doctor_appointment.doctor_id_null");
            }
            DoctorDTO doctorDTOOptional = doctorService.findById(doctorAppointmentDTO.getDoctorId());
            if (doctorDTOOptional == null) {
                throw new BadRequestAlertException("Doctor Not Found", ENTITY_NAME, "doctor_appointment.doctor_not_found");
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
//            throw new BadRequestAlertException("Cơ sở y tế hiện không hoạt động", ENTITY_NAME, "doctor_appointment.health_facility_deactivate");
        }

        boolean available = doctorScheduleTimeService.appointmentTimeAvailable(doctorAppointmentDTO.getHealthFacilityId(),
                doctorAppointmentDTO.getDoctorId(), doctorAppointmentDTO.getStartTime(), doctorAppointmentDTO.getEndTime(), 1, true, false);

        if (available) {
            if (Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC.equals(configAppointment.getConnectWithHis())) {
                doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
            } else {
                doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
            }

            doctorAppointmentDTO.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
            doctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_WATING);
            DoctorAppointmentDTO result = doctorAppointmentService.save(doctorAppointmentDTO);
            activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointmentMapper.toEntity(result));
            return ResponseEntity.created(new URI("/api/doctor-appointments/" + result.getId()))
                    .body(result);
        }
        throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "doctor_appointment.not_available_appointment");

    }

    private boolean checkCreateOverTime(DoctorAppointmentDTO dto) {
        Config configOther = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.TIME_ALLOW_BEFORE_BOOKING);
        ZonedDateTime now = ZonedDateTime.now(DateUtils.getZoneHCM()); // DateTime hiện tại
        LocalTime localTimeNow = now.toLocalTime();
        LocalTime localTimeDefault = LocalTime.parse(configOther.getPropertyValue());

        // Nếu thời gian hiện tại mà >= 16:30 thì không cho phép đặt lịch khám vào ngày hôm sau
        if (localTimeNow.isAfter(localTimeDefault) || localTimeNow.equals(localTimeDefault)) {
            ZonedDateTime startTime = dto.getStartTime().atZone(DateUtils.getZoneHCM());
            long days = ChronoUnit.DAYS.between(now.toLocalDate(), startTime.toLocalDate());
            // if days == 1 => is tomorrow
            if (days == 1) {
                return false;
            }
            return true;
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


    @PostMapping("/doctor-appointments/bulk-create-re-exam")
    public ResponseEntity<Boolean> bulkCreateReExam(@RequestBody @Valid List<DoctorAppointmentDTO> listDTO) {
        for (DoctorAppointmentDTO item : listDTO) {
            if (item.getId() != null) {
                throw new BadRequestAlertException("A new Appointment cannot already have an ID", ENTITY_NAME, "idexists");
            }
        }
        if (listDTO.size() == 0) {
            throw new BadRequestAlertException("Empty list", ENTITY_NAME, "listempty");
        }
        DoctorAppointmentDTO dto = listDTO.get(0);
        boolean available = doctorScheduleTimeService.appointmentTimeAvailable(dto.getHealthFacilityId(),
                dto.getDoctorId(), dto.getStartTime(), dto.getEndTime(), listDTO.size(), true, false);
        if (!available) {
            throw new BadRequestAlertException("Đã hết chỗ vui lòng chọn khung giờ khác", ENTITY_NAME, "notAvailableAppointment");
        }
        for (DoctorAppointmentDTO item : listDTO) {
            DoctorAppointmentDTO oldAppointment = doctorAppointmentService.findByAppointmentCode(item.getAppointmentCode());
            oldAppointment.setReExaminationDate(item.getEndTime());
            doctorAppointmentService.save(oldAppointment);
            item.setIsReExamination(Constants.BOOL_NUMBER.TRUE);
            item.setIsConfirmed(Constants.BOOL_NUMBER.FALSE);
            item.setOldAppointmentCode(item.getAppointmentCode());
            item.setAppointmentCode("");
            item.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
            item.setOldBookingCode(null);
            DoctorAppointmentDTO result = doctorAppointmentService.save(item);

            //   send notification
            sendNotificationForBulkCreate(result);
            // save log to activity
            activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointmentMapper.toEntity(result));
            activityLogService.create(Constants.CONTENT_TYPE.RE_EXAMINATION, doctorAppointmentMapper.toEntity(result));
        }
        return ResponseEntity.ok().body(true);
    }

    private void sendNotificationForBulkCreate(DoctorAppointmentDTO appointmentDTO) {
        FirebaseData firebaseData = new FirebaseData();
        firebaseData.setObjectId(appointmentDTO.getId().toString());
        try {
            firebaseData.setObject(objectMapper.writeValueAsString(appointmentDTO).replaceAll("\\n|\\r", ""));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_REMINDER.id));
        String template = Constants.NotificationConstants.APPOINTMENT_REMINDER.template;
        List<String> paramsBody = new ArrayList<>();
        String timeStr = DateUtils.convertFromInstantToHour2(appointmentDTO.getStartTime());
        String dateStr = DateUtils.convertFromInstantToString(appointmentDTO.getStartTime());
        if (appointmentDTO.getDoctorId() != null) {
            dateStr += " với bác sĩ " + appointmentDTO.getDoctorName();
        }
        HealthFacilitiesDTO healthFacilitiesDTO = healthFacilitiesService.findById(appointmentDTO.getHealthFacilityId());

        String healthFacilitiesStr = " cơ sở y tế";
        if (healthFacilitiesDTO != null) {
            healthFacilitiesStr = healthFacilitiesDTO.getName();
        }
        paramsBody.add(timeStr);
        paramsBody.add(dateStr);
        paramsBody.add(healthFacilitiesStr);

        Optional<PatientRecordDTO> patientRecordOptional = patientRecordService.findOne(appointmentDTO.getPatientRecordId());
        if (patientRecordOptional.isPresent()) {
            notificationService.pushNotification(template, firebaseData, null, paramsBody, patientRecordOptional.get().getUserId());
        }
    }

    @GetMapping("/doctor-appointments/patient-record/{patient_record_id}")
    ResponseEntity<List<String>> getAppointmentCodesByPatientId(@PathVariable(value = "patient_record_id") Long patientId, @RequestParam(name = "healthFacilityId", required = false) Long healthFacilityId) {
        return ResponseEntity.ok().body(doctorAppointmentService.getAppointmentCodesByPatientId(patientId, healthFacilityId, Constants.DOCTOR_APPOINTMENT_STATUS.DONE));
    }

    @GetMapping("/doctor-appointments/{id}/history")
    public ResponseEntity<List<DoctorAppointmentHistoryDTO>> getDoctorAppointmentHistory(@PathVariable Long id) {
        List<DoctorAppointmentHistoryDTO> list = doctorAppointmentService.getHistory(id);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/doctor-appointments/doctor/{id}")
    public ResponseEntity<Boolean> existsByDoctorId(@PathVariable(name = "id") Long doctorId) {
        boolean result = doctorAppointmentService.existByDoctorId(doctorId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/doctor-appointments/reminder-appointment")
    public ResponseEntity<List<DoctorAppointmentDTO>> reminderAppointment() {
        List<DoctorAppointmentDTO> list = doctorAppointmentService.schedulingReminderAppointmentJob();
        if (!list.isEmpty()) {
            //    notificationFireBaseService.pushNotificationReminder(list);
        }
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/public/doctor-appointments/booking")
    public ResponseEntity<List<PatientRecordDTO>> bookingAppointment(@RequestParam String code) {
        // TODO: code => HSBN, SĐT, CMND, CCCD, BHXH
        //  Kiểm tra thông tin trong hệ thống
        List<PatientRecordDTO> patientRecords = patientRecordService.getPhone(code);
        if (!patientRecords.isEmpty()) {
            // TODO: Nếu SĐT tồn tại trong Hệ thống => gửi mã OTP tới SĐT
            List<String> phones = patientRecords.stream().map(PatientRecordDTO::getPhone).collect(Collectors.toList());
            return ResponseEntity.ok().body(patientRecords);
        }
        // TODO: Nếu SĐT không tồn tại trong hệ thống => Gửi thông tin sang SSO để kiểm tra thông tin.
        //  Function Gửi thông tin sang SSO
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/public/doctor-appointments/download")
    public ResponseEntity<String> exportExcelSearchReExam(@RequestParam MultiValueMap<String, String> queryParams,
                                                                       Pageable pageable) throws IOException {
        queryParams.set("pageIsNull", null);
        List<DoctorAppointmentDTO>  list = doctorAppointmentService.search(queryParams, pageable, null).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/re_examination.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_re_examination_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @GetMapping("/public/doctor-appointments/export")
    public void exportExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_re_examination_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_re_examination_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    private User getCurrentUser() {
        Optional<String> optionalLogin = SecurityUtils.getCurrentUserLogin();
        if (!optionalLogin.isPresent()) {
            return null;
        }
        Optional<User> user = userService.findByLogin(optionalLogin.get());
        if (!user.isPresent()) {
            return null;
        }
        return user.get();
    }

    @GetMapping("/doctor-appointments/booking-code/{bookingCode}")
    public ResponseEntity<DoctorAppointmentDTO> getByBookingCode(@PathVariable(name = "bookingCode") String bookingCode) {
        DoctorAppointmentDTO dto = doctorAppointmentService.findTopByBookingCode(bookingCode);
        if (dto != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (dto.getPatientDob() != null) {
                dto.setPatientDobFormat(dto.getPatientDob().atZone(DateUtils.getZoneHCM()).toLocalDate().format(dateTimeFormatter));
            }
            dto.setAppointmentDate(dto.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate().format(dateTimeFormatter));
            dto.setAppointmentTime(DateUtils.getFriendlyTimeFormat(dto.getStartTime(), dto.getEndTime()) + " " + DateUtils.friendlyTimeOfDayFormat(dto.getStartTime()));
        }
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/doctor-appointments/exists-old-appointment/{old_appointment_code}")
    public ResponseEntity<Boolean> existsOldAppointment(@PathVariable("old_appointment_code") String oldAppointmentCode) {
        Boolean existsOldAppointment = doctorAppointmentService.existsByAppointmentCode(oldAppointmentCode);
        if (!existsOldAppointment) {
            Config config = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
            ErrorVM response = restTemplateHelper.execute(config.getPropertyValue() + APPOINTMENT_CODE_CHECK_URL + oldAppointmentCode,
                    HttpMethod.GET, null, ErrorVM.class);
            if (Objects.nonNull(response)) {
                existsOldAppointment = Constants.HIS_STATUS_CODE.SUCCESS.equals(response.getErrorCode());
            } else {
                throw new BadRequestAlertException("Can't connect to HIS for checking appointment code", ENTITY_NAME, "his.failed_connection");
            }
        }
        return ResponseEntity.ok().body(existsOldAppointment);
    }

    @GetMapping("/public/doctor-appointments/export-excel/search")
    public ResponseEntity<String> exportExcelSearch(@RequestHeader(name = "healthFacilityId") Long healthFacilityId,
                                                                 @RequestParam MultiValueMap<String, String> queryParams,
                                                                 Pageable pageable) throws IOException {
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        queryParams.set("pageIsNull", null);
        List<DoctorAppointmentDTO>  list = doctorAppointmentService.search(queryParams, pageable, null).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/doctorAppointments.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_doctorAppointments_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @GetMapping("/public/doctor-appointments/export-excel")
    public void exportExcelDoctorAppointments(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_doctorAppointments_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_doctorAppointments_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }
}
