package com.bkav.lk.web.rest;

import com.bkav.lk.domain.HisClsGoiY;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorAppointmentMapper;
import com.bkav.lk.service.mapper.HisClsGoiYMapper;
import com.bkav.lk.service.mapper.UserMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.ErrorVM;
import com.bkav.lk.web.rest.vm.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API cung cấp cho hệ thống HIS
 */
@RestController
@RequestMapping("/api")
public class HisResource {

    private static final Logger log = LoggerFactory.getLogger(HisResource.class);

    private final DoctorAppointmentService doctorAppointmentService;

    private final PatientRecordService patientRecordService;

    private final HisClsGoiYService hisClsGoiYService;

    private final NotificationService notificationService;

    private final HealthFacilitiesService healthFacilitiesService;

    private final DoctorScheduleTimeService doctorScheduleTimeService;

    private final PatientService patientService;

    private final ObjectMapper objectMapper;

    private final HisClsGoiYMapper hisClsGoiYMapper;

    private final DoctorAppointmentMapper doctorAppointmentMapper;

    private final SubclinicalService subclinicalService;

    private final ActivityLogService activityLogService;

    private final UserService userService;

    private final UserMapper userMapper;

    public HisResource(DoctorAppointmentService doctorAppointmentService, PatientRecordService patientRecordService,
                       HisClsGoiYService hisClsGoiYService, NotificationService notificationService,
                       DoctorScheduleTimeService doctorScheduleTimeService, ObjectMapper objectMapper, HisClsGoiYMapper hisClsGoiYMapper,
                       HealthFacilitiesService healthFacilitiesService, PatientService patientService, DoctorAppointmentMapper doctorAppointmentMapper, SubclinicalService subclinicalService,
                       ActivityLogService activityLogService, UserService userService, UserMapper userMapper) {
        this.doctorAppointmentService = doctorAppointmentService;
        this.patientRecordService = patientRecordService;
        this.hisClsGoiYService = hisClsGoiYService;
        this.notificationService = notificationService;
        this.doctorScheduleTimeService = doctorScheduleTimeService;
        this.objectMapper = objectMapper;
        this.hisClsGoiYMapper = hisClsGoiYMapper;
        this.healthFacilitiesService = healthFacilitiesService;
        this.patientService = patientService;
        this.doctorAppointmentMapper = doctorAppointmentMapper;
        this.subclinicalService = subclinicalService;
        this.activityLogService = activityLogService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * API lấy thông tin bệnh nhân đăng ký khám
     *
     * @param bookingCode Mã đặt lịch khám
     * @return
     */
    @GetMapping("/dangkykham/{dangkykham_id}")
    public ResponseEntity<Object> getPatientRecord(@PathVariable(name = "dangkykham_id") String bookingCode) {
        HisPatientRecord hisPatientRecord;
        hisPatientRecord = patientRecordService.convertToHisPatientRecord(bookingCode);
        if (hisPatientRecord == null) {
            ErrorVM errorVM = new ErrorVM();
            errorVM.setErrorCode("400");
            errorVM.setErrorMsg("Mã lịch khám không tồn tại");
            return ResponseEntity.ok().body(errorVM);
        }
        return ResponseEntity.ok().body(hisPatientRecord);
    }

    /**
     * API đồng bộ thông tin bệnh nhân đăng ký khám
     *
     * @param hisPatientRecord
     * @param bookingCode      Mã đặt lịch khám
     * @return
     */
    @PostMapping("/dangkykham/{dangkykham_id}")
    public ResponseEntity<ErrorVM> asyncPatientRecord(@RequestBody HisPatientRecord hisPatientRecord,
                                                      @PathVariable(name = "dangkykham_id") String bookingCode) {
        ErrorVM errorVM = new ErrorVM();
        errorVM.setErrorCode("0");
        return ResponseEntity.ok(errorVM);
    }

    /**
     * API Phê duyệt lịch khám
     *
     * @param hisAppointment
     * @return
     */
    @PostMapping("/dangkykham/pheduyet")
    public ResponseEntity<ErrorVM> approveAppointment(@RequestBody HisAppointment hisAppointment) {
        Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findByBookingCode(hisAppointment.getBookingCode());
        ErrorVM errorVM = new ErrorVM();
        if (optional.isPresent()) {
            DoctorAppointmentDTO doctorAppointmentDTO = optional.get();
            doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
            errorVM.setErrorCode("0");
        } else {
            errorVM.setErrorCode("400");
            errorVM.setErrorMsg("Lịch khám không tồn tại");
        }
        return ResponseEntity.ok(errorVM);
    }

    /**
     * Cập nhật mã khám khi ng dân đã đến khám
     *
     * @param hisAppointment
     * @return
     */
    @Transactional
    @PostMapping("/capnhatlichkham")
    public ResponseEntity<ErrorVM> updateAppointmentCode(@RequestBody HisAppointment hisAppointment) {
        Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findByBookingCodeAndStatus(hisAppointment.getBookingCode(), Constants.DOCTOR_APPOINTMENT_STATUS.DONE);
        ErrorVM errorVM = new ErrorVM();
        if (optional.isPresent()) {
            DoctorAppointmentDTO doctorAppointmentDTO = optional.get();
            doctorAppointmentDTO.setAppointmentCode(hisAppointment.getAppointmentCode());
            doctorAppointmentService.save(doctorAppointmentDTO);

            //tạo mới thông tin bệnh nhân bên his
            HealthFacilitiesDTO healthFacilityDTO = healthFacilitiesService.findById(doctorAppointmentDTO.getHealthFacilityId());
            PatientDTO patientDTO = new PatientDTO();
            patientDTO.setPatientCode(hisAppointment.getPatientCode());
            patientDTO.setPatientName(hisAppointment.getPatientName());
            patientDTO.setPhone(hisAppointment.getPhone());
            if (Objects.nonNull(healthFacilityDTO)) {
                patientDTO.setHealthFacilityCode(healthFacilityDTO.getCode());
            }
            patientService.save(patientDTO);

            //cập nhật mã bệnh nhân bên HIS cho HSBN
            Optional<PatientRecordDTO> result = patientRecordService.findOne(doctorAppointmentDTO.getPatientRecordId());
            if (result.isPresent()) {
                PatientRecordDTO patientRecordDTO = result.get();
                patientRecordDTO.setHisPatientCode(hisAppointment.getPatientCode());
                patientRecordService.save(patientRecordDTO);
            }
            errorVM.setErrorCode("0");
        } else {
            errorVM.setErrorCode("400");
            errorVM.setErrorMsg("Lịch khám không tồn tại");
        }

        return ResponseEntity.ok(errorVM);
    }

    /**
     * API gợi ý thứ tự thực hiện CLS
     *
     * @param listClsVM
     * @param appointmentCode
     * @return
     */
    @PostMapping("/canlamsang/{dangkikham_id}/{his_makham}")
    public ResponseEntity<ErrorVM> suggestCLS(@RequestBody List<ClsVM> listClsVM,
                                              @PathVariable(name = "his_makham") String appointmentCode,
                                              @PathVariable(name = "dangkikham_id") String bookingCode) {
        ErrorVM errorVM = new ErrorVM();
        Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findByBookingCodeAndStatus(bookingCode, Constants.DOCTOR_APPOINTMENT_STATUS.DONE);
        if (!optional.isPresent()) {
            errorVM.setErrorCode("400");
            errorVM.setErrorMsg("Lịch khám không tồn tại");
            return ResponseEntity.ok(errorVM);
        }
        DoctorAppointmentDTO appointmentDTO = optional.get();
        List<HisClsGoiY> listHisClsGoiY = new ArrayList<>();
        for (ClsVM clsVM : listClsVM) {
            HisClsGoiY item = generateClsGoiY(clsVM, appointmentDTO);
            hisClsGoiYService.save(item);
            listHisClsGoiY.add(item);
        }

        FirebaseData firebaseData = new FirebaseData();
        firebaseData.setObjectId(appointmentDTO.getId().toString());
        try {
            firebaseData.setObject(objectMapper.writeValueAsString(hisClsGoiYMapper.toDto(listHisClsGoiY)).replaceAll("\\n|\\r", ""));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        firebaseData.setType(String.valueOf(Constants.NotificationConstants.SUGGEST_CLS.id));
        String template = Constants.NotificationConstants.SUGGEST_CLS.template;
        List<String> paramsBody = new ArrayList<>();
        String strClsNameList = listHisClsGoiY.stream()
                .map(item -> item.getClsName())
                .collect(Collectors.joining(", "));
        paramsBody.add(strClsNameList);
        notificationService.pushNotification(template, firebaseData, null, paramsBody, appointmentDTO.getUserId());
        errorVM.setErrorCode("0");
        return ResponseEntity.ok(errorVM);
    }

    private HisClsGoiY generateClsGoiY(ClsVM clsVM, DoctorAppointmentDTO appointmentDTO) {
        HisClsGoiY item = new HisClsGoiY();
        item.setClsCode(clsVM.getClsCode());
        item.setClsName(clsVM.getClsName());
        item.setClsNumber(clsVM.getClsNumber());
        item.setClsDepartment(clsVM.getClsDepartment());
        item.setClsDoctor(clsVM.getClsDoctor());
        item.setClsPriority(clsVM.getClsPriority());
        item.setClsCurrentPriority(clsVM.getClsCurrentPriority());
        item.setScheduledTime(clsVM.getScheduledTime());
        item.setDoctorAppointmentCode(appointmentDTO.getAppointmentCode());
        item.setBookingCode(appointmentDTO.getBookingCode());
        item.setPatientRecordId(appointmentDTO.getPatientRecordId());
        Optional<PatientRecordDTO> optional = patientRecordService.findOne(appointmentDTO.getPatientRecordId());
        if (optional.isPresent()) {
            item.setUserId(optional.get().getUserId());
        }
        return item;
    }

    /**
     * API thông báo có kết quả CLS
     *
     * @param hisSubclinical
     * @return
     */
    @PostMapping("/thongbaokqcls")
    public ResponseEntity<ErrorVM> sendNotifyCLS(@RequestBody HisSubclinical hisSubclinical) {
        ErrorVM errorVM = new ErrorVM();
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        if (StringUtils.isNoneBlank(hisSubclinical.getDoctorAppointmentCode())) {
            doctorAppointmentDTO = doctorAppointmentService.findByAppointmentCode(hisSubclinical.getDoctorAppointmentCode());
            if (Objects.nonNull(doctorAppointmentDTO)) {
                subclinicalService.save(this.consumeSubclinicalResult(hisSubclinical, doctorAppointmentDTO));
                errorVM.setErrorCode("0");
            } else {
                errorVM.setErrorCode("400");
                errorVM.setErrorMsg("Lịch khám không tồn tại");
            }
        } else {
            errorVM.setErrorCode("400");
            errorVM.setErrorMsg("Trường mã lịch khám là bắt buộc");
        }
        return ResponseEntity.ok(errorVM);
    }

    private SubclinicalDTO consumeSubclinicalResult(HisSubclinical hisSubclinical, DoctorAppointmentDTO doctorAppointmentDTO) {
        SubclinicalDTO subclinical = new SubclinicalDTO();
        subclinical.setHealthFacilityId(doctorAppointmentDTO.getHealthFacilityId());
        subclinical.setCode(hisSubclinical.getCode());
        subclinical.setDoctorAppointmentCode(hisSubclinical.getDoctorAppointmentCode());
        subclinical.setName(hisSubclinical.getName());
        subclinical.setRoom(hisSubclinical.getRoom());
        subclinical.setTechnician(hisSubclinical.getTechnician());
        subclinical.setStatus(Constants.NOTIFICATION_STATUS.HAVE_NOT_NOTIFIED);
        return subclinical;
    }

    /**
     * API cung cấp thông tin người dùng trong lịch khám online
     *
     * @return
     */
    @GetMapping("/thongtinnguoidung")
    public ResponseEntity<List<UserDTO>> getAllUser() {
        List<UserDTO> users = userMapper.toDto(userService.getListUser());
        return ResponseEntity.ok().body(users);
    }

    @PostMapping("/taolichtaikham/{his_makham}")
    public ResponseEntity<ErrorVM> hisCreateReExamination(
            @PathVariable (name = "his_makham") String appointmentCode,
            @RequestBody HisReExaminationVM hisReExaminationVM){
        ErrorVM errorVM = new ErrorVM();
        DoctorAppointmentDTO oldAppointmentDTO = doctorAppointmentService.findByAppointmentCode(appointmentCode);
        if(oldAppointmentDTO == null){
            errorVM.setErrorCode("400");
            errorVM.setErrorMsg("Lịch khám không tồn tại");
            return ResponseEntity.ok().body(errorVM);
        }
        Instant startTime = DateUtils.parseToInstant(hisReExaminationVM.getStartTime(), "YYYY-MM-DD HH:mm:ss");
        Instant endTime = DateUtils.parseToInstant(hisReExaminationVM.getEndTime(), "YYYY-MM-DD HH:mm:ss");
        boolean available = doctorScheduleTimeService.appointmentTimeAvailable(oldAppointmentDTO.getHealthFacilityId(),
                oldAppointmentDTO.getDoctorId(), startTime, endTime, 1, true, false);
        if (!available) {
            errorVM.setErrorCode("500");
            errorVM.setErrorMsg("Đã hết chỗ vui lòng chọn khung giờ khác");
            return ResponseEntity.ok().body(errorVM);
        }
        oldAppointmentDTO.setReExaminationDate(startTime);
        doctorAppointmentService.save(oldAppointmentDTO);

        DoctorAppointmentDTO appointmentDTO = createReExaminationAppointment(oldAppointmentDTO, startTime, endTime);
        appointmentDTO = doctorAppointmentService.save(appointmentDTO);

        //   send notification
        sendNotificationForCreate(appointmentDTO);
        // save log to activity
        activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT, doctorAppointmentMapper.toEntity(appointmentDTO));
        activityLogService.create(Constants.CONTENT_TYPE.RE_EXAMINATION, doctorAppointmentMapper.toEntity(appointmentDTO));

        errorVM.setErrorCode("0");
        return ResponseEntity.ok(errorVM);
    }

    private DoctorAppointmentDTO createReExaminationAppointment(DoctorAppointmentDTO oldDoctorAppointmentDTO, Instant startTime, Instant endTime){
        DoctorAppointmentDTO appointmentDTO = oldDoctorAppointmentDTO;
        appointmentDTO.setStartTime(startTime);
        appointmentDTO.setEndTime(endTime);
        appointmentDTO.setId(null);
        appointmentDTO.setAppointmentCode(null);
        appointmentDTO.setIsReExamination(Constants.BOOL_NUMBER.TRUE);
        appointmentDTO.setIsConfirmed(Constants.BOOL_NUMBER.FALSE);
        appointmentDTO.setChangeAppointmentReason(null);
        appointmentDTO.setBookingCode(null);
        appointmentDTO.setReExaminationDate(null);
        appointmentDTO.setOldAppointmentCode(oldDoctorAppointmentDTO.getAppointmentCode());
        appointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
        return appointmentDTO;
    }

    private void sendNotificationForCreate(DoctorAppointmentDTO appointmentDTO) {
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
}
