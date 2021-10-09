package com.bkav.lk.service.impl;

import com.bkav.lk.domain.DoctorAppointmentConfiguration;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.DoctorAppointmentConfigurationRepository;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorAppointmentConfigurationMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorAppointmentConfigurationImpl implements DoctorAppointmentConfigurationService {

    private final DoctorAppointmentConfigurationRepository repository;

    private final DoctorAppointmentConfigurationMapper mapper;

    private final DoctorService doctorService;

    private final DoctorAppointmentService doctorAppointmentService;

    private final DoctorScheduleService doctorScheduleService;

    private final DoctorScheduleTimeService doctorScheduleTimeService;

    public DoctorAppointmentConfigurationImpl(DoctorAppointmentConfigurationRepository repository, DoctorAppointmentConfigurationMapper mapper,
                                              @Lazy DoctorService doctorService,
                                              @Lazy DoctorAppointmentService doctorAppointmentService,
                                              @Lazy DoctorScheduleService doctorScheduleService,
                                              @Lazy DoctorScheduleTimeService doctorScheduleTimeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.doctorService = doctorService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorScheduleService = doctorScheduleService;
        this.doctorScheduleTimeService = doctorScheduleTimeService;
    }

    /**
     * Tạo cấu hình mặc định
     */
    @PostConstruct
    public void init() {
        Optional<DoctorAppointmentConfiguration> config = repository.findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
        if (!config.isPresent()) {
            DoctorAppointmentConfigurationDTO dto = new DoctorAppointmentConfigurationDTO();
            dto.setHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
            dto.setAppointmentDaily(Constants.ENTITY_STATUS.ACTIVE);
            dto.setAppointmentDoctor(Constants.ENTITY_STATUS.ACTIVE);
            dto.setMinutesPerAppointmentSchedule(Constants.DOCTOR_APPOINTMENT_CONFIG.MINUTES_PER_APPOINTMENT_SCHEDULE);
            dto.setAllowTimeDefault(Constants.DOCTOR_APPOINTMENT_CONFIG.ALLOW_TIME_DEFAULT);
            dto.setStartDayOfWeekMorning(Constants.DAY_OF_WEEK.MONDAY);
            dto.setEndDayOfWeekMorning(Constants.DAY_OF_WEEK.SATURDAY);
            dto.setStartDayOfWeekAfternoon(Constants.DAY_OF_WEEK.MONDAY);
            dto.setEndDayOfWeekAfternoon(Constants.DAY_OF_WEEK.SATURDAY);
            dto.setStartTimeMorning("08:00");
            dto.setEndTimeMorning("12:00");
            dto.setStartTimeAfternoon("13:00");
            dto.setEndTimeAfternoon("16:00");
            dto.setTimeConfig("420");
            dto.setApplyConfigAfterDay(0);
            dto.setPeriodConfig(1440);
            dto.setTimeConfigSubclinicalResults(720);
            dto.setDayConfig(DateUtils.today());
            dto.setMaxRegisteredPatientsByDaily(Constants.DOCTOR_APPOINTMENT_CONFIG.MAX_REGISTERED_PATIENTS_BY_DAILY);
            dto.setMaxRegisteredPatientsByDoctor(Constants.DOCTOR_APPOINTMENT_CONFIG.MAX_REGISTERED_PATIENTS_BY_DOCTOR);
            dto.setConnectWithHis(Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_MANUAL);
            dto.setPrepaymentMedicalService(Constants.ENTITY_STATUS.ACTIVE);
            dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
            dto.setNotiApproveAuto(Constants.ENTITY_STATUS.ACTIVE);
            DoctorAppointmentConfiguration entity = mapper.toEntity(dto);
            repository.save(entity);
        }
    }

    @Override
    public List<DoctorAppointmentConfigurationDTO> getListConfig() {
        return mapper.toDto(repository.findAll());
    }

    @Override
    public List<DoctorAppointmentConfigurationDTO> findAllPendingConfig() {
        return mapper.toDto(repository.findByStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.PENDING));
    }

    @Override
    public DoctorAppointmentConfigurationDTO getDefaultConfig() {
        Optional<DoctorAppointmentConfiguration> config = repository.findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
        return config.map(mapper::toDto).orElse(null);
    }

    @Override
    public DoctorAppointmentConfigurationDTO save(DoctorAppointmentConfigurationDTO configurationDTO) {
        DoctorAppointmentConfiguration entity = mapper.toEntity(configurationDTO);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public DoctorAppointmentConfigurationDTO findOne(Long healthFacilitiesId, Integer status) {
        Optional<DoctorAppointmentConfiguration> optional = repository.findByHealthFacilitiesIdAndStatus(healthFacilitiesId, status);
        return optional.map(mapper::toDto).orElse(null);
    }

    @Override
    public DoctorAppointmentConfigurationDTO findOneByHealthFacilitiesId(Long healthFacilitiesId) {
//        Optional<DoctorAppointmentConfiguration> optional = repository.findByHealthFacilitiesIdAndStatus(healthFacilitiesId);
        Optional<DoctorAppointmentConfiguration> optional = repository.findByHealthFacilitiesIdAndStatus(healthFacilitiesId, Constants.ENTITY_STATUS.ACTIVE);
        if (!optional.isPresent()) {
            optional = repository.findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
        }
        return optional.map(mapper::toDto).orElse(null);
    }

    @Override
    public void delete(Long healthFacilitiesId) {
        Optional<DoctorAppointmentConfiguration> optional = repository.findByHealthFacilitiesIdAndStatus(healthFacilitiesId, Constants.ENTITY_STATUS.ACTIVE);
        if (optional.isPresent()) {
            optional.get().setStatus(Constants.ENTITY_STATUS.DELETED);
            repository.save(optional.get());
        }
    }

    @Override
    public void deleteById(Long id) {
        Optional<DoctorAppointmentConfiguration> optional = repository.findById(id);
        if (optional.isPresent()) {
            optional.get().setStatus(Constants.ENTITY_STATUS.DELETED);
            repository.save(optional.get());
        }
    }

    @Override
    @Transactional
    public DoctorAppointmentConfigurationDTO updateBothConfig(
            DoctorAppointmentConfigurationDTO mandatoryAppointmentConfig,
            DoctorAppointmentConfigurationDTO optionalAppointmentConfig,
            Integer optionalStatus,
            Integer applyConfigAfterDay) {
        DoctorAppointmentConfigurationDTO result = this.save(mandatoryAppointmentConfig);
        mandatoryAppointmentConfig.setId(optionalAppointmentConfig.getId());
        mandatoryAppointmentConfig.setMinutesPerAppointmentSchedule(optionalAppointmentConfig.getMinutesPerAppointmentSchedule());
        mandatoryAppointmentConfig.setStartDayOfWeekMorning(optionalAppointmentConfig.getStartDayOfWeekMorning());
        mandatoryAppointmentConfig.setEndDayOfWeekMorning(optionalAppointmentConfig.getEndDayOfWeekMorning());
        mandatoryAppointmentConfig.setStartDayOfWeekAfternoon(optionalAppointmentConfig.getStartDayOfWeekAfternoon());
        mandatoryAppointmentConfig.setEndDayOfWeekAfternoon(optionalAppointmentConfig.getEndDayOfWeekAfternoon());
        mandatoryAppointmentConfig.setStartTimeMorning(optionalAppointmentConfig.getStartTimeMorning());
        mandatoryAppointmentConfig.setEndTimeMorning(optionalAppointmentConfig.getEndTimeMorning());
        mandatoryAppointmentConfig.setStartTimeAfternoon(optionalAppointmentConfig.getStartTimeAfternoon());
        mandatoryAppointmentConfig.setEndTimeAfternoon(optionalAppointmentConfig.getEndTimeAfternoon());
        mandatoryAppointmentConfig.setStatus(optionalStatus);
        mandatoryAppointmentConfig.setApplyConfigAfterDay(applyConfigAfterDay);
        this.save(mandatoryAppointmentConfig);
        return result;
    }

    @Override
    public void updateDoctorAppointmentByConfig(List<DoctorAppointmentDTO> appointmentDTOList, Long healthFacilityId, DoctorAppointmentConfigurationDTO currentConfig) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        appointmentDTOList.forEach(item -> {
            List<DoctorScheduleTimeVM> timeVMList = new ArrayList<>();
            String date = item.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate().format(dateTimeFormatter);
            if (item.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE)) {
                Instant workingDate = ZonedDateTime.of(LocalDate.parse(date), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                List<DoctorDTO> doctorDTOList = doctorService.findAllDoctorByHealthFacilityId(healthFacilityId);
                List<Long> doctorIds = doctorDTOList.stream().map(DoctorDTO::getId).collect(Collectors.toList());
                List<DoctorScheduleDTO> doctorScheduleDTOS = doctorScheduleService.findAllByWorkingDateAndStatus(doctorIds, workingDate, Constants.ENTITY_STATUS.ACTIVE);
                List<DoctorDTO> dtoList = new ArrayList<>();
                doctorScheduleDTOS.forEach(doctorScheduleDTO -> {
                    doctorDTOList.forEach(doctorDTO -> {
                        if (doctorDTO.getId().equals(doctorScheduleDTO.getDoctorId())) {
                            doctorDTO.setWorkingTime(doctorScheduleDTO.getWorkingTime());
                            dtoList.add(doctorDTO);
                        }
                    });
                });
                timeVMList = doctorScheduleTimeService.findSchedulesOfHospitalAvailable(healthFacilityId, currentConfig, date, dtoList, false, false);
                boolean isDuplicateTimeOld = false;
                DoctorScheduleTimeVM timeVM = null;
                for (DoctorScheduleTimeVM doctorScheduleTimeVM : timeVMList) {
                    if (doctorScheduleTimeVM.getStartTime().equals(item.getStartTime()) && doctorScheduleTimeVM.getEndTime().equals(item.getEndTime())) {
                        // Thời gian khám không thay đổi
                        isDuplicateTimeOld = true;
                    } else if (doctorScheduleTimeVM.getStartTime().isBefore(item.getStartTime()) || doctorScheduleTimeVM.getStartTime().equals(item.getStartTime())) {
                        // lưu lại thời gian gần nhất với bản ghi cũ để cập nhật giờ khám
                        timeVM = doctorScheduleTimeVM;
                    }
                }
                if (!isDuplicateTimeOld && Objects.nonNull(timeVM)) {
                    item.setStartTime(timeVM.getStartTime());
                    item.setEndTime(timeVM.getEndTime());
                    doctorAppointmentService.saveNormal(item);
                    doctorScheduleTimeService.plusSubscriptions(null, timeVM.getStartTime(), timeVM.getEndTime(), item.getHealthFacilityId(), true);
                }
            } else if (item.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DOCTOR)) {
                timeVMList = doctorScheduleTimeService.findSchedulesOfDoctorAvailable(healthFacilityId, currentConfig, date, item.getDoctorId(), false, false);
                boolean isDuplicateTimeOld = false;
                DoctorScheduleTimeVM timeVM = null;
                for (DoctorScheduleTimeVM doctorScheduleTimeVM : timeVMList) {
                    if (doctorScheduleTimeVM.getStartTime().equals(item.getStartTime()) && doctorScheduleTimeVM.getEndTime().equals(item.getEndTime())) {
                        // Thời gian khám không thay đổi
                        isDuplicateTimeOld = true;
                    } else if (doctorScheduleTimeVM.getStartTime().isBefore(item.getStartTime()) || doctorScheduleTimeVM.getStartTime().equals(item.getStartTime())) {
                        // lưu lại thời gian gần nhất với bản ghi cũ để cập nhật giờ khám
                        timeVM = doctorScheduleTimeVM;
                    }
                }
                if (!isDuplicateTimeOld && Objects.nonNull(timeVM)) {
                    item.setStartTime(timeVM.getStartTime());
                    item.setEndTime(timeVM.getEndTime());
                    doctorAppointmentService.saveNormal(item);
                    doctorScheduleTimeService.plusSubscriptions(item.getDoctorId(), timeVM.getStartTime(), timeVM.getEndTime(), item.getHealthFacilityId(), true);
                }
            }
        });
    }
}
