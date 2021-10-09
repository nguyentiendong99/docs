package com.bkav.lk.service.storage;

import com.bkav.lk.config.ApplicationProperties;
import com.bkav.lk.service.util.DataUtils;
import com.bkav.lk.service.util.MD5Util;
import com.bkav.lk.util.DateUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);

    private Path rootLocation;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    public FileSystemStorageService(ApplicationProperties applicationProperties) {
        this.rootLocation = Paths.get(applicationProperties.getFolderUpload());
    }

    @Override
    public String store(MultipartFile file) {
        String newFileName = "";
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            String fullName = DataUtils.getSafeFileName(file.getOriginalFilename());
            String fileType = "";
            if (fullName != null) {
                int last = fullName.lastIndexOf(".");
                if (last >= 0) {
                    fileType = fullName.substring(last);
                }
            }
            newFileName = MD5Util.MD5(file.getBytes()) + fileType;
            Path path = rootLocation.resolve(newFileName);
            if (!Files.exists(Paths.get(rootLocation.toString()))) {
                new File(rootLocation.toString()).mkdirs();
            }

            Files.copy(file.getInputStream(), path);
            return newFileName;
        } catch (FileAlreadyExistsException e) {
            log.error("store file error", e);
            return newFileName;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(rootLocation, 1)
                    .filter(path -> !path.equals(rootLocation))
                    .map(path -> rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        log.debug(filename, rootLocation);
        try {
            Path file = load(filename);
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public String store(MultipartFile filePart, String nameImage) {
        String filename = StringUtils.cleanPath(filePart.getOriginalFilename());
        String folderName = "";
        try {
            if (filePart.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            folderName = DateUtils.formatInstantAsString(DateUtils.nowInstant(), "yyyy/MM/dd");
            folderName = folderName + "/" + nameImage.substring(0, 2);
            Path path = rootLocation.resolve(folderName);
            InputStream inputStream = filePart.getInputStream();
            if (!path.toFile().exists()) {
                new File(path.toString()).mkdirs();
            }
            Files.copy(inputStream, path.resolve(nameImage));
            return folderName + "/" + nameImage;
        } catch (FileAlreadyExistsException e) {
            log.error("store file error", e);
            return folderName + "/" + nameImage;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Resource loadAsResourceFolder(String storedFileName) {
        try {
            Path file = loadFolder(storedFileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + storedFileName);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + storedFileName, e);
        }
    }

    @Override
    public Path loadFolder(String filename) {
        String folderName = filename.substring(0, 2);
        return rootLocation.resolve(folderName).resolve(filename);
    }

    @Override
    public InputStreamResource download(String filename) {
        try {
            File file = ResourceUtils.getFile(applicationProperties.getFolderUpload() + "/" + filename);
            try {
                byte[] data = FileUtils.readFileToByteArray(file);
                InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(data));
                InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
                return inputStreamResource;
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        } catch (FileNotFoundException e) {
            log.error("Error: ", e);
        }
        return null;
    }

    @Override
    public InputStream downloadExcelTemplateFromResource(String filename) throws IOException {
        return new ClassPathResource(applicationProperties.getTemplate() + "/" + filename).getInputStream();
    }
}
