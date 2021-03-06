package com.bkav.lk.web.rest;

import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.notification.firebase.FCMNotificationRequest;
import com.bkav.lk.service.notification.firebase.FCMService;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.vm.AppointmentDateConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DoctorAppointmentConfigurationResource {

    private final DoctorAppointmentConfigurationService service;
    private final DoctorScheduleService doctorScheduleService;
    private final DoctorAppointmentService doctorAppointmentService;
    private final DoctorScheduleTimeService doctorScheduleTimeService;
    private final DoctorService doctorService;

    public DoctorAppointmentConfigurationResource(
            DoctorAppointmentConfigurationService service,
            DoctorScheduleService doctorScheduleService, DoctorAppointmentService doctorAppointmentService, DoctorScheduleTimeService doctorScheduleTimeService, DoctorService doctorService) {
        this.service = service;
        this.doctorScheduleService = doctorScheduleService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorScheduleTimeService = doctorScheduleTimeService;
        this.doctorService = doctorService;
    }

    @GetMapping("/config/doctor-appointments/{healthFacilitiesId}")
    public ResponseEntity<DoctorAppointmentConfigurationDTO> findOneByHealthFacilitiesId(@PathVariable Long healthFacilitiesId) {
        DoctorAppointmentConfigurationDTO dto = service.findOne(healthFacilitiesId, Constants.ENTITY_STATUS.ACTIVE);
        if (dto == null) {
            dto = service.getDefaultConfig();
        }
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/public/config/doctor-appointments/{healthFacilitiesId}")
    public ResponseEntity<DoctorAppointmentConfigurationDTO> publicFindOneByHealthFacilitiesId(@PathVariable Long healthFacilitiesId) {
        DoctorAppointmentConfigurationDTO dto = service.findOne(healthFacilitiesId, Constants.ENTITY_STATUS.ACTIVE);
        if (dto == null) {
            dto = service.getDefaultConfig();
        }
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping("/config/doctor-appointments")
    public ResponseEntity<DoctorAppointmentConfigurationDTO> update(@RequestBody DoctorAppointmentConfigurationDTO doctorAppointmentConfigurationDTO) {
        DoctorAppointmentConfigurationDTO dto = null;
        boolean isOriginal = false;

        boolean isMorningDateRangeValid = Utils.validTimeRange(
                doctorAppointmentConfigurationDTO.getStartTimeMorning(), doctorAppointmentConfigurationDTO.getEndTimeMorning());

        boolean isAfternoonDateRangeValid = Utils.validTimeRange(
                doctorAppointmentConfigurationDTO.getStartTimeAfternoon(), doctorAppointmentConfigurationDTO.getEndTimeAfternoon());

        if (!isMorningDateRangeValid || !isAfternoonDateRangeValid) {
            throw new BadRequestAlertException("Range time config is not correct!", "", "config.time_invalid");
        }

        DoctorAppointmentConfigurationDTO currentActiveAppointmentConfig = service.findOne(
                doctorAppointmentConfigurationDTO.getHealthFacilitiesId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);

        DoctorAppointmentConfigurationDTO pendingAppointmentConfig = service.findOne(
                doctorAppointmentConfigurationDTO.getHealthFacilitiesId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);

        // status = {1, 2} => ch??? duy???t - ???? duy???t
        Integer[] status = { Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE, com.bkav.lk.util.Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED };

        if (Objects.nonNull(currentActiveAppointmentConfig) && Objects.nonNull(pendingAppointmentConfig)) {
            if (doctorAppointmentConfigurationDTO.getApplyConfigAfterDay().equals(Constants.IMMEDIATELY_DAY_CONFIG_APPLY)) { // n???u == 0, apply c???u h??nh m???i ngay l???p t???c
                // lo???i b??? c???u h??nh hi???n t???i => active c???u h??nh t??? request
                currentActiveAppointmentConfig.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.DELETED);
                pendingAppointmentConfig.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.DELETED);
                service.save(currentActiveAppointmentConfig);
                dto = service.updateBothConfig(pendingAppointmentConfig, doctorAppointmentConfigurationDTO, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE, doctorAppointmentConfigurationDTO.getApplyConfigAfterDay());
                doctorScheduleService.updateViolatedSchedule(currentActiveAppointmentConfig, dto);
                // c???p nh???t l???i c??c b???n ghi ???? ?????t l???ch kh??m trong kho???ng th???i gian t??? hi???n t???i tr??? ??i theo c???u h??nh m???i
                // danh s??ch c??c b???n ghi c???n ???????c c???p nh???t theo c???u h??nh m???i
                Long healthFacilityId = doctorAppointmentConfigurationDTO.getHealthFacilitiesId();

                List<DoctorAppointmentDTO> appointmentDTOList = doctorAppointmentService
                        .findAllByHealthFacilityIdStatusInAndStartTimeIsGreaterThanEqual(healthFacilityId, status, Instant.now());

                DoctorAppointmentConfigurationDTO currentConfig = service.findOne(
                        doctorAppointmentConfigurationDTO.getHealthFacilitiesId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);

                service.updateDoctorAppointmentByConfig(appointmentDTOList, healthFacilityId, currentConfig);

            } else { // c???p nh???t l???i c???u h??nh PENDING
                // remove c???u h??nh pending c??
                pendingAppointmentConfig.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.DELETED);
                service.save(pendingAppointmentConfig);

                // t???o pending m???i
                doctorAppointmentConfigurationDTO.setId(null);
                doctorAppointmentConfigurationDTO.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
                dto = service.save(doctorAppointmentConfigurationDTO);
                doctorScheduleService.updateViolatedSchedule(currentActiveAppointmentConfig, dto);
                Long healthFacilityId = doctorAppointmentConfigurationDTO.getHealthFacilitiesId();
                ZonedDateTime dayByPendingConfig = ZonedDateTime.of(LocalDate.now(DateUtils.getZoneHCM()), LocalTime.MIN, DateUtils.getZoneHCM()).plusDays(dto.getApplyConfigAfterDay());
                List<DoctorAppointmentDTO> appointmentDTOList = doctorAppointmentService
                        .findAllByHealthFacilityIdStatusInAndStartTimeIsGreaterThanEqual(healthFacilityId, status, dayByPendingConfig.toInstant());

                service.updateDoctorAppointmentByConfig(appointmentDTOList, healthFacilityId, dto);
            }
        } else if (Objects.nonNull(currentActiveAppointmentConfig)) {
            // [ C???u h??nh PENDING ch??a t???n t???i ]
            // Ki???m tra c???u h??nh th???i gian ?????t l???ch c?? thay ?????i hay kh??ng so v???i dto t??? request
            isOriginal = Utils.equalOriginal(
                    new AppointmentDateConfig(currentActiveAppointmentConfig),
                    new AppointmentDateConfig(doctorAppointmentConfigurationDTO));
            if (!isOriginal) {
                if (doctorAppointmentConfigurationDTO.getApplyConfigAfterDay().equals(Constants.IMMEDIATELY_DAY_CONFIG_APPLY)) { // n???u == 0, apply c???u h??nh m???i ngay l???p t???c
                    // lo???i b??? c???u h??nh hi???n t???i => active c???u h??nh t??? request
                    currentActiveAppointmentConfig.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.DELETED);
                    dto = service.updateBothConfig(currentActiveAppointmentConfig, doctorAppointmentConfigurationDTO, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE, Constants.IMMEDIATELY_DAY_CONFIG_APPLY);

                    // c???p nh???t l???i c??c b???n ghi ???? ?????t l???ch kh??m trong kho???ng th???i gian t??? hi???n t???i tr??? ??i theo c???u h??nh m???i
                    doctorScheduleService.updateViolatedSchedule(currentActiveAppointmentConfig, dto);

                    // danh s??ch c??c b???n ghi c???n ???????c c???p nh???t theo c???u h??nh m???i
                    Long healthFacilityId = doctorAppointmentConfigurationDTO.getHealthFacilitiesId();
                    List<DoctorAppointmentDTO> appointmentDTOList = doctorAppointmentService
                            .findAllByHealthFacilityIdStatusInAndStartTimeIsGreaterThanEqual(healthFacilityId, status, Instant.now());

                    DoctorAppointmentConfigurationDTO currentConfig = service.findOne(
                            doctorAppointmentConfigurationDTO.getHealthFacilitiesId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);
                    service.updateDoctorAppointmentByConfig(appointmentDTOList, healthFacilityId, currentConfig);
                } else {
                    doctorAppointmentConfigurationDTO.setId(null);
                    doctorAppointmentConfigurationDTO.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
                    dto = service.updateBothConfig(doctorAppointmentConfigurationDTO, currentActiveAppointmentConfig, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE, Constants.IMMEDIATELY_DAY_CONFIG_APPLY);
                    Long healthFacilityId = currentActiveAppointmentConfig.getHealthFacilitiesId();
                    ZonedDateTime dayByPendingConfig = ZonedDateTime.of(LocalDate.now(DateUtils.getZoneHCM()), LocalTime.MIN, DateUtils.getZoneHCM()).plusDays(dto.getApplyConfigAfterDay());

                    List<DoctorAppointmentDTO> appointmentDTOList = doctorAppointmentService
                            .findAllByHealthFacilityIdStatusInAndStartTimeIsGreaterThanEqual(healthFacilityId, status, dayByPendingConfig.toInstant());
                    service.updateDoctorAppointmentByConfig(appointmentDTOList, healthFacilityId, dto);
                }
            } else {
                doctorAppointmentConfigurationDTO.setApplyConfigAfterDay(Constants.IMMEDIATELY_DAY_CONFIG_APPLY);
                dto = service.save(doctorAppointmentConfigurationDTO);
            }
        } else if (Objects.nonNull(pendingAppointmentConfig)) {
            Map<String, Object> values = new HashMap<>();
            Integer dayLeft = pendingAppointmentConfig.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(pendingAppointmentConfig.getApplyConfigAfterDay()).getDayOfYear()
                    - Instant.now().atZone(DateUtils.getZoneHCM()).getDayOfYear();
            values.put("dayLeft", dayLeft);
            throw new BadRequestAlertException("The current health facility already have existed doctor appointment date config pending to be active after " + dayLeft + " days",
                    "", "appointment-pending-exists", new HashMap<>(), values);
        } else {
            doctorAppointmentConfigurationDTO.setId(null);
            boolean existDoctorScheduleByHealthFacilityId = doctorScheduleService.existsByHealthFacilityId(doctorAppointmentConfigurationDTO.getHealthFacilitiesId());
            if (existDoctorScheduleByHealthFacilityId) {
                DoctorAppointmentConfigurationDTO defaultAppointmentConfig = service.getDefaultConfig();
                defaultAppointmentConfig.setId(null);
                defaultAppointmentConfig.setHealthFacilitiesId(doctorAppointmentConfigurationDTO.getHealthFacilitiesId());
                defaultAppointmentConfig.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);
                currentActiveAppointmentConfig = service.save(defaultAppointmentConfig);
                doctorAppointmentConfigurationDTO.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
            }
            dto = service.save(doctorAppointmentConfigurationDTO);
        }

        // C???p nh???t l???ch kh??m b???nh theo c???u h??nh c?? n???m trong kho???ng ???nh h?????ng c???a c???u h??nh m???i
        if (Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING.equals(dto.getStatus())) {
            doctorScheduleService.updateViolatedSchedule(currentActiveAppointmentConfig, dto);
        }

        return ResponseEntity.ok().body(dto);
    }

    @PutMapping("/config/doctor-appointments/notification-time")
    public ResponseEntity<DoctorAppointmentConfigurationDTO> updateNotificationTime(@RequestBody DoctorAppointmentConfigurationDTO doctorAppointmentConfigurationDTO) {
        if (doctorAppointmentConfigurationDTO.getId() == null) {
            throw new BadRequestAlertException("A doctor appointment config cannot update with ID null", "", "idnull");
        }

        // Ki???m tra ???? t???n t???i c???u h??nh ?????t l???ch trong tr???ng th??i ch??? hay ch??a (n???u c?? kh??ng cho ph??p c???p nh???t)
        DoctorAppointmentConfigurationDTO pendingAppointmentConfig = service.findOne(
                doctorAppointmentConfigurationDTO.getHealthFacilitiesId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
        if (Objects.nonNull(pendingAppointmentConfig)) {
            pendingAppointmentConfig.setNotiApproveAuto(doctorAppointmentConfigurationDTO.getNotiApproveAuto());
            pendingAppointmentConfig.setPeriodConfig(doctorAppointmentConfigurationDTO.getPeriodConfig());
            pendingAppointmentConfig.setTimeConfig(doctorAppointmentConfigurationDTO.getTimeConfig());
            pendingAppointmentConfig.setDayConfig(doctorAppointmentConfigurationDTO.getDayConfig());
            pendingAppointmentConfig.setTimeConfigSubclinicalResults(doctorAppointmentConfigurationDTO.getTimeConfigSubclinicalResults());
            service.save(pendingAppointmentConfig);
        }
        DoctorAppointmentConfigurationDTO dto = service.save(doctorAppointmentConfigurationDTO);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/config/doctor-appointments/prepayment/{healthFacilitiesId}")
    public ResponseEntity<Boolean> checkPrepayment(@PathVariable Long healthFacilitiesId) {
        DoctorAppointmentConfigurationDTO doctorAppointmentConfigurationDTO = service.findOneByHealthFacilitiesId(healthFacilitiesId);
        Boolean result = true;
        if (doctorAppointmentConfigurationDTO.getPrepaymentMedicalService() == 2) {
            result = false;
        }
        return ResponseEntity.ok().body(result);
    }
}
