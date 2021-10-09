package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Feedback;
import com.bkav.lk.domain.Topic;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.FeedbackRepository;
import com.bkav.lk.repository.TopicRepository;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigValueService;
import com.bkav.lk.service.TopicService;
import com.bkav.lk.service.mapper.TopicMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TopicServiceImpl implements TopicService {

    private final Logger log = LoggerFactory.getLogger(TopicService.class);

    private final TopicRepository topicRepository;

    private final FeedbackRepository feedbackRepository;

    private final CategoryConfigFieldService categoryConfigFieldService;

    private final CategoryConfigValueService categoryConfigValueService;

    private static final String ENTITY_NAME = "topic";

    private final TopicMapper topicMapper;

    public TopicServiceImpl(TopicRepository topicRepository, FeedbackRepository feedbackRepository, CategoryConfigFieldService categoryConfigFieldService, CategoryConfigValueService categoryConfigValueService, TopicMapper topicMapper) {
        this.topicRepository = topicRepository;
        this.feedbackRepository = feedbackRepository;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigValueService = categoryConfigValueService;
        this.topicMapper = topicMapper;
    }

    @Override
    public Optional<TopicDTO> findOne(Long id) {
        log.debug("Request to get agent : {}", id);
        return topicRepository.findById(id).map(topicMapper::toDto);
    }

    @Override
    public Page<TopicDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        log.debug("Request to search for a page of Topics with multi query {}", queryParams);
        List<TopicDTO> categoryDTOList = topicMapper.toDto(topicRepository.search(queryParams, pageable));
        return new PageImpl<>(categoryDTOList, pageable, topicRepository.count(queryParams));
    }

    @Override
    public TopicDTO save(TopicDTO topicDTO) {
        log.debug("Request to save Topic : {}", topicDTO);
        if(StringUtils.isEmpty(topicDTO.getId())) {
            topicDTO.setCode(generateTopicCode(topicDTO.getCode(), topicDTO.getName(), false));

        } else {
            Optional<Topic> topic2 = topicRepository.findById(topicDTO.getId());
            if(!topic2.get().getCode().equals(topicDTO.getCode()) || !topic2.get().getName().equals(topicDTO.getName())) {
                topicDTO.setCode(generateTopicCode(topicDTO.getCode(), topicDTO.getName(), !topic2.get().getName().equals(topicDTO.getName())));
            }
        }
        Topic topic = topicMapper.toEntity(topicDTO);
        topicRepository.save(topic);
        //Check config
        //Create
        if (topicDTO.getId() == null) {
            if (!CollectionUtils.isEmpty(topicDTO.getTopicCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueDTOS = new ArrayList<>();

                topicDTO.getTopicCustomConfigDTOS().stream().filter(dto -> !StringUtils.isEmpty(dto.getValue())).forEach(topicCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = new CategoryConfigValueDTO();
                    configValueDTO.setValue(topicCustomConfigDTO.getValue());
                    configValueDTO.setFieldId(topicCustomConfigDTO.getFieldId());
                    configValueDTO.setObjectId(topic.getId());
                    configValueDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                    configValueDTOS.add(configValueDTO);
                });
                categoryConfigValueService.createAll(configValueDTOS);
            }
        } else if (topicDTO.getId() != null) {
            // Update
            if (!CollectionUtils.isEmpty(topicDTO.getTopicCustomConfigDTOS())) {
                List<CategoryConfigValueDTO> configValueUpdateDTOS = new ArrayList<>();
                List<CategoryConfigValueDTO> configValueCreateDTOS = new ArrayList<>();
                topicDTO.getTopicCustomConfigDTOS().forEach(topicCustomConfigDTO -> {
                    CategoryConfigValueDTO configValueDTO = categoryConfigValueService.findByObjectIdAndFieldId(topic.getId(), topicCustomConfigDTO.getFieldId());
                    configValueDTO.setValue(topicCustomConfigDTO.getValue());
                    if (configValueDTO.getFieldId() != null) {
                        configValueUpdateDTOS.add(configValueDTO);
                    } else {
                        configValueDTO.setObjectId(topic.getId());
                        configValueDTO.setFieldId(topicCustomConfigDTO.getFieldId());
                        configValueDTO.setValue(topicCustomConfigDTO.getValue());
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
        return topicMapper.toDto(topic);
    }

    @Override
    public void delete(String ids) {
        log.debug("Request to delete Topic : {}", ids);
        if (ids.contains(",")) {      // xóa nhiều bản ghi
           String[] listIds = ids.split(",");
           for (String id : listIds) {
               List<Feedback> feedbackList = feedbackRepository.findByTopicId(Long.parseLong(id));
               if (feedbackList.isEmpty()) {
                   topicRepository.delete(ids);
               } else {
                   throw new BadRequestAlertException("Topic already used", ENTITY_NAME, "topic.topicUsed");
               }
           }
        } else {        // xóa 1 bản ghi
            List<Feedback> feedbackList = feedbackRepository.findByTopicId(Long.parseLong(ids));
            if (feedbackList.isEmpty()) {
                topicRepository.delete(ids);
            } else {
                throw new BadRequestAlertException("Topic already used", ENTITY_NAME, "topic.topicUsed");
            }
        }

    }

    @Override
    public List<TopicDTO> findByStatus(Integer status) {
        List<TopicDTO> list = topicMapper.toDto(topicRepository.findByStatus(status));
        return list;
    }

    private String generateTopicCode (String code, String name, boolean checkName) {
        int count = 0;
        Topic topic = null;
        String generateCode = null;
        String newCode = null;
        if (StringUtils.isEmpty(code) || checkName) {
            generateCode = Utils.autoInitializationCode(name.trim());
            while(true) {
                if (count > 0) {
                    newCode = generateCode + String.format("%01d", count);
                } else {
                    newCode = generateCode;
                }
                topic = topicRepository.findTopByCode(newCode);
                if (Objects.isNull(topic)) {
                    break;
                }
                count++;
            }
    } else {
            newCode = code.trim();
            topic = topicRepository.findTopByCode(code);
            if (Objects.nonNull(topic)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }

    @Override
    public ByteArrayInputStream exportTopicToExcel(List<TopicDTO> list, InputStream file){
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 4;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for(TopicDTO topicDTO : list){
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(topicDTO.getCode());
                row.createCell(1).setCellValue(topicDTO.getName());
                row.createCell(2).setCellValue(convertStatus(topicDTO.getStatus()));
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception exception) {
            log.error("Error: " + exception);
            return null;
        }
    }

    @Override
    public boolean isDeactivable(Long id) {
        MultiValueMap<String, String> querySearch = new LinkedMultiValueMap<>();
        querySearch.set("topicId", id.toString());
        querySearch.set("status", Constants.ENTITY_STATUS.ACTIVE.toString());
        List<Feedback> feedbackList = feedbackRepository.search(querySearch, null);
        return feedbackList.size() == 0;
    }

    private static String convertStatus(Integer status){
        if(status.equals(Constants.ENTITY_STATUS.ACTIVE)){
            return "Đang hoạt động";
        }
        if(status.equals(Constants.ENTITY_STATUS.DEACTIVATE)){
            return "Dừng hoạt động";
        }
        return "Đã xóa";
    }

    @Override
    public List<TopicCustomConfigDTO> findAllCustomConfigByTopicId(Long id, Long healthFacilityId) {
        log.debug("Find all custom config of topic Start");
        TopicDTO topicDTO = topicMapper.toDto(topicRepository.findById(id).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")));
        List<CategoryConfigFieldDTO> configFieldDTOS = categoryConfigFieldService.findAllByHealthFacilityIdAndStatusAndConfigType(healthFacilityId, Constants.ENTITY_STATUS.ACTIVE, Constants.CONFIG_CATEGORY_TYPE.TOPIC.code);
        Map<Long, CategoryConfigValueDTO> configValueDTOMap = categoryConfigValueService.findAllByObjectId(id)
                .stream()
                .collect(Collectors.toMap(CategoryConfigValueDTO::getFieldId, Function.identity()));

        if (configValueDTOMap.size() == 0) {
            return Collections.emptyList();
        }
        // put value map format: field - value
        log.debug("Find all custom config of doctor End");
        return configFieldDTOS.stream()
                .map(field -> new TopicCustomConfigDTO(field.getId(), field.getName(), configValueDTOMap.getOrDefault(field.getId(), new CategoryConfigValueDTO()).getValue(), field.getDataType()))
                .collect(Collectors.toList());
    }
}
