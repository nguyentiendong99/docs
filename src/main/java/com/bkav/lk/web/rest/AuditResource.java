package com.bkav.lk.web.rest;

import com.bkav.lk.service.AuditEventService;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.bkav.lk.web.rest.util.ResponseUtil;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST controller for getting the {@link AuditEvent}s.
 */
@RestController
@RequestMapping("/api/management/audits")
public class AuditResource {
    private final AuditEventService auditEventService;

    public AuditResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAll(@RequestParam MultiValueMap<String, String> queryParams,
                                                   UriComponentsBuilder uriBuilder, Pageable pageable) {
        Page<AuditEvent> page = auditEventService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping(params = {"fromDate", "toDate"})
    public ResponseEntity<List<AuditEvent>> getByDates(
            @RequestParam(value = "fromDate") String fromDate,
            @RequestParam(value = "toDate") String toDate,
            @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder,
            Pageable pageable) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //convert String to LocalDate
        LocalDate date1 = LocalDate.parse(fromDate, formatter);
        LocalDate date2 = LocalDate.parse(toDate, formatter);

        Page<AuditEvent> page = auditEventService.findByDates(
                date1.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                date2.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant(),
                pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id:.+}")
    public ResponseEntity<AuditEvent> get(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(auditEventService.find(id));
    }
}
