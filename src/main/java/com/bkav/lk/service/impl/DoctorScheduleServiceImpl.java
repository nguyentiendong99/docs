package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.domain.DoctorAppointmentConfiguration;
import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.dto.DoctorScheduleDTO;
import com.bkav.lk.repository.*;
import com.bkav.lk.service.DoctorScheduleService;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.service.mapper.DoctorScheduleMapper;
import com.bkav.lk.service.mapper.DoctorScheduleTimeMapper;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {
    private static final String SHEET_DOCTOR_SCHEDULE = "DoctorSchedule";

    private final Logger log = LoggerFactory.getLogger(DoctorScheduleService.class);

    private final DoctorScheduleRepository repository;

    private final DoctorScheduleMapper mapper;

    private final DoctorAppointmentRepository doctorAppointmentRepository;

    private final DoctorScheduleTimeRepository scheduleTimeRepository;

    private final DoctorAppointmentConfigurationRepository doctorAppointmentConfigurationRepository;

    private final DoctorScheduleTimeMapper scheduleTimeMapper;

    private final SimpleDateFormat formatter;

    private final DoctorService doctorService;

    private final HealthFacilitiesRepository healthFacilitiesRepository;

    private final ClinicRepository clinicRepository;

    private static final String ENTITY_NAME = "Doctor Schedule";

    public DoctorScheduleServiceImpl(
            DoctorScheduleRepository repository,
            DoctorScheduleTimeRepository scheduleTimeRepository,
            DoctorScheduleMapper mapper,
            DoctorAppointmentConfigurationRepository doctorAppointmentConfigurationRepository, DoctorScheduleTimeMapper scheduleTimeMapper,
            DoctorService doctorService,
            HealthFacilitiesRepository healthFacilitiesRepository, ActivityLogServiceImpl activityLogService, DoctorAppointmentRepository doctorAppointmentRepository,
            ClinicRepository clinicRepository) {
        this.repository = repository;
        this.scheduleTimeRepository = scheduleTimeRepository;
        this.mapper = mapper;
        this.doctorAppointmentConfigurationRepository = doctorAppointmentConfigurationRepository;
        this.scheduleTimeMapper = scheduleTimeMapper;
        this.healthFacilitiesRepository = healthFacilitiesRepository;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
        this.formatter = new SimpleDateFormat("yyyy-MM-dd");
        this.doctorService = doctorService;
        this.clinicRepository = clinicRepository;
    }


    @Override
    public Page<DoctorScheduleDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable, User user) {
        log.debug("Request to search for a page of Doctor Schedule with multi query {}", queryParams);
        if (queryParams.containsKey("pageable")) {
            pageable = null;
        }
        if (user != null && user.getDoctorId() != null) {
            queryParams.set("doctorId", user.getDoctorId().toString());
        }
        List<DoctorScheduleDTO> listDto = mapper.toDto(repository.search(queryParams, pageable));
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        for (DoctorScheduleDTO element: listDto) {

            if(element.getWorkingTime().equals(3)){
                element.setAfternoonWork("Có");
                element.setMorningWork("Có");
            }else {
                if(element.getWorkingTime().equals(1)){
                    element.setMorningWork("Có");
                    element.setAfternoonWork("Không");
                }else if (element.getWorkingTime().equals(2)){
                    element.setAfternoonWork("Có");
                    element.setMorningWork("Không");
                }
            }
            element.setWorkingDateFormat(df.format(Date.from(element.getWorkingDate())));
        }
        if (pageable == null) {
            return new PageImpl<>(listDto);
        }
        return new PageImpl<>(listDto, pageable, repository.count(queryParams));
    }

    @Override
    public Optional<DoctorScheduleDTO> findOne(Long id) {
        log.debug("Request to get Doctor Schedule : {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public List<DoctorScheduleDTO> findSchedulesOfDoctor(Long id) {
        log.debug("REST request to get all Schedule of a Doctor : {}", id);
        return mapper.toDto(repository.findAllByDoctor_IdAndStatusAndWorkingDateAfter(id, Constants.ENTITY_STATUS.ACTIVE, Instant.now()));
    }

    @Override
    public DoctorScheduleDTO save(DoctorScheduleDTO doctorScheduleDTO) {
        log.debug("Request to save Doctor Schedule : {}", doctorScheduleDTO);
        DoctorDTO doctorDTO = doctorService.findByIdAndStatus(doctorScheduleDTO.getDoctorId(), Constants.ENTITY_STATUS.ACTIVE);
        if (doctorDTO == null) {
            throw new BadRequestAlertException("Doctor not found", "", "doctor_not_found");
        }
        DoctorSchedule newDoctorSchedule = mapper.toEntity(doctorScheduleDTO);
        this.updateClinicToDoctorScheduleByDoctorId(newDoctorSchedule, doctorScheduleDTO.getDoctorId());
        DoctorSchedule schedule = repository.save(newDoctorSchedule);
        return mapper.toDto(schedule);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Doctor Schedule : {}", id);
        Optional<DoctorSchedule> doctorSchedule = repository.findOneByIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE);
        doctorSchedule.ifPresent(schedule -> {
            schedule.setStatus((Constants.ENTITY_STATUS.DELETED));
            Long healthFacilityId = schedule.getDoctor().getHealthFacilityId();
            Long doctorId = schedule.getDoctor().getId();
            Integer workingTime = schedule.getWorkingTime();
            Instant workingDate = schedule.getWorkingDate();
            if (healthFacilityId != null) {
                DoctorAppointmentConfiguration config = null;
                DoctorAppointmentConfiguration currentActiveConfig = doctorAppointmentConfigurationRepository
                        .findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE).orElse(null);
                DoctorAppointmentConfiguration pendingConfig = doctorAppointmentConfigurationRepository
                        .findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING).orElse(null);

                if (Objects.nonNull(currentActiveConfig) && Objects.nonNull(pendingConfig)) {
                    config = currentActiveConfig;
                    if (workingDate.isAfter(pendingConfig.getCreatedDate().plus(30, ChronoUnit.DAYS))) {
                        config = pendingConfig;
                    }
                } else if (Objects.nonNull(currentActiveConfig)) {
                    config = currentActiveConfig;
                } else {
                    config = doctorAppointmentConfigurationRepository.findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT).get();
                }

                Instant startTimeMorning = ZonedDateTime.of(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                        LocalTime.parse(config.getStartTimeMorning()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                Instant endTimeMorning = ZonedDateTime.of(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                        LocalTime.parse(config.getEndTimeMorning()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                Instant startTimeAfternoon = ZonedDateTime.of(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                        LocalTime.parse(config.getStartTimeAfternoon()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                Instant endTimeAfternoon = ZonedDateTime.of(workingDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                        LocalTime.parse(config.getEndTimeAfternoon()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

                if (workingTime.equals(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING)) {
                    Integer existRecordInDoctorAppointment = doctorAppointmentRepository
                            .countDoctorAppointmentInRangeTime(healthFacilityId, doctorId, startTimeMorning, endTimeMorning);
                    if (existRecordInDoctorAppointment > 0) {
                        throw new BadRequestAlertException("Doctor appointment exist cannot delete", "DoctorSchedule", "doctor-schedule.doctor_appointment_exist");
                    }

                } else if (workingTime.equals(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING)) {
                    Integer existRecordInDoctorAppointment = doctorAppointmentRepository
                            .countDoctorAppointmentInRangeTime(healthFacilityId, doctorId, startTimeAfternoon, endTimeAfternoon);
                    if (existRecordInDoctorAppointment > 0) {
                        throw new BadRequestAlertException("Doctor appointment exist cannot delete", "DoctorSchedule", "doctor-schedule.doctor_appointment_exist");
                    }

                } else if (workingTime.equals(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING)) {
                    Integer existRecordInDoctorAppointment = doctorAppointmentRepository
                            .countDoctorAppointmentInRangeTime(healthFacilityId, doctorId, startTimeMorning, endTimeAfternoon);
                    if (existRecordInDoctorAppointment > 0) {
                        throw new BadRequestAlertException("Doctor appointment exist cannot delete", "DoctorSchedule", "doctor-schedule.doctor_appointment_exist");
                    }

                }
                schedule.setStatus((Constants.ENTITY_STATUS.DELETED));
            }
        });
    }

    @Override
    public List<DoctorScheduleDTO> saveAll(Long healthFacilityId, List<DoctorScheduleDTO> doctorScheduleDTOs) {
        log.debug("Request to save Doctor Schedules : {}", doctorScheduleDTOs);
        doctorScheduleDTOs.forEach(o -> {
            o.setWorkingDate(o.getWorkingDate());
        });
        List<DoctorSchedule> doctorSchedules = mapper.toEntity(doctorScheduleDTOs);
        for (int i = 0; i < doctorSchedules.size(); i++) {
            this.updateClinicToDoctorScheduleByDoctorId(doctorSchedules.get(i), doctorScheduleDTOs.get(i).getDoctorId());
        }

        DoctorAppointmentConfiguration currentActiveConfig = doctorAppointmentConfigurationRepository
                .findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE).orElse(null);
        DoctorAppointmentConfiguration pendingConfig = doctorAppointmentConfigurationRepository
                .findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING).orElse(null);
        if (Objects.nonNull(currentActiveConfig) && Objects.nonNull(pendingConfig)) {
            // kiểm tra thông tin lịch khám bệnh mới dựa theo thông tin cấu hình của cả cấu hình hiện tại + cấu hình chờ
            doctorSchedules.forEach(o -> {
                ZonedDateTime lastDateForActivate = pendingConfig.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(pendingConfig.getApplyConfigAfterDay());
                ZonedDateTime currentDate = o.getWorkingDate().atZone(DateUtils.getZoneHCM());
                Integer dayOfWeek = currentDate.getDayOfWeek().getValue();
                Set<Integer> dayOfWeeks = null;
                Integer dayLeft = lastDateForActivate.getDayOfYear() - currentDate.getDayOfYear();
                if (dayLeft > 0) {
                    if (o.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING)) {
                        dayOfWeeks = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekMorning(), currentActiveConfig.getEndDayOfWeekMorning());
                    } else if (o.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING)) {
                        dayOfWeeks = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekAfternoon(), currentActiveConfig.getEndDayOfWeekAfternoon());
                    } else {
                        Set<Integer> dayOfWeekAtMornings = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekMorning(), currentActiveConfig.getEndDayOfWeekMorning());
                        Set<Integer> dayOfWeekAfternoons = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekAfternoon(), currentActiveConfig.getEndDayOfWeekAfternoon());
                        dayOfWeeks = Stream.concat(
                                dayOfWeekAtMornings.stream(),
                                dayOfWeekAfternoons.stream())
                                .filter(dow ->
                                        dayOfWeekAtMornings.stream().anyMatch(dowm -> dowm.equals(dow))
                                                && dayOfWeekAfternoons.stream().anyMatch(dowa -> dowa.equals(dow))
                                )
                                .collect(Collectors.toSet());
                    }
                } else {
                    if (o.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING)) {
                        dayOfWeeks = this.getDayOfWeeksInConfig(pendingConfig.getStartDayOfWeekMorning(), pendingConfig.getEndDayOfWeekMorning());
                    } else if (o.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING)) {
                        dayOfWeeks = this.getDayOfWeeksInConfig(pendingConfig.getStartDayOfWeekAfternoon(), pendingConfig.getEndDayOfWeekAfternoon());
                    } else {
                        Set<Integer> dayOfWeekAtMornings = this.getDayOfWeeksInConfig(pendingConfig.getStartDayOfWeekMorning(), pendingConfig.getEndDayOfWeekMorning());
                        Set<Integer> dayOfWeekAfternoons = this.getDayOfWeeksInConfig(pendingConfig.getStartDayOfWeekAfternoon(), pendingConfig.getEndDayOfWeekAfternoon());
                        dayOfWeeks = Stream.concat(
                                dayOfWeekAtMornings.stream(),
                                dayOfWeekAfternoons.stream())
                                .filter(dow ->
                                        dayOfWeekAtMornings.stream().anyMatch(dowm -> dowm.equals(dow))
                                                && dayOfWeekAfternoons.stream().anyMatch(dowa -> dowa.equals(dow))
                                )
                                .collect(Collectors.toSet());
                    }
                }

                if (!dayOfWeeks.isEmpty()) {
                    boolean dayOffExists = dayOfWeeks.stream().anyMatch(dow -> dow.equals(dayOfWeek));
                    if (!dayOffExists) {
                        throw new BadRequestAlertException(
                                "Can't create a new doctor schedule on day-off that already config",
                                ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                    }
                } else {
                    throw new BadRequestAlertException(
                            "Can't create a new doctor schedule on day-off that already config",
                            ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                }
            });
        } else if (Objects.nonNull(currentActiveConfig)) {
            doctorSchedules.forEach(o -> {
                Integer dayOfWeek = o.getWorkingDate().atZone(DateUtils.getZoneHCM()).getDayOfWeek().getValue();
                Set<Integer> dayOfWeeks = null;
                if (o.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING)) {
                    dayOfWeeks = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekMorning(), currentActiveConfig.getEndDayOfWeekMorning());
                } else if (o.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING)) {
                    dayOfWeeks = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekAfternoon(), currentActiveConfig.getEndDayOfWeekAfternoon());
                } else {
                    Set<Integer> dayOfWeekAtMornings = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekMorning(), currentActiveConfig.getEndDayOfWeekMorning());
                    Set<Integer> dayOfWeekAfternoons = this.getDayOfWeeksInConfig(currentActiveConfig.getStartDayOfWeekAfternoon(), currentActiveConfig.getEndDayOfWeekAfternoon());
                    dayOfWeeks = Stream.concat(
                            dayOfWeekAtMornings.stream(),
                            dayOfWeekAfternoons.stream())
                            .filter(dow ->
                                    dayOfWeekAtMornings.stream().anyMatch(dowm -> dowm.equals(dow))
                                            && dayOfWeekAfternoons.stream().anyMatch(dowa -> dowa.equals(dow))
                            )
                            .collect(Collectors.toSet());
                }

                if (!dayOfWeeks.isEmpty()) {
                    boolean dayOffExists = dayOfWeeks.stream().anyMatch(dow -> dow.equals(dayOfWeek));
                    if (!dayOffExists) {
                        throw new BadRequestAlertException(
                                "Can't create a new doctor schedule on day-off that already config",
                                ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                    }
                } else {
                    throw new BadRequestAlertException(
                            "Can't create a new doctor schedule on day-off that already config",
                            ENTITY_NAME, "doctor-schedule.schedules-out-of-date");
                }
            });
        }


        List<DoctorSchedule> result = repository.saveAll(doctorSchedules);
        return mapper.toDto(result);
    }


    private DoctorScheduleDTO findByDoctorIdAndWorkingDate(Long doctorId, Instant workingDate) {
        log.debug("Request to get Doctor Schedule with : id = {}; working date = {}", doctorId, workingDate);
        Optional<DoctorSchedule> result = repository.findByDoctorIdAndWorkingDateAndStatus(doctorId, workingDate, Constants.ENTITY_STATUS.ACTIVE.intValue());
        return result.isPresent() ? mapper.toDto(result.get()) : null;
    }

    @Override
    public boolean isDoctorSchedulesExist(List<DoctorScheduleDTO> doctorScheduleDTOs) {
        boolean isExist = false;
        DoctorScheduleDTO tempDoctorSchedule = null;
        for (DoctorScheduleDTO doctorScheduleDTO : doctorScheduleDTOs) {
            tempDoctorSchedule = this.findByDoctorIdAndWorkingDate(
                    doctorScheduleDTO.getDoctorId(), doctorScheduleDTO.getWorkingDate());
            // không check theo id mà check theo workingTime có bằng nhau không?
            if (tempDoctorSchedule != null && ( doctorScheduleDTO.getWorkingTime().equals(tempDoctorSchedule.getWorkingTime()) ||
                    tempDoctorSchedule.getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING) )) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    @Override
    public boolean isDoctorScheduleExist(DoctorScheduleDTO doctorScheduleDTO) {
        boolean isExist = false;
        DoctorScheduleDTO tempDoctorSchedule = this.findByDoctorIdAndWorkingDate(
                doctorScheduleDTO.getDoctorId(), doctorScheduleDTO.getWorkingDate());
        if (tempDoctorSchedule != null && !doctorScheduleDTO.getId().equals(tempDoctorSchedule.getId())) {
            isExist = true;
        }
        return isExist;
    }

    public Map<String, List<DoctorScheduleDTO>> handleTree(List<DoctorScheduleDTO> list) {
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        list.forEach(item -> {
            item.setGroupByParentId(this.formatter.format(Date.from(item.getWorkingDate())).replaceAll("-", ""));
        });
        return new TreeMap<>(list.stream().collect(Collectors.groupingBy(d -> this.formatter.format(Date.from(d.getWorkingDate())),
                Collectors.mapping((DoctorScheduleDTO d) -> d, toList()))));
    }

    @Override
    public List<DoctorScheduleDTO> findAll() {
        log.debug("Request to find Doctor Schedule status = 1");
        List<DoctorScheduleDTO> dto = mapper.toDto(repository.findAll());
        return dto;
    }

    @Override
    public List<DoctorScheduleDTO> findAllByWorkingDateAndStatus(List<Long> doctorIds, Instant workingDate, Integer status) {
        return mapper.toDto(repository.findAllByDoctor_IdInAndWorkingDateEqualsAndStatus(doctorIds, workingDate, status));
    }

    @Override
    public List<DoctorScheduleDTO> findSchedulesOfDoctorValid(Long doctorId, Instant startTime, Instant endTime) {
        return mapper.toDto(repository.findAllByDoctor_IdAndStatusAndWorkingDateAfterAndWorkingDateBefore(doctorId, Constants.ENTITY_STATUS.ACTIVE, startTime, endTime));
    }

    @Override
    public List<Instant> findAllAvailableInHospital(Long healthFacilityId, Instant startDate, Instant endDate) {
        return repository.findAllAvailableInHospital(healthFacilityId, startDate, endDate);
    }

    public ResultExcel bulkUploadSchedules(Long healthFacilityId, List<DoctorScheduleDTO> list) {
        ResultExcel resultExcel = new ResultExcel();
        List<ErrorExcel> detail = new ArrayList<>();
        Boolean pass = true;
        if (list.isEmpty()) {
            return null;
        }
        // Check field = null
        list.forEach(item -> {
            int index = list.indexOf(item) + 1;
            String error = "";
            if (item.getDoctorCode() == null) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", String.valueOf(index));
                ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.codeDoctorIsBlank", mapError);
                detail.add(errorExcels);
            }
            if (item.getWorkingDate() == null && item.getWorkingDateFormat() == null) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", String.valueOf(index));
                ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.dateFormat", mapError);
                detail.add(errorExcels);
            }
            if (item.getWorkingDate() == null && item.getWorkingDateFormat() != null && item.getWorkingDateFormat().equals("date_not_format")) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", String.valueOf(index));
                ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.dayNotAvailable", mapError);
                detail.add(errorExcels);
            }
            if (item.getWorkingTime() == null) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", String.valueOf(index));
                ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.doctorTimeNotHave", mapError);
                detail.add(errorExcels);
            }
        });

        if (!detail.isEmpty()) {
            resultExcel.setSucess(false);
            resultExcel.setErrorExcels(detail);
            return resultExcel;
        } else {
            List<String> listCodeAndDate = new ArrayList<>();
            Set<String> set = new HashSet<>();
            list.forEach(item -> {
                listCodeAndDate.add(item.getWorkingDateFormat() + "-" + item.getDoctorCode().toUpperCase());
            });
            // Kiểm tra các dòng phải khác nhau -> ngày != ngày && mã != mã
            set.add(listCodeAndDate.get(0));
            for (int i = 1; i < listCodeAndDate.size(); i++) {
                if (set.contains(listCodeAndDate.get(i))) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("row", String.valueOf(i + 1));
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.isDuplicated", mapError);
                    detail.add(errorExcels);
                } else {
                    set.add(listCodeAndDate.get(i));
                }
            }
            // return trùng dữ liệu
            if (!detail.isEmpty()) {
                resultExcel.setSucess(false);
                resultExcel.setErrorExcels(detail);
                return resultExcel;
            }
        }
        List<String> codes = new ArrayList<>();
        for (DoctorScheduleDTO d : list) {
            codes.add(d.getDoctorCode());
        }
        // Check ma code bac si
        if (!codes.isEmpty()) {
            List<DoctorDTO> doctorDTOList = doctorService.findByCodesAndHealthFacilityId(codes, Constants.ENTITY_STATUS.ACTIVE, healthFacilityId);
            for (DoctorScheduleDTO doctorScheduleDTO : list) {
                for (DoctorDTO doctorDTO : doctorDTOList) {
                    if (doctorScheduleDTO.getDoctorCode().equalsIgnoreCase(doctorDTO.getCode())) {
                        doctorScheduleDTO.setDoctorId(doctorDTO.getId());
                        doctorScheduleDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    }
                }
            }
            if (doctorDTOList.isEmpty()) {
                for (String code : codes) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("code", code);
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.doctorCodeIsNotFound", mapError);
                    detail.add(errorExcels);
                }
            } else {
                List<String> codeError = codes.stream()
                        .filter(code -> !checkCode(code, doctorDTOList))
                        .collect(toList());
                for (String code : codeError) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("code", code);
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.doctorCodeIsNotFound", mapError);
                    detail.add(errorExcels);
                }
            }
            // Check chon thoi gian lam viec cua bac si va ngay nhap vao nho hon ngay hien tai
            for (DoctorScheduleDTO doctorScheduleDTO : list) {
                if (doctorScheduleDTO.getWorkingTime() == null) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("code", doctorScheduleDTO.getDoctorCode());
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.doctorTimeNotChoose", mapError);
                    detail.add(errorExcels);
//                    String error = "Bạn chưa chọn lịch làm việc cho bác sĩ " + doctorScheduleDTO.getDoctorCode();
//                    detail.add(error);
                }
                if (doctorScheduleDTO.getWorkingDate() == null) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("code", "");
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.dateFormat", mapError);
                    detail.add(errorExcels);
                    break;
                } else if (doctorScheduleDTO.getWorkingDate() != null && doctorScheduleDTO.getWorkingDate().compareTo(Objects.requireNonNull(DateUtils.parseStartOfDay(DateUtils.now()))) < 1) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("code", doctorScheduleDTO.getDoctorCode());
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.inputDayNoLessThanToday", mapError);
                    detail.add(errorExcels);
