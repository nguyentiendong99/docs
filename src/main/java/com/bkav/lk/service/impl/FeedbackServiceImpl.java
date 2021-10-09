package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Feedback;
import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.domain.Topic;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.FeedbackMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {
    @Value("${feedback.change_state_day_config}")
    private Long DAY_CONFIG;

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private static final String ENTITY_NAME = "feedback";

    private final FeedbackMapper feedbackMapper;

    private final FeedbackRepository feedbackRepository;

    private final ActivityLogRepository activityLogRepository;

    private final FeedbackContentService feedbackContentService;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private final CategoryConfigValueService categoryConfigValueService;

    private final TopicService topicService;

    private final TopicRepository topicRepository;

    private final UserRepository userRepository;

    private final HealthFacilitiesRepository healthFacilitiesRepository;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper, FeedbackRepository feedbackRepository,
                               ActivityLogRepository activityLogRepository, FeedbackContentService feedbackContentService, CategoryConfigFieldService categoryConfigFieldService, CategoryConfigValueService categoryConfigValueService, TopicService topicService, TopicRepository topicRepository, UserRepository userRepository, HealthFacilitiesRepository healthFacilitiesRepository) {
        this.feedbackMapper = feedbackMapper;
        this.feedbackRepository = feedbackRepository;
        this.activityLogRepository = activityLogRepository;
        this.feedbackContentService = feedbackContentService;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigValueService = categoryConfigValueService;
        this.topicService = topicService;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.healthFacilitiesRepository = healthFacilitiesRepository;
    }

    @Override
    public Page<FeedbackDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.info("Search list of feedback with conditions: {}", queryParams);

        List<Feedback> feedbackList = feedbackRepository.search(queryParams, pageable);
        List<FeedbackDTO> feedbackDTOList = feedbackMapper.toDto(feedbackList);
        feedbackDTOList.forEach(item -> {
            List<FeedbackContentDTO> feedbackContentDTOList = this.feedbackContentService.findListByFeedbackId(item.getId());
            item.setFeedbackContentDTOList(feedbackContentDTOList);
            Optional<TopicDTO> topicDTO = this.topicService.findOne(item.getTopicId());
            item.setTopicName(topicDTO.get().getName());
            Optional<User> user = userRepository.findById(item.getUserId());
            user.ifPresent(value -> item.setUserAvatar(value.getAvatar()));
        });

        return new PageImpl<>(feedbackDTOList, pageable, feedbackRepository.count(queryParams));
    }

    @Override
    public FeedbackDTO save(FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackMapper.toEntity(feedbackDTO);
        feedbackRepository.save(feedback);
        //Check config
        //Create
        if (feedbackDTO.getId() == null) {
            if (!CollectionUtils.isEmpty(feedbackDTO.getFeedbackCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

                feedbackDTO.getFeedbackCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(feedbackCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                    configValueDTO.setValue(feedbackCustomConfigDTO.getValue());
                    configValueDTO.setFieldId(feedbackCustomConfigDTO.getFieldId());
                    configValueDTO.setObjectId(feedback.getId());
                    configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    configValueDTOS.add(configValueDTO);
                });
                categoryConfigValueService.createAll(configValueDTOS);
            }
        } else if (feedbackDTO.getId() != null) {
            // Update
            if (!CollectionUtils.isEmpty(feedbackDTO.getFeedbackCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
                List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
                feedbackDTO.getFeedbackCustomConfigDTOS().forEach(feedbackCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(feedback.getId(), feedbackCustomConfigDTO.getFieldId());
                    configValueDTO.setValue(feedbackCustomConfigDTO.getValue());
                    if (configValueDTO.getFieldId() != null) {
                        configValueUpdateDTOS.add(configValueDTO);
                    } else {
                        configValueDTO.setObjectId(feedback.getId());
                        configValueDTO.setFieldId(feedbackCustomConfigDTO.getFieldId());
                        configValueDTO.setValue(feedbackCustomConfigDTO.getValue());
                        configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                        configValueCreateDTOS.add(configValueDTO);
                    }
                });
                if (configValueUpdateDTOS.size() > 0) {
                    categoryConfigValueService.updateAll(configValueUpdateDTOS);
                }
                if (configValueCreateDTOS.size() > 0) {
                    categoryConfigValueService.createAll(configValueCreateDTOS);
                }
            }
        }
        return feedbackMapper.toDto(feedback);
    }

    @Override
    public FeedbackDTO createFeedback (FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackMapper.toEntity(feedbackDTO);
        feedback.setStatus(Constants.REPORT_STATUS.WAITING);
        feedback.setContent(feedbackDTO.getContent());
        feedbackRepository.save(feedback);
        return feedbackMapper.toDto(feedback);
    }

    @Override
    public FeedbackDTO findById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        Optional<Topic> topic = topicRepository.findById(feedback.getTopicId());
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);
        feedbackDTO.setTopicName(topic.get().getName());
        Optional<HealthFacilities> healthFacilities = healthFacilitiesRepository.findById(feedbackDTO.getFeedbackedUnitId());
        if(healthFacilities.isPresent()){
            feedbackDTO.setFeedbackedUnitAvatar(healthFacilities.get().getImgPath());
        }
        Optional<User> user = userRepository.findById(feedbackDTO.getUserId());
        if(user.isPresent()){
            feedbackDTO.setUserAvatar(user.get().getAvatar());
        }
        return feedbackDTO;
    }

    @Override
    public List<FeedbackDTO> findByUserId(Long userId) {
        List<Feedback> feedbackList = feedbackRepository.findAllByUserId(userId);
        return feedbackMapper.toDto(feedbackList);
    }

    @Override
    public void autoChangeStatusEndTime() {
        String nowStr = DateUtils.now();
        Instant now = DateUtils.parseToInstant(nowStr, DateUtils.NORM_DATETIME_PATTERN);
        Instant date = now.plus(- DAY_CONFIG, ChronoUnit.DAYS);
        feedbackRepository.ChangeStatus(date, Constants.REPORT_STATUS.PROCESSING, Constants.REPORT_STATUS.DONE);
    }

    @Override
    public List<FeedbackCustomConfigDTO> findAllCustomConfigByFeedbackId(Long id, Long healthFacilityId) {
        log.debug("Find all custom config of feedback Start");
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllReportByHealthFacilityIdAndStatusAndConfigType(
                healthFacilityId, Constants.ENTITY_STATUS.ACTIVE,
                Constants.CONFIG_REPORT_TYPE.FEEDBACK.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(id)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of feedback End");
        return configFieldDTOS.stream()
                .map(field -> new FeedbackCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }
}
