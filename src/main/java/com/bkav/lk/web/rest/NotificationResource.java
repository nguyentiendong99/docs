package com.bkav.lk.web.rest;

import com.bkav.lk.dto.*;
import com.bkav.lk.service.NotificationService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationResource {

    private final Logger log = LoggerFactory.getLogger(NotificationResource.class);

    private static final String ENTITY_NAME = "Notification";

    private final ObjectMapper objectMapper;

    private final NotificationService service;

    @Autowired
    public NotificationResource(ObjectMapper objectMapper, NotificationService service) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.service = service;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> search(@RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<NotificationDTO> notificationDTOList = new ArrayList<>(service.search(queryParams, pageable).getContent());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(),
                new PageImpl<>(notificationDTOList));
        return ResponseEntity.ok().headers(headers).body(notificationDTOList);
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<NotificationDTO> findById(@PathVariable Long id) {
        NotificationDTO dto = service.findById(id);
        if (dto != null) {
            return ResponseEntity.ok().body(dto);
        }
        return null;
    }

    @GetMapping("/notifications/{id}/seen")
    public ResponseEntity<NotificationDTO> notificationSeen(@PathVariable Long id) {
        NotificationDTO dto = service.findById(id);
        dto.setStatus(Constants.ENTITY_STATUS.NOTIFICATION_SEEN);
        service.save(dto);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<Long> notificationUnRead() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("status", Constants.ENTITY_STATUS.ACTIVE.toString());
        Long count = service.findAllByNotificationUnRead(queryParams);
        return ResponseEntity.ok().body(count);
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<NotificationDTO> delete(@PathVariable Long id) {
        NotificationDTO dto = service.findById(id);
        dto.setStatus(Constants.ENTITY_STATUS.DELETED);
        service.save(dto);
        return ResponseEntity.ok().body(dto);
    }

}
