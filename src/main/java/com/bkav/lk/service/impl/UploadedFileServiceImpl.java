package com.bkav.lk.service.impl;

import com.bkav.lk.config.ApplicationProperties;
import com.bkav.lk.domain.UploadedFile;
import com.bkav.lk.repository.UploadedFileRepository;
import com.bkav.lk.service.UploadedFileService;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.MD5Util;
import com.bkav.lk.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class UploadedFileServiceImpl implements UploadedFileService {
    private final Logger log = LoggerFactory.getLogger(UploadedFileService.class);
    private StorageService storageService;
    private UploadedFileRepository fileRepo;
    private ApplicationProperties applicationProperties;

    public UploadedFileServiceImpl(StorageService storageService, UploadedFileRepository fileRepo, ApplicationProperties applicationProperties) {
        this.storageService = storageService;
        this.fileRepo = fileRepo;
        this.applicationProperties = applicationProperties;
    }

    @Override
    @Transactional
    public UploadedFile store(MultipartFile part) throws IOException {
        String originalName = StringUtils.cleanPath(part.getOriginalFilename());
        String extention = originalName.substring(originalName.lastIndexOf('.'));
        String nameImage = MD5Util.MD5(part.getBytes()) + extention;
        String storedName = storageService.store(part, nameImage);
        UploadedFile uFile = new UploadedFile();
        uFile.setStatus(Constants.ENTITY_STATUS.ACTIVE);
//        uFile.setFolderName(folderName);
        uFile.setOriginalName(originalName);
        uFile.setStoredName(storedName);
        String mimeType = part.getContentType();
        uFile.setMimeType(mimeType);
        fileRepo.save(uFile);
        return uFile;
    }

    @Override
    @Transactional
    public UploadedFile storeNew(MultipartFile part, String tableName, Long id) throws IOException {
        String originalName = StringUtils.cleanPath(part.getOriginalFilename());
        String extention = originalName.substring(originalName.lastIndexOf('.'));
        String nameImage = MD5Util.MD5(part.getBytes()) + extention;
        String storedName = storageService.store(part, nameImage);
        UploadedFile uFile = new UploadedFile();
        uFile.setStatus(Constants.ENTITY_STATUS.ACTIVE);
//        uFile.setFolderName(folderName);
        uFile.setOwnerId(id);
        uFile.setOwnerType(tableName);
        uFile.setOriginalName(originalName);
        uFile.setStoredName(storedName);
        String mimeType = part.getContentType();
        uFile.setMimeType(mimeType);
        fileRepo.save(uFile);
        return uFile;
    }

    @Override
    public List<UploadedFile> findByStoredName(String storedName) {
        List<UploadedFile> optFile = fileRepo.findByStoredNameAndStatus(storedName, Constants.ENTITY_STATUS.ACTIVE);
        return optFile;
    }

    @Override
    public void deleteByStoredName(String storedName) {
        List<UploadedFile> uploadedFiles = fileRepo.findByStoredNameAndStatus(storedName, Constants.ENTITY_STATUS.ACTIVE);
        if (!uploadedFiles.isEmpty()) {
            UploadedFile uploadedFile = uploadedFiles.get(0);
            uploadedFile.setStatus(Constants.ENTITY_STATUS.DELETED);
            fileRepo.save(uploadedFile);
        }
    }

    public List<UploadedFile> findByOwnerId(Long id, String tableName) {
        List<UploadedFile> optFile = fileRepo.findAllByOwnerIdAndOwnerType(id, tableName);
        return optFile;
    }
}
