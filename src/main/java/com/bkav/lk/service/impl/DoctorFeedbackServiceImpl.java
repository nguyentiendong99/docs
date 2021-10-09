package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.DoctorFeedback;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.DoctorFeedbackRepository;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorFeedBackMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorFeedbackServiceImpl implements DoctorFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private static final String ENTITY_NAME = "doctor_feedback";

    private final DoctorFeedBackMapper doctorFeedBackMapper;

    private final DoctorFeedbackRepository feedbackRepository;

    private final ActivityLogService activityLogService;

    private final DoctorService doctorService;

    private final UserService userService;

    private final HealthFacilitiesService healthFacilitiesService;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private final CategoryConfigValueService categoryConfigValueService;

    public DoctorFeedbackServiceImpl(DoctorFeedBackMapper doctorFeedBackMapper, UserService userService, DoctorService doctorService, DoctorFeedbackRepository feedbackRepository,
                                     ActivityLogService activityLogService, HealthFacilitiesService healthFacilitiesService, CategoryConfigFieldService categoryConfigFieldService, CategoryConfigValueService categoryConfigValueService) {
        this.doctorFeedBackMapper = doctorFeedBackMapper;
        this.feedbackRepository = feedbackRepository;
        this.activityLogService = activityLogService;
        this.doctorService = doctorService;
        this.userService = userService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigValueService = categoryConfigValueService;
    }

    @Override
    public Page<DoctorFeedbackDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<DoctorFeedback> feedbackList = feedbackRepository.search(queryParams, pageable);
        List<DoctorFeedbackDTO> feedbackDTOList = doctorFeedBackMapper.toDto(feedbackList);
        feedbackDTOList.forEach(i -> {
            if (i.getDoctorId() != null) {
                DoctorDTO doctorDTO = doctorService.findById(i.getDoctorId());
                Optional<User> user = userService.findOne(i.getUserId());
                Optional<HealthFacilitiesDTO> healthFacilities = healthFacilitiesService.findOne(doctorDTO.getHealthFacilityId());
                i.setHealthFacilityName(healthFacilities.get().getName());
                i.setHealthFacilityId(healthFacilities.get().getId());
                i.setUserAvatar(user.get().getAvatar());
            }

        });
        return new PageImpl<>(feedbackDTOList, pageable, feedbackRepository.count(queryParams));
    }

    @Override
    public DoctorFeedbackDTO save(DoctorFeedbackDTO doctorFeedbackDTO) {
        DoctorFeedback doctorFeedback = doctorFeedBackMapper.toEntity(doctorFeedbackDTO);
        feedbackRepository.save(doctorFeedback);
        //Check config
        //Create
        if (doctorFeedbackDTO.getId() == null) {
            if (!CollectionUtils.isEmpty(doctorFeedbackDTO.getDoctorFeedbackCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

                doctorFeedbackDTO.getDoctorFeedbackCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(doctorFeedbackCustomConfigDTO     -> {
                    CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                    configValueDTO.setValue(doctorFeedbackCustomConfigDTO.getValue());
                    configValueDTO.setFieldId(doctorFeedbackCustomConfigDTO.getFieldId());
                    configValueDTO.setObjectId(doctorFeedback.getId());
                    configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    configValueDTOS.add(configValueDTO);
                });
                categoryConfigValueService.createAll(configValueDTOS);
            }
        } else if (doctorFeedbackDTO.getId() != null) {
            // Update
            if (!CollectionUtils.isEmpty(doctorFeedbackDTO.getDoctorFeedbackCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
                List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
                doctorFeedbackDTO.getDoctorFeedbackCustomConfigDTOS().forEach(doctorFeedbackCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(doctorFeedback.getId(), doctorFeedbackCustomConfigDTO.getFieldId());
                    configValueDTO.setValue(doctorFeedbackCustomConfigDTO.getValue());
                    if (configValueDTO.getFieldId() != null) {
                        configValueUpdateDTOS.add(configValueDTO);
                    } else {
                        configValueDTO.setObjectId(doctorFeedback.getId());
                        configValueDTO.setFieldId(doctorFeedbackCustomConfigDTO.getFieldId());
                        configValueDTO.setValue(doctorFeedbackCustomConfigDTO.getValue());
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
        return doctorFeedBackMapper.toDto(doctorFeedback);
    }

    @Override
    public DoctorFeedback createFeedback(DoctorFeedbackDTO doctorFeedbackDTO) {
        DoctorFeedback doctorFeedback = doctorFeedBackMapper.toEntity(doctorFeedbackDTO);
        doctorFeedback.setStatus(Constants.REPORT_STATUS.WAITING);
        feedbackRepository.save(doctorFeedback);
        return doctorFeedback;
    }

    @Override
    public DoctorFeedbackDTO findById(Long id) {
        DoctorFeedback result = feedbackRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        return doctorFeedBackMapper.toDto(result);
    }


    @Override
    public Optional<DoctorFeedbackDTO> findOne(Long id) {
        return feedbackRepository.findById(id).map(doctorFeedBackMapper::toDto);
    }

    @Override
    public List<PositionHistoryDTO> getPositionHistory(MultiValueMap<String, String> queryParam) {
        List<ActivityLog> activityLogs = activityLogService.search(queryParam);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<PositionHistoryDTO> positionHistoryDTOList = new ArrayList<>();


        for (ActivityLog activityLog : activityLogs) {
            PositionHistoryDTO history = new PositionHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            List<String> newContentList = new ArrayList<>();
            List<String> oldContentList = new ArrayList<>();
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                newContentList.add("Thêm mới");
                history.setNewContents(newContentList);
                positionHistoryDTOList.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                DoctorFeedback oldP = convertToPosition(activityLog.getOldContent());
                DoctorFeedback newP = convertToPosition(activityLog.getContent());
                createContentList(oldP, newP, oldContentList, newContentList);
            }
            if (oldContentList.size() == 0 || newContentList.size() == 0) {
                continue;
            }
            history.setOldContents(oldContentList);
            history.setNewContents(newContentList);
            positionHistoryDTOList.add(history);
        }

        return positionHistoryDTOList;
    }

    private DoctorFeedback convertToPosition(String input) {
        if (input.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(input, DoctorFeedback.class);
    }

    private void createContentList(DoctorFeedback oldP, DoctorFeedback newP, List<String> oldContentList, List<String> newContentList) {
        if (oldP == null) {
            return;
        }
        if (oldP.getFeedbackContent() != null) {
            if (!oldP.getFeedbackContent().equals(newP.getFeedbackContent())) {
                oldContentList.add("Phản hồi: " + oldP.getFeedbackContent());
                newContentList.add("Phản hồi: " + newP.getFeedbackContent());
            }
        } else {
            if (newP.getFeedbackContent() != null) {
                newContentList.add("Phản hồi: " + newP.getFeedbackContent());
            }
        }

        if (!oldP.getStatus().equals(newP.getStatus())) {
            oldContentList.add("Trạng thái: " +
                    (oldP.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE) ? "Đã xử lý" : "Chờ xử lý"));
            newContentList.add("Trạng thái: " +
                    (newP.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE) ? "Đã xử lý" : "Chờ xử lý"));
        }
    }


    @Override
    public List<DoctorFeedbackCustomConfigDTO> findAllCustomConfigByFeedbackId(Long id, Long healthFacilityId) {
        log.debug("Find all custom config of doctor feedback Start");
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllReportByHealthFacilityIdAndStatusAndConfigType(
                healthFacilityId, Constants.ENTITY_STATUS.ACTIVE,
                Constants.CONFIG_REPORT_TYPE.DOCTOR_FEEDBACK.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(id)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of doctor feedback End");
        return configFieldDTOS.stream()
                .map(field -> new DoctorFeedbackCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }
}