//                    String error = "Bạn không thể nhập ngày nhỏ hơn hoặc bằng ngày hiện tại với bác sĩ " + doctorScheduleDTO.getDoctorCode();
//                    detail.add(error);
                }
            }
            // Check lich cua bac si da ton tai trong DB
            for (DoctorScheduleDTO doctorScheduleDTO : list) {
                if (this.checkDoctorScheduleIsExist(doctorScheduleDTO)) {
                    Map<String, String> mapError = new HashMap<>();
                    mapError.put("code", doctorScheduleDTO.getDoctorCode());
                    mapError.put("date", DateUtils.convertFromInstantToString(doctorScheduleDTO.getWorkingDate()));
                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.scheduleIsExistedByDate", mapError);
                    detail.add(errorExcels);
//                    String error = "Lịch đã tồn tại với bác sĩ " + doctorScheduleDTO.getDoctorCode() + " ngày " + DateUtils.convertFromInstantToString(doctorScheduleDTO.getWorkingDate());
//                    detail.add(error);
                }
                // nếu k trùng lịch => tổng hợp lại lịch làm việc của bác sĩ đó theo ngày đã chọn. (VD: cũ 1, mới 2 => save = 3)
                DoctorScheduleDTO oldDoctorScheduleDTO = this.findByDoctorIdAndWorkingDateAndStatus(doctorScheduleDTO.getDoctorId(), doctorScheduleDTO.getWorkingDate(), Constants.ENTITY_STATUS.ACTIVE);
                if (Objects.nonNull(oldDoctorScheduleDTO)) {
                    if (!oldDoctorScheduleDTO.getWorkingTime().equals(doctorScheduleDTO.getWorkingTime())) {
                        doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING);
                    }
                    doctorScheduleDTO.setId(oldDoctorScheduleDTO.getId());
                }
            }

            // Phòng khám không bắt buộc nên bỏ check TH này
            // Check phong kham khong ton tai
