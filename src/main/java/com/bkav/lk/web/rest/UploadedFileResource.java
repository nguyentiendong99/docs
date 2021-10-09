package com.bkav.lk.web.rest;

import com.bkav.lk.domain.UploadedFile;
import com.bkav.lk.dto.UploadedFileDTO;
import com.bkav.lk.service.UploadedFileService;
import com.bkav.lk.service.mapper.UploadedFileMapper;
import com.bkav.lk.service.storage.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadedFileResource {
    private final UploadedFileService uploadedFileService;

    private final StorageService storageService;

    private final UploadedFileMapper uploadedFileMapper;

    public UploadedFileResource(UploadedFileService uploadedFileService, StorageService storageService, UploadedFileMapper uploadedFileMapper) {
        this.uploadedFileService = uploadedFileService;
        this.storageService = storageService;
        this.uploadedFileMapper = uploadedFileMapper;
    }

    @GetMapping(value = {"/file/media/**/{storedName}"})
    public ResponseEntity<Resource> getImage(@PathVariable(name = "storedName") String storedName, HttpServletRequest request) {
        String restOfTheUrl = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = restOfTheUrl.substring(restOfTheUrl.indexOf("media") + 6);
        List<UploadedFile> listFile = uploadedFileService.findByStoredName(filePath);

        if (!listFile.isEmpty()) {
            UploadedFile upFile = listFile.get(0);
            String mimeType = upFile.getMimeType();
            // show file image, mp3, mp4
            if ("image/jpeg".equals(mimeType) || "image/png".equals(mimeType) || "image/gif".equals(mimeType)
                    || "video/mp4".equals(mimeType) || "audio/mpeg".equals(mimeType)) {
                Resource resource = storageService.loadAsResource(upFile.getStoredName());
                if (resource.exists()) {
                    return ResponseEntity
                            .ok()
                            .header(HttpHeaders.CONTENT_TYPE, mimeType)
                            .body(resource);
                }
            } else {
                throw new AccessDeniedException("");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/file/download/**/{storedName}")
    public ResponseEntity<InputStreamResource> download(@PathVariable(name = "storedName") String storedName, HttpServletRequest request) {
        String restOfTheUrl = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = restOfTheUrl.substring(restOfTheUrl.indexOf("download") + 9);
        List<UploadedFile> listFile = uploadedFileService.findByStoredName(filePath);
        HttpHeaders responseHeader = new HttpHeaders();
        if (!listFile.isEmpty()) {
            UploadedFile upFile = listFile.get(0);
            InputStreamResource inputStreamResource = storageService.download(filePath);

            responseHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeader.set("Content-disposition", "attachment; filename=" + upFile.getOriginalName());
            return new ResponseEntity<>(inputStreamResource, responseHeader, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, responseHeader, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/file/get-original-name/**/{storedName}")
    public ResponseEntity<Map<String, String>> getOriginalNameByStoredName(@PathVariable(name = "storedName") String storedName, HttpServletRequest request) {
        String restOfTheUrl = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String filePath = restOfTheUrl.substring(restOfTheUrl.indexOf("get-original-name") + 18);
        List<UploadedFile> listFile = uploadedFileService.findByStoredName(filePath);
        String originalName = "";
        if (!listFile.isEmpty()) {
            originalName = listFile.get(0).getOriginalName();
        }
        Map map = new HashMap();
        map.put("path", originalName.toString());
        return ResponseEntity.ok().body(map);
    }

    @GetMapping("/file/get-stored-name/{id}")
    public ResponseEntity<List<UploadedFileDTO>> getStoredNameOfFeedback (@PathVariable(name = "id") Long id) {
        List<UploadedFile> uploadedFile = uploadedFileService.findByOwnerId(id,"feedback");
        return ResponseEntity.ok(uploadedFileMapper.toDto(uploadedFile));
    }
}
