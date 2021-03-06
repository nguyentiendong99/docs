package com.bkav.lk.service.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    String store(MultipartFile file);

    Path load(String filename);

    Resource loadAsResource(String filename);

    Stream<Path> loadAll();

    String store(MultipartFile filePart, String destName);

    Resource loadAsResourceFolder(String filename);

    Path loadFolder(String filename);

    InputStreamResource download(String filename);

    InputStream downloadExcelTemplateFromResource(String filename) throws IOException;

}
