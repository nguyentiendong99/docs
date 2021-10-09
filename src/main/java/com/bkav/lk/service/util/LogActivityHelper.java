package com.bkav.lk.service.util;

import com.bkav.lk.domain.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.*;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;

@Component
public class LogActivityHelper {

    private static final Logger log = LoggerFactory.getLogger(LogActivityHelper.class);
    private static final String PREV_INFORMATION_MESSAGE = "Thông tin cũ: ";
    private static final String NEXT_INFORMATION_MESSAGE = " - Thông tin mới: ";

    private final DoctorAppointmentService doctorAppointmentService;
    private final DoctorScheduleService doctorScheduleService;
    private final ClinicService clinicService;
    private final DoctorService doctorService;
    private final MedicalServiceService medicalServiceService;
    private final TopicService topicService;
    private final HealthFacilitiesService healthFacilitiesService;
    private final PositionService positionService;
    private final UserService userService;
    private final GroupService groupService;
    private final DepartmentService departmentService;
    private final MedicalSpecialityService medicalSpecialityService;
    private final DoctorAppointmentMapper doctorAppointmentMapper;
    private final DoctorScheduleMapper doctorScheduleMapper;
    private final ClinicMapper clinicMapper;
    private final DoctorMapper doctorMapper;
    private final MedicalServiceMapper medicalServiceMapper;
    private final TopicMapper topicMapper;
    private final HealthFacilitiesMapper healthFacilitiesMapper;
    private final PositionMapper positionMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final DepartmentMapper departmentMapper;
    private final MedicalSpecialityMapper medicalSpecialityMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public LogActivityHelper(
            DoctorAppointmentService doctorAppointmentService,
            DoctorScheduleService doctorScheduleService,
            ClinicService clinicService,
            DoctorService doctorService,
            MedicalServiceService medicalServiceService,
            TopicService topicService,
            HealthFacilitiesService healthFacilitiesService,
            PositionService positionService,
            UserService userService,
            GroupService groupService,
            DepartmentService departmentService,
            MedicalSpecialityService medicalSpecialityService,
            DoctorAppointmentMapper doctorAppointmentMapper,
            DoctorScheduleMapper doctorScheduleMapper,
            ClinicMapper clinicMapper,
            DoctorMapper doctorMapper,
            MedicalServiceMapper medicalServiceMapper,
            TopicMapper topicMapper,
            HealthFacilitiesMapper healthFacilitiesMapper,
            PositionMapper positionMapper,
            UserMapper userMapper,
            GroupMapper groupMapper,
            DepartmentMapper departmentMapper,
            MedicalSpecialityMapper medicalSpecialityMapper) {
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorScheduleService = doctorScheduleService;
        this.clinicService = clinicService;
        this.doctorService = doctorService;
        this.medicalServiceService = medicalServiceService;
        this.topicService = topicService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.positionService = positionService;
        this.userService = userService;
        this.groupService = groupService;
        this.departmentService = departmentService;
        this.medicalSpecialityService = medicalSpecialityService;
        this.doctorAppointmentMapper = doctorAppointmentMapper;
        this.doctorScheduleMapper = doctorScheduleMapper;
        this.clinicMapper = clinicMapper;
        this.doctorMapper = doctorMapper;
        this.medicalServiceMapper = medicalServiceMapper;
        this.topicMapper = topicMapper;
        this.healthFacilitiesMapper = healthFacilitiesMapper;
        this.positionMapper = positionMapper;
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
        this.departmentMapper = departmentMapper;
        this.medicalSpecialityMapper = medicalSpecialityMapper;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public <T> String getUpdateContent(ActivityLog activityLog, Class<T> clazz) {
        T currentData = null;
        T oldData = null;
        try {
            currentData = objectMapper.readValue(activityLog.getContent(), clazz);
            oldData = objectMapper.readValue(activityLog.getOldContent(), clazz);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return this.readChangedHistory(currentData, oldData);
    }

    public <T> T getContent(ActivityLog activityLog, Class<T> clazz) {
        T value = null;
        try {
            value = objectMapper.readValue(activityLog.getContent(), clazz);
        } catch (Exception ex) {
            log.error("Error: " + ex);
        }
        return value;
    }

    private <T> String readChangedHistory(T currentData, T oldData) {
        StringBuilder content = new StringBuilder();
        Field[] currentFields = currentData.getClass().getDeclaredFields();
        Field[] oldFields = oldData.getClass().getDeclaredFields();

        Field currentField = null;
        Field oldField = null;

        for (int i = 0; i < currentFields.length; i++) {
            currentField = currentFields[i];
            oldField = oldFields[i];
            currentField.setAccessible(true);
            oldField.setAccessible(true);
            try {
                if (!String.valueOf(oldField.get(oldData)).equals(String.valueOf(currentField.get(currentData)))) {
                    if (currentField.get(currentData) instanceof DoctorAppointment) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof DoctorSchedule) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Clinic) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Doctor) {
                        Doctor currentDoctor = (Doctor) currentField.get(currentData);
                        Doctor oldDoctor = (Doctor) oldField.get(oldData);
                        currentDoctor = doctorMapper.toEntity(doctorService.findById(currentDoctor.getId()));
                        oldDoctor = doctorMapper.toEntity(doctorService.findById(oldDoctor.getId()));
                        if (!currentDoctor.getId().equals(oldDoctor.getId())) {
                            content.append(this.createDetailUpdatedContent(oldDoctor.getName(), currentDoctor.getName()));
                        }
                    } else if (currentField.get(currentData) instanceof MedicalService) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Topic) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof HealthFacilities) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Position) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof User) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Group) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Department) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof MedicalSpeciality) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof Feedback) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof PatientRecord) {
                        //:TODO: Bổ sung nếu có
                    } else if (currentField.get(currentData) instanceof DoctorFeedback) {
                        //:TODO: Bổ sung nếu có
                    } else {
                        //TODO: Bổ sung trường đặc biêt (Giá trị != dữ liệu hiển thị)
                        if (currentField.get(currentData) instanceof Integer &&
                                currentField.getName().equals("workingTime")) {
                            content.append(this.createDetailUpdatedContent(
                                    Utils.getWorkingTime(Integer.parseInt(oldField.get(oldData).toString())),
                                    Utils.getWorkingTime(Integer.parseInt(currentField.get(currentData).toString()))));
                        } else if (currentField.get(currentData) instanceof Long &&
                                currentField.getName().equals("parentId")) {
                            if (currentData instanceof Department) {
                                Department currentDepartment = departmentMapper.toEntity(
                                        departmentService.findById(Long.parseLong(currentField.get(currentData).toString())));
                                Department oldDepartment = departmentMapper.toEntity(
                                        departmentService.findById(Long.parseLong(oldField.get(oldData).toString())));
                                if (!currentDepartment.getId().equals(oldDepartment.getId())) {
                                    content.append(this.createDetailUpdatedContent(oldDepartment.getName(), currentDepartment.getName()));
                                }
                            }
                        } else if (currentField.get(currentData) instanceof Instant) {
                            String currentDate = DateUtils.convertFromInstantToString(
                                    Instant.parse(currentField.get(currentData).toString()));
                            String oldDate = DateUtils.convertFromInstantToString(
                                    Instant.parse(oldField.get(oldData).toString()));
                            content.append(this.createDetailUpdatedContent(oldDate, currentDate));
                        } else if (currentField.get(currentData) instanceof Integer &&
                                currentField.getName().equals("gender")) {
                            content.append(this.createDetailUpdatedContent(
                                    Utils.getGenderName(oldField.get(oldData).toString()),
                                    Utils.getGenderName(currentField.get(currentData).toString())));
                        } else if (currentField.get(currentData) instanceof Integer &&
                                currentField.getName().equals("status")) {
                            if (currentData instanceof Feedback || currentData instanceof DoctorFeedback) {
                                content.append(this.createDetailUpdatedContent(
                                        Utils.getFeedbackStatusName(Integer.parseInt(oldField.get(oldData).toString())),
                                        Utils.getFeedbackStatusName(Integer.parseInt(currentField.get(currentData).toString()))));
                            } else {
                                content.append(this.createDetailUpdatedContent(
                                        Utils.getStatusName(Integer.parseInt(oldField.get(oldData).toString())),
                                        Utils.getStatusName(Integer.parseInt(currentField.get(currentData).toString()))));
                            }
                        } else {
                            content.append(this.createDetailUpdatedContent(String.valueOf(oldField.get(oldData)),
                                    String.valueOf(currentField.get(currentData))));
                        }
                    }
                }
            } catch (IllegalAccessException e) {

            } catch (NullPointerException e) {
                log.error("Error: ", e);
            }
        }
        return content.toString();
    }

    private String createDetailUpdatedContent(String oldValue, String newValue) {
        StringBuilder content = new StringBuilder();
        content.append(PREV_INFORMATION_MESSAGE)
                .append(oldValue)
                .append(NEXT_INFORMATION_MESSAGE)
                .append(newValue)
                .append("\n");
        return content.toString();
    }
}
