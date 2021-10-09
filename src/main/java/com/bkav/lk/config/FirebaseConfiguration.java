package com.bkav.lk.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfiguration {
    private final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    @Value("${firebase.fcm-account-service}")
    private String fcmAccountServicePath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FirebaseOptions options = new FirebaseOptions
                .Builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(fcmAccountServicePath).getInputStream()))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase application has been initialized");
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
