package com.bkav.lk.service.impl;

import com.bkav.lk.config.SchedulingTaskProperties;
import com.bkav.lk.domain.*;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.SubclinicalRepository;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.SubclinicalMapper;
import com.bkav.lk.service.mapper.SystemNotificationMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulingJobServiceImpl implements SchedulingJobService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingJobService.class);

    @Value("${appointment.config.max-day-pending:30}")
    private Integer maxDayConfigPending;

    @Value("${scheduling.doctor-appointment-reminder-notification.time-default}")
    private String timeConfigDefault;

    private final SchedulingTaskProperties schedulingTaskProperties;

    private final DoctorAppointmentService doctorAppointmentService;

    private final ConfigService configService;

    private final NotificationService notificationService;

    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    private final SubclinicalRepository subclinicalRepository;

    private final FeedbackService feedbackService;

    private final SubclinicalMapper subclinicalMapper;

    private final UserService userService;

    private final PatientRecordService patientRecordService;

    private final SystemNotificationService systemNotificationService;

    private final ObjectMapper objectMapper;

    private final SystemNotificationMapper systemNotificationMapper;

    private final AppointmentCancelConfigService appointmentCancelConfigService;

    public SchedulingJobServiceImpl(SchedulingTaskProperties schedulingTaskProperties, DoctorAppointmentService doctorAppointmentService,
                                    ConfigService configService, NotificationService notificationService, DoctorAppointmentConfigurationService doctorAppointmentConfigurationService,
                                    SubclinicalRepository subclinicalRepository, FeedbackService feedbackService,
                                    SubclinicalMapper subclinicalMapper, UserService userService, PatientRecordService patientRecordService,
                                    SystemNotificationService systemNotificationService, ObjectMapper objectMapper, SystemNotificationMapper systemNotificationMapper, AppointmentCancelConfigService appointmentCancelConfigService) {
        this.schedulingTaskProperties = schedulingTaskProperties;
        this.doctorAppointmentService = doctorAppointmentService;
        this.configService = configService;
        this.notificationService = notificationService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
        this.subclinicalRepository = subclinicalRepository;
        this.feedbackService = feedbackService;
        this.subclinicalMapper = subclinicalMapper;
        this.userService = userService;
        this.patientRecordService = patientRecordService;
        this.systemNotificationService = systemNotificationService;
        this.objectMapper = objectMapper;
        this.systemNotificationMapper = systemNotificationMapper;
        this.appointmentCancelConfigService = appointmentCancelConfigService;
    }

    @Scheduled(cron = "${scheduling.doctor-appointment.time-cron}")
    @Override
    public void schedulingDoctorAppointment() {
        if (schedulingTaskProperties.getDoctorAppointmentEnable()) {
            doctorAppointmentService.schedulingDoctorAppointmentJob();
        }
    }

    /*@Scheduled(cron = "${scheduling.notification-reminder.time-cron}")
    @Override
    public void schedulingNotificationReminder() {
        if(schedulingTaskProperties.getNotificationReminderEnable()) {
            notificationService.findAllBeforeADays();
        }
    }

    @Scheduled(cron = "${scheduling.medication-reminder.time-cron}")
    @Override
    public void schedulingMedicationReminder() {
        if (schedulingTaskProperties.getMedicationReminderEnable()) {
            notificationService.schedulingPushMedicationReminderJob();
        }
    }*/

    @Scheduled(cron = "${scheduling.feedback-reminder.time-cron}")
    @Override
    public void schedulingFeedbackReminder() {
        if (schedulingTaskProperties.getFeedbackReminderEnable()) {
            feedbackService.autoChangeStatusEndTime();
        }
    }

    @Scheduled(cron = "${scheduling.appointment-cancel-log.refresh-daily.time-cron}")
    public void schedulingRefreshDayPermission() {
        if (schedulingTaskProperties.getAppointmentCancelLogRefreshDailyEnable()) {
            Config config = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_DAY);
            List<AppointmentCancelLog> accs = appointmentCancelConfigService.findAll();
            accs.stream().filter(o -> o.getMaxDayCanceled() > 0 && o.getMaxWeekCanceled() > 0).forEach(o -> {
                o.setMaxDayCanceled(Integer.parseInt(config.getPropertyValue()));
            });
            appointmentCancelConfigService.updateAll(accs);
        }
    }

    @Scheduled(cron = "${scheduling.appointment-cancel-log.refresh-weekly.time-cron}")
    public void schedulingRefreshWeekPermission() {
        if (schedulingTaskProperties.getAppointmentCancelLogRefreshWeeklyEnable()) {
            Config config = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_WEEK);
            List<AppointmentCancelLog> accs = appointmentCancelConfigService.findAll();
            accs.stream().filter(o -> o.getMaxDayCanceled() > 0 && o.getMaxWeekCanceled() > 0).forEach(o -> {
                o.setMaxWeekCanceled(Integer.parseInt(config.getPropertyValue()));
            });
            appointmentCancelConfigService.updateAll(accs);
        }
    }

    @Scheduled(cron = "${scheduling.appointment-cancel-log.check-blocked.time-cron}")
    public void schedulingUnlockedUser() {
        if (schedulingTaskProperties.getAppointmentCancelLogCheckBlockedEnable()) {
            List<AppointmentCancelLog> accs = appointmentCancelConfigService.findAll();
            Config config = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_IN_DAY);
            List<Long> ids = accs.stream()
                    .filter(o -> Constants.BOOL_NUMBER.TRUE.equals(o.getIsBlocked())
                            && Duration.between(o.getStartBlockedDate(), DateUtils.nowInstant()).toDays() >= Integer.parseInt(config.getPropertyValue()))
                    .map(AppointmentCancelLog::getId).collect(Collectors.toList());
            appointmentCancelConfigService.deleteAll(ids);

        }
    }

    @Transactional
    @Scheduled(cron = "${scheduling.doctor-appointment-active-pending-config.time-cron}")
    public void SchedulingActiveNewDoctorAppointmentConfig() {
        if (schedulingTaskProperties.getAppointmentActivePendingConfigEnable()) {
            List<DoctorAppointmentConfigurationDTO> pendingConfigs = doctorAppointmentConfigurationService.findAllPendingConfig();
            pendingConfigs.forEach(o -> {
                int timeLeft = o.getCreatedDate().atZone(DateUtils.getZoneHCM()).plusDays(o.getApplyConfigAfterDay()).getDayOfYear()
                        - ZonedDateTime.now(DateUtils.getZoneHCM()).getDayOfYear();
                if (timeLeft <= 0) {
                    DoctorAppointmentConfigurationDTO activeConfig = doctorAppointmentConfigurationService.findOne(
                            o.getHealthFacilitiesId(), Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);
                    if (Objects.nonNull(activeConfig)) {
                        doctorAppointmentConfigurationService.deleteById(activeConfig.getId());
                    }
                    o.setStatus(Constants.DOCTOR_APPOINTMENT_CONFIG_STATUS.ACTIVE);
                    doctorAppointmentConfigurationService.save(o);
                }
            });
        }
    }

    @Scheduled(cron = "${scheduling.notification-cls-kq.time-cron}")
    public void schedulingNotificationCls() {
        // Get list doctor appointment configuration
        List<DoctorAppointmentConfigurationDTO> listConfig = doctorAppointmentConfigurationService.getListConfig();
        listConfig.forEach(item -> {
            if (item.getTimeConfigSubclinicalResults() == null) {
                item.setTimeConfigSubclinicalResults(1); // default a hour
            }
        });
        Map<Long, Integer> mapConfig = listConfig.stream()
                .collect(Collectors.toMap(DoctorAppointmentConfigurationDTO::getHealthFacilitiesId, DoctorAppointmentConfigurationDTO::getTimeConfigSubclinicalResults));

        // Get list Subclinical with status not notified
        List<Subclinical> listSubclinical = subclinicalRepository.findByStatus(Constants.NOTIFICATION_STATUS.HAVE_NOT_NOTIFIED);
        for (Subclinical item : listSubclinical) {
            Instant createdDate = item.getCreatedDate();
            if (createdDate == null) {
                continue;
            }
            Instant now = Instant.now();
            Duration timeElapsed = Duration.between(createdDate, now);
            long timeElapsedInHour = timeElapsed.toHours();
            int autoNotifyTime = mapConfig.get(item.getHealthFacilityId());

            // Check current date - created date > X hour
            if (timeElapsedInHour >= autoNotifyTime) {

                // change status
                item.setStatus(Constants.NOTIFICATION_STATUS.NOTIFIED);
                subclinicalRepository.save(item);

                // send notification to app user
                SubclinicalDTO subclinicalDTO = subclinicalMapper.toDto(item);
                DoctorAppointmentDTO doctorAppointment = doctorAppointmentService.findByAppointmentCode(item.getDoctorAppointmentCode());
                FirebaseData firebaseData = new FirebaseData();
                firebaseData.setType(String.valueOf(Constants.NotificationConstants.GET_RESULT_CLS.id));
                firebaseData.setObjectId(item.getId().toString());
                try {
                    firebaseData.setObject(objectMapper.writeValueAsString(subclinicalDTO).replaceAll("\\n|\\r", ""));
                } catch (JsonProcessingException e) {
                    log.error("Error: ", e);
                }
                String template = Constants.NotificationConstants.GET_RESULT_CLS.template;

                List<String> paramsBody = new ArrayList<>();
                paramsBody.add(item.getName());
                paramsBody.add(item.getRoom());
                Optional<PatientRecordDTO> optional = patientRecordService.findOne(doctorAppointment.getPatientRecordId());
                if (optional.isPresent()) {
                    User user = userService.findOne(optional.get().getUserId())
                            .orElseThrow(() -> new BadRequestAlertException("Invalid id", null, "id_incorrect"));
                    notificationService.pushNotification(template, firebaseData, null, paramsBody, user.getId());
                }
            }
        }
    }

    @Scheduled(cron = "${scheduling.doctor-appointment-reminder-notification.time-cron}")
    @Override
    public void schedulingPushNotifyDoctorAppointment() {
        if (schedulingTaskProperties.getDoctorAppointmentReminderNotificationEnable()) {
            int hourNow = LocalDateTime.now(DateUtils.getZoneHCM()).getHour();
            String minuteStr = String.valueOf(hourNow * 60); // Vi trong DB dang luu la phut

            // Tim ra duoc danh sach benh vien co config gio ban notification
            // Vi co the 1 Benh vien cau hinh nhieu khung gio ban notify trong 1 ngay
            List<DoctorAppointmentConfigurationDTO> listConfig = doctorAppointmentConfigurationService.getListConfig();
            listConfig.forEach(config -> {
                String[] timeArray = null;
                if (config.getTimeConfig() != null) {
                    timeArray = config.getTimeConfig().split(",");
                } else {
                    timeArray = timeConfigDefault.split(",");
                }
                config.setListTimeConfig(Arrays.asList(timeArray));
            });
            listConfig = listConfig.stream().filter(item -> item.getListTimeConfig().contains(minuteStr)).collect(Collectors.toList());
            if (!listConfig.isEmpty()) {
                doctorAppointmentService.schedulingDoctorAppointmentReminderNotificationJob(listConfig);
            }
        }
    }

    @Scheduled(cron = "${scheduling.system-notification.time-cron}")
    public void schedulingPushSystemNotification(){
        List<SystemNotification> list = new ArrayList<>();
        list.addAll(systemNotificationService.searchForCronJob(Constants.SYS_NOTI_STYLE.ON_DAY));
        list.addAll(systemNotificationService.searchForCronJob(Constants.SYS_NOTI_STYLE.FROM_DAY_TO_DAY));
        for(SystemNotification systemNotification: list){
            sendSystemNotification(systemNotification);
            systemNotification.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED);
            systemNotificationService.save(systemNotificationMapper.toDto(systemNotification));
        }
    }

    private void sendSystemNotification(SystemNotification systemNotification){
        List<UserDTO> users = userService.findByAreaCode(systemNotification.getTargetCode());
        List<Long> userIds = users.stream().map(UserDTO::getId)
                .distinct()
                .collect(Collectors.toList());

        FirebaseData firebaseData = new FirebaseData();
        firebaseData.setObjectId(systemNotification.getId().toString());
        try {
            firebaseData.setObject(objectMapper.writeValueAsString(systemNotification).replaceAll("\\n|\\r", ""));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        firebaseData.setType(String.valueOf(Constants.NotificationConstants.OTHER.id));
        notificationService.pushNotificationNoTemplate(firebaseData, systemNotification.getTitle(), systemNotification.getContent(), userIds);
    }
}
