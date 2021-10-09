package com.bkav.lk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nmhung
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String folderUpload = "";
    private String template = "";

    public String getFolderUpload() {
        return folderUpload;
    }

    public void setFolderUpload(String folderUpload) {
        this.folderUpload = folderUpload;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
