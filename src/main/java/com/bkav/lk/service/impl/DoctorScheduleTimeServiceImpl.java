package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Doctor;
import com.bkav.lk.domain.DoctorAppointmentConfiguration;
import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.domain.DoctorScheduleTime;
import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.dto.DoctorScheduleTimeDTO;
import com.bkav.lk.dto.DoctorScheduleTimeVM;
import com.bkav.lk.repository.DoctorAppointmentConfigurationRepository;
import com.bkav.lk.repository.DoctorRepository;
import com.bkav.lk.repository.DoctorScheduleRepository;
import com.bkav.lk.repository.DoctorScheduleTimeRepository;
import com.bkav.lk.service.DoctorScheduleTimeService;
import com.bkav.lk.service.mapper.DoctorScheduleTimeMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class DoctorScheduleTimeServiceImpl implements DoctorScheduleTimeService {

    private final DoctorScheduleTimeRepository repository;

    private final DoctorRepository doctorRepository;

    private final DoctorScheduleRepository doctorScheduleRepository;

    private final DoctorAppointmentConfigurationRepository doctorAppointmentConfigurationRepository;

    private final DoctorScheduleTimeMapper mapper;

    public DoctorScheduleTimeServiceImpl(DoctorScheduleTimeRepository repository, DoctorRepository doctorRepository, DoctorScheduleRepository doctorScheduleRepository, DoctorAppointmentConfigurationRepository doctorAppointmentConfigurationRepository, DoctorScheduleTimeMapper mapper) {
        this.repository = repository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.doctorAppointmentConfigurationRepository = doctorAppointmentConfigurationRepository;
        this.mapper = mapper;
    }

    @Override
    public DoctorScheduleTimeDTO findOne(Long doctorId, Instant startTime, Instant endTime, Long healthFacilityId) {
        Optional<DoctorScheduleTime> entity = repository.findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndHealthFacilityId(doctorId, startTime, endTime, healthFacilityId);
        return entity.map(mapper::toDto).orElse(null);
    }

    @Override
    public DoctorScheduleTimeDTO findOne(Instant startTime, Instant endTime, Long healthFacilitiesId) {
        Optional<DoctorScheduleTime> entity = repository.findByTime(startTime, endTime, healthFacilitiesId);
        return entity.map(mapper::toDto).orElse(null);
    }

    @Override
    public DoctorScheduleTimeDTO findOne(Long doctorId, Instant startTime, Instant endTime) {
        Optional<DoctorScheduleTime> entity = repository.findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(doctorId, startTime, endTime);
        return entity.map(mapper::toDto).orElse(null);
    }

    /**
     * @Description isolation = Isolation.SERIALIZABLE is locks data
     * không cho những tiến trình xử lý khác thực hiện các operations trên dữ liệu khi transaction
     * hiện thời đang làm việc và việc khóa này sẽ được mở (giải phóng) ở cuối transaction
     * Tránh trường hợp dùng tools Jmeter, ... thực hiện đồng thời call api.
     * Số lượt đặt khám > số lượt config
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class })
    public boolean appointmentTimeAvailable(Long healthFacilityId, Long doctorId, Instant startTime, Instant endTime, int numberOfItem, boolean isCreateNew, boolean isChangeSchedule) {
        // TODO:
        //  if (isCreateNew === True) => create new record in doctor_appointment_time
        //  else return true;

        // TODO:
        //  if (isChangeSchedule === True) => if error => throw exception
        //  else return false;


        // check startTime < today + 1 => return false
        /*ZonedDateTime tomorrow = ZonedDateTime.now(DateUtils.getZoneHCM()).plus(1, ChronoUnit.DAYS).with(LocalTime.MIN);
        if (startTime.isBefore(tomorrow.toInstant())) {
            return false;
        }*/

        ZonedDateTime dateOfAppointment = ZonedDateTime.ofInstant(startTime, DateUtils.getZoneHCM()).with(LocalTime.MIN);

        List<Doctor> doctors = doctorRepository.findAllByHealthFacilityIdAndStatus(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
        if (doctors.isEmpty()) {
            throw new BadRequestAlertException("CSYT không có bác sĩ nào hoạt động!", "DoctorScheduleTime", "appointment.doctorEmpty");
        }

        Optional<DoctorAppointmentConfiguration> config = doctorAppointmentConfigurationRepository.findByHealthFacilitiesIdAndStatus(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE);
        if (!config.isPresent()) {
            config = doctorAppointmentConfigurationRepository.findByHealthFacilitiesId(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT);
        }

        // check exist workingDate valid
        List<Integer> workingTimes = new ArrayList<>();
        workingTimes.add(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING);
        workingTimes.add(checkingTimeSchedulesIsTimeOfDay(config.get(), startTime, endTime, dateOfAppointment.toLocalDate()));

        List<Long> doctorIds = doctors.stream().map(Doctor::getId).collect(Collectors.toList());
        List<DoctorSchedule> schedules = doctorScheduleRepository.findAllByDoctor_IdInAndWorkingDateAndWorkingTimeInAndStatus(
                doctorIds,
                dateOfAppointment.toInstant(),
                workingTimes,
                Constants.ENTITY_STATUS.ACTIVE);

        if (schedules.isEmpty()) {
            throw new BadRequestAlertException("Lịch làm việc của bác sĩ không còn lịch!", "DoctorScheduleTime", "appointment.scheduleEmpty");
        }

        int totalMaxByDoctor = config.get().getMaxRegisteredPatientsByDoctor();
        int totalMaxByDaily = config.get().getMaxRegisteredPatientsByDaily();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> values = new HashMap<>();
        if (doctorId == null) {
            Integer totalSUMPeopleRegisteredByTime = repository.totalSUMPeopleRegisteredByTime(startTime, endTime, healthFacilityId);
            if (numberOfItem > totalMaxByDaily) {
                if (!isChangeSchedule) {
                    return false;
                }
                values.put("value", totalMaxByDaily);
                throw new BadRequestAlertException("Khung giờ này chỉ có tối đa " + totalMaxByDaily + " lượt đặt khám",
                        "DoctorScheduleTime",
                        "appointment.maxDaily",
                        params, values);
            }
            if (totalSUMPeopleRegisteredByTime == null) {
                if (isCreateNew) {
                    DoctorScheduleTimeDTO timeDTO = create(null, startTime, endTime, numberOfItem, healthFacilityId);
                    repository.save(mapper.toEntity(timeDTO));
                }
                return true;
            }
            if (totalSUMPeopleRegisteredByTime + numberOfItem > totalMaxByDaily) {
                if (!isChangeSchedule) {
                    return false;
                }
                int dailyAvailable = totalMaxByDaily - totalSUMPeopleRegisteredByTime;
                values.put("value", dailyAvailable);
                throw new BadRequestAlertException("Khung giờ này chỉ còn " + dailyAvailable + " lượt đặt khám",
                        "DoctorScheduleTime",
                        "appointment.maxAvailableDaily",
                        params, values);
            }
            if (isCreateNew) {
                Optional<DoctorScheduleTime> scheduleTime = repository.findByTime(startTime, endTime, healthFacilityId);
                updateScheduleTime(scheduleTime, startTime, endTime, numberOfItem, healthFacilityId);
            }
            return true;
        }
        Integer totalSUMPeopleRegisteredByTime = repository.totalSUMPeopleRegistered(Collections.singletonList(doctorId), startTime, endTime, healthFacilityId);
        Optional<Doctor> doctor = this.doctorRepository.findById(doctorId);
        String doctorName = "";
        if (doctor.isPresent()) {
            doctorName = doctor.get().getName();
        }
        if (numberOfItem > totalMaxByDoctor) {
            if (!isChangeSchedule) {
                return false;
            }
            values.put("value", totalMaxByDoctor);
            values.put("doctorName", doctorName);
            throw new BadRequestAlertException("Khung giờ này chỉ có tối đa " + totalMaxByDoctor + " lượt đặt khám theo bác sĩ " + doctorName,
                    "DoctorScheduleTime",
                    "appointment.maxDoctor",
                    params, values);
        }
        if (totalSUMPeopleRegisteredByTime == null) {
            if (isCreateNew) {
                DoctorScheduleTimeDTO timeDTO = create(doctorId, startTime, endTime, numberOfItem, healthFacilityId);
                repository.save(mapper.toEntity(timeDTO));
            }
            return true;
        }
        if (totalSUMPeopleRegisteredByTime + numberOfItem > totalMaxByDoctor) {
            if (!isChangeSchedule) {
                return false;
            }
            int totalSUMPeopleCanRegisterByTime = totalMaxByDoctor - totalSUMPeopleRegisteredByTime;
            values.put("value", totalSUMPeopleCanRegisterByTime);
            values.put("doctorName", doctorName);
            throw new BadRequestAlertException("Khung giờ này chỉ còn " + totalSUMPeopleCanRegisterByTime + " lượt đặt khám theo bác sĩ " + doctorName,
                    "DoctorScheduleTime",
                    "appointment.maxAvailableDoctor",
                    params, values);
        }
        if (isCreateNew) {
            Optional<DoctorScheduleTime> scheduleTime = repository.findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndHealthFacilityId(doctorId, startTime, endTime, healthFacilityId);
            updateScheduleTime(scheduleTime, startTime, endTime, numberOfItem, healthFacilityId);
        }
        return true;
    }

    private void updateScheduleTime(Optional<DoctorScheduleTime> optional, Instant startTime, Instant endTime, int numberOfItem, Long healthFacilityId) {
        if (optional.isPresent()) {
            optional.get().setPeopleRegistered(optional.get().getPeopleRegistered() + numberOfItem);
            repository.save(optional.get());
        } else {
            DoctorScheduleTimeDTO timeDTO = create(null, startTime, endTime, numberOfItem, healthFacilityId);
            repository.save(mapper.toEntity(timeDTO));
        }
    }


    @Override
    public DoctorScheduleTimeDTO save(DoctorScheduleTimeDTO doctorScheduleTimeDTO) {
        DoctorScheduleTime entity = mapper.toEntity(doctorScheduleTimeDTO);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public DoctorScheduleTimeDTO create(Long doctorId, Instant startTime, Instant endTime, int numberOfItem, Long healthFacilityId) {
        DoctorScheduleTimeDTO doctorScheduleTimeDTO = new DoctorScheduleTimeDTO();
        doctorScheduleTimeDTO.setDoctorId(doctorId);
        doctorScheduleTimeDTO.setStartTime(startTime);
        doctorScheduleTimeDTO.setEndTime(endTime);
        doctorScheduleTimeDTO.setPeopleRegistered(numberOfItem);
        doctorScheduleTimeDTO.setHealthFacilityId(healthFacilityId);
        return doctorScheduleTimeDTO;
    }

    @Override
    public void minusSubscriptions(Long doctorId, Instant startTime, Instant endTime, int numberOfItem, Long healthFacilityId) {
        Optional<DoctorScheduleTime> optional;
        if (doctorId == null) {
            optional = repository.findByTime(startTime, endTime, healthFacilityId);
        } else {
            optional = repository.findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndHealthFacilityId(doctorId, startTime, endTime, healthFacilityId);
        }
        if (optional.isPresent()) {
            if (optional.get().getPeopleRegistered() > 0) {
                optional.get().setPeopleRegistered(optional.get().getPeopleRegistered() - numberOfItem);
                repository.save(optional.get());
            }
        }
    }

    @Override
    public void plusSubscriptions(Long doctorId, Instant startTime, Instant endTime, Long healthFacilityId, boolean isNotFoundWillCreateNew) {
        Optional<DoctorScheduleTime> optional;
        if (doctorId == null) {
            optional = repository.findByTime(startTime, endTime, healthFacilityId);
        } else {
            optional = repository.findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndHealthFacilityId(doctorId, startTime, endTime, healthFacilityId);
        }
        if (optional.isPresent()) {
            optional.get().setPeopleRegistered(optional.get().getPeopleRegistered() + 1);
            repository.save(optional.get());
        } else {
            if (isNotFoundWillCreateNew) {
                DoctorScheduleTimeDTO doctorScheduleTimeDTO = this.create(doctorId, startTime, endTime, 1, healthFacilityId);
                this.save(doctorScheduleTimeDTO);
            }
        }
    }

    @Override
    public List<DoctorScheduleTimeVM> findSchedulesOfHospitalAvailable(Long healthFacilityId, DoctorAppointmentConfigurationDTO config, String day, List<DoctorDTO> doctorDTOList, boolean isRandomTimeOption, boolean isMorning) {
        List<DoctorScheduleTimeVM> timeList = new ArrayList<>();
        LocalDate currentSelectDay = LocalDate.parse(day);
        List<Long> doctorIds = doctorDTOList.stream().map(DoctorDTO::getId).collect(Collectors.toList());
        if (doctorIds.isEmpty()) {
            throw new BadRequestAlertException("List doctor is empty", "DoctorScheduleTime", "doctor_ids_null");
        }
        DayOfWeek dayOfWeek = currentSelectDay.getDayOfWeek();

        // Tính số lượng bệnh nhân đăng ký khám tối đa/ca khám
        int minutesPerAppointmentSchedule = config.getMinutesPerAppointmentSchedule(); // Số phút/ca khám
        int startDayOfWeekMorning = config.getStartDayOfWeekMorning(); // 1 - Thứ 2
        int endDayOfWeekMorning = config.getEndDayOfWeekMorning(); // 6 - Thứ 7
        int startDayOfWeekAfternoon = config.getStartDayOfWeekAfternoon(); // - Thứ 2
        int endDayOfWeekAfternoon = config.getEndDayOfWeekAfternoon(); // 6 - Thứ 7

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

        // Kiểm tra ngày hợp lệ
        boolean checkingDayOfWeekMorningIsValid = dayOfWeekMorningValid.contains(dayOfWeek.getValue());
        boolean checkingDayOfWeekAfternoonIsValid = dayOfWeekAfternoonValid.contains(dayOfWeek.getValue());

        if (checkingDayOfWeekMorningIsValid || checkingDayOfWeekAfternoonIsValid) {
            String startTimeMorning = config.getStartTimeMorning(); // 08:00
            String endTimeMorning = config.getEndTimeMorning(); // 12:00
            String startTimeAfternoon = config.getStartTimeAfternoon(); // 13:00
            String endTimeAfternoon = config.getEndTimeAfternoon(); // 16:00
            ZonedDateTime startTimeMorningOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(startTimeMorning), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime endTimeMorningOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(endTimeMorning), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime startTimeAfternoonOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(startTimeAfternoon), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime endTimeAfternoonOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(endTimeAfternoon), ZoneId.of("Asia/Ho_Chi_Minh"));
            int totalMax = config.getMaxRegisteredPatientsByDaily();
            int totalMaxByDoctor = config.getMaxRegisteredPatientsByDoctor();
            if (checkingDayOfWeekMorningIsValid && checkingDayOfWeekAfternoonIsValid) {
                timeList.addAll(createSchedule(healthFacilityId, startTimeMorningOfDay, endTimeMorningOfDay, minutesPerAppointmentSchedule, totalMax, totalMaxByDoctor, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING));
                timeList.addAll(createSchedule(healthFacilityId, startTimeAfternoonOfDay, endTimeAfternoonOfDay, minutesPerAppointmentSchedule, totalMax, totalMaxByDoctor, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING));
            } else if (checkingDayOfWeekMorningIsValid) {
                timeList.addAll(createSchedule(healthFacilityId, startTimeMorningOfDay, endTimeMorningOfDay, minutesPerAppointmentSchedule, totalMax, totalMaxByDoctor, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING));
            } else {
                timeList.addAll(createSchedule(healthFacilityId, startTimeAfternoonOfDay, endTimeAfternoonOfDay, minutesPerAppointmentSchedule, totalMax, totalMaxByDoctor, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING));
            }
            if (timeList.isEmpty()) {
                throw new BadRequestAlertException("There are no empty schedules", "DoctorScheduleTime", "no_empty_schedules");
            }
            if (isRandomTimeOption) {
                List<DoctorScheduleTimeVM> timeMorning = new ArrayList<>();
                if (isMorning) {
                    timeList.forEach(item -> {
                        if (item.isAvailable() && item.isMorning()) {
                            Integer totalPeopleRegistered = repository.totalSUMPeopleRegisteredByTime(item.getStartTime(), item.getEndTime(), healthFacilityId);
                            if (totalPeopleRegistered == null) {
                                item.setPeopleRegistered(0);
                            } else {
                                item.setPeopleRegistered(totalPeopleRegistered);
                            }
                            timeMorning.add(item);
                        }
                    });
                    timeMorning.sort(Comparator.comparing(DoctorScheduleTimeVM::getPeopleRegistered));
                    if (timeMorning.size() > 0) {
                        return Collections.singletonList(timeMorning.get(0));
                    }
                    return timeMorning;
                }
                List<DoctorScheduleTimeVM> timeAfternoon = new ArrayList<>();
                timeList.forEach(item -> {
                    if (item.isAvailable() && !item.isMorning()) {
                        Integer totalPeopleRegistered = repository.totalSUMPeopleRegisteredByTime(item.getStartTime(), item.getEndTime(), healthFacilityId);
                        if (totalPeopleRegistered == null) {
                            item.setPeopleRegistered(0);
                        } else {
                            item.setPeopleRegistered(totalPeopleRegistered);
                        }
                        timeAfternoon.add(item);
                    }
                });
                timeAfternoon.sort(Comparator.comparing(DoctorScheduleTimeVM::getPeopleRegistered));
                if (timeAfternoon.size() > 0) {
                    return Collections.singletonList(timeAfternoon.get(0));
                }
                return timeAfternoon;
            }
            return timeList;
        } else {
            throw new BadRequestAlertException("DayOfWeekMorning or DayOfWeekAfternoon is invalid", "DoctorScheduleTime", "dayOfWeek_invalid");
        }
    }

    @Override
    public List<DoctorScheduleTimeVM> findSchedulesOfDoctorAvailable(Long healthFacilityId, DoctorAppointmentConfigurationDTO dto, String day, Long doctorId, boolean isRandomTimeOption, boolean isMorning) {
        LocalDate currentSelectDay = LocalDate.parse(day);
        List<DoctorScheduleTimeVM> timeList = new ArrayList<>();
        List<Long> doctorIds = Collections.singletonList(doctorId);
        Optional<DoctorSchedule> doctorSchedule = doctorScheduleRepository
                .findByDoctorIdAndWorkingDateAndStatus(doctorId, ZonedDateTime.of(currentSelectDay, LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(), Constants.ENTITY_STATUS.ACTIVE);
        int maxRegisteredPatientsByDoctor = dto.getMaxRegisteredPatientsByDoctor(); // Số lượng bệnh nhân đăng ký khám tối đa/bác sỹ - 30
        int minutesPerAppointmentSchedule = dto.getMinutesPerAppointmentSchedule(); // Số phút/ca khám - 30 minutes
        String startTimeMorning = dto.getStartTimeMorning(); // 08:00
        String endTimeMorning = dto.getEndTimeMorning(); // 12:00
        String startTimeAfternoon = dto.getStartTimeAfternoon(); // 13:00
        String endTimeAfternoon = dto.getEndTimeAfternoon(); // 16:00
        if (doctorSchedule.isPresent()) {
            ZonedDateTime startTimeMorningOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(startTimeMorning), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime endTimeMorningOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(endTimeMorning), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime startTimeAfternoonOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(startTimeAfternoon), ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime endTimeAfternoonOfDay = ZonedDateTime.of(currentSelectDay, LocalTime.parse(endTimeAfternoon), ZoneId.of("Asia/Ho_Chi_Minh"));
            if (doctorSchedule.get().getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING)) {
                timeList.addAll(createSchedule(healthFacilityId, startTimeMorningOfDay, endTimeMorningOfDay, minutesPerAppointmentSchedule, maxRegisteredPatientsByDoctor, null, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING));
            } else if (doctorSchedule.get().getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING)) {
                timeList.addAll(createSchedule(healthFacilityId, startTimeAfternoonOfDay, endTimeAfternoonOfDay, minutesPerAppointmentSchedule, maxRegisteredPatientsByDoctor, null, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING));
            } else if (doctorSchedule.get().getWorkingTime().equals(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING)) {
                timeList.addAll(createSchedule(healthFacilityId, startTimeMorningOfDay, endTimeMorningOfDay, minutesPerAppointmentSchedule, maxRegisteredPatientsByDoctor, null, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING));
                timeList.addAll(createSchedule(healthFacilityId, startTimeAfternoonOfDay, endTimeAfternoonOfDay, minutesPerAppointmentSchedule, maxRegisteredPatientsByDoctor, null, doctorIds, Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING));
            }
            if (isRandomTimeOption) {
                List<DoctorScheduleTimeVM> timeMorning = new ArrayList<>();
                if (isMorning) {
                    timeList.forEach(item -> {
                        if (item.isAvailable() && item.isMorning()) {
                            Integer totalPeopleRegistered = repository.totalSUMPeopleRegisteredByTime(item.getStartTime(), item.getEndTime(), healthFacilityId);
                            if (totalPeopleRegistered == null) {
                                item.setPeopleRegistered(0);
                            } else {
                                item.setPeopleRegistered(totalPeopleRegistered);
                            }
                            timeMorning.add(item);
                        }
                    });
                    timeMorning.sort(Comparator.comparing(DoctorScheduleTimeVM::getPeopleRegistered));
                    if (timeMorning.size() > 0) {
                        return Collections.singletonList(timeMorning.get(0));
                    }
                    return timeMorning;
                }
                List<DoctorScheduleTimeVM> timeAfternoon = new ArrayList<>();
                timeList.forEach(item -> {
                    if (item.isAvailable() && !item.isMorning()) {
                        Integer totalPeopleRegistered = repository.totalSUMPeopleRegisteredByTime(item.getStartTime(), item.getEndTime(), healthFacilityId);
                        if (totalPeopleRegistered == null) {
                            item.setPeopleRegistered(0);
                        } else {
                            item.setPeopleRegistered(totalPeopleRegistered);
                        }
                        timeAfternoon.add(item);
                    }
                });
                timeAfternoon.sort(Comparator.comparing(DoctorScheduleTimeVM::getPeopleRegistered));
                if (timeAfternoon.size() > 0) {
                    return Collections.singletonList(timeAfternoon.get(0));
                }
                return timeAfternoon;
            }
            return timeList;
        }
        throw new BadRequestAlertException("Doctor schedule is not exist", "DoctorScheduleTime", "doctor_schedule_not_exist");
    }

    public Integer checkingTimeSchedulesIsTimeOfDay(DoctorAppointmentConfiguration config, Instant startTime, Instant endTime, LocalDate localDate) {
        String sTM = config.getStartTimeMorning(); // 08:00
        String eTM = config.getEndTimeMorning(); // 12:00
        String sTA = config.getStartTimeAfternoon(); // 13:00
        String eTA = config.getEndTimeAfternoon(); // 16:00

        Instant startMorningOfDay = ZonedDateTime.of(localDate, LocalTime.parse(sTM), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Instant endMorningOfDay = ZonedDateTime.of(localDate, LocalTime.parse(eTM), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Instant startAfternoonOfDay = ZonedDateTime.of(localDate, LocalTime.parse(sTA), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Instant endAfternoonOfDay = ZonedDateTime.of(localDate, LocalTime.parse(eTA), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

        if (DateUtils.timestampInRange(startMorningOfDay, endMorningOfDay, startTime, endTime)) {
            return Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING;
        } else if (DateUtils.timestampInRange(startAfternoonOfDay, endAfternoonOfDay, startTime, endTime)) {
            return Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING;
        } else {
            return 0;
        }
    }

    public List<DoctorScheduleTimeVM> createSchedule(Long healthFacilityId,
                                                     ZonedDateTime zonedDateTime,
                                                     ZonedDateTime end,
                                                     long minutesPerAppointmentSchedule,
                                                     Integer maxRegistered, // config theo ngày hoặc theo bác sỹ
                                                     Integer maxRegistered2nd, // config bổ sung
                                                     List<Long> doctorIds,
                                                     Integer scheduleStatus) {
        List<DoctorScheduleTimeVM> timeList = new ArrayList<>();
        ZonedDateTime start = zonedDateTime;
        while (start.isBefore(end)) {
            DoctorScheduleTimeVM timeVM = new DoctorScheduleTimeVM();
            timeVM.setStartTime(start.toInstant());
            timeVM.setEndTime(start.plusMinutes(minutesPerAppointmentSchedule).toInstant());
            Integer totalSUM;
            if (maxRegistered2nd != null) { // config by daily
                totalSUM = repository.totalSUMPeopleRegisteredByTime(start.toInstant(), start.plusMinutes(minutesPerAppointmentSchedule).toInstant(), healthFacilityId);
            } else { // config by doctor
                totalSUM = repository.totalSUMPeopleRegistered(doctorIds, start.toInstant(), start.plusMinutes(minutesPerAppointmentSchedule).toInstant(), healthFacilityId);
            }
            if (totalSUM == null) { // Lịch còn trống
                timeVM.setAvailable(true);
            } else {
                if (totalSUM >= maxRegistered) {
                    if (maxRegistered2nd != null) { // Check theo config ngày
                        timeVM.setAvailable(maxRegistered2nd - totalSUM > 0);
                    } else {
                        timeVM.setAvailable(false);
                    }
                } else {
                    timeVM.setAvailable(true);
                }
            }
            if (Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING.equals(scheduleStatus)) {
                timeVM.setMorning(true);
            }
            timeVM.setTime(start.toLocalTime().toString() + " - " + start.plusMinutes(minutesPerAppointmentSchedule).toLocalTime().toString());
            timeList.add(timeVM);
            start = start.plusMinutes(minutesPerAppointmentSchedule);
        }
        if (!timeList.isEmpty()) {
            if (timeList.size() == 1) {
                DoctorScheduleTimeVM vm = timeList.get(0); // Lấy ra phần tử duy nhất trong mảng
                if (!DateUtils.timestampInRange(zonedDateTime.toInstant(), end.toInstant(), vm.getStartTime(), vm.getEndTime())) {
                    timeList.forEach(item -> item.setEndTime(end.toInstant()));
                }
            } else {
                DoctorScheduleTimeVM vm = timeList.get(timeList.size() - 1); // Lấy ra phần tử cuối cùng trong mảng
                if (!DateUtils.timestampInRange(zonedDateTime.toInstant(), end.toInstant(), vm.getStartTime(), vm.getEndTime())) {
                    timeList.remove(vm);
                    DoctorScheduleTimeVM timeVM = timeList.get(timeList.size() - 1);
                    timeVM.setEndTime(end.toInstant());
                    timeVM.setTime(LocalTime.from(timeVM.getStartTime().atZone(ZoneId.of("Asia/Ho_Chi_Minh")))
                            + " - " + LocalTime.from(timeVM.getEndTime().atZone(ZoneId.of("Asia/Ho_Chi_Minh"))));
                    timeList.set(timeList.size() - 1, timeVM);
                }
            }
        }
        return timeList;
    }
}
