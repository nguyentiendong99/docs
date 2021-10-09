package com.bkav.lk.web.rest;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.FeedbackMapper;
import com.bkav.lk.service.mapper.UploadedFileMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.service.notification.firebase.NotificationFireBaseService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class FeedbackResource {
    private final Logger log = LoggerFactory.getLogger(FeedbackResource.class);

    private static final String ENTITY_NAME = "Feedback";

    private final FeedbackService feedbackService;

    private final FeedbackContentService feedbackContentService;

    private final NotificationService notificationService;

    private final NotificationFireBaseService fireBaseService;

    private final FeedbackMapper feedbackMapper;

    private final UploadedFileService uploadedFileService;

    private final ActivityLogService activityLogService;

    private final UserService userService;

    private final UploadedFileMapper uploadedFileMapper;

    private final HealthFacilitiesService healthFacilitiesService;

    private final TopicService topicService;

    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String applicationName;

    public FeedbackResource(FeedbackService feedbackService, FeedbackContentService feedbackContentService, NotificationService notificationService, NotificationFireBaseService fireBaseService, FeedbackMapper feedbackMapper, UploadedFileService uploadedFileService, ActivityLogService activityLogService, UserService userService, UploadedFileMapper uploadedFileMapper, HealthFacilitiesService healthFacilitiesService, TopicService topicService, ObjectMapper objectMapper) {
        this.feedbackService = feedbackService;
        this.feedbackContentService = feedbackContentService;
        this.notificationService = notificationService;
        this.fireBaseService = fireBaseService;
        this.feedbackMapper = feedbackMapper;
        this.uploadedFileService = uploadedFileService;
        this.activityLogService = activityLogService;
        this.userService = userService;
        this.uploadedFileMapper = uploadedFileMapper;
        this.healthFacilitiesService = healthFacilitiesService;
        this.topicService = topicService;
        this.objectMapper = objectMapper;
    }

    /**
     * search
     * Tìm kiếm danh sách ý kiến đóng góp
     *
     * @param queryParams name
     * @return List<FeedbackDTO> - Danh sách ý kiến đóng góp
     */
    // API cho mobile lấy danh sách đóng góp theo user
    @GetMapping("/feedbacks")
    public ResponseEntity<List<FeedbackDTO>> search(
            @RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        queryParams.add("userId", userService.getUserWithAuthorities().get().getId().toString());
        Page<FeedbackDTO> page = feedbackService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/feedbacks/all")
    public ResponseEntity<List<FeedbackDTO>> search(
            @RequestHeader(name = "healthFacilityId", required = false) Long healthFacilityId,
            @RequestParam MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("REST request to search for a page of feedback for query {}", queryParams);
        if (healthFacilityId != null) {
            queryParams.set("healthFacilityId", healthFacilityId.toString());
        }
        Page<FeedbackDTO> page = feedbackService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PutMapping("/feedbacks/update")
    public ResponseEntity<FeedbackDTO> update(@RequestBody FeedbackDTO feedbackDTO) {
        log.debug("REST request to update feedback Service : {}", feedbackDTO);
        feedbackDTO.setProcessedBy(userService.getUserWithAuthorities().get().getName());

        // don't have content => don't push notify
        if (StringUtils.isBlank(feedbackDTO.getContentFeedback())) {
            feedbackDTO.setStatus(Constants.REPORT_STATUS.DONE);
            FeedbackDTO dtoFeedback = feedbackService.save(feedbackDTO);
            activityLogService.done(Constants.CONTENT_TYPE.FEEDBACK, feedbackMapper.toEntity(dtoFeedback));
            if (feedbackDTO.getId() != null) {
                List<FeedbackContentDTO> list = feedbackContentService.findListByFeedbackId(feedbackDTO.getId());
                if (feedbackDTO.getFeedbackContentDTOList().size() != list.size()) {
                    feedbackContentService.createFeedbackContent(dtoFeedback.getId(), "Đã xử lý", "ADMIN");
                }
            } else {
                feedbackContentService.createFeedbackContent(dtoFeedback.getId(), "Đã xử lý", "ADMIN");
            }
        } else {
            // Todo - Gửi thông báo
            FirebaseData firebaseData = new FirebaseData();
            firebaseData.setObjectId(feedbackDTO.getId().toString());
            try {
                firebaseData.setObject(objectMapper.writeValueAsString(feedbackDTO).replaceAll("\\n|\\r", ""));
            } catch (JsonProcessingException e) {
                log.error("Error: ", e);
            }
            String template;
            List<String> paramsBody = new ArrayList<>();

            // the first feedback has content => notify accept
            if (feedbackDTO.getStatus().equals(Constants.REPORT_STATUS.WAITING)){
                feedbackDTO.setStatus(Constants.REPORT_STATUS.PROCESSING);
                FeedbackDTO dtoFeedback = feedbackService.save(feedbackDTO);
                activityLogService.processing(Constants.CONTENT_TYPE.FEEDBACK, feedbackMapper.toEntity(dtoFeedback));
                feedbackContentService.createFeedbackContent(dtoFeedback.getId(), feedbackDTO.getContentFeedback(), "ADMIN");
                firebaseData.setType(String.valueOf(Constants.NotificationConstants.FEEDBACK_ACCEPT.id));
                template = Constants.NotificationConstants.FEEDBACK_ACCEPT.template;
            } else {
                FeedbackDTO dtoFeedback = feedbackService.save(feedbackDTO);
                activityLogService.processing(Constants.CONTENT_TYPE.FEEDBACK, feedbackMapper.toEntity(dtoFeedback));
                feedbackContentService.createFeedbackContent(dtoFeedback.getId(), feedbackDTO.getContentFeedback(), "ADMIN");
                firebaseData.setType(String.valueOf(Constants.NotificationConstants.FEEDBACK_RESPONSE.id));
                template = Constants.NotificationConstants.FEEDBACK_RESPONSE.template;
                paramsBody.add(feedbackDTO.getTopicName());
            }
            notificationService.pushNotification(template, firebaseData, null, paramsBody, feedbackDTO.getUserId());
        }

        return ResponseEntity.ok(feedbackDTO);
    }

    @PostMapping("/feedbacks")
    public ResponseEntity<FeedbackDTO> create(
            @RequestParam(name = "file", required = false) List<MultipartFile> listfile,
            @ModelAttribute @Valid FeedbackDTO feedbackDTO) throws URISyntaxException {
        if (!StringUtils.isNotBlank(feedbackDTO.getContent())) {
            throw new BadRequestAlertException("content not null, empty", ENTITY_NAME, "contentnull");
        }
        if (Objects.isNull(feedbackDTO.getTopicId())) {
            throw new BadRequestAlertException("TopicId not null, empty", ENTITY_NAME, "idnull");
        }
        if (Objects.isNull(feedbackDTO.getFeedbackedUnitId())) {
            throw new BadRequestAlertException("FeedbackedUnitId not null, empty", ENTITY_NAME, "idnull");
        }
        if (Objects.isNull(feedbackDTO.getProcessingUnitId())) {
            throw new BadRequestAlertException("getProcessingUnitId not null, empty", ENTITY_NAME, "idnull");
        }
        if (listfile.size() > 0 && !listfile.get(0).isEmpty()) {
            listfile.forEach((file) -> {
                String filename = Objects.requireNonNull(file.getResource().getFilename()).toLowerCase();
                if (!(filename.contains(".jpg") || filename.contains(".png") || filename.contains(".avi") || filename.contains(".mp3")
                        || filename.contains(".flv") || filename.contains(".wmv") || filename.contains(".mp4"))) {
                    throw new BadRequestAlertException("Contains invalid file, only use image and video", ENTITY_NAME, "invalidfile");
                }
            });

            int totalSizeFile= 0;
            for (MultipartFile file : listfile ) {
                totalSizeFile = totalSizeFile + (int) file.getSize();
            }

            // kiểm tra file tải lên có dung lượng lớn hơn 100MB hay không?
            if (totalSizeFile > 104857600) {
                throw new BadRequestAlertException("File upload maximum 100MB", ENTITY_NAME, "maxFileUpload100MB");
            }
        }
        feedbackDTO.setContent(feedbackDTO.getContent().trim());
        Optional<TopicDTO> topic = topicService.findOne(feedbackDTO.getTopicId());
        Optional<HealthFacilitiesDTO> healthFacilitiesDTO1 = healthFacilitiesService.findOne(feedbackDTO.getFeedbackedUnitId());
        Optional<HealthFacilitiesDTO> healthFacilitiesDTO2 = healthFacilitiesService.findOne(feedbackDTO.getProcessingUnitId());
        if (!topic.isPresent()) {
            throw new BadRequestAlertException("topicId not found", ENTITY_NAME, "not exist");
        }
        if (!healthFacilitiesDTO1.isPresent()) {
            throw new BadRequestAlertException("FeedbackedUnitId not found", ENTITY_NAME, "not exist");
        }
        if (!healthFacilitiesDTO2.isPresent()) {
            throw new BadRequestAlertException("ProcessingUnitId not found", ENTITY_NAME, "not exist");
        }

        //check lại một lần nữa trạng thái của chủ đề ý kiến đóng góp trước khi tạo mới
        TopicDTO topicDTO = topicService.findOne(feedbackDTO.getTopicId()).orElse(null);
        if (topicDTO != null && topicDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            throw new BadRequestAlertException("Topic is deactivated", ENTITY_NAME, "feedback.topicIsDeactivate");
        }

            feedbackDTO.setUserId(userService.getUserWithAuthorities().get().getId());
            FeedbackDTO feedback = feedbackService.createFeedback(feedbackDTO);
            activityLogService.waiting(Constants.CONTENT_TYPE.FEEDBACK, feedbackMapper.toEntity(feedback));
        //    notificationService.saveFeedback(feedbackDTO, Constants.NotificationConstants.SEND_FEEDBACK.value, Constants.NotificationConstants.SEND_FEEDBACK.name());
            if (listfile.size() > 0 && !listfile.get(0).isEmpty()) {
                listfile.forEach((file) -> {
                    try {
                        uploadedFileService.storeNew(file, "feedback", feedback.getId());
                    } catch (IOException e) {
                        log.error("Error: ", e);
                    }
                });
        }
    //    fireBaseService.pushNotiCreateFeedback(feedback);
        return ResponseEntity.created(new URI("/api/feedbacks/" + feedback.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                            feedback.getId().toString()))
                    .body(feedback);
    }

    @PostMapping("/feedbacks/confirm")
    public ResponseEntity<FeedbackDTO> confirmFeedback(@RequestBody @Valid FeedbackDTO feedbackDTO) throws URISyntaxException {
        log.debug("REST request to approve comment Service : {}", feedbackDTO);
        FeedbackDTO confirm = feedbackService.findById(feedbackDTO.getId());
        if (feedbackDTO.getStatus().equals(Constants.REPORT_STATUS.DONE)) {
            confirm.setStatus(feedbackDTO.getStatus());
            confirm = feedbackService.save(confirm);
            activityLogService.done(Constants.CONTENT_TYPE.FEEDBACK, feedbackMapper.toEntity(confirm));
            // Todo - Gửi thông báo
            FirebaseData firebaseData = new FirebaseData();
            firebaseData.setObjectId(confirm.getId().toString());
            try {
                firebaseData.setObject(objectMapper.writeValueAsString(confirm).replaceAll("\\n|\\r", ""));
            } catch (JsonProcessingException e) {
                log.error("Error: ", e);
            }
            String template;
            List<String> paramsBody = new ArrayList<>();
            firebaseData.setType(String.valueOf(Constants.NotificationConstants.FEEDBACK_ACCEPT.id));
            template = Constants.NotificationConstants.FEEDBACK_ACCEPT.template;
            notificationService.pushNotification(template, firebaseData, null, paramsBody, confirm.getUserId());
        //    notificationService.saveFeedback(feedbackDTO, Constants.NotificationConstants.DONE_FEEDBACK.value, Constants.NotificationConstants.DONE_FEEDBACK.name());
        }
        else {
            Feedback feedback = feedbackMapper.toEntity(confirm);
            feedbackContentService.createFeedbackContent(feedback.getId(), feedbackDTO.getContent(), "USER");
            confirm = feedbackService.save(confirm);
            activityLogService.processing(Constants.CONTENT_TYPE.FEEDBACK, feedbackMapper.toEntity(confirm));
        //    notificationService.saveFeedback(feedbackDTO, Constants.NotificationConstants.SEND_FEEDBACK.value, Constants.NotificationConstants.SEND_FEEDBACK.name());
        }
        return ResponseEntity.created(new URI("/api/doctors/" + confirm.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        confirm.getId().toString()))
                .body(confirm);
    }

    @GetMapping("feedbacks/{id}")
    public ResponseEntity<FeedbackDTO> findOne(@PathVariable Long id) {
        FeedbackDTO feedbackDTO = feedbackService.findById(id);
        List<FeedbackContentDTO> feedbackContentDTOList = this.feedbackContentService.findListByFeedbackId(feedbackDTO.getId());
        List<UploadedFile> uploadedFile = uploadedFileService.findByOwnerId(id, "feedback");
        feedbackDTO.setFeedbackContentDTOList(feedbackContentDTOList);
        feedbackDTO.setUploadedFileDTOList(uploadedFileMapper.toDto(uploadedFile));
        return ResponseEntity.ok(feedbackDTO);
    }

    @GetMapping("feedbacks/user")
    public ResponseEntity<List<FeedbackDTO>> findListFeedbackByUser() {
        List<FeedbackDTO> feedbackDTOList = feedbackService.findByUserId(userService.getUserWithAuthorities().get().getId());
        return ResponseEntity.ok(feedbackDTOList);
    }

    @GetMapping("/feedbacks/custom-config/{id}")
    public ResponseEntity<List<FeedbackCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id, @RequestHeader("healthFacilityId") Long healthFacilityId) {
        return ResponseEntity.ok().body(feedbackService.findAllCustomConfigByFeedbackId(id, healthFacilityId));
    }
}

