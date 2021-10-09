package com.bkav.lk.service.notification.firebase;

import java.util.Map;

public class FCMNotificationRequest {
    private String topic;
    private String token;
    private String title;
    private String body;
    private Map<String, String> data;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FCMNotificationRequest{" +
                "topic='" + topic + '\'' +
                ", token='" + token + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", data=" + data +
                '}';
    }
}
