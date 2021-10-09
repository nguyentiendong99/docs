package com.bkav.lk.service.impl;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorAppointmentConfigurationMapper;
import com.bkav.lk.service.mapper.DoctorMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.rest.vm.HisDoctor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private static final String ENTITY_NAME = "doctor";

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorAppointmentRepository doctorAppointmentRepository;
    private final MedicalSpecialityRepository medicalSpecialityRepository;
    private final DoctorAppointmentConfigurationRepository configurationRepository;
    private final HealthFacilitiesRepository healthFacilitiesRepository;
    private final DoctorScheduleTimeRepository scheduleTimeRepository;

    private final DoctorAppointmentConfigurationService configurationService;
    private final PositionRepository positionRepository;
    private final AcademicRepository academicRepository;
    private final ClinicRepository clinicRepository;

    private final DoctorMapper doctorMapper;
    private final DoctorAppointmentConfigurationMapper configurationMapper;
    private final ActivityLogService activityLogService;
    private final RestTemplate restTemplate;
    private final ConfigRepository configRepository;
    private final DoctorFeedbackRepository doctorFeedbackRepository;
    private final CategoryConfigFieldService categoryConfigFieldService;
    private final CategoryConfigValueService categoryConfigValueService;
    @Value("${his.doctor_list_url}")
    private String DOCTOR_URL_LIST;


    @Autowired
    public DoctorServiceImpl(
            DoctorMapper doctorMapper,
            DoctorRepository doctorRepository,
            DoctorScheduleRepository doctorScheduleRepository, DoctorAppointmentRepository doctorAppointmentRepository,
            MedicalSpecialityRepository medicalSpecialityRepository, DoctorAppointmentConfigurationRepository configurationRepository,
            HealthFacilitiesRepository healthFacilitiesRepository, DoctorScheduleTimeRepository scheduleTimeRepository,
            @Lazy DoctorAppointmentConfigurationService configurationService, PositionRepository positionRepository, AcademicRepository academicRepository,
            ClinicRepository clinicRepository, DoctorAppointmentConfigurationMapper configurationMapper, ActivityLogServiceImpl activityLogService,
            RestTemplateBuilder restTemplateBuilder, ConfigRepository configRepository, DoctorFeedbackRepository doctorFeedbackRepository,
            CategoryConfigValueService categoryConfigValueService, CategoryConfigFieldService categoryConfigFieldService, CategoryConfigValueService categoryConfigValueService1) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
        this.configurationRepository = configurationRepository;
        this.healthFacilitiesRepository = healthFacilitiesRepository;
        this.scheduleTimeRepository = scheduleTimeRepository;
        this.configurationService = configurationService;
        this.clinicRepository = clinicRepository;
        this.configurationMapper = configurationMapper;
        this.activityLogService = activityLogService;
        this.medicalSpecialityRepository = medicalSpecialityRepository;
        this.positionRepository = positionRepository;
        this.academicRepository = academicRepository;
        this.restTemplate = restTemplateBuilder.build();
        this.configRepository = configRepository;
        this.doctorFeedbackRepository = doctorFeedbackRepository;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigValueService = categoryConfigValueService1;
    }

    @Override
    public List<DoctorDTO> findAllByTimeSelected(Long healthFacilitiesId, String date, Instant startTime, Instant endTime) {
        return this.findAllByTimeSelected(healthFacilitiesId, null,null, date, startTime, endTime);
    }

    @Override
    public List<DoctorDTO> findAllByTimeSelected(Long healthFacilitiesId, Long medicalSpecialityId, Long clinicId, String date, Instant startTime, Instant endTime) {
        LocalDate localDate = LocalDate.parse(date);
        Instant currentSelectDay = ZonedDateTime.of(localDate, LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Optional<DoctorAppointmentConfiguration> config = configurationRepository.findByHealthFacilitiesIdAndStatus(healthFacilitiesId, Constants.ENTITY_STATUS.ACTIVE);
        List<DoctorSchedule> doctorSchedules = doctorScheduleRepository.findAllByTimeSelected(healthFacilitiesId, currentSelectDay);
        if (!doctorSchedules.isEmpty()) {
            List<DoctorDTO> doctorDTOS = new ArrayList<>();
            DoctorAppointmentConfigurationDTO configAppointment;
            if (config.isPresent()) {
                configAppointment = configurationMapper.toDto(config.get());
            } else {
                configAppointment = configurationService.getDefaultConfig();
            }

            // Tạo thời gian buổi sáng
            Instant startTimeMorning = ZonedDateTime.of(localDate, LocalTime.parse(configAppointment.getStartTimeMorning()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            Instant endTimeMorning = ZonedDateTime.of(localDate, LocalTime.parse(configAppointment.getEndTimeMorning()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            // Tạo thời gian buổi chiều
            Instant startTimeAfternoon = ZonedDateTime.of(localDate, LocalTime.parse(configAppointment.getStartTimeAfternoon()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            Instant endTimeAfternoon = ZonedDateTime.of(localDate, LocalTime.parse(configAppointment.getEndTimeAfternoon()), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

            Integer maxRegisteredByDoctor = configAppointment.getMaxRegisteredPatientsByDoctor();
            Integer maxRegisteredByDaily = configAppointment.getMaxRegisteredPatientsByDaily();

            doctorSchedules.forEach(doctorSchedule -> {
                Integer workingTime = doctorSchedule.getWorkingTime();
                DoctorDTO doctorDTO = doctorMapper.toDto(doctorSchedule.getDoctor());
                doctorDTO.setWorkingTime(workingTime);
                if (workingTime.equals(Constants.DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING)) {
                    doctorDTO.setWorkingTimeAvailable(LocalTime.parse(configAppointment.getStartTimeMorning()) + " - " + configAppointment.getEndTimeAfternoon());
                    Integer totalSUM = scheduleTimeRepository.totalSUMPeopleRegistered(Collections.singletonList(doctorDTO.getId()), startTime, endTime, healthFacilitiesId);
                    if (scheduleTimeAvailable(totalSUM, maxRegisteredByDoctor)) {
                        doctorDTOS.add(doctorDTO);
                    }
                } else if (workingTime.equals(Constants.DOCTOR_SCHEDULE_STATUS.MORNING_WORKING)) {
                    if (DateUtils.timestampInRange(startTimeMorning, endTimeMorning, startTime, endTime)) {
                        doctorDTO.setWorkingTimeAvailable(LocalTime.parse(configAppointment.getStartTimeMorning()) + " - " + configAppointment.getEndTimeMorning());
                        Integer totalSUM = scheduleTimeRepository.totalSUMPeopleRegistered(Collections.singletonList(doctorDTO.getId()), startTime, endTime, healthFacilitiesId);
                        if (scheduleTimeAvailable(totalSUM, maxRegisteredByDoctor)) {
                            doctorDTOS.add(doctorDTO);
                        }
                    }
                } else if (workingTime.equals(Constants.DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING)) {
                    if (DateUtils.timestampInRange(startTimeAfternoon, endTimeAfternoon, startTime, endTime)) {
                        doctorDTO.setWorkingTimeAvailable(LocalTime.parse(configAppointment.getStartTimeAfternoon()) + " - " + configAppointment.getEndTimeAfternoon());
                        Integer totalSUM = scheduleTimeRepository.totalSUMPeopleRegistered(Collections.singletonList(doctorDTO.getId()), startTime, endTime, healthFacilitiesId);
                        if (scheduleTimeAvailable(totalSUM, maxRegisteredByDoctor)) {
                            doctorDTOS.add(doctorDTO);
                        }
                    }
                }
            });

            List<DoctorDTO> newDoctorDTOS = null;
            if (Objects.nonNull(medicalSpecialityId) && Objects.isNull(clinicId)) {
                newDoctorDTOS = doctorDTOS
                        .stream()
                        .filter(o -> medicalSpecialityId.equals(o.getMedicalSpecialityId()))
                        .collect(Collectors.toList());
            } else if (Objects.nonNull(clinicId) && Objects.isNull(medicalSpecialityId)) {
                newDoctorDTOS = doctorDTOS
                        .stream()
                        .filter(o -> clinicId.equals(o.getClinicId()))
                        .collect(Collectors.toList());
            } else if (Objects.nonNull(medicalSpecialityId) && Objects.nonNull(clinicId)) {
                newDoctorDTOS = doctorDTOS
                        .stream()
                        .filter(o -> medicalSpecialityId.equals(o.getMedicalSpecialityId()) && clinicId.equals(o.getClinicId()))
                        .collect(Collectors.toList());
            } else {
                newDoctorDTOS = doctorDTOS;
            }
            newDoctorDTOS.forEach(this::getDoctorRating);

            return newDoctorDTOS;
        }
        throw new BadRequestAlertException("No calendar for this day exists", ENTITY_NAME, "calendar_not_exists");
    }

    private boolean scheduleTimeAvailable(Integer totalSUM, Integer maxRegistered) {
        if (totalSUM == null) { // Lịch còn trống
            return true;
        }
        return totalSUM < maxRegistered;
    }

    @Override
    public Page<DoctorDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.info("Search list of doctor with conditions: {}", queryParams);
        List<DoctorDTO> result = doctorMapper.toDto(doctorRepository.search(queryParams, pageable));
        for (DoctorDTO doctorDTO : result) {
            Optional<MedicalSpeciality> medicalSpeciality = medicalSpecialityRepository.findById(doctorDTO.getMedicalSpecialityId());
            medicalSpeciality.ifPresent(speciality -> doctorDTO.setMedicalSpecialityName(speciality.getName()));

            Optional<Position> optionalPosition = positionRepository.findById(doctorDTO.getPositionId());
            optionalPosition.ifPresent(position -> doctorDTO.setPositionName(position.getName()));

            Optional<HealthFacilities> healthFacilities = healthFacilitiesRepository.findById(doctorDTO.getHealthFacilityId());
            healthFacilities.ifPresent(item -> doctorDTO.setHealthFacilityName( item.getName()));

        }
        result.forEach(this::getDoctorRating);
        return new PageImpl<>(result, pageable, doctorRepository.count(queryParams));
    }

    @Override
    public Page<DoctorDTO> findAllDoctorsWithin30Days(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<DoctorDTO> result = doctorMapper.toDto(doctorRepository.findAllDoctorsWithin30Days(queryParams, pageable));
        return new PageImpl<>(result, pageable, doctorRepository.countDoctorsWithin30Days(queryParams));
    }

    @Override
    public DoctorDTO findById(Long id) throws BadRequestAlertException {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        DoctorDTO result = doctorMapper.toDto(doctor);
        this.getDoctorRating(result);
        return result;
    }

    @Override
    public DoctorDTO findByDoctorId(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        return doctor.map(doctorMapper::toDto).orElse(null);
    }

    @Override
    public DoctorDTO findByIdAndStatus(Long doctorId, Integer status) {
        Optional<Doctor> doctor = doctorRepository.findByIdAndStatus(doctorId, status);
        DoctorDTO result = doctor.map(doctorMapper::toDto).orElse(null);
        if (Objects.nonNull(result)) {
            this.getDoctorRating(result);
        }
        return doctor.map(doctorMapper::toDto).orElse(null);
    }

    @Override
    public List<DoctorDTO> findAll() {
        List<DoctorDTO> listDTO = new ArrayList<>();
        List<Doctor> listDoctor = doctorRepository.findAll();
        for (Doctor doctor : listDoctor) {
            listDTO.add(doctorMapper.toDto(doctor));
        }
        listDTO.forEach(this::getDoctorRating);
        return listDTO;
    }

    @Override
    public DoctorDTO create(DoctorDTO doctorDTO) {
        log.info("REST request to save Doctor: {}", doctorDTO);
        doctorDTO.setCode(this.generateDoctorCode(doctorDTO.getCode(), doctorDTO.getName(), false));
        Doctor newDoctor = doctorMapper.toEntity(doctorDTO);
        if(doctorDTO.getClinicId() == null){
            newDoctor.setClinic(null);
        }
        Doctor result = doctorRepository.save(newDoctor);
        if (!CollectionUtils.isEmpty(doctorDTO.getDoctorCustomConfigDTOS())) {
            List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

            doctorDTO.getDoctorCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(doctorCustomConfigDTO -> {
                CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                configValueDTO.setValue(doctorCustomConfigDTO.getValue());
                configValueDTO.setFieldId(doctorCustomConfigDTO.getFieldId());
                configValueDTO.setObjectId(result.getId());
                configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                configValueDTOS.add(configValueDTO);
            });
            categoryConfigValueService.createAll(configValueDTOS);
        }
        activityLogService.create(Constants.CONTENT_TYPE.DOCTOR, result);
        return doctorMapper.toDto(result);
    }

    @Override
    public DoctorDTO update(DoctorDTO doctorDTO) throws BadRequestAlertException {
        log.info("Update doctor id = {} with data: {}", doctorDTO.getId(), doctorDTO);
        Doctor doctor = doctorRepository.findById(doctorDTO.getId())
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        //Bác sỹ có lịch khám hoặc lịch làm việc không thể dừng hoạt động
        if (Constants.ENTITY_STATUS.DEACTIVATE.equals(doctorDTO.getStatus())) {
            List<DoctorAppointment> doctorAppointments = doctorAppointmentRepository.findByDoctorId(doctorDTO.getId());
            if (!doctorAppointments.isEmpty()) {
                throw new BadRequestAlertException("Can't disable a doctor already has appointment", ENTITY_NAME, "doctor.doctor-appointment-exists");
            }

            List<DoctorSchedule> doctorSchedules = doctorScheduleRepository.findAllByDoctor_IdAndStatusAndWorkingDateAfter(
                    doctorDTO.getId(), Constants.ENTITY_STATUS.ACTIVE, Instant.now());
            if (!doctorSchedules.isEmpty()) {
                throw new BadRequestAlertException("Can't disable a doctor already has schedule", ENTITY_NAME, "doctor.doctor-schedule-exists");
            }
        }

        if (!CollectionUtils.isEmpty(doctorDTO.getDoctorCustomConfigDTOS())) {
            List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
            List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
            doctorDTO.getDoctorCustomConfigDTOS().forEach(doctorCustomConfigDTO -> {
                CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(doctor.getId(), doctorCustomConfigDTO.getFieldId());
                configValueDTO.setValue(doctorCustomConfigDTO.getValue());
                if (configValueDTO.getFieldId() != null) {
                    configValueUpdateDTOS.add(configValueDTO);
                } else {
                    configValueDTO.setObjectId(doctor.getId());
                    configValueDTO.setFieldId(doctorCustomConfigDTO.getFieldId());
                    configValueDTO.setValue(doctorCustomConfigDTO.getValue());
                    configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    configValueCreateDTOS.add(configValueDTO);
                }
            });
            if (configValueUpdateDTOS.size() > 0) {
                categoryConfigValueService.updateAll(configValueUpdateDTOS);
            }
            if (configValueCreateDTOS.size() > 0) {
                categoryConfigValueService.createAll(configValueCreateDTOS);
            }
        }
        Academic academic = academicRepository.findById(doctorDTO.getAcademicId()).orElse(null);
        Clinic clinic = null;
        if(doctorDTO.getClinicId()!= null){
            clinic  = clinicRepository.findById(doctorDTO.getClinicId()).orElse(null);
        }
        MedicalSpeciality medicalSpeciality = medicalSpecialityRepository.findById(doctorDTO.getMedicalSpecialityId()).orElse(null);
//        if (!doctorDTO.getCode().trim().equals(doctor.getCode())) {
//            doctor.setCode(this.generateDoctorCode(doctorDTO.getCode(), doctorDTO.getName()));
//        }
        String updatedDoctorCode = this.generateDoctorCode(doctorDTO.getCode(), doctorDTO.getName(), !doctorDTO.getName().equals(doctor.getName()));
        if (updatedDoctorCode == null) {
            updatedDoctorCode = doctorDTO.getCode();
        }
        doctor.setCode(updatedDoctorCode);
        doctor.setName(doctorDTO.getName());
        doctor.setDob(doctorDTO.getDob());
        doctor.setPhone(doctorDTO.getPhone());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setEducation(doctorDTO.getEducation());
        doctor.setExperience(doctorDTO.getExperience());
        doctor.setStatus(doctorDTO.getStatus());
        doctor.setDescription(doctorDTO.getDescription());
        doctor.setGender(doctorDTO.getGender());
        doctor.setAcademic(academic);
        doctor.setMedicalSpeciality(medicalSpeciality);
        doctor.setAvatar(doctorDTO.getAvatar());
        doctor.setPositionId(doctorDTO.getPositionId());
        doctor.setClinic(clinic);
        doctor.setDifferentFacility(doctorDTO.getDifferentFacility());
        Doctor updatedDoctor = doctorRepository.save(doctor);
        activityLogService.update(Constants.CONTENT_TYPE.DOCTOR, doctor, updatedDoctor);
        return doctorMapper.toDto(updatedDoctor);
    }

    @Override
    @Transactional
    public void delete(Long id) throws BadRequestAlertException {
        List<DoctorSchedule> doctorSchedules;
        Doctor doctor = doctorRepository.findById(id).
                orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        doctorSchedules = doctorScheduleRepository.findByDoctorIdAndStatusNot(
                doctor.getId(), Constants.ENTITY_STATUS.DELETED);
        if (!doctorSchedules.isEmpty()) {
            doctorSchedules.forEach(o -> o.setStatus(Constants.ENTITY_STATUS.DELETED));
            doctorScheduleRepository.saveAll(doctorSchedules);
        }
        doctor.setStatus(Constants.ENTITY_STATUS.DELETED);
        doctorRepository.save(doctor);
        activityLogService.delete(Constants.CONTENT_TYPE.DOCTOR, doctor);
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {
        List<DoctorSchedule> doctorSchedules;
        List<Doctor> doctors = doctorRepository.findAllById(ids);
        for (Doctor doctor : doctors) {
            doctorSchedules = doctorScheduleRepository.findByDoctorIdAndStatusNot(
                    doctor.getId(), Constants.ENTITY_STATUS.DELETED);
            if (!doctorSchedules.isEmpty()) {
                throw new BadRequestAlertException("Can't delete doctor already has doctor schedule", ENTITY_NAME, "doctor.appointments-exist");
            }
            doctor.setStatus(Constants.ENTITY_STATUS.DELETED);
        }
        doctorRepository.saveAll(doctors);
        activityLogService.multipleDelete(Constants.CONTENT_TYPE.DOCTOR,
                doctors.stream().map(o -> (AbstractAuditingEntity) o).collect(Collectors.toList()));
    }

    @Override
    public List<DoctorDTO> findAllDoctorByHealthFacilityId(Long id) {
        List<DoctorDTO> results = doctorMapper.toDto(doctorRepository.findAllByHealthFacilityIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE));
        results.forEach(this::getDoctorRating);
        return results;
    }

    @Override
    public List<DoctorDTO> findAllDoctorByHealthFacilityId(Long id, Integer[] status) {
        List<DoctorDTO> results = doctorMapper.toDto(doctorRepository.findAllByHealthFacilityIdAndStatus(id, status));
        results.forEach(this::getDoctorRating);
        return results;
    }

    @Override
    public List<HisDoctor> getListDoctorFromHis(String healthFacilityCode) {
        Config config = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
        if (config != null) {
            ResponseEntity<HisDoctor[]> response =
                    restTemplate.getForEntity(
                            config.getPropertyValue() + DOCTOR_URL_LIST,
                            HisDoctor[].class);
            HisDoctor[] hisDoctors = response.getBody();
            return Arrays.asList(hisDoctors);
        } else {
            return null;
        }
    }

    @Override
    public List<DoctorDTO> findByMainHealthFacilityId(Long parentId) {
        List<Doctor> doctors = doctorRepository.findAllByHealthFacilityIdAndStatus(parentId, Constants.ENTITY_STATUS.ACTIVE);
        List<DoctorDTO> results = doctorMapper.toDto(doctors);
        results.forEach(this::getDoctorRating);
        return results;
    }

    @Override
    public DoctorDTO findByCode(String code) {
        Doctor doctor = doctorRepository.findByCodeAndStatusIsGreaterThanEqual(code, Constants.ENTITY_STATUS.ACTIVE)
                .orElseThrow(() -> new BadRequestAlertException("Invalid code", ENTITY_NAME, "codenull"));
        DoctorDTO result = doctorMapper.toDto(doctor);
        this.getDoctorRating(result);
        return result;
    }

    @Override
    public boolean isDoctorCodeExist(String code) {
        DoctorDTO result = null;
        try {
            result = this.findByCode(code);
        } catch (BadRequestAlertException ex) {
            log.error(ex.getMessage());
        }
        return result != null;
    }

    @Override
    public boolean isAcademicIdExist(Long academicId) {
        return doctorRepository.existsByAcademicId(academicId);
    }

    @Override
    public List<DoctorDTO> createAll(List<DoctorDTO> doctorDTOs) {
        log.info("REST request to save Doctors: {}", doctorDTOs);
        List<Doctor> newDoctors = doctorMapper.toEntity(doctorDTOs);
        List<String> newDoctorCodes = newDoctors.stream()
                .filter(o -> !StringUtils.isEmpty(o.getCode()))
                .map(Doctor::getCode)
                .collect(Collectors.toList());
        newDoctorCodes.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .forEach(o -> {
                    if (o.getValue() > 1) {
                        throw new BadRequestAlertException("Contain duplicate doctor's code in file", ENTITY_NAME, "doctor.duplicate-code-in-file");
                    }
                });
        newDoctors.forEach(o -> {
            o.setCode(this.generateDoctorCode(o.getCode(), o.getName(), false));
            o.setAcademic(academicRepository.findById(o.getAcademic().getId()).orElse(null));
            if(o.getClinic() != null && o.getClinic().getId() == null){
                o.setClinic(null);
            }
        });
        List<Doctor> result = doctorRepository.saveAll(newDoctors);
        activityLogService.multipleCreate(Constants.CONTENT_TYPE.DOCTOR,
                result.stream().map(o -> (AbstractAuditingEntity) o).collect(Collectors.toList()));
        return doctorMapper.toDto(result);
    }

    @Override
    public List<DoctorDTO> findAllByPosition(Long positionId) {
        List<DoctorDTO> results = doctorMapper.toDto(doctorRepository.findAllByPositionIdAndStatus(positionId, Constants.ENTITY_STATUS.ACTIVE));
        results.forEach(this::getDoctorRating);
        return results;
    }

    @Override
    public List<DoctorDTO> findByCodesAndHealthFacilityId(List<String> codes, int status, Long healthFacilityId) {
        List<Doctor> list = doctorRepository.findByCodesAndHealthFacilityId(codes, status, healthFacilityId);
        List<DoctorDTO> results = doctorMapper.toDto(list);
        results.forEach(this::getDoctorRating);
        return results;
    }

    @Override
    public List<DoctorDTO> findByDoctorIdsAndExistsAppointment(List<Long> ids) {
        List<Doctor> doctors = doctorRepository.findAllById(ids);
        List<Doctor> result = new ArrayList<>();
        Integer[] status = {Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, Constants.DOCTOR_APPOINTMENT_STATUS.DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID};
        boolean exist;
        for (Doctor doctor : doctors) {
            exist = doctorAppointmentRepository.existsByDoctorIdAndStatusNotIn(doctor.getId(), status);
            if (exist) {
                result.add(doctor);
            }
        }
        List<DoctorDTO> results = doctorMapper.toDto(result);
        results.forEach(this::getDoctorRating);
        return results;
    }

    @Override
    public List<DoctorDTO> addDoctorsByExcelFile(InputStream inputStream, List<ErrorExcel> errorDetails) {
        List<DoctorDTO> doctorDTOs = new ArrayList<>();
        DoctorDTO doctorDTO;
        int rowIndex = 0;
        int index = 1;
        String tempCode;
        int tempStatus;
        String tempAcademicCode;
        String tempMedicalSpecialityCode;
        String positionCode;
        String clinicCode;
        MedicalSpeciality tempMedicalSpeciality;
        Position tempPosition;
        Academic tempAcademic;
        Clinic tempClinic;
        boolean correctData;
        boolean checkStatus;
        boolean checkPosition ;
        boolean checkMedicalSpeciality;
        boolean checkAcademic ;
        boolean checkName;
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet("Thông tin bác sỹ");
            if (Objects.isNull(sheet) || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestAlertException("format template file invalid", ENTITY_NAME, "excel.formatTemplate");
            }
            List<ErrorExcel> excelList = new ArrayList<>();
            for (Row row : sheet) {
                if (rowIndex < 7) {
                    rowIndex++;
                    continue;
                }
                checkStatus = true;
                checkPosition = true;
                checkMedicalSpeciality = true;
                checkAcademic = true;
                checkName = true;
                doctorDTO = new DoctorDTO();
                correctData = true;
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        case 0:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempCode = cell.getRichStringCellValue().getString().trim();
                                if (this.isDoctorCodeExist(tempCode)) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.codeIsExisted", mapError);
                                    excelList.add(errorExcels);
                                } else if (tempCode.length() > 10) {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.codeMax10", mapError);
                                    excelList.add(errorExcels);
                                }else if(tempCode.length() <2){
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.codeMin2", mapError);
                                    excelList.add(errorExcels);
                                } else {
                                    doctorDTO.setCode(tempCode);
                                }
                            }
                            break;
                        case 1:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                doctorDTO.setName(cell.getRichStringCellValue().getString().trim());
                            } else {
                                checkName = false;
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("doctor.nameNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 2:
                            if (CellType.NUMERIC.equals(cell.getCellType())) {
                                tempStatus = (int) cell.getNumericCellValue();
                                if (tempStatus >= Constants.ENTITY_STATUS.DELETED && tempStatus <= Constants.ENTITY_STATUS.DEACTIVATE) {
                                    doctorDTO.setStatus(tempStatus);
                                } else {
                                    checkStatus = false;
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.statusIs1or2", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                checkStatus = false;
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("doctor.statusNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 3:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempAcademicCode = cell.getRichStringCellValue().getString().trim();
                                tempAcademic = academicRepository.findByCodeAndStatus(
                                        tempAcademicCode, Constants.ENTITY_STATUS.ACTIVE).orElse(null);
                                if (Objects.nonNull(tempAcademic)) {
                                    doctorDTO.setAcademicId(tempAcademic.getId());
                                } else {
                                    checkAcademic = false;
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.academicNotExisted", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                checkAcademic = false;
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("doctor.academicNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 4:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                tempMedicalSpecialityCode = cell.getRichStringCellValue().getString().trim();
                                tempMedicalSpeciality = medicalSpecialityRepository.findByCodeAndStatus(
                                        tempMedicalSpecialityCode, Constants.ENTITY_STATUS.ACTIVE).orElse(null);
                                if (Objects.nonNull(tempMedicalSpeciality)) {
                                    doctorDTO.setMedicalSpecialityId(tempMedicalSpeciality.getId());
                                } else {
                                    checkMedicalSpeciality = false;
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.medicalSpecialityNotExisted", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                checkMedicalSpeciality = false;
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("doctor.medicalSpecialityNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 5:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                positionCode = cell.getRichStringCellValue().getString().trim();
                                tempPosition = positionRepository.findByCodeAndStatus(positionCode, Constants.ENTITY_STATUS.ACTIVE).orElse(null);
                                if (Objects.nonNull(tempPosition)) {
                                    doctorDTO.setPositionId(tempPosition.getId());
                                } else {
                                    checkPosition = false;
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.positionNotExisted", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                checkPosition = false;
                                correctData = false;
                                Map<String, String> mapError = new HashMap<>();
                                mapError.put("row", String.valueOf(index));
                                ErrorExcel errorExcels = new ErrorExcel("doctor.positionNotSuitable", mapError);
                                excelList.add(errorExcels);
                            }
                            break;
                        case 6:
                            if (CellType.STRING.equals(cell.getCellType()) && !StrUtil.isBlank(cell.getStringCellValue())) {
                                clinicCode = cell.getRichStringCellValue().getString().trim();
                                tempClinic = clinicRepository.findByCode(clinicCode);
                                if (tempClinic != null){
                                    doctorDTO.setClinicId(tempClinic.getId());
                                }else {
                                    correctData = false;
                                    Map<String, String> mapError = new HashMap<>();
                                    mapError.put("row", String.valueOf(index));
                                    ErrorExcel errorExcels = new ErrorExcel("doctor.clinicCodeNotExist", mapError);
                                    excelList.add(errorExcels);
                                }
                            } else {
                                doctorDTO.setClinicId(null);
                            }
                            break;
                        case 7:
                            if (CellType.STRING.equals(cell.getCellType())  && !StrUtil.isBlank(cell.getStringCellValue())) {
                                doctorDTO.setDob(cell.getRichStringCellValue().getString());
                            }else{
                                doctorDTO.setDob(null);
                            }
                            break;
                        case 8:
                            if (CellType.STRING.equals(cell.getCellType())  && !StrUtil.isBlank(cell.getStringCellValue())) {
                                doctorDTO.setGender(cell.getRichStringCellValue().getString());
                            }else{
                                doctorDTO.setGender(null);
                            }
                            break;
                        case 9:
                            if (CellType.STRING.equals(cell.getCellType())  && !StrUtil.isBlank(cell.getStringCellValue())) {
                                doctorDTO.setEmail(cell.getRichStringCellValue().getString());
                            }else{
                                doctorDTO.setEmail(null);
                            }
                            break;
                        case 10:
                            if (CellType.STRING.equals(cell.getCellType())) {
                                doctorDTO.setPhone(cell.getRichStringCellValue().getString());
                            } else {
                                doctorDTO.setPhone(null);
                            }
                            break;
                        default:
                    }
                }
//                List<String> emptyFieldErrors = this.collectEmptyFieldError(doctorDTO);
//                if (correctData && !emptyFieldErrors.isEmpty()) {
//                    correctData = false;
//                    Map<String, String> mapError = new HashMap<>();
//                    mapError.put("row", String.valueOf(index));
//                    ErrorExcel errorExcels = new ErrorExcel("doctor.lackOfDataField", mapError);
//                    excelList.add(errorExcels);
//                    errorDetails.add("Hàng " + index + ": thiếu trường dữ liệu - " + emptyFieldErrors.toString());
//                }

                if (doctorDTO.getName() != null || doctorDTO.getStatus() != null || doctorDTO.getMedicalSpecialityId() != null
                        || doctorDTO.getAcademicId() != null || doctorDTO.getPositionId() != null) {
                    if (correctData && this.isEmpty(doctorDTO)) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("doctor.lackOfDataField", mapError);
                        excelList.add(errorExcels);
                    }
                    if (doctorDTO.getName() == null && checkName) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("doctor.nameIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (doctorDTO.getAcademicId() == null && checkAcademic) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("doctor.academicIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (doctorDTO.getMedicalSpecialityId() == null && checkMedicalSpeciality) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("doctor.medicalSpecialityIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (doctorDTO.getPositionId() == null && checkPosition) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("doctor.positionIsBlank", mapError);
                        excelList.add(errorExcels);
                    }
                    if (doctorDTO.getStatus() == null && checkStatus) {
                        correctData = false;
                        Map<String, String> mapError = new HashMap<>();
                        mapError.put("row", String.valueOf(index));
                        ErrorExcel errorExcels = new ErrorExcel("doctor.statusIsBlank", mapError);
                        excelList.add(errorExcels);
                    }

                }

                if (doctorDTO.getName() == null && doctorDTO.getAcademicId() == null && doctorDTO.getPositionId() == null &&
                        doctorDTO.getMedicalSpecialityId() == null && doctorDTO.getStatus() == null) {
                    correctData = false;
                    excelList.clear();
                } else {
                    errorDetails.addAll(excelList);
                    excelList.clear();
                }

                if (correctData) {
                    doctorDTOs.add(doctorDTO);
                }
                index++;
            }
            if (doctorDTOs.isEmpty() && errorDetails.isEmpty()) {
                Map<String, String> mapError = new HashMap<>();
                mapError.put("row", "");
                ErrorExcel errorExcels = new ErrorExcel("fileIsBlank", mapError);
                errorDetails.add(errorExcels);
            }
        } catch (IOException e) {
            log.error("Error: ", e);
        }
        return doctorDTOs;
    }

    @Override
    public ByteArrayInputStream exportDoctorToExcel(List<DoctorDTO> doctorDTOs, InputStream file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            int rowCount = 3;
            int index = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);

            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);
            style.setWrapText(true);

            for (DoctorDTO doctorDTO : doctorDTOs) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(++index);
                row.createCell(1).setCellValue(doctorDTO.getCode());
                row.createCell(2).setCellValue(doctorDTO.getName());
                row.createCell(3).setCellValue(doctorDTO.getAcademicName());
                row.createCell(4).setCellValue(this.getStatusName(doctorDTO.getStatus()));
                rowCount++;
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("Error: ", e);
        }
        return null;
    }

    private boolean isEmpty(DoctorDTO doctorDTO) {
        boolean isEmpty = false;
        for (Field field : doctorDTO.getClass().getDeclaredFields()) {
            if (this.isRequiredField(field.getName())) {
                field.setAccessible(true);
                try {
                    if (field.get(doctorDTO) == null) {
                        isEmpty = true;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return isEmpty;
    }
    private List<String> collectEmptyFieldError(DoctorDTO doctorDTO) {
        List<String> emptyFieldErrors = new ArrayList<>();
        for (Field field : doctorDTO.getClass().getDeclaredFields()) {
            if (this.isRequiredField(field.getName())) {
                field.setAccessible(true);
                try {
                    if (field.get(doctorDTO) == null) {
                        emptyFieldErrors.add(Constants.DOCTOR_REQUIRED_FIELD.getDisplayName(field.getName()));
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return emptyFieldErrors;
    }

    private boolean isRequiredField(String fieldName) {
        return Constants.DOCTOR_REQUIRED_FIELD.getRequiredFields().stream().anyMatch(o -> o.equals(fieldName));
    }

    private String getStatusName(Integer status) {
        String statusName = null;
        if (Constants.ENTITY_STATUS.DELETED.equals(status)) {
            statusName = "ĐÃ XÓA";
        } else if (Constants.ENTITY_STATUS.ACTIVE.equals(status)) {
            statusName = "ĐANG HOẠT ĐỘNG";
        } else if (Constants.ENTITY_STATUS.DEACTIVATE.equals(status)) {
            statusName = "DỪNG HOẠT ĐỘNG";
        }
        return statusName;
    }

    private String generateDoctorCode(String code, String name, boolean checkName) {
        int count = 0;
        Doctor doctor;
        String generateCode;
        String newCode;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCodeForDoctor(name.trim());
            while (true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                doctor = doctorRepository.findByCodeAndStatusGreaterThan(
                        newCode, Constants.ENTITY_STATUS.DELETED).orElse(null);
                if (Objects.isNull(doctor)) {
                    break;
                }
                count++;
            }
        } else {
            newCode = code.trim();
            doctor = doctorRepository.findByCodeAndStatusGreaterThan(
                    newCode, Constants.ENTITY_STATUS.DELETED).orElse(null);
            if (Objects.nonNull(doctor)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }

    private void getDoctorRating(DoctorDTO doctorDTO) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        // Tính đánh giá bác sĩ trung bình theo all trạng thái
        List<DoctorFeedback> doctorFeedbacks = doctorFeedbackRepository.findAllByDoctorId(doctorDTO.getId());
        if (!CollectionUtils.isEmpty(doctorFeedbacks)) {
            doctorDTO.setAveragePoint(formatter.format(this.getAveragePoint(doctorFeedbacks)));
            doctorDTO.setTotalFeedback(doctorFeedbacks.size());
        } else {
            doctorDTO.setAveragePoint(formatter.format(0.0));
            doctorDTO.setTotalFeedback(0);
        }
    }

    private Double getAveragePoint(List<DoctorFeedback> doctorFeedbacks) {
        double totalPoint = 0.0;
        double doctorRatePoint;
        for (DoctorFeedback doctorFeedback : doctorFeedbacks) {
            doctorRatePoint = Objects.nonNull(doctorFeedback.getRate()) ? doctorFeedback.getRate() : 0.0;
            totalPoint += doctorRatePoint;
        }
        try {
            return totalPoint / doctorFeedbacks.size();
        } catch (ArithmeticException e) {
            return 0.0;
        }
    }

    @Override
    public List<DoctorCustomConfigDTO> findAllCustomConfigByDoctorId(Long doctorId) {
        log.debug("Find all custom config of doctor Start");
        DoctorDTO doctorDTO = doctorMapper.toDto(doctorRepository.findById(doctorId).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllByHealthFacilityIdAndStatusAndConfigType(doctorDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE, Constants.CONFIG_CATEGORY_TYPE.DOCTOR.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(doctorId)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of doctor End");
        return configFieldDTOS.stream()
                .map(field -> new DoctorCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPositionId(Long positionId) {
        return doctorRepository.existsByPositionIdAndStatus(positionId, Constants.ENTITY_STATUS.ACTIVE);
    }

    @Override
    public List<DoctorDTO> findByClincAndStatus(Long clinicId, Integer[] status) {
        List<Doctor> list = doctorRepository.findByClinicIdAndStatusIn(clinicId, status);
        List<DoctorDTO> results = doctorMapper.toDto(list);
        return results;
    }

    @Override
    public List<DoctorDTO> findAllByIds(List<Long> ids) {
        return doctorMapper.toDto(doctorRepository.findAllById(ids));
    }
}
