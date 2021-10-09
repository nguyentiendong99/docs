package com.bkav.lk.web.rest;

import com.bkav.lk.dto.ActivityLogForManagementDTO;
import com.bkav.lk.service.ActivityLogManagementService;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ActivityLogManagementResource {

    private static final String ENTITY_NAME = "activity log management";

    private ActivityLogManagementService activityLogManagementService;

    public ActivityLogManagementResource(ActivityLogManagementService activityLogManagementService) {
        this.activityLogManagementService = activityLogManagementService;
    }

    @GetMapping("/activity-log-management/search")
    public ResponseEntity<List<ActivityLogForManagementDTO>> search(@RequestParam MultiValueMap<String, String> queryParam, Pageable pageable){
        Page<ActivityLogForManagementDTO> page = activityLogManagementService.search(queryParam, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
