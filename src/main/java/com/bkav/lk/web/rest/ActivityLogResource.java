package com.bkav.lk.web.rest;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.service.HealthFacilitiesService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.util.LogActivityHelper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.vm.ActivityLogDiaryVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ActivityLogResource {

    private static final String ENTITY_NAME = "activity log";

    private final ActivityLogService activityLogService;
    private final HealthFacilitiesService healthFacilitiesService;
    private final UserService userService;
    private final LogActivityHelper logActivityHelper;
    private final DoctorService doctorService;
    private final ObjectMapper objectMapper;

    public ActivityLogResource(ActivityLogService activityLogService, HealthFacilitiesService healthFacilitiesService, UserService userService, LogActivityHelper logActivityHelper, DoctorService doctorService, ObjectMapper objectMapper) {
        this.activityLogService = activityLogService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.userService = userService;
        this.logActivityHelper = logActivityHelper;
        this.doctorService = doctorService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/activity-log/search")
    public ResponseEntity<List<ActivityLog>> search(@RequestParam MultiValueMap<String, String> queryParam) {
        List<ActivityLog> activityLogs = activityLogService.search(queryParam);
        Integer contentType;
        for (ActivityLog activityLog : activityLogs) {
            StringBuilder content = new StringBuilder();
            Integer actionType = activityLog.getActionType();
            if (Constants.ACTION_TYPE.CREATE.equals(actionType)) {
                content.append("Thêm mới");
            } else if (Constants.ACTION_TYPE.DELETE.equals(actionType)) {
                content.append("Xóa");
            } else if (Constants.ACTION_TYPE.UPDATE.equals(actionType)) {
                content.append("Sửa thông tin\n");
                contentType = activityLog.getContentType();
                if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, DoctorAppointment.class));
                } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, DoctorSchedule.class));
                } else if (Constants.CONTENT_TYPE.CLINIC.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Clinic.class));
                } else if (Constants.CONTENT_TYPE.DOCTOR.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Doctor.class));
                } else if (Constants.CONTENT_TYPE.MEDICAL_SERVICE.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, MedicalService.class));
                } else if (Constants.CONTENT_TYPE.TOPIC.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Topic.class));
                } else if (Constants.CONTENT_TYPE.HEALTH_FACILITY.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, HealthFacilities.class));
                } else if (Constants.CONTENT_TYPE.POSITION.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Position.class));
                } else if (Constants.CONTENT_TYPE.USER.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, User.class));
                } else if (Constants.CONTENT_TYPE.GROUP.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Group.class));
                } else if (Constants.CONTENT_TYPE.DEPARTMENT.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Department.class));
                } else if (Constants.CONTENT_TYPE.MEDICAL_SPECIALITY.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, MedicalSpeciality.class));
                } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, Feedback.class));
                } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, PatientRecord.class));
                } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
                    content.append(logActivityHelper.getUpdateContent(activityLog, DoctorFeedback.class));
                } else {
                    throw new BadRequestAlertException("Unsupported content type", ENTITY_NAME, "content_type.unsupported");
                }
            } else if (Constants.ACTION_TYPE.APPROVE.equals(actionType)) {
                content.append("Xét duyệt");
            } else if (Constants.ACTION_TYPE.DENY.equals(actionType)) {
                content.append("Từ chối");
            } else if (Constants.ACTION_TYPE.WAITING.equals(actionType)) {
                content.append("Chờ xử lý");
            } else if (Constants.ACTION_TYPE.PROCESSING.equals(actionType)) {
                content.append("Đang xử lý");
            } else if (Constants.ACTION_TYPE.DONE.equals(actionType)) {
                content.append("Đã xử lý");
            } else if (Constants.ACTION_TYPE.CONFIRM.equals(actionType)) {
                content.append("Xác nhận");
            } else if (Constants.ACTION_TYPE.CANCEL.equals(actionType)) {
                content.append("Hủy");
            } else {
                throw new BadRequestAlertException("Invalid action type", ENTITY_NAME, "action_type.invalid");
            }
            activityLog.setContent(content.toString());
        }
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/activity-log/diary")
    public ResponseEntity<List<ActivityLogDiaryVM>> diary() {
        MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
        List<Integer> contentTypes = Arrays.asList(
                Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT,
                Constants.CONTENT_TYPE.PATIENT_RECORD,
                Constants.CONTENT_TYPE.FEEDBACK,
                Constants.CONTENT_TYPE.DOCTOR_FEEDBACK
        );
        List<Integer> actionTypes = Arrays.asList(
                Constants.ACTION_TYPE.CREATE,
                Constants.ACTION_TYPE.UPDATE,
                Constants.ACTION_TYPE.DELETE,
                Constants.ACTION_TYPE.APPROVE,
                Constants.ACTION_TYPE.DENY,
                Constants.ACTION_TYPE.CONFIRM,
                Constants.ACTION_TYPE.CANCEL,
                Constants.ACTION_TYPE.WAITING,
                Constants.ACTION_TYPE.PROCESSING,
                Constants.ACTION_TYPE.DONE
        );
        queryParam.set("userId", userService.getUserWithAuthorities().map(user -> String.valueOf(user.getId())).orElse(null));
        queryParam.set("contentTypes", contentTypes.toString().replace("[", "").replace("]", ""));
        queryParam.set("actionTypes", actionTypes.toString().replace("[", "").replace("]", ""));
        //:TODO: Bổ sung nếu có - Nhật ký hoạt động

        List<ActivityLog> activityLogs = activityLogService.search(queryParam);
        List<ActivityLogDiaryVM> logDiary = new ArrayList<>();
        String space = " ";
        if (!activityLogs.isEmpty()) {
            activityLogs.forEach(itemLog -> {
                ActivityLogDiaryVM diaryVM = new ActivityLogDiaryVM();
                String actionType = "";
                String contentType = "";
                String content = "";
                boolean isCreateAppointment = false;
                boolean isWaitingFeedback = false;
                boolean isProcessingFeedback = false;

                //:TODO: setter actionType
                if (Constants.ACTION_TYPE.CREATE.equals(itemLog.getActionType())) {
                    actionType = "Thêm mới";
                    isCreateAppointment = true;
                } else if (Constants.ACTION_TYPE.UPDATE.equals(itemLog.getActionType())) {
                    actionType = "Chỉnh sửa";
                } else if (Constants.ACTION_TYPE.DELETE.equals(itemLog.getActionType())) {
                    actionType = "Xóa";
                } else if (Constants.ACTION_TYPE.CANCEL.equals(itemLog.getActionType())) {
                    actionType = "Hủy lịch";
                } else if (Constants.ACTION_TYPE.CONFIRM.equals(itemLog.getActionType())) {
                    actionType = "Xác nhận chuyển lịch";
                } else if (Constants.ACTION_TYPE.WAITING.equals(itemLog.getActionType())) {
                    actionType = "Thêm mới";
                    isWaitingFeedback = true;
                } else if (Constants.ACTION_TYPE.PROCESSING.equals(itemLog.getActionType())) {
                    actionType = "Phản hồi";
                    isProcessingFeedback = true;
                }

                //:TODO: setter contentType & content
                if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(itemLog.getContentType())) {
                    if (isCreateAppointment) {
                        actionType = "Tạo lịch";
                    }
                    content = logActivityHelper.getContent(itemLog, DoctorAppointment.class).getBookingCode();
                    contentType = "Lịch khám";
                } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(itemLog.getContentType())) {
                    Feedback feedback = logActivityHelper.getContent(itemLog, Feedback.class);
                    HealthFacilitiesDTO healthFacilities = new HealthFacilitiesDTO();
                    if (isWaitingFeedback) {
                        healthFacilities = healthFacilitiesService.findOne(feedback.getFeedbackedUnit().getId()).get();
                        content = healthFacilities.getName();
                    } else if (isProcessingFeedback) {
                        healthFacilities = healthFacilitiesService.findOne(feedback.getProcessingUnit().getId()).get();
                        content = healthFacilities.getName();
                    }
                    contentType = "Ý kiến đóng góp";
                } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(itemLog.getContentType())) {
                    content = "";
                    if (isCreateAppointment) {
                        Long doctorId = logActivityHelper.getContent(itemLog, DoctorFeedback.class).getDoctor().getId();
                        DoctorDTO doctorDTO = doctorService.findByDoctorId(doctorId);
                        if (Objects.nonNull(doctorDTO)) {
                            content = doctorDTO.getName();
                        }
                    } else {
                        content = logActivityHelper.getContent(itemLog, DoctorFeedback.class).getContent();
                    }
                    contentType = "Đánh giá bác sỹ";
                } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(itemLog.getContentType())) {
                    content = logActivityHelper.getContent(itemLog, PatientRecord.class).getPatientRecordCode();
                    contentType = "Hồ sơ bệnh nhân";
                }

                diaryVM.setId(itemLog.getId());
                diaryVM.setActionType(actionType);
                diaryVM.setContentType(contentType);
                diaryVM.setActionTypeValue(itemLog.getActionType());
                diaryVM.setContentTypeValue(itemLog.getContentType());
                diaryVM.setContent(content);
                if (isWaitingFeedback) {
                    diaryVM.setContentFormatter("Bạn đã " + actionType + space + contentType + space + "cho " + content);
                } else if (isProcessingFeedback) {
                    diaryVM.setContentFormatter(content + " đã " + actionType + space + contentType + space + "của bạn");
                } else {
                    diaryVM.setContentFormatter("Bạn đã " + actionType + space + contentType + space + content);
                }
                diaryVM.setCreatedDate(itemLog.getCreatedDate());
                logDiary.add(diaryVM);
            });
        }
        return ResponseEntity.ok().body(logDiary);
    }

}