//            List<DoctorScheduleDTO> doctorScheduleDTOS = list.stream().filter(item -> checkCode(item.getDoctorCode(), doctorDTOList)).collect(Collectors.toList());
//            List<DoctorSchedule> doctorSchedules = mapper.toEntity(doctorScheduleDTOS);
//            for (int i = 0; i < doctorSchedules.size(); i++) {
//                this.updateClinicToDoctorScheduleByDoctorCode(doctorSchedules.get(i), doctorScheduleDTOS.get(i).getDoctorCode());
//                if (doctorSchedules.get(i).getClinic() == null) {
//                    Map<String, String> mapError = new HashMap<>();
//                    mapError.put("code", list.get(i).getDoctorCode());
//                    ErrorExcel errorExcels = new ErrorExcel("doctorSchedule.clinicNotExistedWithDoctor", mapError);
//                    detail.add(errorExcels);
////                    String error = "Không tồn tại phòng khám với bác sĩ " + list.get(i).getDoctorCode();
////                    detail.add(error);
//                }
//            }
            if (detail.size() > 0) {
                pass = false;
                resultExcel.setSucess(false);
                resultExcel.setErrorExcels(detail);
            }
            if (pass) {
                this.saveAll(healthFacilityId, list);
                resultExcel.setSucess(true);
                resultExcel.setErrorExcels(detail);
            }
        }
        return resultExcel;
    }

    private Boolean checkCode(String code, List<DoctorDTO> list) {
        for (DoctorDTO doctorDTO : list) {
            if (doctorDTO.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DoctorScheduleDTO> excelToSchedules(InputStream inputStream) {
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(SHEET_DOCTOR_SCHEDULE);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestAlertException("format template file invalid", "", "excel.formatTemplate");
            }

            Iterator<Row> rows = sheet.iterator();

            List<DoctorScheduleDTO> list = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber < 6) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                DoctorScheduleDTO doctorScheduleDTO = new DoctorScheduleDTO();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (currentCell.getColumnIndex()) {
                        case 0:
                            if (currentCell.getCellType() == CellType.STRING) {
                                try {
                                    df.setLenient(false);
                                    df.parse(currentCell.getStringCellValue().trim());
                                    doctorScheduleDTO.setWorkingDate(DateUtils.parseStartOfDay2(currentCell.getStringCellValue().trim()));
                                    if (doctorScheduleDTO.getWorkingDate() != null) {
                                        doctorScheduleDTO.setWorkingDateFormat(doctorScheduleDTO.getWorkingDate().atZone(DateUtils.getZoneHCM()).toLocalDate().toString());
                                    }
                                } catch (Exception exception) {
                                    doctorScheduleDTO.setWorkingDate(null);
                                    doctorScheduleDTO.setWorkingDateFormat("date_not_format");
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                try {
                                    String dateStr = df.format(currentCell.getDateCellValue());
                                    doctorScheduleDTO.setWorkingDate(DateUtils.parseStartOfDay2(dateStr));
                                    if (doctorScheduleDTO.getWorkingDate() != null) {
                                        doctorScheduleDTO.setWorkingDateFormat(doctorScheduleDTO.getWorkingDate().atZone(DateUtils.getZoneHCM()).toLocalDate().toString());
                                    }
                                } catch (Exception exception) {
                                    doctorScheduleDTO.setWorkingDateFormat(null);
                                    doctorScheduleDTO.setWorkingDate(null);
                                }
                            } else {
                                doctorScheduleDTO.setWorkingDateFormat(null);
                                doctorScheduleDTO.setWorkingDate(null);
                            }
                            break;
                        case 1:
                            if (currentCell.getCellType() == CellType.STRING) {
                                if (currentCell.getStringCellValue() != null && !currentCell.getStringCellValue().equals("")) {
                                    doctorScheduleDTO.setDoctorCode(currentCell.getStringCellValue().trim());
                                } else {
                                    doctorScheduleDTO.setDoctorCode(null);
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                doctorScheduleDTO.setDoctorCode(String.valueOf(currentCell.getNumericCellValue()));
                            } else {
                                doctorScheduleDTO.setDoctorCode(null);
                            }
                            break;
                        case 2:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                double value = currentCell.getNumericCellValue();
                                if (value == (double) 1) {
                                    doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING); // Lam buoi sang
                                } else {
                                    doctorScheduleDTO.setWorkingTime(null);
                                }
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                try {
                                    Integer value = Integer.parseInt(currentCell.getStringCellValue());
                                    if (value == 1) {
                                        doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING); // Lam buoi sang
                                    } else {
                                        doctorScheduleDTO.setWorkingTime(null);
                                    }
                                } catch (NumberFormatException exception) {
                                    doctorScheduleDTO.setWorkingTime(null);
                                }
                            }
                            break;
                        case 3:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                double value = currentCell.getNumericCellValue();
                                if (value == (double) 1) {
                                    if (doctorScheduleDTO.getWorkingTime() == null) {
                                        doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING); // Lam buoi chieu
                                    } else {
                                        doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING); // Lam ban ngay
                                    }
                                } else {
                                    if (doctorScheduleDTO.getWorkingTime() == null) {
                                        doctorScheduleDTO.setWorkingTime(null);
                                    }
                                }
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                try {
                                    Integer value = Integer.parseInt(currentCell.getStringCellValue());
                                    if (value == 1) {
                                        if (doctorScheduleDTO.getWorkingTime() == null) {
                                            doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING); // Lam buoi chieu
                                        } else {
                                            doctorScheduleDTO.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING); // Lam ban ngay
                                        }
                                    } else {
                                        if (doctorScheduleDTO.getWorkingTime() == null) {
                                            doctorScheduleDTO.setWorkingTime(null);
                                        }
                                    }
                                } catch (NumberFormatException exception) {
                                    doctorScheduleDTO.setWorkingTime(null);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                if (doctorScheduleDTO.getWorkingDate() == null && doctorScheduleDTO.getDoctorCode() == null && doctorScheduleDTO.getWorkingTime() == null) {
                    continue;
                }
                list.add(doctorScheduleDTO);
            }
            workbook.close();
            return list;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream exportDoctorScheduleToExcel(List<DoctorScheduleDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 4;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (DoctorScheduleDTO dsDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(DateUtils.convertFromInstantToString(dsDTO.getWorkingDate()));
                row.createCell(1).setCellValue(dsDTO.getDoctorCode());
                Cell cellMorning = row.createCell(2);
                Cell cellAfternoon = row.createCell(3);
                if (dsDTO.getWorkingTime() == Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING) {
                    cellMorning.setCellValue(1);
                } else if (dsDTO.getWorkingTime() == Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING) {
                    cellAfternoon.setCellValue(1);
                } else if (dsDTO.getWorkingTime() == Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING) {
                    cellMorning.setCellValue(1);
                    cellAfternoon.setCellValue(1);
                }
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public List<DoctorScheduleDTO> findByIds(List<Long> ids) {
        List<DoctorScheduleDTO> dto = mapper.toDto(repository.findByIds(ids));
        return dto;
    }

    @Override
    public Map<String, Object> timeOfDayValid(Long healthFacilityId) {
        Map<String, Object> map = new LinkedHashMap<>();
        Optional<DoctorAppointmentConfiguration> config = null;
        Optional<DoctorAppointmentConfiguration> currentActiveConfig = doctorAppointmentConfigurationRepository
                .findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);
        Optional<DoctorAppointmentConfiguration> pendingConfig = doctorAppointmentConfigurationRepository
                .findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
        if (currentActiveConfig.isPresent() && pendingConfig.isPresent()) {
            // lấy day-off dựa theo thông tin cấu hình của cả cấu hình hiện tại + cấu hình chờ
            return this.timeOfDayValidWithMultipleConfig(currentActiveConfig.get(), pendingConfig.get());
        } else if (currentActiveConfig.isPresent()) {
            config = currentActiveConfig;
        } else {
            config = doctorAppointmentConfigurationRepository
                    .findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
        }
        int startDayOfWeekMorning = config.get().getStartDayOfWeekMorning(); // 1 - Thứ 2
        int endDayOfWeekMorning = config.get().getEndDayOfWeekMorning(); // 6 - Thứ 7
        int startDayOfWeekAfternoon = config.get().getStartDayOfWeekAfternoon(); // - Thứ 2
        int endDayOfWeekAfternoon = config.get().getEndDayOfWeekAfternoon(); // 6 - Thứ 7

        List<Integer> dayOfWeekMorningValid = new ArrayList<>();
        List<Integer> dayOfWeekAfternoonValid = new ArrayList<>();

        if (startDayOfWeekMorning == endDayOfWeekMorning) {
            dayOfWeekMorningValid.add(startDayOfWeekMorning);
        } else if (startDayOfWeekMorning < endDayOfWeekMorning) {
            dayOfWeekMorningValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekMorning, endDayOfWeekMorning + 1).toArray()).boxed().collect(Collectors.toList()));
        } else {
            dayOfWeekMorningValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekMorning, 8).toArray()).boxed().collect(Collectors.toList()));
            dayOfWeekMorningValid.addAll(Arrays.stream(IntStream.range(1, endDayOfWeekMorning + 1).toArray()).boxed().collect(Collectors.toList()));
        }

        if (startDayOfWeekAfternoon == endDayOfWeekAfternoon) {
            dayOfWeekAfternoonValid.add(startDayOfWeekAfternoon);
        } else if (startDayOfWeekAfternoon < endDayOfWeekAfternoon) {
            dayOfWeekAfternoonValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekAfternoon, endDayOfWeekAfternoon + 1).toArray()).boxed().collect(Collectors.toList()));
        } else {
            dayOfWeekAfternoonValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekAfternoon, 8).toArray()).boxed().collect(Collectors.toList()));
            dayOfWeekAfternoonValid.addAll(Arrays.stream(IntStream.range(1, endDayOfWeekAfternoon + 1).toArray()).boxed().collect(Collectors.toList()));
        }
        map.put("morning", dayOfWeekMorningValid);
        map.put("afternoon", dayOfWeekAfternoonValid);
        return map;
    }

    private Map<String, Object> timeOfDayValidWithMultipleConfig(
            DoctorAppointmentConfiguration currentActiveConfig, DoctorAppointmentConfiguration pendingConfig) {
        Map<String, Object> map = new LinkedHashMap<>();
        int startDayOfWeekMorningCurrentConfig = currentActiveConfig.getStartDayOfWeekMorning();
        int endDayOfWeekMorningCurrentConfig = currentActiveConfig.getEndDayOfWeekMorning();
        int startDayOfWeekAfternoonCurrentConfig = currentActiveConfig.getStartDayOfWeekAfternoon();
        int endDayOfWeekAfternoonCurrentConfig = currentActiveConfig.getEndDayOfWeekAfternoon();

        int startDayOfWeekMorningPendingConfig = pendingConfig.getStartDayOfWeekMorning();
        int endDayOfWeekMorningPendingConfig = pendingConfig.getEndDayOfWeekMorning();
        int startDayOfWeekAfternoonPendingConfig = pendingConfig.getStartDayOfWeekAfternoon();
        int endDayOfWeekAfternoonPendingConfig = pendingConfig.getEndDayOfWeekAfternoon();

        // Lấy ngày theo cấu hình cũ xa nhất
        ZonedDateTime lastDateForActivate = pendingConfig.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(pendingConfig.getApplyConfigAfterDay());

        List<Integer> dayOfWeekMorningActiveValid = new ArrayList<>();
        List<Integer> dayOfWeekAfternoonActiveValid = new ArrayList<>();

        List<Integer> dayOfWeekMorningPendingValid = new ArrayList<>();
        List<Integer> dayOfWeekAfternoonPendingValid = new ArrayList<>();

        // Lấy danh sách ngày trong tuần theo cấu hình hiện tại
        if (startDayOfWeekMorningCurrentConfig == endDayOfWeekMorningCurrentConfig) {
            dayOfWeekMorningActiveValid.add(startDayOfWeekMorningCurrentConfig);
        } else if (startDayOfWeekMorningCurrentConfig < endDayOfWeekMorningCurrentConfig) {
            dayOfWeekMorningActiveValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekMorningCurrentConfig, endDayOfWeekMorningCurrentConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        } else {
            dayOfWeekMorningActiveValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekMorningCurrentConfig, 8).toArray()).boxed().collect(Collectors.toList()));
            dayOfWeekMorningActiveValid.addAll(Arrays.stream(IntStream.range(1, endDayOfWeekMorningCurrentConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        }

        if (startDayOfWeekAfternoonCurrentConfig == endDayOfWeekAfternoonCurrentConfig) {
            dayOfWeekAfternoonActiveValid.add(startDayOfWeekAfternoonCurrentConfig);
        } else if (startDayOfWeekAfternoonCurrentConfig < endDayOfWeekAfternoonCurrentConfig) {
            dayOfWeekAfternoonActiveValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekAfternoonCurrentConfig, endDayOfWeekAfternoonCurrentConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        } else {
            dayOfWeekAfternoonActiveValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekAfternoonCurrentConfig, 8).toArray()).boxed().collect(Collectors.toList()));
            dayOfWeekAfternoonActiveValid.addAll(Arrays.stream(IntStream.range(1, endDayOfWeekAfternoonCurrentConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        }

        // Lấy danh sách ngày trong tuần theo cấu hình chờ
        if (startDayOfWeekMorningPendingConfig == endDayOfWeekMorningPendingConfig) {
            dayOfWeekMorningPendingValid.add(startDayOfWeekMorningPendingConfig);
        } else if (startDayOfWeekMorningPendingConfig < endDayOfWeekMorningPendingConfig) {
            dayOfWeekMorningPendingValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekMorningPendingConfig, endDayOfWeekMorningPendingConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        } else {
            dayOfWeekMorningPendingValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekMorningPendingConfig, 8).toArray()).boxed().collect(Collectors.toList()));
            dayOfWeekMorningPendingValid.addAll(Arrays.stream(IntStream.range(1, endDayOfWeekMorningPendingConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        }

        if (startDayOfWeekAfternoonPendingConfig == endDayOfWeekAfternoonPendingConfig) {
            dayOfWeekAfternoonPendingValid.add(startDayOfWeekAfternoonPendingConfig);
        } else if (startDayOfWeekAfternoonPendingConfig < endDayOfWeekAfternoonPendingConfig) {
            dayOfWeekAfternoonPendingValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekAfternoonPendingConfig, endDayOfWeekAfternoonPendingConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        } else {
            dayOfWeekAfternoonPendingValid.addAll(Arrays.stream(IntStream.range(startDayOfWeekAfternoonPendingConfig, 8).toArray()).boxed().collect(Collectors.toList()));
            dayOfWeekAfternoonPendingValid.addAll(Arrays.stream(IntStream.range(1, endDayOfWeekAfternoonPendingConfig + 1).toArray()).boxed().collect(Collectors.toList()));
        }

        map.put("lastActiveDate", lastDateForActivate);
        map.put("morningActive", dayOfWeekMorningActiveValid);
        map.put("afternoonActive", dayOfWeekAfternoonActiveValid);
        map.put("morningPending", dayOfWeekMorningPendingValid);
        map.put("afternoonPending", dayOfWeekAfternoonPendingValid);
        return map;
    }

    @Override
    public DayOfWeek getDayOfWeekByDateString(String day) {
        ZonedDateTime date = ZonedDateTime.of(LocalDate.parse(day), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh"));
        return date.getDayOfWeek();
    }

    @Override
    public Integer getWorkingTimeByDateByConfig(DayOfWeek dayOfWeek, List<Integer> dayOfWeekMorningValid, List<Integer> dayOfWeekAfternoonValid) {
        if (dayOfWeekMorningValid.contains(dayOfWeek.getValue()) && dayOfWeekAfternoonValid.contains(dayOfWeek.getValue())) {
            return Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING;
        } else if (dayOfWeekMorningValid.contains(dayOfWeek.getValue()) && !dayOfWeekAfternoonValid.contains(dayOfWeek.getValue())) {
            return Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING;
        } else if (!dayOfWeekMorningValid.contains(dayOfWeek.getValue()) && dayOfWeekAfternoonValid.contains(dayOfWeek.getValue())) {
            return Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING;
        }
        return Constants.DOCTOR_SCHEDULE_STATUS.ERROR_WORKING;
    }

    @Override
    public List<DoctorScheduleDTO> findByClinicAndStatusNot(Long clinicId, Integer status) {
        return mapper.toDto(repository.findByClinicAndStatusNot(clinicId, status));
    }

    private void updateClinicToDoctorScheduleByDoctorId(DoctorSchedule newDoctorSchedule, Long doctorId) {
        DoctorDTO selectedDoctor = doctorService.findById(doctorId);
        if (Objects.nonNull(selectedDoctor) && Objects.nonNull(selectedDoctor.getClinicId())) {
            Clinic clinic = clinicRepository.findById(selectedDoctor.getClinicId()).orElse(null);
            newDoctorSchedule.setClinic(clinic);
        }
    }

    private void updateClinicToDoctorScheduleByDoctorCode(DoctorSchedule newDoctorSchedule, String code) {
        DoctorDTO selectedDoctor = doctorService.findByCode(code);
        if (Objects.nonNull(selectedDoctor)) {
            newDoctorSchedule.setClinic(clinicRepository.findByDoctorId(selectedDoctor.getId()).orElse(null));
        }
    }

    private Boolean checkDoctorScheduleIsExist(DoctorScheduleDTO doctorScheduleDTO) {
       DoctorScheduleDTO dto = mapper.toDto(repository.findByDoctorIdAndWorkingDateAndStatus(
               doctorScheduleDTO.getDoctorId(),
                doctorScheduleDTO.getWorkingDate(),
                Constants.ENTITY_STATUS.ACTIVE).orElse(null));
        return dto != null && dto.getWorkingTime().equals(doctorScheduleDTO.getWorkingTime());
    }

    private Set<Integer> getDayOfWeeksInConfig(Integer startDay, Integer endDay) {
        Set<Integer> ranges = new HashSet<>();
        if (startDay > endDay) {
            while (!startDay.equals(endDay)) {
                ranges.add(startDay);
                startDay++;
                if (startDay > 7) {
                    startDay -= 7;
                }
            }
            ranges.add(endDay);
        } else {
            while (startDay <= endDay) {
                ranges.add(startDay);
                startDay++;
            }
        }
        return ranges;
    }

    @Override
    public InputStream downloadTemplateExcelDoctorSchedule() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_DOCTOR_SCHEDULE);
        sheet.setDefaultColumnWidth(20);
        // Create header
        Row headRow = sheet.createRow(0);
        CellStyle headStyle = workbook.createCellStyle();
        XSSFFont headFont = workbook.createFont();
        headFont.setBold(true);
        headFont.setFontHeight(14);
        headStyle.setFont(headFont);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        Cell cell = headRow.createCell(0);
        cell.setCellValue("THỜI GIAN LÀM VIỆC");
        cell.setCellStyle(headStyle);

//        Row headRow2 = sheet.createRow(1);
//        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        // Create template Date and Time
        XSSFCellStyle headStyleFieldTem = workbook.createCellStyle();
        headStyleFieldTem.setAlignment(HorizontalAlignment.CENTER);
        headStyleFieldTem.setVerticalAlignment(VerticalAlignment.CENTER);
        Font tempFont = workbook.createFont();
        tempFont.setColor(IndexedColors.BLACK.getIndex());
        tempFont.setBold(true);

        Row headRow1 = sheet.createRow(2);

        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));
        Cell cellTempDateTitle = headRow1.createCell(0);
        cellTempDateTitle.setCellValue("Định dạng ngày tháng năm");
        cellTempDateTitle.setCellStyle(headStyleFieldTem);

        sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 4));
        Cell cellTempTimeTitle = headRow1.createCell(3);
        cellTempTimeTitle.setCellValue("Quy ước đăng ký lịch làm việc");
        cellTempTimeTitle.setCellStyle(headStyleFieldTem);

        Row headRow2 = sheet.createRow(3);
        Cell cellTempDate1 = headRow2.createCell(0);
        cellTempDate1.setCellValue("Nhập tháng/ngày/năm");
        cellTempDate1.setCellStyle(headStyleFieldTem);

        Cell cellTempDate2 = headRow2.createCell(1);
        cellTempDate2.setCellValue("VD: 03/29/2021");
        cellTempDate2.setCellStyle(headStyleFieldTem);

        Cell cellTempTime1 = headRow2.createCell(3);
        cellTempTime1.setCellValue("Có làm việc");
        cellTempTime1.setCellStyle(headStyleFieldTem);


        Cell cellTempTime2 = headRow2.createCell(4);
        cellTempTime2.setCellValue("1");
        cellTempTime2.setCellStyle(headStyleFieldTem);

        Row headRow3 = sheet.createRow(4);
        Cell cellTempTime3 = headRow3.createCell(3);
        cellTempTime3.setCellValue("Không làm việc");
        cellTempTime3.setCellStyle(headStyleFieldTem);

        Cell cellTempTime4 = headRow3.createCell(4);
        cellTempTime4.setCellValue("0");
        cellTempTime4.setCellStyle(headStyleFieldTem);

        //  Style
        XSSFCellStyle headStyleField = workbook.createCellStyle();
        headStyleField.setAlignment(HorizontalAlignment.CENTER);
        headStyleField.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyleField.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headStyleField.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        headStyleField.setFont(font);

        Row headRow6 = sheet.createRow(6);
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 2, 3));

        Row headRow7 = sheet.createRow(7);
        sheet.addMergedRegion(new CellRangeAddress(6, 7, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(6, 7, 1, 1));

        Cell cellDate = headRow6.createCell(0);
        cellDate.setCellValue("Ngày");
        cellDate.setCellStyle(headStyleField);

        Cell cellCode = headRow6.createCell(1);
        cellCode.setCellValue("Mã bác sĩ");
        cellCode.setCellStyle(headStyleField);

        Cell cellTime = headRow6.createCell(2);
        cellTime.setCellValue("Lịch làm việc");
        cellTime.setCellStyle(headStyleField);

        Cell cellMorning = headRow7.createCell(2);
        cellMorning.setCellValue("Sáng");
        cellMorning.setCellStyle(headStyleField);

        Cell cellAfternoon = headRow7.createCell(3);
        cellAfternoon.setCellValue("Chiều");
        cellAfternoon.setCellStyle(headStyleField);

        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setDataFormat(creationHelper.createDataFormat().getFormat("mm/dd/yyyy"));
        sheet.setDefaultColumnStyle(0, textStyle);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean existsPendingConfigWithHealthFacility(Long healthFacilityId) {
        return doctorAppointmentConfigurationRepository
                .existsByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING);
    }

    @Override
    public boolean existsByHealthFacilityId(Long healthFacilityId) {
        boolean existsByHealthFacilityId = false;
        List<DoctorDTO> doctorDTOs = doctorService.findByMainHealthFacilityId(healthFacilityId);
        if (!doctorDTOs.isEmpty()) {
            List<Long> doctorIds = doctorDTOs.stream()
                    .filter(o -> Constants.ENTITY_STATUS.ACTIVE.equals(o.getStatus()))
                    .map(DoctorDTO::getId)
                    .collect(toList());
            existsByHealthFacilityId = repository.existsByDoctorIdIn(doctorIds);
        }
        return existsByHealthFacilityId;
    }

    @Override
    public void updateViolatedSchedule(
            DoctorAppointmentConfigurationDTO currentActiveAppointmentConfig,
            DoctorAppointmentConfigurationDTO pendingAppointmentConfig) {
        // Lấy thông tin lịch khám bệnh của bác sỹ theo cấu hinh cũ nhưng nằm trong khoảng ảnh hưởng của cấu hình mới
        // (xa hơn 31 ngày so với ngày cập nhật cấu hình pending)
        if (Objects.nonNull(pendingAppointmentConfig)) {
            ZonedDateTime lastDateForActivate = pendingAppointmentConfig.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(pendingAppointmentConfig.getApplyConfigAfterDay());
            List<Long> doctorIds = doctorService.findByMainHealthFacilityId(currentActiveAppointmentConfig.getHealthFacilitiesId())
                    .stream().map(DoctorDTO::getId).collect(Collectors.toList());
            List<DoctorSchedule> doctorSchedules = repository.findByDoctorIdInAndWorkingDateGreaterThanEqual(doctorIds, lastDateForActivate.toInstant());

            List<DoctorSchedule> violatedSchedules = new ArrayList<>();
            doctorSchedules.forEach(o -> {
                ZonedDateTime currentDate = o.getWorkingDate().atZone(DateUtils.getZoneHCM());
                Integer dayOfWeek = currentDate.getDayOfWeek().getValue();

                Set<Integer> dayOfWeeks = this.getDayOfWeeksByTimeAndConfig(o.getWorkingTime(), pendingAppointmentConfig);
                if (!dayOfWeeks.isEmpty()) {
                    // Kiểm tra cùng khung giờ
                    if (!this.hasInDayOfWeekRange(dayOfWeek, dayOfWeeks)) {
                        // Thay đổi theo khung giờ
                        this.updateNewTimeForDoctorSchedules(violatedSchedules, o, dayOfWeek, pendingAppointmentConfig);
                    }
                }
            });

            repository.saveAll(violatedSchedules);
        }
    }

    /**
     * Cập nhật thông tin phù hợp cho lịch khám bệnh
     * Giữ nguyên nếu hợp lệ với cấu hình pending, ngược lại thay đổi theo quy tắc:
     * (cả ngày -> nửa ngày = nửa ngày / nửa ngày -> cả ngày = nửa ngày / nửa ngày -> nửa ngày còn lại = xóa)
     *
     * @param violatedSchedules
     * @param doctorSchedule
     * @param dayOfWeek
     * @param appointmentConfig
     */
    private void updateNewTimeForDoctorSchedules(List<DoctorSchedule> violatedSchedules, DoctorSchedule doctorSchedule, Integer dayOfWeek, DoctorAppointmentConfigurationDTO appointmentConfig) {
        Set<Integer> listDayOfWeekMorning = this.getDayOfWeeksByTimeAndConfig(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING, appointmentConfig);
        Set<Integer> listDayOfWeekAfternoon = this.getDayOfWeeksByTimeAndConfig(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING, appointmentConfig);
        Set<Integer> listDayOfWeekFullTime = this.getDayOfWeeksByTimeAndConfig(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING, appointmentConfig);


        if (this.hasInDayOfWeekRange(dayOfWeek, listDayOfWeekFullTime)) {
            if (Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING.equals(doctorSchedule.getWorkingTime())) {
                doctorSchedule.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING);
            }
            if (Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING.equals(doctorSchedule.getWorkingTime())) {
                doctorSchedule.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING);
            }
        } else if (this.hasInDayOfWeekRange(dayOfWeek, listDayOfWeekMorning)) {
            if (Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING.equals(doctorSchedule.getWorkingTime())) {
                doctorSchedule.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING);
            }
            if (Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING.equals(doctorSchedule.getWorkingTime())) {
                doctorSchedule.setStatus(Constants.ENTITY_STATUS.DELETED);
            }
        } else if (this.hasInDayOfWeekRange(dayOfWeek, listDayOfWeekAfternoon)) {
            if (Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING.equals(doctorSchedule.getWorkingTime())) {
                doctorSchedule.setWorkingTime(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING);
            }
            if (Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING.equals(doctorSchedule.getWorkingTime())) {
                doctorSchedule.setStatus(Constants.ENTITY_STATUS.DELETED);
            }
        } else {
            doctorSchedule.setStatus(Constants.ENTITY_STATUS.DELETED);
        }
        violatedSchedules.add(doctorSchedule);
    }

    private boolean hasInDayOfWeekRange(Integer dayOfWeek, Set<Integer> dayOfWeeks) {
        return dayOfWeeks.stream().anyMatch(dow -> dow.equals(dayOfWeek));
    }

    private Set<Integer> getDayOfWeeksByTimeAndConfig(Integer workingTime, DoctorAppointmentConfigurationDTO appointmentConfig) {
        Set<Integer> dayOfWeeks = null;

        // Lấy danh sách ngày trong tuần theo cấu hình pending
        if (Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING.equals(workingTime)) {
            dayOfWeeks = this.getDayOfWeeksInConfig(appointmentConfig.getStartDayOfWeekMorning(), appointmentConfig.getEndDayOfWeekMorning());
        } else if (Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING.equals(workingTime)) {
            dayOfWeeks = this.getDayOfWeeksInConfig(appointmentConfig.getStartDayOfWeekAfternoon(), appointmentConfig.getEndDayOfWeekAfternoon());
        } else {
            Set<Integer> dayOfWeekAtMornings = this.getDayOfWeeksInConfig(appointmentConfig.getStartDayOfWeekMorning(), appointmentConfig.getEndDayOfWeekMorning());
            Set<Integer> dayOfWeekAfternoons = this.getDayOfWeeksInConfig(appointmentConfig.getStartDayOfWeekAfternoon(), appointmentConfig.getEndDayOfWeekAfternoon());
            dayOfWeeks = Stream.concat(
                    dayOfWeekAtMornings.stream(),
                    dayOfWeekAfternoons.stream())
                    .filter(dow ->
                            dayOfWeekAtMornings.stream().anyMatch(dowm -> dowm.equals(dow))
                                    && dayOfWeekAfternoons.stream().anyMatch(dowa -> dowa.equals(dow))
                    )
                    .collect(Collectors.toSet());
        }

        return dayOfWeeks;
    }

    @Override
    public DoctorScheduleDTO findByDoctorIdAndWorkingDateAndStatus(Long doctorId, Instant workingDate, Integer status) {
        Optional<DoctorSchedule> result = repository.findByDoctorIdAndWorkingDateAndStatus(doctorId, workingDate, status);
        return result.map(mapper::toDto).orElse(null);
    }
}
