package com.bkav.lk.service.notification.firebase;

import com.bkav.lk.web.rest.DeviceResource;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FCMService {
    private final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    private final FirebaseMessaging firebaseMessaging;

    public FCMService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public String sendToDevice(FCMNotificationRequest request, String token) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .build())
//                .putAllData(request.getData())
                .build();

        String response = null;
        try {
            response = firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Fail to send firebase notification", e);
        }

        return response;
    }

    public String sendToDevice(FCMNotificationRequest request) {
        Message message = null;
        String response = null;
        try {
            message = Message.builder()
                    .setToken(request.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody().replaceAll("\\<.*?\\>", ""))
                            .build())
                    .putAllData(request.getData())
                    .build();
            try {
                response = firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                log.error("Fail to send firebase notification", e);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return response;
    }
}
