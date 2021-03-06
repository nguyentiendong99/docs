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
            fcmNotificationRequest.setTitle("TH??NG B??O L???CH KH??M - L???CH KH??M ???????C DUY???T");
            for (NotifyDoctorAppointmentDTO notifyDTO : listNotify) {
                Map<String, String> data = new HashMap<>();
                data.put("object_id", String.valueOf(notifyDTO.getIdDoctorAppointment()));
                data.put("type", Constants.NOTIFICATION_TYPE.DOCTOR_APPOINTMENT);
                String body = "B???n c?? l???ch ?????t kh??m v???i b??c s??: " + notifyDTO.getDoctorName()
                        + ". ?????a ch???: " + notifyDTO.getClinicName()
                        + ". B???nh vi???n: " + notifyDTO.getHealthFacilitiesName()
                        + ". Th???i gian: " + DateUtils.convertFromInstantToString(notifyDTO.getStartTime())
                        + ". " + DateUtils.convertFromInstantToHour(notifyDTO.getStartTime()) + " - " + DateUtils.convertFromInstantToHour(notifyDTO.getEndTime())
                        + " " + DateUtils.friendlyTimeOfDayFormat(notifyDTO.getStartTime()) + " " + DateUtils.friendlyDateFormat(notifyDTO.getStartTime()).toLowerCase()
                        + ". M?? ?????t l???ch: " + notifyDTO.getBookingCode();
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
            fcmNotificationRequest.setTitle("TH??NG B??O L???CH KH??M - L???CH KH??M B??? T??? CH???I");
            for (NotifyDoctorAppointmentDTO notifyDTO : listNotify) {
                String body = "M?? ?????t l???ch " + notifyDTO.getBookingCode() + " b??? t??? ch???i";
                if (notifyDTO.getRejectReason() != null && !notifyDTO.getRejectReason().equals("")) {
                    body = body + " v???i l?? do: " + notifyDTO.getRejectReason();
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
            fcmNotificationRequest.setTitle("TH??NG B??O L???CH KH??M - L???CH KH??M THAY ?????I");
            for (NotifyDoctorAppointmentDTO notifyDTO : listNotify) {
                String body = "M?? ?????t l???ch " + notifyDTO.getBookingCode() + " ???????c thay ?????i";
                if (notifyDTO.getChangeAppointmentReason() != null && !notifyDTO.getChangeAppointmentReason().equals("")) {
                    body = body + " v???i l?? do: " + notifyDTO.getChangeAppointmentReason() + ". ";
                }
                body = body + "Th???i gian kh??m m???i " + DateUtils.convertFromInstantToHour(notifyDTO.getStartTime())
                        + " - " + DateUtils.convertFromInstantToHour(notifyDTO.getEndTime()) + " " + DateUtils.convertFromInstantToString(notifyDTO.getStartTime())
                        + " " + DateUtils.friendlyTimeOfDayFormat(notifyDTO.getStartTime()) + " " + DateUtils.friendlyDateFormat(notifyDTO.getStartTime()).toLowerCase()
                        + ". ?????a ch???: " + notifyDTO.getClinicName()
                        + ". B???nh vi???n: " + notifyDTO.getHealthFacilitiesName()
                        + ". M?? ?????t l???ch: " + notifyDTO.getBookingCode();
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
        fcmNotificationRequest.setTitle("TH??NG B??O NH???C L???CH KH??M");
        for (DoctorAppointmentDTO dto : listNotify) {
            try {
                String body = "B???n c?? l???ch ?????t kh??m v???i b??c s??: " + dto.getDoctorName()
                        + ". ?????a ch???: " + dto.getClinicName()
                        + ". Th???i gian: " + DateUtils.convertFromInstantToString(dto.getStartTime())
                        + ". " + DateUtils.convertFromInstantToHour(dto.getStartTime()) + " - " + DateUtils.convertFromInstantToHour(dto.getEndTime())
                        + " " + DateUtils.friendlyTimeOfDayFormat(dto.getStartTime()) + " " + DateUtils.friendlyDateFormat(dto.getStartTime()).toLowerCase()
                        + ". M?? ?????t l???ch: " + dto.getBookingCode();
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
        fcmNotificationRequest.setTitle("Th??ng b??o nh???c l???ch u???ng thu???c");
        for (MedicationReminder reminder : reminderList) {
            Map<String, String> data = new HashMap<>();
            String body = "???? ?????n gi??? u???ng thu???c. B???n ?????ng qu??n u???ng thu???c nh?? !";
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
        fcmNotificationRequest.setTitle("TH??NG B??O PH???N H???I ????NH GI?? B??C S???");
        String body = "???? ph???n h???i ????nh gi?? c???a b???n"
                + ". N???i dung: " + feedbackDTO.getFeedbackContent()
                + ". Th???i gian: " + DateUtils.convertFromInstantToString(feedbackDTO.getCreatedDate());
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
        fcmNotificationRequest.setTitle("TH??NG B??O PH???N H???I ?? KI???N ????NG G??P");
        String body = feedbackDTO.getProcessingUnitName() + " ???? ph???n h???i ?? ki???n c???a b???n"
                + ". N???i dung: " + contentFeedback
                + ". Th???i gian: " + DateUtils.convertFromInstantToString(feedbackDTO.getCreatedDate());
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
        fcmNotificationRequest.setTitle("Th??ng b??o");
        fcmNotificationRequest.setBody("C???m ??n b???n ???? g???i g??p ?? cho ch??ng t??i Ch??ng t??i s??? x??? l?? trong th???i gian s???m nh???t");
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

        fcmNotificationRequest.setTitle("Th??ng b??o b???nh nh??n l???y K???t qu??? C???n l??m s??ng");
        String body = "B???n ???? c?? k???t qu??? kh??m C???n l??m s??ng "
                + ". M?? d???ch v??? c???n l??m s??ng: " + subclinicalDTO.getCode()
                + ". T??n d???ch v??? c???n l??m s??ng: " + subclinicalDTO.getName()
                + ". K??? thu???t vi??n: " + subclinicalDTO.getTechnician()
                + ". Ph??ng th???c hi???n: " + subclinicalDTO.getRoom();
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
