package com.bkav.lk.service;

import com.bkav.lk.domain.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UploadedFileService {
    UploadedFile store(MultipartFile part)  throws IOException;

    UploadedFile storeNew(MultipartFile part, String tableName,Long id)  throws IOException;

    List<UploadedFile> findByStoredName(String storedName);

    void deleteByStoredName(String storedName);

    List<UploadedFile> findByOwnerId(Long id, String tableName);
}
