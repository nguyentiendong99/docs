package com.bkav.lk.web.rest;

import com.bkav.lk.domain.SystemNotification;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.SystemNotificationMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SystemNotificationResource {

    private static final Logger log = LoggerFactory.getLogger(SystemNotificationResource.class);

    @Value("${spring.application.name}")
    private String applicationName;

    private static final String ENTITY_NAME = "System Notification";

    private final SystemNotificationService systemNotificationService;

    private final PatientRecordService patientRecordService;

    private final UserService userService;

    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    private final ActivityLogService activityLogService;

    private final NotificationService notificationService;

    private final SystemNotificationMapper systemNotificationMapper;

    private final ObjectMapper objectMapper;

    public SystemNotificationResource(SystemNotificationService systemNotificationService, PatientRecordService patientRecordService,
                                      UserService userService, DoctorAppointmentConfigurationService doctorAppointmentConfigurationService,
                                      SystemNotificationMapper systemNotificationMapper, ActivityLogService activityLogService,
                                      NotificationService notificationService, ObjectMapper objectMapper) {
        this.systemNotificationService = systemNotificationService;
        this.patientRecordService = patientRecordService;
        this.userService = userService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
        this.systemNotificationMapper = systemNotificationMapper;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/system-notifications")
    public ResponseEntity<List<SystemNotificationDTO>> search(@RequestHeader(name = "healthFacilityId") Long healthFacilityId
            , @RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        queryParams.set("healthFacilityId", healthFacilityId.toString());
        Page<SystemNotificationDTO> result = systemNotificationService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), result);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @GetMapping("/system-notifications/mobile")
    public ResponseEntity<List<SystemNotificationDTO>> searchForMobile() {
        List<SystemNotificationDTO> list = systemNotificationService.searchForMobile();
        return ResponseEntity.ok().body(list);
    }

    @PostMapping("/system-notifications/{type}")
    public ResponseEntity<SystemNotificationDTO> create(
            @RequestHeader(name = "healthFacilityId") Long healthFacilityId,
            @RequestBody SystemNotificationDTO systemNotificationDTO,
            @PathVariable Integer type) {
        if (systemNotificationDTO.getId() != null) {
            throw new BadRequestAlertException("A new System Notification cannot already have an ID",
                    ENTITY_NAME, "idexists");
        }
        // TODO:
        if (type.intValue() == Constants.SYS_NOTI_CREATE_TYPE.DEMO) {
            systemNotificationDTO.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.DEMO);
        }
        if (type.intValue() == Constants.SYS_NOTI_CREATE_TYPE.COMPLETE) {
            DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOneByHealthFacilitiesId(healthFacilityId);
            if (config != null && config.getHealthFacilitiesId() != 0 && config.getNotiApproveAuto() != null
                    && config.getNotiApproveAuto().equals(Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC)) {
                systemNotificationDTO.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED);
            } else {
                systemNotificationDTO.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE);
            }
        }

        systemNotificationDTO.setHealthFacilityId(healthFacilityId);
        systemNotificationDTO.setCode(systemNotificationService.generateSystemNotificationCode(systemNotificationDTO.getTitle()));
        SystemNotification result = systemNotificationService.save(systemNotificationDTO);

        // gui thong bao cho user neu trang thai la thong bao khi duyet
        if (Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED.equals(result.getStatus())
                && Constants.SYS_NOTI_STYLE.AFTER_APPROVE.equals(result.getNotiStyle())) {
            sendSystemNotification(result);
            result.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED);
            result = systemNotificationService.save(systemNotificationMapper.toDto(result));
        }

        activityLogService.create(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, result);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert
                        ("", true, ENTITY_NAME, result.getId().toString()))
                .body(systemNotificationMapper.toDto(result));
    }

    @PutMapping("/system-notifications")
    public ResponseEntity<SystemNotificationDTO> update(
            @RequestHeader(name = "healthFacilityId") Long healthFacilityId,
            @RequestBody SystemNotificationDTO systemNotificationDTO) {
        if (systemNotificationDTO.getId() == null) {
            throw new BadRequestAlertException("System Notification has empty id",
                    ENTITY_NAME, "idnull");
        }
        // kiểm tra user và trạng thái của bản ghi cần update có đủ điều kiện không
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestAlertException("Empty current user",
                    ENTITY_NAME, "usernull");
        }
        if (!systemNotificationService.checkUpdateCondition(currentUser.getLogin(), systemNotificationDTO)) {
            throw new BadRequestAlertException("Not suitable condition to update",
                    ENTITY_NAME, "unsuitable_condition");
        }
        if (systemNotificationDTO.getStatus().equals(Constants.SYSTEM_NOTIFICATION_STATUS.DENY)) {
            systemNotificationDTO.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE);
        }
        if (systemNotificationDTO.getStatus().equals(Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE)) {
            DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOneByHealthFacilitiesId(healthFacilityId);
            if (config != null && config.getHealthFacilitiesId() != 0 && config.getNotiApproveAuto() != null
            && config.getNotiApproveAuto().equals(Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC)) {
                if(systemNotificationDTO.getNotiStyle().equals(Constants.SYS_NOTI_STYLE.AFTER_APPROVE)){
                    systemNotificationDTO.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED);
                    sendSystemNotification(systemNotificationMapper.toEntity(systemNotificationDTO));
                }else{
                    systemNotificationDTO.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED);
                }
            }
        }
        SystemNotification systemNotificationDTO1 = systemNotificationService.findById(systemNotificationDTO.getId());
        if(systemNotificationDTO1 != null){
            if (!systemNotificationDTO.getCode().equals(systemNotificationDTO1.getCode()) || !systemNotificationDTO.getTitle().equals(systemNotificationDTO1.getTitle())) {
                systemNotificationDTO.setCode(systemNotificationService.generateSystemNotificationCode(systemNotificationDTO.getTitle()));
            }
        }
        // update system notification
        SystemNotification oldData = systemNotificationService.findById(systemNotificationDTO.getId());
        SystemNotification newData = systemNotificationService.save(systemNotificationDTO);
        activityLogService.update(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, oldData, newData);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                String.valueOf(newData.getId()));
        return ResponseEntity.ok().headers(headers).body(systemNotificationMapper.toDto(newData));
    }

    @PutMapping("/system-notifications/approve")
    public ResponseEntity<SystemNotificationDTO> approve(
            @RequestBody Map<String, String> queryParams) {
        String strId = null;
        if (queryParams.containsKey("id") && !StrUtil.isBlank(queryParams.get("id"))) {
            strId = queryParams.get("id");
        }
        if (StrUtil.isBlank(strId)) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        long id = Long.valueOf(strId);

        // kiểm tra user và trạng thái của bản ghi cần approve có đủ điều kiện không
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestAlertException("Empty current user",
                    ENTITY_NAME, "usernull");
        }

        SystemNotification entity = systemNotificationService.findById(id);
        if (!systemNotificationService.checkApproveCondition(currentUser.getLogin(), systemNotificationMapper.toDto(entity))) {
            throw new BadRequestAlertException("Not suitable condition to approve",
                    ENTITY_NAME, "unsuitable_condition");
        }

        SystemNotification result = systemNotificationService.approve(id);

        // gui thong bao cho user neu trang thai la thong bao khi duyet
        if (result.getNotiStyle().equals(Constants.SYS_NOTI_STYLE.AFTER_APPROVE)) {
            sendSystemNotification(result);
        }

        activityLogService.approve(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, entity);
        return ResponseEntity.ok().body(systemNotificationMapper.toDto(result));
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

    @PutMapping("/system-notifications/deny")
    public ResponseEntity<SystemNotificationDTO> deny(
            @RequestBody Map<String, String> queryParams) {
        String strId = null;
        String rejectReason = "";
        if (queryParams.containsKey("id") && !StrUtil.isBlank(queryParams.get("id"))) {
            strId = queryParams.get("id");
        }
        if (StrUtil.isBlank(strId)) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (queryParams.containsKey("rejectReason")) {
            rejectReason = queryParams.get("rejectReason");
        }

        long id = Long.valueOf(strId);

        // kiểm tra user và trạng thái của bản ghi để từ chối có đủ điều kiện không
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestAlertException("Empty current user",
                    ENTITY_NAME, "usernull");
        }

        SystemNotification entity = systemNotificationService.findById(id);
        if (!systemNotificationService.checkApproveCondition(currentUser.getLogin(), systemNotificationMapper.toDto(entity))) {
            throw new BadRequestAlertException("Not suitable condition to deny",
                    ENTITY_NAME, "unsuitable_condition");
        }
        SystemNotification result = systemNotificationService.deny(id, rejectReason);
        activityLogService.deny(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, entity);
        return ResponseEntity.ok().body(systemNotificationMapper.toDto(result));
    }

    @PutMapping("/system-notifications/{id}/retrieve")
    public ResponseEntity<SystemNotificationDTO> retrieve(
            @PathVariable Long id) {
        // kiểm tra user và trạng thái của bản ghi cần thu hồi có đủ điều kiện không
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestAlertException("Empty current user",
                    ENTITY_NAME, "usernull");
        }
        SystemNotification entity = systemNotificationService.findById(id);
        if (!systemNotificationService.checkRetrieveCondition(currentUser.getLogin(), systemNotificationMapper.toDto(entity))) {
            throw new BadRequestAlertException("Not suitable condition to retrieve",
                    ENTITY_NAME, "unsuitable_condition");
        }
        SystemNotification result = systemNotificationService.retrieve(id);
        activityLogService.retrieve(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, entity);
        return ResponseEntity.ok().body(systemNotificationMapper.toDto(result));
    }

    @PutMapping("/system-notifications/{id}/delete")
    public ResponseEntity<SystemNotificationDTO> delete(
            @PathVariable Long id) {
        //  kiểm tra user và trạng thái của bản ghi cần xóa có đủ điều kiện không
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestAlertException("Empty current user",
                    ENTITY_NAME, "usernull");
        }
        SystemNotification entity = systemNotificationService.findById(id);
        if (!systemNotificationService.checkDeleteCondition(currentUser.getLogin(), systemNotificationMapper.toDto(entity))) {
            throw new BadRequestAlertException("Not suitable condition to delete",
                    ENTITY_NAME, "unsuitable_condition");
        }
        SystemNotification result = systemNotificationService.delete(id);
        activityLogService.delete(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, entity);
        return ResponseEntity.ok().body(systemNotificationMapper.toDto(result));
    }

    @PutMapping("/system-notifications/{id}/cancel")
    public ResponseEntity<SystemNotificationDTO> cancel(
            @PathVariable Long id) {
        //  kiểm tra user và trạng thái của bản ghi cần hủy có đủ điều kiện không
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestAlertException("Empty current user",
                    ENTITY_NAME, "usernull");
        }
        SystemNotification entity = systemNotificationService.findById(id);
        if (!systemNotificationService.checkCancelCondition(currentUser.getLogin(), systemNotificationMapper.toDto(entity))) {
            throw new BadRequestAlertException("Not suitable condition to cancel",
                    ENTITY_NAME, "unsuitable_condition");
        }
        SystemNotification result = systemNotificationService.cancel(id);
        activityLogService.cancel(Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION, entity);
        return ResponseEntity.ok().body(systemNotificationMapper.toDto(result));
    }

    @GetMapping("/system-notifications/{id}/history")
    public ResponseEntity<List<SystemNotificationHistoryDTO>> history(
            @PathVariable Long id) {
        List<SystemNotificationHistoryDTO> list = systemNotificationService.getHistory(id);
        return ResponseEntity.ok().body(list);
    }

}
