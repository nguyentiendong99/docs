package com.bkav.lk.web.rest;

import com.bkav.lk.domain.DoctorFeedback;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorFeedBackMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.service.notification.firebase.NotificationFireBaseService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DoctorFeedbackResource {
    private static final Logger log = LoggerFactory.getLogger(DoctorFeedbackResource.class);

    private static final String ENTITY_NAME = "doctorFeedback";

    private final DoctorFeedbackService doctorFeedbackService;

    private final DoctorFeedBackMapper doctorFeedBackMapper;

    private final NotificationFireBaseService fireBaseService;

    private final NotificationService notificationService;

    private final UploadedFileService uploadedFileService;

    private final ActivityLogService activityLogService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final DoctorService doctorService;

    public DoctorFeedbackResource(DoctorFeedbackService doctorFeedbackService, DoctorFeedBackMapper doctorFeedBackMapper,
                                  NotificationFireBaseService fireBaseService, NotificationService notificationService,
                                  UploadedFileService uploadedFileService, UserService userService, ActivityLogService activityLogService, ObjectMapper objectMapper, DoctorService doctorService) {
        this.doctorFeedbackService = doctorFeedbackService;
        this.doctorFeedBackMapper = doctorFeedBackMapper;
        this.fireBaseService = fireBaseService;
        this.notificationService = notificationService;
        this.uploadedFileService = uploadedFileService;
        this.activityLogService = activityLogService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.doctorService = doctorService;
    }

    /**
     * search
     * Tìm kiếm danh sách ý kiến đóng góp
     *
     * @param queryParams name
     * @return List<FeedbackDTO> - Danh sách ý kiến đóng góp
     */
    @GetMapping("/doctor-feedback")
    public ResponseEntity<List<DoctorFeedbackDTO>> search(
            @RequestHeader(name = "healthFacilityId", required = false) Long healthFacilityId,
            @RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("REST request to search for a page of feedback for query {}", ENTITY_NAME);
        if(healthFacilityId != null){
            queryParams.set("healthFacilityId", healthFacilityId.toString());
        }
        Page<DoctorFeedbackDTO> page = doctorFeedbackService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * Lấy 1 bản ghi
     */
    @GetMapping("/doctor-feedback/{id}")
    public ResponseEntity<DoctorFeedbackDTO> getDoctorFeedback(@PathVariable Long id) {
        Optional<DoctorFeedbackDTO> doctorFeedbackDTO = doctorFeedbackService.findOne(id);
        if (!doctorFeedbackDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(doctorFeedbackDTO);
    }

    @PutMapping("/doctor-feedback/approve")
    public ResponseEntity<DoctorFeedbackDTO> approve(@RequestBody DoctorFeedbackDTO doctorFeedbackDTO) {
        log.debug("REST request to approve feedback Service : {}", doctorFeedbackDTO);
        DoctorFeedbackDTO old = doctorFeedbackService.findById(doctorFeedbackDTO.getId());
        DoctorFeedbackDTO newDoctor = doctorFeedbackService.findById(doctorFeedbackDTO.getId());
        newDoctor.setStatus(2);
        newDoctor.setFeedbackContent(doctorFeedbackDTO.getFeedbackContent());
        newDoctor.setDoctorFeedbackCustomConfigDTOS(doctorFeedbackDTO.getDoctorFeedbackCustomConfigDTOS());
        doctorFeedbackService.save(newDoctor);
    //    fireBaseService.pushFeedbackDoctor(newDoctor);
        activityLogService.update(Constants.CONTENT_TYPE.DOCTOR_FEEDBACK, doctorFeedBackMapper.toEntity(old), doctorFeedBackMapper.toEntity(newDoctor));
        // Todo - Gửi thông báo
        DoctorDTO doctorDTO = doctorService.findById(doctorFeedbackDTO.getDoctorId());
        FirebaseData firebaseData = new FirebaseData();
        firebaseData.setObjectId(doctorFeedbackDTO.getId().toString());
        try {
            firebaseData.setObject(objectMapper.writeValueAsString(doctorFeedbackDTO).replaceAll("\\n|\\r", ""));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        String template;
        List<String> paramsBody = new ArrayList<>();
        firebaseData.setType(String.valueOf(Constants.NotificationConstants.REVIEW_DOCTOR_RESPONSE.id));
        template = Constants.NotificationConstants.REVIEW_DOCTOR_RESPONSE.template;
        paramsBody.add(doctorDTO.getAcademicName() + "." + doctorDTO.getName());
        notificationService.pushNotification(template, firebaseData, null, paramsBody, doctorFeedbackDTO.getUserId());
        return ResponseEntity.ok(newDoctor);
    }

    @PostMapping("/doctor-feedback")
    public ResponseEntity<DoctorFeedbackDTO> create(@RequestBody DoctorFeedbackDTO doctorFeedbackDTO) {
        doctorFeedbackDTO.setUserId(userService.getUserWithAuthorities().get().getId());
        if (doctorFeedbackDTO.getDoctorId() == null) {
            throw new BadRequestAlertException("doctorId not null, empty", ENTITY_NAME, "doctorId");
        }
        if (StringUtils.isNoneBlank(doctorFeedbackDTO.getContent()) && doctorFeedbackDTO.getContent().length() > 1000) {
            throw new BadRequestAlertException("Max length is 1000", ENTITY_NAME, "content");
        }
        DoctorFeedback feedback = doctorFeedbackService.createFeedback(doctorFeedbackDTO);
        activityLogService.waiting(Constants.CONTENT_TYPE.DOCTOR_FEEDBACK, feedback);
     /*   notificationService.saveFeedbackDoctor(doctorFeedBackMapper.toDto(feedback),
                Constants.NotificationConstants.SEND_FEEDBACK_DOCTOR.value,
                Constants.NotificationConstants.SEND_FEEDBACK_DOCTOR.name());*/
        activityLogService.create(Constants.CONTENT_TYPE.DOCTOR_FEEDBACK, feedback);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(feedback.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(doctorFeedBackMapper.toDto(feedback));
    }

    @GetMapping("/doctor-feedback-history")
    public ResponseEntity<List<PositionHistoryDTO>> findAllHistoryPosition(@RequestParam MultiValueMap<String, String> queryParam) {
        log.debug("REST request to get all position history: {}", queryParam.get("name"));
        List<PositionHistoryDTO> list = doctorFeedbackService.getPositionHistory(queryParam);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/doctor-feedback/custom-config/{id}")
    public ResponseEntity<List<DoctorFeedbackCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id, @RequestHeader("healthFacilityId") Long healthFacilityId) {
        return ResponseEntity.ok().body(doctorFeedbackService.findAllCustomConfigByFeedbackId(id, healthFacilityId));
    }
}
