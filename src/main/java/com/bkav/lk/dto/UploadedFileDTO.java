package com.bkav.lk.dto;

import java.io.Serializable;

public class UploadedFileDTO implements Serializable {
    private String originalName;

    private String storedName;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }
}
