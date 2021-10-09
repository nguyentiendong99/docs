package com.bkav.lk.config;

import com.bkav.lk.helper.OTPGenerator;
import com.telesign.MessagingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class OTPConfiguration {
    private final Logger log = LoggerFactory.getLogger(OTPConfiguration.class);


    @ConfigurationProperties(prefix = "otp")
    @Component
    public static class OTPProperties {
        // the verify code's length
        private int numDigits;
        private String messageTemplate;

        public int getNumDigits() {
            return numDigits;
        }

        public void setNumDigits(int numDigits) {
            this.numDigits = numDigits;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }

        public void setMessageTemplate(String messageTemplate) {
            this.messageTemplate = messageTemplate;
        }
    }

    @ConfigurationProperties(prefix = "otp.provider.telesign")
    @Component
    public static class TeleSignProperties {
        private String customerId;
        private String apiKey;

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    /**
     * Initialize TeleSign SMS provider
     *
     * @param teleSignProperties
     * @return MessagingClient
     */
    @Bean
    public MessagingClient messagingClient(TeleSignProperties teleSignProperties) {
        return new MessagingClient(teleSignProperties.customerId, teleSignProperties.apiKey);
    }

    @Bean
    public OTPGenerator otpGenerator(OTPProperties otpProperties) {
        return new OTPGenerator(otpProperties.numDigits, otpProperties.messageTemplate);
    }
}
