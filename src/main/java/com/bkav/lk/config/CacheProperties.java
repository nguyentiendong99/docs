package com.bkav.lk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    private String localIp;

    private String remoteIp;

    private Integer timeToLiveSeconds;

    private int backupCount = 1;

    private ManagementCenter managementCenter;

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public Integer getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(Integer timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public int getBackupCount() {
        return backupCount;
    }

    public void setBackupCount(int backupCount) {
        this.backupCount = backupCount;
    }

    public ManagementCenter getManagementCenter() {
        return managementCenter;
    }

    public void setManagementCenter(ManagementCenter managementCenter) {
        this.managementCenter = managementCenter;
    }

    public static class ManagementCenter {
        private boolean enabled = false;
        private int updateInterval = 3;
        private String url = "";

        public ManagementCenter() {
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getUpdateInterval() {
            return this.updateInterval;
        }

        public void setUpdateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
