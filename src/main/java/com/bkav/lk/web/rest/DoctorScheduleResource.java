package com.bkav.lk.web.rest;

import com.bkav.lk.domain.AbstractAuditingEntity;
import com.bkav.lk.domain.Config;
import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorScheduleMapper;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class DoctorScheduleResource {

    private final Logger log = LoggerFactory.getLogger(DoctorScheduleResource.class);

    private static final String ENTITY_NAME = "Doctor Schedule";

    private final DoctorScheduleService service;

    private final DoctorService doctorService;

    private final DoctorScheduleTimeService doctorScheduleTimeService;

    private final DoctorAppointmentService doctorAppointmentService;

    private final HealthFacilitiesService healthFacilitiesService;

    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    private final StorageService storageService;

    private final UserService userService;

    private final ConfigService configService;

    private final ActivityLogService activityLogService;

    private final DoctorScheduleMapper mapper;

    private final TransactionService transactionService;

    public DoctorScheduleResource(
            DoctorScheduleService service,
            DoctorService doctorService,
            DoctorScheduleTimeService doctorScheduleTimeService,
            DoctorAppointmentService doctorAppointmentService, HealthFacilitiesService healthFacilitiesService,
            DoctorAppointmentConfigurationService doctorAppointmentConfigurationService,
            StorageService storageService,
            UserService userService, ConfigService configService, ActivityLogService activityLogService,
            DoctorScheduleMapper mapper, TransactionService transactionService) {
        this.service = service;
        this.doctorService = doctorService;
        this.doctorScheduleTimeService = doctorScheduleTimeService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
        this.storageService = storageService;
        this.userService = userService;
        this.configService = configService;
        this.activityLogService = activityLogService;
        this.mapper = mapper;
        this.transactionService = transactionService;
    }

    @PostMapping("/doctor-schedules")
    public ResponseEntity<DoctorScheduleDTO> create(@Valid @RequestBody DoctorScheduleDTO doctorScheduleDTO) throws URISyntaxException {
        log.debug("REST request to save Doctor Schedule : {}", doctorScheduleDTO);
        if (doctorScheduleDTO.getId() != null) {
            throw new BadRequestAlertException("A new Doctor Schedule cannot already have an ID", ENTITY_NAME, "idexists");
        }

        doctorScheduleDTO = service.save(doctorScheduleDTO);
        activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_SCHEDULE, mapper.toEntity(doctorScheduleDTO));
        return ResponseEntity.created(new URI("/api/doctor-schedule/" + doctorScheduleDTO.getId())).body(doctorScheduleDTO);
    }

    @PostMapping("/doctor-schedules/creates")
    public ResponseEntity<List<DoctorScheduleDTO>> creates(@RequestHeader(name = "healthFacilityId") Long healthFacilityId,
                                                           @Valid @RequestBody List<DoctorScheduleDTO> doctorScheduleDTOS) throws URISyntaxException {
        List<DoctorScheduleDTO> list = new ArrayList<>();

        // checking date valid with workingTime by config
        Map<String, Object> map = service.timeOfDayValid(healthFacilityId);
        boolean hasPendingConfig = service.existsPendingConfigWithHealthFacility(healthFacilityId);
        if (!hasPendingConfig) {
            List<Integer> dayOfWeekMorningValid = (List<Integer>) map.get("morning");
            List<Integer> dayOfWeekAfternoonValid = (List<Integer>) map.get("afternoon");

            doctorScheduleDTOS.forEach(item -> {
                DayOfWeek dayOfWeek = service.getDayOfWeekByDateString(item.getWorkingDateFormat());
                Integer timeOfDayValidByConfig = service.getWorkingTimeByDateByConfig(dayOfWeek, dayOfWeekMorningValid, dayOfWeekAfternoonValid);
                // TODO: bad request workingTime = ERROR_WORKING (-1) => invalid
                if (timeOfDayValidByConfig.equals(Constants.DOCTOR_SCHEDULE_STATUS.ERROR_WORKING)) {
                    throw new BadRequestAlertException("Lịch làm việc ngoài vùng đăng ký", ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                }
                // TODO: ignore case workingTime = FULL_TIME (3) => valid
                if (!timeOfDayValidByConfig.equals(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING)) {
                    if (!item.getWorkingTime().equals(timeOfDayValidByConfig)) {
                        throw new BadRequestAlertException("Lịch làm việc ngoài vùng đăng ký", ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                    }
                }
            });
        } else {
            ZonedDateTime lastActiveDate = (ZonedDateTime) map.get("lastActiveDate");
            List<Integer> dayOfWeekMorningActiveValid = (List<Integer>) map.get("morningActive");
            List<Integer> dayOfWeekAfternoonActiveValid = (List<Integer>) map.get("afternoonActive");
            List<Integer> dayOfWeekMorningPendingValid = (List<Integer>) map.get("morningPending");
            List<Integer> dayOfWeekAfternoonPendingValid = (List<Integer>) map.get("afternoonPending");

            doctorScheduleDTOS.forEach(item -> {
                ZonedDateTime selectedDate = ZonedDateTime.of(LocalDate.parse(item.getWorkingDateFormat()), LocalTime.MIN, DateUtils.getZoneHCM());
                DayOfWeek dayOfWeek = service.getDayOfWeekByDateString(item.getWorkingDateFormat());

                Integer dayLeft = lastActiveDate.getDayOfYear() - selectedDate.getDayOfYear();
                Integer timeOfDayValidByConfig = null;
                if (dayLeft > 0) {
                    timeOfDayValidByConfig = service.getWorkingTimeByDateByConfig(dayOfWeek, dayOfWeekMorningActiveValid, dayOfWeekAfternoonActiveValid);
                } else {
                    timeOfDayValidByConfig = service.getWorkingTimeByDateByConfig(dayOfWeek, dayOfWeekMorningPendingValid, dayOfWeekAfternoonPendingValid);
                }
                // TODO: bad request workingTime = ERROR_WORKING (-1) => invalid
                if (timeOfDayValidByConfig.equals(Constants.DOCTOR_SCHEDULE_STATUS.ERROR_WORKING)) {
                    throw new BadRequestAlertException("Lịch làm việc ngoài vùng đăng ký", ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                }
                // TODO: ignore case workingTime = FULL_TIME (3) => valid
                if (!timeOfDayValidByConfig.equals(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING)) {
                    if (!item.getWorkingTime().equals(timeOfDayValidByConfig)) {
                        throw new BadRequestAlertException("Lịch làm việc ngoài vùng đăng ký", ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                    }
                }
            });
        }


        doctorScheduleDTOS.forEach(item -> {
            DoctorScheduleDTO dto = new DoctorScheduleDTO();
            dto = service.save(item);
            activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_SCHEDULE, mapper.toEntity(dto));
            list.add(dto);
        });
        return ResponseEntity.ok().body(list);
    }

    @PutMapping("/doctor-schedules")
    public ResponseEntity<DoctorScheduleDTO> update(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestBody DoctorScheduleDTO doctorScheduleDTO) throws URISyntaxException {
        log.debug("REST request to save Doctor Schedule : {}", doctorScheduleDTO);
        if (doctorScheduleDTO.getId() == null) {
            throw new BadRequestAlertException("A Doctor Schedule cannot save has not ID", ENTITY_NAME, "idnull");
        }
        if (!service.isDoctorScheduleExist(doctorScheduleDTO)) {
            DoctorSchedule oldDoctorSchedule = mapper.toEntity(service.findOne(doctorScheduleDTO.getId()).orElse(null));
            Instant startTime = ZonedDateTime.of(oldDoctorSchedule.getWorkingDate().atZone(DateUtils.getZoneHCM()).toLocalDate(),
                    LocalTime.MIN, DateUtils.getZoneHCM()).toInstant();
            Instant endTime = ZonedDateTime.of(oldDoctorSchedule.getWorkingDate().atZone(DateUtils.getZoneHCM()).toLocalDate(),
                    LocalTime.MAX, DateUtils.getZoneHCM()).toInstant();
            Integer count = doctorAppointmentService.countHealthFacilityAndDoctorId(healthFacilityId, oldDoctorSchedule.getDoctor().getId(), startTime, endTime);
            if (count > 0) {
                throw new BadRequestAlertException("Bạn không sửa được thời gian khám bệnh do đã phát sinh lịch đặt khám", ENTITY_NAME, "doctor-schedule.cannot-edit-exist-appointment");
            }
            doctorScheduleDTO = service.save(doctorScheduleDTO);
            DoctorSchedule newDoctorSchedule = mapper.toEntity(doctorScheduleDTO);
            activityLogService.update(Constants.CONTENT_TYPE.DOCTOR_SCHEDULE, oldDoctorSchedule, newDoctorSchedule);
            return ResponseEntity.created(new URI("/api/doctor-schedule/" + doctorScheduleDTO.getId())).body(doctorScheduleDTO);
        } else {
            throw new BadRequestAlertException("A new Doctor Schedule cannot already exist", ENTITY_NAME, "idexists");
        }
    }

    @GetMapping("/doctor-schedules/{id}")
    public ResponseEntity<DoctorScheduleDTO> findOne(@PathVariable Long id) {
        log.debug("REST request to get Doctor Schedule : {}", id);
        Optional<DoctorScheduleDTO> dto = service.findOne(id);
        if (!dto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(dto);
    }

    @GetMapping("/doctor-schedules/doctor/{id}")
    public ResponseEntity<List<DoctorScheduleDTO>> findSchedulesOfDoctor(@PathVariable Long id) {
        log.debug("REST request to get all Schedule of a Doctor : {}", id);
        List<DoctorScheduleDTO> dto = service.findSchedulesOfDoctor(id);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/doctor-schedules/doctor/{doctorId}/calendar")
    public ResponseEntity<Object> findSchedulesOfDoctorValidInMonth(@PathVariable Long doctorId, @RequestParam(defaultValue = "false") boolean isExistSchedule) {
        if (doctorId == null) {
            throw new BadRequestAlertException("DoctorId is empty", "DoctorSchedule", "doctorId_empty");
        }

        DoctorDTO doctorDTO = doctorService.findByIdAndStatus(doctorId, Constants.ENTITY_STATUS.ACTIVE);
        if (doctorDTO == null) {
            throw new BadRequestAlertException("Doctor not exist", "DoctorSchedule", "doctor_not_exist");
        }

        Long healthFacilityId = doctorDTO.getHealthFacilityId();
        DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOneByHealthFacilitiesId(healthFacilityId);
        if (config == null) {
            throw new BadRequestAlertException("Health facilities not exist", "DoctorSchedule", "health_facilities_not_exist");
        }
        Config configOther = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.ALLOW_BOOKING_BEFORE_DAY);
        ZonedDateTime now = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime nowPlus30Days = now.plusDays(Long.parseLong(configOther.getPropertyValue()) + 1);
        long daysBetween = ChronoUnit.DAYS.between(now, nowPlus30Days);

        List<DoctorScheduleDTO> schedulesOfDoctorValid = service.findSchedulesOfDoctorValid(doctorId, now.toInstant(), nowPlus30Days.toInstant());
        if (!schedulesOfDoctorValid.isEmpty()) {
            List<Instant> workingDates = schedulesOfDoctorValid.stream().map(DoctorScheduleDTO::getWorkingDate).collect(toList());
            List<DoctorScheduleVM> doctorScheduleVMS = new ArrayList<>();
            boolean isExistScheduleIn30Days = false;
            for (int i = 1; i < daysBetween; i++) {
                Instant workingDate = now.plusDays(i).toInstant();
                DoctorScheduleVM scheduleVM = new DoctorScheduleVM();
                if (workingDates.contains(workingDate)) {
                    scheduleVM.setAvailable(true);
                    isExistScheduleIn30Days = true;
                } else {
                    scheduleVM.setAvailable(false);
                }
                scheduleVM.setWorkingDate(workingDate);
                scheduleVM.setDate(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate().toString());
                doctorScheduleVMS.add(scheduleVM);
            }
            doctorScheduleVMS.stream().sorted(Comparator.comparing(DoctorScheduleVM::getWorkingDate)).collect(Collectors.toList());
            if (isExistSchedule) {
                return ResponseEntity.ok(isExistScheduleIn30Days);
            }
            return ResponseEntity.ok(doctorScheduleVMS);
        } else {
            throw new BadRequestAlertException("Working date of doctor is empty", "DoctorSchedule", "working_date_empty");
        }
    }

    @GetMapping("/doctor-schedules/doctor/calendar-period-valid")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<List<DoctorScheduleTimeVM>> findSchedulesOfDoctorAvailable(@RequestParam(name = "timeOption", required = false, defaultValue = "false") Boolean timeOption,
                                                                                     @RequestParam(name = "isMorning", required = false, defaultValue = "true") Boolean isMorning,
                                                                                     @RequestParam MultiValueMap<String, String> queryParams) {
        List<DoctorScheduleTimeVM> timeList = new ArrayList<>();
        if ((queryParams.containsKey("doctorId") && !StrUtil.isBlank(queryParams.get("doctorId").get(0)))
                && (queryParams.containsKey("date") && !StrUtil.isBlank(queryParams.get("date").get(0)))) {
            Long doctorId = Long.valueOf(queryParams.get("doctorId").get(0));
            String day = queryParams.get("date").get(0);
            DoctorDTO doctorDTO = doctorService.findByIdAndStatus(doctorId, Constants.ENTITY_STATUS.ACTIVE);
            if (doctorDTO != null) {
                Optional<HealthFacilitiesDTO> healthFacilities = healthFacilitiesService.findOne(doctorDTO.getHealthFacilityId());
                if (healthFacilities.isPresent()) {
                    DoctorAppointmentConfigurationDTO dto = doctorAppointmentConfigurationService.findOne(healthFacilities.get().getId(), Constants.ENTITY_STATUS.ACTIVE);
                    boolean isRandomTimeOption = timeOption;
                    if (dto != null) {
                        DoctorAppointmentConfigurationDTO pendingDto = doctorAppointmentConfigurationService.findOne(healthFacilities.get().getId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
                        if (Objects.nonNull(pendingDto)) {
                            // cung cấp cấu hình lịch khám mới hay cũ dựa theo ngày đặt lịch đã chọn

                            Integer dayLeft = pendingDto.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(dto.getApplyConfigAfterDay()).getDayOfYear()
                                    - ZonedDateTime.of(LocalDate.parse(day), LocalTime.MIN, DateUtils.getZoneHCM()).getDayOfYear();
                            if (dayLeft > 0) {
                                timeList = doctorScheduleTimeService.findSchedulesOfDoctorAvailable(dto.getHealthFacilitiesId(), dto, day, doctorId, isRandomTimeOption, isMorning);
                            } else {
                                timeList = doctorScheduleTimeService.findSchedulesOfDoctorAvailable(dto.getHealthFacilitiesId(), pendingDto, day, doctorId, isRandomTimeOption, isMorning);
                            }
                        } else {
                            timeList = doctorScheduleTimeService.findSchedulesOfDoctorAvailable(dto.getHealthFacilitiesId(), dto, day, doctorId, isRandomTimeOption, isMorning);
                        }
                    } else {
                        DoctorAppointmentConfigurationDTO configDefault = doctorAppointmentConfigurationService.getDefaultConfig();
                        timeList = doctorScheduleTimeService.findSchedulesOfDoctorAvailable(configDefault.getHealthFacilitiesId(), configDefault, day, doctorId, isRandomTimeOption, isMorning);
                    }
                    this.handleDoctorAppointmentTempAndTransactionTempInvalid(timeList, dto.getHealthFacilitiesId(), doctorDTO.getId());
                    return ResponseEntity.ok().body(timeList);
                }
                throw new BadRequestAlertException("Health facilities not exist", "DoctorScheduleTime", "health_facilities_not_exist");
            }
            throw new BadRequestAlertException("Doctor is not exist", "DoctorScheduleTime", "doctor_not_exist");
        }
        throw new BadRequestAlertException("query params is invalid !", "DoctorScheduleTime", "query_params_invalid");
    }

    @GetMapping("/doctor-schedules/hospital/{healthFacilityId}")
    public ResponseEntity<List<DoctorScheduleVM>> findSchedulesOfHospitalValidInAMonth(@PathVariable Long healthFacilityId) {
        if (healthFacilityId != null) {
            DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOne(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
            if (config == null) {
                config = doctorAppointmentConfigurationService.getDefaultConfig();
            }
            Config configOther = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.ALLOW_BOOKING_BEFORE_DAY);
            ZonedDateTime now = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime nowPlus30Days = now.plusDays(Long.parseLong(configOther.getPropertyValue()));
            long daysBetween = ChronoUnit.DAYS.between(now, nowPlus30Days);
            List<Instant> workingDates = service.findAllAvailableInHospital(healthFacilityId, now.plusDays(1).toInstant(), nowPlus30Days.toInstant());
            List<DoctorScheduleVM> doctorScheduleVMS = new ArrayList<>();
            for (int i = 1; i <= daysBetween; i++) {
                Instant workingDate = now.plusDays(i).toInstant();
                DoctorScheduleVM scheduleVM = new DoctorScheduleVM();
                if (workingDates.contains(workingDate)) {
                    scheduleVM.setAvailable(true);
                } else {
                    scheduleVM.setAvailable(false);
                }
                scheduleVM.setWorkingDate(workingDate);
                scheduleVM.setDate(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate().toString());
                doctorScheduleVMS.add(scheduleVM);
            }
            return ResponseEntity.ok(doctorScheduleVMS);
        }
        throw new BadRequestAlertException("Health facilities invalid !", "DoctorSchedule", "health_facilities_invalid");
    }

    @GetMapping("/doctor-schedules/hospital/calendar-period-valid")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<List<DoctorScheduleTimeVM>> findSchedulesOfHospitalAvailable(@RequestParam(name = "timeOption", required = false, defaultValue = "false") Boolean timeOption,
                                                                                       @RequestParam(name = "isMorning", required = false, defaultValue = "true") Boolean isMorning,
                                                                                       @RequestParam MultiValueMap<String, String> queryParams) {
        if ((queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0)))
                && (queryParams.containsKey("date") && !StrUtil.isBlank(queryParams.get("date").get(0)))) {
            Long healthFacilityId = Long.valueOf(queryParams.get("healthFacilityId").get(0));
            String day = queryParams.get("date").get(0);
            DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOne(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
            Instant workingDate = ZonedDateTime.of(LocalDate.parse(day), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            List<DoctorDTO> doctorDTOList = doctorService.findAllDoctorByHealthFacilityId(healthFacilityId);
            if (doctorDTOList.isEmpty()) {
                throw new BadRequestAlertException("No doctor's work schedule", "DoctorScheduleTime", "no_doctor_work_schedule");
            }
            List<Long> doctorIds = doctorDTOList.stream().map(DoctorDTO::getId).collect(Collectors.toList());
            List<DoctorScheduleDTO> doctorScheduleDTOS = service.findAllByWorkingDateAndStatus(doctorIds, workingDate, Constants.ENTITY_STATUS.ACTIVE);

            List<DoctorDTO> doctorDTOS = new ArrayList<>();
            doctorScheduleDTOS.forEach(doctorScheduleDTO -> {
                doctorDTOList.forEach(doctorDTO -> {
                    if (doctorDTO.getId().equals(doctorScheduleDTO.getDoctorId())) {
                        doctorDTO.setWorkingTime(doctorScheduleDTO.getWorkingTime());
                        doctorDTOS.add(doctorDTO);
                    }
                });
            });
            if (doctorDTOS.isEmpty()) {
                throw new BadRequestAlertException("There is no doctor's schedule according to the date selected", "DoctorScheduleTime", "no_doctor_work_schedule_selected_day");
            }
            List<DoctorScheduleTimeVM> timeList = new ArrayList<>();
            boolean isRandomTimeOption = timeOption;
            if (config != null) {
                DoctorAppointmentConfigurationDTO pendingConfig = doctorAppointmentConfigurationService.findOne(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
                if (Objects.nonNull(pendingConfig)) {
                    // cung cấp cấu hình lịch khám mới hay cũ dựa theo ngày đặt lịch đã chọn
                    Integer dayLeft = pendingConfig.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(pendingConfig.getApplyConfigAfterDay()).getDayOfYear()
                            - ZonedDateTime.of(LocalDate.parse(day), LocalTime.MIN, DateUtils.getZoneHCM()).getDayOfYear();
                    if (dayLeft > 0) {
                        timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(config.getHealthFacilitiesId(), config, day, doctorDTOS, isRandomTimeOption, isMorning);
                    } else {
                        timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(config.getHealthFacilitiesId(), pendingConfig, day, doctorDTOS, isRandomTimeOption, isMorning);
                    }
                } else {
                    timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(healthFacilityId, config, day, doctorDTOS, isRandomTimeOption, isMorning);
                }
            } else {
                DoctorAppointmentConfigurationDTO configDefault = doctorAppointmentConfigurationService.getDefaultConfig();
                timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(healthFacilityId, configDefault, day, doctorDTOS, isRandomTimeOption, isMorning);
            }
            this.handleDoctorAppointmentTempAndTransactionTempInvalid(timeList, healthFacilityId, null);
            return ResponseEntity.ok().body(timeList);
//            throw new BadRequestAlertException("Health facilities not exist", "DoctorScheduleTime", "health_facilities_not_exist");
        }
        throw new BadRequestAlertException("query params is invalid !", "DoctorScheduleTime", "query_params_invalid");
    }

    @GetMapping("/public/doctor-schedules/hospital/{healthFacilityId}")
    public ResponseEntity<List<DoctorScheduleVM>> findSchedulesOfHospitalValidInAMonthPublic(@PathVariable Long healthFacilityId) {
        if (healthFacilityId != null) {
            DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOne(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
            if (config == null) {
                config = doctorAppointmentConfigurationService.getDefaultConfig();
            }
            Config configOther = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.ALLOW_BOOKING_BEFORE_DAY);
            ZonedDateTime now = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime nowPlus30Days = now.plusDays(Long.parseLong(configOther.getPropertyValue()));
            long daysBetween = ChronoUnit.DAYS.between(now, nowPlus30Days);
            List<Instant> workingDates = service.findAllAvailableInHospital(healthFacilityId, now.plusDays(1).toInstant(), nowPlus30Days.toInstant());
            List<DoctorScheduleVM> doctorScheduleVMS = new ArrayList<>();
            for (int i = 1; i <= daysBetween; i++) {
                Instant workingDate = now.plusDays(i).toInstant();
                DoctorScheduleVM scheduleVM = new DoctorScheduleVM();
                if (workingDates.contains(workingDate)) {
                    scheduleVM.setAvailable(true);
                } else {
                    scheduleVM.setAvailable(false);
                }
                scheduleVM.setWorkingDate(workingDate);
                scheduleVM.setDate(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate().toString());
                doctorScheduleVMS.add(scheduleVM);
            }
            return ResponseEntity.ok(doctorScheduleVMS);
        }
        throw new BadRequestAlertException("Health facilities invalid !", "DoctorSchedule", "health_facilities_invalid");
    }

    @GetMapping("/public/doctor-schedules/hospital/calendar-period-valid")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<List<DoctorScheduleTimeVM>> findSchedulesOfHospitalAvailablePublic(@RequestParam(name = "timeOption", required = false, defaultValue = "false") Boolean timeOption,
                                                                                             @RequestParam(name = "isMorning", required = false, defaultValue = "true") Boolean isMorning,
                                                                                             @RequestParam MultiValueMap<String, String> queryParams) {
        if ((queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0)))
                && (queryParams.containsKey("date") && !StrUtil.isBlank(queryParams.get("date").get(0)))) {
            Long healthFacilityId = Long.valueOf(queryParams.get("healthFacilityId").get(0));
            String day = queryParams.get("date").get(0);
            DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOne(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
            Instant workingDate = ZonedDateTime.of(LocalDate.parse(day), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            List<DoctorDTO> doctorDTOList = doctorService.findAllDoctorByHealthFacilityId(healthFacilityId);
            if (doctorDTOList.isEmpty()) {
                throw new BadRequestAlertException("No doctor's work schedule", "DoctorScheduleTime", "no_doctor_work_schedule");
            }
            List<Long> doctorIds = doctorDTOList.stream().map(DoctorDTO::getId).collect(Collectors.toList());
            List<DoctorScheduleDTO> doctorScheduleDTOS = service.findAllByWorkingDateAndStatus(doctorIds, workingDate, Constants.ENTITY_STATUS.ACTIVE);

            List<DoctorDTO> doctorDTOS = new ArrayList<>();
            doctorScheduleDTOS.forEach(doctorScheduleDTO -> {
                doctorDTOList.forEach(doctorDTO -> {
                    if (doctorDTO.getId().equals(doctorScheduleDTO.getDoctorId())) {
                        doctorDTO.setWorkingTime(doctorScheduleDTO.getWorkingTime());
                        doctorDTOS.add(doctorDTO);
                    }
                });
            });
            if (doctorDTOS.isEmpty()) {
                throw new BadRequestAlertException("There is no doctor's schedule according to the date selected", "DoctorScheduleTime", "no_doctor_work_schedule_selected_day");
            }
            List<DoctorScheduleTimeVM> timeList = new ArrayList<>();
            boolean isRandomTimeOption = timeOption;
            if (config != null) {
                DoctorAppointmentConfigurationDTO pendingConfig = doctorAppointmentConfigurationService.findOne(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
                if (Objects.nonNull(pendingConfig)) {
                    // cung cấp cấu hình lịch khám mới hay cũ dựa theo ngày đặt lịch đã chọn
                    Integer dayLeft = pendingConfig.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(pendingConfig.getApplyConfigAfterDay()).getDayOfYear()
                            - ZonedDateTime.of(LocalDate.parse(day), LocalTime.MIN, DateUtils.getZoneHCM()).getDayOfYear();
                    if (dayLeft > 0) {
                        timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(config.getHealthFacilitiesId(), config, day, doctorDTOS, isRandomTimeOption, isMorning);
                    } else {
                        timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(config.getHealthFacilitiesId(), pendingConfig, day, doctorDTOS, isRandomTimeOption, isMorning);
                    }
                } else {
                    timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(healthFacilityId, config, day, doctorDTOS, isRandomTimeOption, isMorning);
                }
            } else {
                DoctorAppointmentConfigurationDTO configDefault = doctorAppointmentConfigurationService.getDefaultConfig();
                timeList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(healthFacilityId, configDefault, day, doctorDTOS, isRandomTimeOption, isMorning);
            }
            this.handleDoctorAppointmentTempAndTransactionTempInvalid(timeList, healthFacilityId, null);
            return ResponseEntity.ok().body(timeList);
//            throw new BadRequestAlertException("Health facilities not exist", "DoctorScheduleTime", "health_facilities_not_exist");
        }
        throw new BadRequestAlertException("query params is invalid !", "DoctorScheduleTime", "query_params_invalid");
    }

    @GetMapping("/doctor-schedules")
    public ResponseEntity<List<DoctorScheduleDTO>> search(@RequestHeader(name = "healthFacilityId") Long healthFacilityId,
                                                          @RequestParam MultiValueMap<String, String> queryParams,
                                                          Pageable pageable) {
        log.debug("REST request to search for a page of Doctor Schedule for query {}", queryParams);
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.noContent().build();
        }
        Page<DoctorScheduleDTO> page = service.search(queryParams, pageable, user);
//        Map<String, List<DoctorScheduleDTO>> scheduleVMList = service.handleTree(page.getContent());
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @DeleteMapping("/doctor-schedules/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete Doctor Schedule : {}", id);
        service.delete(id);
        DoctorSchedule doctorSchedule = mapper.toEntity(service.findOne(id).orElse(null));
        activityLogService.delete(Constants.CONTENT_TYPE.DOCTOR_SCHEDULE, doctorSchedule);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert("", true, ENTITY_NAME, id.toString()))
                .build();
    }

    @PostMapping("/doctor-schedules/save-all")
    public ResponseEntity<List<DoctorScheduleDTO>> saveAll(
            @RequestHeader(name = "healthFacilityId") Long healthFacilityId,
            @RequestBody List<DoctorScheduleDTO> doctorSchedules) {
        log.debug("REST request to save Doctor Schedules : {}", doctorSchedules);
        List<DoctorScheduleDTO> result = null;
        if (!service.isDoctorSchedulesExist(doctorSchedules)) {
            doctorSchedules.forEach( item -> {
                DoctorScheduleDTO oldDoctorScheduleDTO = service.findByDoctorIdAndWorkingDateAndStatus(item.getDoctorId(), item.getWorkingDate(), Constants.ENTITY_STATUS.ACTIVE);
                if (Objects.nonNull(oldDoctorScheduleDTO)) {
                    if (!oldDoctorScheduleDTO.getWorkingTime().equals(item.getWorkingTime())) {
                        item.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING);
                    }
                    item.setId(oldDoctorScheduleDTO.getId());
                }
            });
            result = service.saveAll(healthFacilityId, doctorSchedules);
            activityLogService.multipleCreate(Constants.CONTENT_TYPE.DOCTOR_SCHEDULE,
                    result.stream().map(o -> (AbstractAuditingEntity) mapper.toEntity(o)).collect(toList()));
            return ResponseEntity.ok().body(result);
        } else {
            throw new BadRequestAlertException("A new Doctor Schedule cannot already exist", ENTITY_NAME, "doctor-schedule.schedules-exist-list");
        }
    }

    @PostMapping("/doctor-schedules/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadDoctorSchedule(
            @RequestHeader("healthFacilityId") Long healthFacilityId,
            @RequestParam("file") MultipartFile file) {
        log.debug("REST request upload doctor schedule");
        if (healthFacilityId == null) {
            throw new BadRequestAlertException("You have not selected HealthFacility", ENTITY_NAME, "have_not_selected_healthFacility");
        }

        List<DoctorScheduleDTO> doctorScheduleDTOS = new ArrayList<>();
        try {
            doctorScheduleDTOS = service.excelToSchedules(file.getInputStream());
            if (doctorScheduleDTOS.isEmpty()) {
                throw new BadRequestAlertException("File is empty", ENTITY_NAME, "excel.emptyFile");
            }
            ResultExcel resultExcel = service.bulkUploadSchedules(healthFacilityId, doctorScheduleDTOS);
            return ResponseEntity.ok().body(resultExcel);

        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/doctor-schedules/ids")
    public ResponseEntity<Void> deletes(@RequestBody List<Long> ids) {
        log.debug("REST request to delete ids Doctor Schedule : {}", "ids");
        ids.forEach(service::delete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/doctor-schedules/export-excel")
    public ResponseEntity<InputStreamResource> exportExcel(@RequestParam(name = "ids") List<Long> ids) throws IOException {
        List<DoctorScheduleDTO> list = new ArrayList<>();
        if (ids.size() == 0) {
            list = service.findAll();
        } else {
            list = service.findByIds(ids);
        }
        InputStream file = storageService.downloadExcelTemplateFromResource("doctor_schedule.xlsx");
        ByteArrayInputStream in = service.exportDoctorScheduleToExcel(list, file);
        InputStream inputStream = new BufferedInputStream(in);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-disposition", "attachment;filename=" + "doctor_schedule.xlsx");
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        return ResponseEntity.ok().headers(headers).body(inputStreamResource);
    }

    @GetMapping("/public/doctor-schedules/export-excel/search")
    public ResponseEntity<String> exportExcelSearch(@RequestHeader(name = "healthFacilityId") Long healthFacilityId, @RequestParam MultiValueMap<String, String> queryParams,
                                                                 Pageable pageable) throws IOException {
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.noContent().build();
        }
        List<DoctorScheduleDTO> list = service.search(queryParams, pageable, user).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/doctor_schedule.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/doctor_schedule_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }

    @GetMapping("/public/doctor-schedules/download")
    public void exportExcel(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/doctor_schedule_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=doctor_schedule_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/public/doctor-schedules/download/template-excel")
    public void downloadTemplateExcelDoctorSchedule(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=" + "doctor-schedule.xlsx");
        IOUtils.copy(storageService.downloadExcelTemplateFromResource("doctor_schedule_import.xlsx"), response.getOutputStream());
//        IOUtils.copy(service.downloadTemplateExcelDoctorSchedule(), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/doctor-schedules/day-off")
    public ResponseEntity<List<String>> findAllDayOffInRange(@RequestHeader(name = "healthFacilityId") Long healthFacilityId,
                                                             @RequestParam MultiValueMap<String, String> queryParams) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime startDateTime = ZonedDateTime.of(LocalDateTime.parse(queryParams.get("startTime").get(0), formatter), ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime endDateTime = ZonedDateTime.of(LocalDateTime.parse(queryParams.get("endTime").get(0), formatter), ZoneId.of("Asia/Ho_Chi_Minh"));

        List<String> dayOff = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(startDateTime, endDateTime);

        Map<String, Object> map = service.timeOfDayValid(healthFacilityId);
        boolean hasPendingConfig = service.existsPendingConfigWithHealthFacility(healthFacilityId);
        if (!hasPendingConfig) {
            List<Integer> dayOfWeekMorningValid = (List<Integer>) map.get("morning");
            List<Integer> dayOfWeekAfternoonValid = (List<Integer>) map.get("afternoon");

            for (int i = 0; i < daysBetween; i++) {
                Instant workingDate = startDateTime.plusDays(i).toInstant();
                DayOfWeek dayOfWeek = workingDate.atZone(DateUtils.getZoneHCM()).getDayOfWeek();
                boolean checkingDayOfWeekMorningIsValid = dayOfWeekMorningValid.contains(dayOfWeek.getValue());
                boolean checkingDayOfWeekAfternoonIsValid = dayOfWeekAfternoonValid.contains(dayOfWeek.getValue());
                if (!checkingDayOfWeekMorningIsValid && !checkingDayOfWeekAfternoonIsValid) {
                    dayOff.add(workingDate.atZone(DateUtils.getZoneHCM()).toLocalDate().toString());
                }
            }
        } else {
            ZonedDateTime lastActiveDate = (ZonedDateTime) map.get("lastActiveDate");
            List<Integer> dayOfWeekMorningActiveValid = (List<Integer>) map.get("morningActive");
            List<Integer> dayOfWeekAfternoonActiveValid = (List<Integer>) map.get("afternoonActive");
            List<Integer> dayOfWeekMorningPendingValid = (List<Integer>) map.get("morningPending");
            List<Integer> dayOfWeekAfternoonPendingValid = (List<Integer>) map.get("afternoonPending");

            for (int i = 0; i < daysBetween; i++) {
                Instant workingDate = startDateTime.plusDays(i).toInstant();
                DayOfWeek dayOfWeek = workingDate.atZone(DateUtils.getZoneHCM()).getDayOfWeek();
                boolean checkingDayOfWeekMorningIsValid = false;
                boolean checkingDayOfWeekAfternoonIsValid = false;
                Integer dayLeft = lastActiveDate.getDayOfYear() - startDateTime.plusDays(i).getDayOfYear();
                if (dayLeft > 0) {
                    checkingDayOfWeekMorningIsValid = dayOfWeekMorningActiveValid.contains(dayOfWeek.getValue());
                    checkingDayOfWeekAfternoonIsValid = dayOfWeekAfternoonActiveValid.contains(dayOfWeek.getValue());
                } else {
                    checkingDayOfWeekMorningIsValid = dayOfWeekMorningPendingValid.contains(dayOfWeek.getValue());
                    checkingDayOfWeekAfternoonIsValid = dayOfWeekAfternoonPendingValid.contains(dayOfWeek.getValue());
                }

                if (!checkingDayOfWeekMorningIsValid && !checkingDayOfWeekAfternoonIsValid) {
                    dayOff.add(workingDate.atZone(DateUtils.getZoneHCM()).toLocalDate().toString());
                }
            }
        }

        return ResponseEntity.ok().body(dayOff);
    }

    @GetMapping("/doctor-schedules/working-time-of-day")
    public ResponseEntity<Integer> workingTimeOfDay(@RequestHeader(name = "healthFacilityId") Long healthFacilityId,
                                                    @RequestParam MultiValueMap<String, String> queryParams) {
        String localDate = queryParams.get("date").get(0);
        ZonedDateTime selectedDate = ZonedDateTime.of(LocalDate.parse(localDate), LocalTime.MIN, DateUtils.getZoneHCM());
        DayOfWeek dayOfWeek = service.getDayOfWeekByDateString(localDate);
        Map<String, Object> map = service.timeOfDayValid(healthFacilityId);
        boolean hasPendingConfig = service.existsPendingConfigWithHealthFacility(healthFacilityId);
        Integer workingTime = null;
        if (!hasPendingConfig) {
            List<Integer> dayOfWeekMorningValid = (List<Integer>) map.get("morning");
            List<Integer> dayOfWeekAfternoonValid = (List<Integer>) map.get("afternoon");
            workingTime = service.getWorkingTimeByDateByConfig(dayOfWeek, dayOfWeekMorningValid, dayOfWeekAfternoonValid);
        } else {
            ZonedDateTime lastActiveDate = (ZonedDateTime) map.get("lastActiveDate");
            List<Integer> dayOfWeekMorningActiveValid = (List<Integer>) map.get("morningActive");
            List<Integer> dayOfWeekAfternoonActiveValid = (List<Integer>) map.get("afternoonActive");
            List<Integer> dayOfWeekMorningPendingValid = (List<Integer>) map.get("morningPending");
            List<Integer> dayOfWeekAfternoonPendingValid = (List<Integer>) map.get("afternoonPending");

            Integer dayLeft = lastActiveDate.getDayOfYear() - selectedDate.getDayOfYear();
            if (dayLeft > 0) {
                workingTime = service.getWorkingTimeByDateByConfig(dayOfWeek, dayOfWeekMorningActiveValid, dayOfWeekAfternoonActiveValid);
            } else {
                workingTime = service.getWorkingTimeByDateByConfig(dayOfWeek, dayOfWeekMorningPendingValid, dayOfWeekAfternoonPendingValid);
            }
        }

        return ResponseEntity.ok().body(workingTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleDoctorAppointmentTempAndTransactionTempInvalid(List<DoctorScheduleTimeVM> timeList, Long healthFacilityId, Long doctorId) {
        // Load lai trang thi se xoa bo DoctorAppointment INVALID va Transaction INVALID -> status = -1
        List<DoctorScheduleTimeVM> doctorScheduleTimeVMUnavailable = timeList
                .stream()
                .filter(item -> item.isAvailable() == false)
                .collect(toList());
        for(DoctorScheduleTimeVM doctorScheduleTimeVM : doctorScheduleTimeVMUnavailable) {
            List<DoctorAppointmentDTO> list = null;

            if (doctorId == null && healthFacilityId != null) {
                list = doctorAppointmentService
                        .findTempDoctorAppointmentInvalidBothDoctorAndNotDoctor(Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, healthFacilityId, doctorScheduleTimeVM.getStartTime(), doctorScheduleTimeVM.getEndTime(), Constants.TIME_OUT_VNPAY.TIME_OUT_MINUTE);
            }
            if (doctorId != null && healthFacilityId == null) {
                list = doctorAppointmentService.findTempDoctorAppointmentInvalidWithDoctor(Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, doctorId, doctorScheduleTimeVM.getStartTime(), doctorScheduleTimeVM.getEndTime(), Constants.TIME_OUT_VNPAY.TIME_OUT_MINUTE);
            }
            if (Objects.nonNull(list) && list.size() > 0) {
                // Xoa DoctorAppointment Nhap
                List<Long> ids = list.stream().map(DoctorAppointmentDTO::getId).collect(toList());
                if (!ids.isEmpty() && ids.size() > 0) {
                    doctorAppointmentService.deleteTempDoctorAppointment(ids);
                }
                // Xoa Transaction Nhap
                List<String> bookingCodes = list.stream().map(DoctorAppointmentDTO::getBookingCode).collect(toList());
                if (!bookingCodes.isEmpty() && bookingCodes.size() > 0) {
                    transactionService.deleteTempTransactions(bookingCodes);
                }

                Map<String, Long> requirementCountMap = list
                        .stream()
                        .collect(Collectors.groupingBy(item -> item.getDoctorId() == null ? "null" : item.getDoctorId().toString(), Collectors.counting()));

                Set<String> set = requirementCountMap.keySet();
                for (String key : set) {
                    if (!"null".equals(key)) {
                        doctorScheduleTimeService.minusSubscriptions(Long.valueOf(key),
                                doctorScheduleTimeVM.getStartTime(), doctorScheduleTimeVM.getEndTime(),
                                requirementCountMap.get(key).intValue(), healthFacilityId );
                    } else {
                        doctorScheduleTimeService.minusSubscriptions(null,
                                doctorScheduleTimeVM.getStartTime(), doctorScheduleTimeVM.getEndTime(),
                                requirementCountMap.get(key).intValue(), healthFacilityId);
                    }
                }
            }
        }
    }
}
