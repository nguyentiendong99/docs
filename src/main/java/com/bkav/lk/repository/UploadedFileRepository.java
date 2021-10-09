package com.bkav.lk.repository;

import com.bkav.lk.domain.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository  extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByStoredNameAndStatus(String storedName, Integer status);

    List<UploadedFile> findByStoredName(String storedName);

    List<UploadedFile> findAllByOwnerIdAndOwnerType(Long id, String tableName);
}
