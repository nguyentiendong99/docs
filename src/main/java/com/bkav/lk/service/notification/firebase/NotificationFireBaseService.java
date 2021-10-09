package com.bkav.lk.service.notification.firebase;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.domain.MedicationReminder;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.PatientRecordRepository;
import com.bkav.lk.service.DeviceService;
import com.bkav.lk.service.NotificationService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

@Service
@Transactional
public class NotificationFireBaseService {
    /*private final DeviceService deviceService;

    private final NotificationService notificationService;

    private final ObjectMapper objectMapper;

    private final FCMService fcmService;

    private final PatientRecordRepository patientRecordRepository;

    public NotificationFireBaseService(DeviceService deviceService, @Lazy NotificationService notificationService, ObjectMapper objectMapper, FCMService fcmService, PatientRecordRepository patientRecordRepository) {
        this.deviceService = deviceService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.fcmService = fcmService;
        this.patientRecordRepository = patientRecordRepository;
    }

    private void pushNotification(String template,
                                       Map<String, String> data,
                                       Object[] paramTitle, Object[] paramBody,
                                       List<DeviceDTO> devicesDTO) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle(getStringFromBundle(template + ".title", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, paramTitle));
        fcmNotificationRequest.setBody(getStringFromBundle(template + ".body", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, paramBody));
        fcmNotificationRequest.setData(data);
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
    }

    private String getStringFromBundle(String key, String lang, Object[] params) {
        Locale locale = new Locale(lang);
        ResourceBundle messages = ResourceBundle.getBundle("i18n/messages", locale);
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);
        formatter.applyPattern(messages.getString(key));
        return formatter.format(params);
    }

    public void pushNotiApprove(List<NotifyDoctorAppointmentDTO> listNotify) {
        if (!listNotify.isEmpty()) {
            FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
            fcmNotificationRequest.setTitle("THÔNG BÁO LỊCH KHÁM - LỊCH KHÁM ĐƯỢC DUYỆT");
            for (NotifyDoctorAppointmentDTO notifyDTO : listNotify) {
                Map<String, String> data = new HashMap<>();
                data.put("object_id", String.valueOf(notifyDTO.getIdDoctorAppointment()));
                data.put("type", Constants.NOTIFICATION_TYPE.DOCTOR_APPOINTMENT);
                String body = "Bạn có lịch đặt khám với bác sĩ: " + notifyDTO.getDoctorName()
                        + ". Địa chỉ: " + notifyDTO.getClinicName()
                        + ". Bệnh viện: " + notifyDTO.getHealthFacilitiesName()
                        + ". Thời gian: " + DateUtils.convertFromInstantToString(notifyDTO.getStartTime())
                        + ". " + DateUtils.convertFromInstantToHour(notifyDTO.getStartTime()) + " - " + DateUtils.convertFromInstantToHour(notifyDTO.getEndTime())
                        + " " + DateUtils.friendlyTimeOfDayFormat(notifyDTO.getStartTime()) + " " + DateUtils.friendlyDateFormat(notifyDTO.getStartTime()).toLowerCase()
                        + ". Mã đặt lịch: " + notifyDTO.getBookingCode();
                fcmNotificationRequest.setBody(body);
                Map<String, String> data = new HashMap<>();
                data.put("object_id", String.valueOf(notifyDTO.getIdDoctorAppointment()));
                data.put("notificationType", String.valueOf(Constants.NotificationConstants.SUCCESS.code));
                data.put("type", Constants.NOTIFICATION_TYPE.DOCTOR_APPOINTMENT);
                fcmNotificationRequest.setData(data);
                List<DeviceDTO> devicesDTO = deviceService.findByUserId(notifyDTO.getUserId());
                notificationService.save(notifyDTO, Constants.NotificationConstants.SUCCESS.value, Constants.NotificationConstants.SUCCESS.name());
                for (DeviceDTO deviceDTO : devicesDTO) {
                    fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
                    fcmService.sendToDevice(fcmNotificationRequest);
                }
            }
        }
    }

    public void pushNotiDeny(List<NotifyDoctorAppointmentDTO> listNotify) {
        if (!listNotify.isEmpty()) {
            FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
            fcmNotificationRequest.setTitle("THÔNG BÁO LỊCH KHÁM - LỊCH KHÁM BỊ TỪ CHỐI");
            for (NotifyDoctorAppointmentDTO notifyDTO : listNotify) {
                String body = "Mã đặt lịch " + notifyDTO.getBookingCode() + " bị từ chối";
                if (notifyDTO.getRejectReason() != null && !notifyDTO.getRejectReason().equals("")) {
                    body = body + " với lý do: " + notifyDTO.getRejectReason();
                }
                fcmNotificationRequest.setBody(body);
                Map<String, String> data = new HashMap<>();
                data.put("bookingCode", notifyDTO.getBookingCode());
                data.put("doctorAppointmentId", String.valueOf(notifyDTO.getIdDoctorAppointment()));
                data.put("notificationType", String.valueOf(Constants.NotificationConstants.REJECT.code));
                data.put("type", Constants.NOTIFICATION_TYPE.DOCTOR_APPOINTMENT);
                fcmNotificationRequest.setData(data);
                List<DeviceDTO> devicesDTO = deviceService.findByUserId(notifyDTO.getUserId());
                notificationService.save(notifyDTO, Constants.NotificationConstants.REJECT.value, Constants.NotificationConstants.REJECT.name());
                for (DeviceDTO deviceDTO : devicesDTO) {
                    fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
                    fcmService.sendToDevice(fcmNotificationRequest);
                }
            }
        }
    }

    public void pushNotiChange(List<NotifyDoctorAppointmentDTO> listNotify) {
        if (!listNotify.isEmpty()) {
            FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
            fcmNotificationRequest.setTitle("THÔNG BÁO LỊCH KHÁM - LỊCH KHÁM THAY ĐỔI");
            for (NotifyDoctorAppointmentDTO notifyDTO : listNotify) {
                String body = "Mã đặt lịch " + notifyDTO.getBookingCode() + " được thay đổi";
                if (notifyDTO.getChangeAppointmentReason() != null && !notifyDTO.getChangeAppointmentReason().equals("")) {
                    body = body + " với lý do: " + notifyDTO.getChangeAppointmentReason() + ". ";
                }
                body = body + "Thời gian khám mới " + DateUtils.convertFromInstantToHour(notifyDTO.getStartTime())
                        + " - " + DateUtils.convertFromInstantToHour(notifyDTO.getEndTime()) + " " + DateUtils.convertFromInstantToString(notifyDTO.getStartTime())
                        + " " + DateUtils.friendlyTimeOfDayFormat(notifyDTO.getStartTime()) + " " + DateUtils.friendlyDateFormat(notifyDTO.getStartTime()).toLowerCase()
                        + ". Địa chỉ: " + notifyDTO.getClinicName()
                        + ". Bệnh viện: " + notifyDTO.getHealthFacilitiesName()
                        + ". Mã đặt lịch: " + notifyDTO.getBookingCode();
                fcmNotificationRequest.setBody(body);
                Map<String, String> data = new HashMap<>();
                data.put("bookingCode", notifyDTO.getBookingCode());
                data.put("doctorAppointmentId", String.valueOf(notifyDTO.getIdDoctorAppointment()));
                data.put("notificationType", String.valueOf(Constants.NotificationConstants.CHANGE_SCHEDULE.code));
                data.put("type", Constants.NOTIFICATION_TYPE.DOCTOR_APPOINTMENT);
                fcmNotificationRequest.setData(data);
                List<DeviceDTO> devicesDTO = deviceService.findByUserId(notifyDTO.getUserId());
                notificationService.save(notifyDTO, Constants.NotificationConstants.CHANGE_SCHEDULE.value, Constants.NotificationConstants.CHANGE_SCHEDULE.name());
                for (DeviceDTO deviceDTO : devicesDTO) {
                    fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
                    fcmService.sendToDevice(fcmNotificationRequest);
                }
            }
        }
    }

    public void pushNotificationReminder(List<DoctorAppointmentDTO> listNotify) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle("THÔNG BÁO NHẮC LỊCH KHÁM");
        for (DoctorAppointmentDTO dto : listNotify) {
            try {
                String body = "Bạn có lịch đặt khám với bác sĩ: " + dto.getDoctorName()
                        + ". Địa chỉ: " + dto.getClinicName()
                        + ". Thời gian: " + DateUtils.convertFromInstantToString(dto.getStartTime())
                        + ". " + DateUtils.convertFromInstantToHour(dto.getStartTime()) + " - " + DateUtils.convertFromInstantToHour(dto.getEndTime())
                        + " " + DateUtils.friendlyTimeOfDayFormat(dto.getStartTime()) + " " + DateUtils.friendlyDateFormat(dto.getStartTime()).toLowerCase()
                        + ". Mã đặt lịch: " + dto.getBookingCode();
                fcmNotificationRequest.setBody(body);
                Map<String, String> data = new HashMap<>();
                data.put("bookingCode", dto.getBookingCode());
                data.put("appointment", objectMapper.writeValueAsString(dto));
                data.put("doctorAppointmentId", String.valueOf(dto.getId()));
                data.put("notificationType", String.valueOf(Constants.NotificationConstants.REMINDER_SCHEDULE.code));
                data.put("type", Constants.NOTIFICATION_TYPE.REMINDER_APPOINTMENT);
                fcmNotificationRequest.setData(data);
                Optional<PatientRecord> patientRecord = patientRecordRepository.findById(dto.getPatientRecordId());
                if (patientRecord.isPresent()) {
                    List<DeviceDTO> devicesDTO = deviceService.findByUserId(patientRecord.get().getUserId());
                    NotifyDoctorAppointmentDTO appointmentDTO = new NotifyDoctorAppointmentDTO();
                    appointmentDTO.setBookingCode(dto.getBookingCode());
                    appointmentDTO.setUserId(patientRecord.get().getUserId());
                    appointmentDTO.setDoctorName(dto.getDoctorName());
                    appointmentDTO.setClinicName(dto.getClinicName());
                    appointmentDTO.setRejectReason(dto.getRejectReason());
                    appointmentDTO.setStartTime(dto.getStartTime());
                    appointmentDTO.setEndTime(dto.getEndTime());
                    appointmentDTO.setIdDoctorAppointment(dto.getId());
                    appointmentDTO.setChangeAppointmentReason(dto.getChangeAppointmentReason());
                    appointmentDTO.setFriendlyDateTimeFormat(
                            DateUtils.friendlyTimeOfDayFormat(appointmentDTO.getStartTime())
                                    + " " + DateUtils.friendlyDateFormat(appointmentDTO.getStartTime()).toLowerCase());
                    notificationService.save(appointmentDTO, Constants.NotificationConstants.REMINDER_SCHEDULE.value, Constants.NotificationConstants.REMINDER_SCHEDULE.name());
                    for (DeviceDTO deviceDTO : devicesDTO) {
                        fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
                        fcmService.sendToDevice(fcmNotificationRequest);
                    }
                }
            } catch (JsonProcessingException ignored) {
            }
        }
    }

    public void pushNotificationMedicationReminder(List<MedicationReminder> reminderList) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle("Thông báo nhắc lịch uống thuốc");
        for (MedicationReminder reminder : reminderList) {
            Map<String, String> data = new HashMap<>();
            String body = "Đã đến giờ uống thuốc. Bạn đừng quên uống thuốc nhé !";
            data.put("bookingCode", reminder.getBookingCode());
            data.put("notificationType", String.valueOf(Constants.NotificationConstants.MEDICATION_REMINDER.code));
            data.put("type", Constants.NOTIFICATION_TYPE.MEDICATION_REMINDER);
            fcmNotificationRequest.setBody(body);
            fcmNotificationRequest.setData(data);
            List<DeviceDTO> devicesDTO = deviceService.findByUserId(reminder.getUserId());
            notificationService.saveMedicationReminder(reminder, Constants.NotificationConstants.MEDICATION_REMINDER.value, Constants.NotificationConstants.MEDICATION_REMINDER.name());
            for (DeviceDTO deviceDTO : devicesDTO) {
                fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
                fcmService.sendToDevice(fcmNotificationRequest);
            }
        }
    }

    public void pushFeedbackDoctor(DoctorFeedbackDTO feedbackDTO) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle("THÔNG BÁO PHẢN HỒI ĐÁNH GIÁ BÁC SỸ");
        String body = "Đã phản hồi đánh giá của bạn"
                + ". Nội dung: " + feedbackDTO.getFeedbackContent()
                + ". Thời gian: " + DateUtils.convertFromInstantToString(feedbackDTO.getCreatedDate());
        fcmNotificationRequest.setBody(body);
        Map<String, String> data = new HashMap<>();
        data.put("contentFeedback", feedbackDTO.getFeedbackContent());
        try {
            data.put("feedbackDoctor", objectMapper.writeValueAsString(feedbackDTO));
        } catch (JsonProcessingException e) {
            data.put("feedbackDoctor", null);
        }
        data.put("notificationType", String.valueOf(Constants.NotificationConstants.FEEDBACK_DOCTOR.code));
        data.put("type", Constants.NOTIFICATION_TYPE.FEEDBACK_DOCTOR);
        fcmNotificationRequest.setData(data);
        List<DeviceDTO> devicesDTO = deviceService.findByUserId(feedbackDTO.getUserId());
        notificationService.saveFeedbackDoctor(feedbackDTO, Constants.NotificationConstants.FEEDBACK_DOCTOR.value, Constants.NotificationConstants.FEEDBACK_DOCTOR.name());
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
    }

    public void pushFeedback(FeedbackDTO feedbackDTO, String contentFeedback) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle("THÔNG BÁO PHẢN HỒI Ý KIẾN ĐÓNG GÓP");
        String body = feedbackDTO.getProcessingUnitName() + " đã phản hồi ý kiến của bạn"
                + ". Nội dung: " + contentFeedback
                + ". Thời gian: " + DateUtils.convertFromInstantToString(feedbackDTO.getCreatedDate());
        fcmNotificationRequest.setBody(body);
        Map<String, String> data = new HashMap<>();
        data.put("contentFeedback", contentFeedback);
        try {
            feedbackDTO.setContentFeedback(contentFeedback);
            data.put("feedback", objectMapper.writeValueAsString(feedbackDTO));
        } catch (JsonProcessingException e) {
            data.put("feedback", null);
        }
        data.put("notificationType", String.valueOf(Constants.NotificationConstants.FEEDBACK.code));
        data.put("type", Constants.NOTIFICATION_TYPE.FEEDBACK);
        fcmNotificationRequest.setData(data);
        List<DeviceDTO> devicesDTO = deviceService.findByUserId(feedbackDTO.getUserId());
        notificationService.saveFeedback(feedbackDTO, Constants.NotificationConstants.FEEDBACK.value, Constants.NotificationConstants.FEEDBACK.name());
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
    }

    public void pushNotiCreateFeedback(FeedbackDTO feedbackDTO) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        List<DeviceDTO> devicesDTO = deviceService.findByUserId(feedbackDTO.getUserId());
        fcmNotificationRequest.setTitle("Thông báo");
        fcmNotificationRequest.setBody("Cảm ơn bạn đã gửi góp ý cho chúng tôi Chúng tôi sẽ xử lý trong thời gian sớm nhất");
        Map<String, String> data = new HashMap<>();
        data.put("feedbackId", String.valueOf(feedbackDTO.getId()));
        data.put("userId", String.valueOf(feedbackDTO.getUserId()));
        fcmNotificationRequest.setData(data);
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
    }

    public void pushNotiSubclinicalResult(SubclinicalDTO subclinicalDTO) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        Optional<PatientRecord> optional = patientRecordRepository.findById(subclinicalDTO.getPatientRecordId());
        if (!optional.isPresent()) {
            return;
        }
        PatientRecord patientRecord = optional.get();
        List<DeviceDTO> devicesDTO = deviceService.findByUserId(patientRecord.getUserId());

        fcmNotificationRequest.setTitle("Thông báo bệnh nhân lấy Kết quả Cận lâm sàng");
        String body = "Bạn đã có kết quả khám Cận lâm sàng "
                + ". Mã dịch vụ cận lâm sàng: " + subclinicalDTO.getCode()
                + ". Tên dịch vụ cận lâm sàng: " + subclinicalDTO.getName()
                + ". Kỹ thuật viên: " + subclinicalDTO.getTechnician()
                + ". Phòng thực hiện: " + subclinicalDTO.getRoom();
        fcmNotificationRequest.setBody(body);
        Map<String, String> data = new HashMap<>();
        data.put("subclinicalResultId", String.valueOf(subclinicalDTO.getId()));
        data.put("notificationType", String.valueOf(Constants.NotificationConstants.SUBCLINICAL_RESULT.code));
        data.put("type", Constants.NOTIFICATION_TYPE.SUBCLINICAL_RESULT);
        data.put("userId", String.valueOf(patientRecord.getUserId()));
        fcmNotificationRequest.setData(data);
        notificationService.saveSubclinicalResult(subclinicalDTO, patientRecord.getUserId(),
                Constants.NotificationConstants.SUBCLINICAL_RESULT.value, Constants.NotificationConstants.SUBCLINICAL_RESULT.name());
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
    }*/
}
