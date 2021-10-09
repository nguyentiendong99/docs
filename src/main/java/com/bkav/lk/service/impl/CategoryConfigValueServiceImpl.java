package com.bkav.lk.service.impl;

import com.bkav.lk.domain.CategoryConfigValue;
import com.bkav.lk.dto.CategoryConfigValueDTO;
import com.bkav.lk.repository.CategoryConfigValueRepository;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigValueService;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.service.mapper.CategoryConfigValueMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryConfigValueServiceImpl implements CategoryConfigValueService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private static final String ENTITY_NAME = "categoryConfigValue";

    private final CategoryConfigValueMapper categoryConfigValueMapper;
    private final CategoryConfigValueRepository categoryConfigValueRepository;
    private final CategoryConfigFieldService categoryConfigFieldService;

    public CategoryConfigValueServiceImpl(CategoryConfigValueMapper categoryConfigValueMapper, CategoryConfigValueRepository categoryConfigValueRepository,@Lazy CategoryConfigFieldService categoryConfigFieldService) {
        this.categoryConfigValueMapper = categoryConfigValueMapper;
        this.categoryConfigValueRepository = categoryConfigValueRepository;
        this.categoryConfigFieldService = categoryConfigFieldService;
    }

    @Override
    public Page<CategoryConfigValueDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        return null;
    }

    @Override
    public CategoryConfigValueDTO findById(Long id) throws EntityNotFoundException {
        return null;
    }

    @Override
    public CategoryConfigValueDTO findByIdAndStatus(Long fieldId, Integer status) {
        return null;
    }

    @Override
    public List<CategoryConfigValueDTO> findAll() {
        return null;
    }

    @Override
    public CategoryConfigValueDTO create(CategoryConfigValueDTO categoryConfigFieldDTO) {
        return null;
    }

    @Override
    public CategoryConfigValueDTO update(CategoryConfigValueDTO categoryConfigFieldDTO) throws EntityNotFoundException {
        return null;
    }

    @Override
    public List<CategoryConfigValueDTO> createAll(List<CategoryConfigValueDTO> categoryConfigValueDTOS) {
        return categoryConfigValueMapper.toDto(categoryConfigValueRepository.saveAll(categoryConfigValueMapper.toEntity(categoryConfigValueDTOS)));
    }

    @Override
    public List<CategoryConfigValueDTO> updateAll(List<CategoryConfigValueDTO> categoryConfigValueDTOS) {
        categoryConfigValueDTOS.forEach(configValueDTO -> {
            if (!categoryConfigFieldService.checkExitsByIdAndStatusNot(configValueDTO.getFieldId(), Constants.ENTITY_STATUS.DELETED)) throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        });
        return categoryConfigValueMapper.toDto(categoryConfigValueRepository.saveAll(categoryConfigValueMapper.toEntity(categoryConfigValueDTOS)));
    }

    @Override
    public void delete(Long id) throws EntityNotFoundException {
        CategoryConfigValue configValue = categoryConfigValueRepository.findByIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        configValue.setStatus(Constants.ENTITY_STATUS.DELETED);
        categoryConfigValueRepository.save(configValue);
    }

    @Override
    public void deleteAll(List<Long> ids) {
        List<CategoryConfigValue> categoryConfigValue = new ArrayList<>();
        ids.forEach(id -> {
            CategoryConfigValue configValue = categoryConfigValueRepository.findByIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE).orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
            configValue.setStatus(Constants.ENTITY_STATUS.DELETED);
            categoryConfigValue.add(configValue);
        });
        categoryConfigValueRepository.saveAll(categoryConfigValue);
    }

    @Override
    public List<CategoryConfigValueDTO> findAllByFieldIdAndStatus(Long fieldId, Integer status) {
        return categoryConfigValueMapper.toDto(categoryConfigValueRepository.findAllByFieldIdAndStatus(fieldId, status).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigValueDTO> findAllByFieldId(Long fieldId) {
        return categoryConfigValueMapper.toDto(categoryConfigValueRepository.findAllByFieldId(fieldId).orElseGet(ArrayList::new));
    }

    @Override
    public List<CategoryConfigValueDTO> findAllByObjectId(Long objectId) {
        return categoryConfigValueMapper.toDto(categoryConfigValueRepository.findAllByObjectIdAndStatus(objectId, Constants.ENTITY_STATUS.ACTIVE).orElseGet(ArrayList::new));
    }

    @Override
    public CategoryConfigValueDTO findByObjectIdAndFieldId(Long objectId, Long fieldId) {
        return categoryConfigValueMapper.toDto(categoryConfigValueRepository.findByObjectIdAndFieldIdAndStatus(objectId, fieldId, Constants.ENTITY_STATUS.ACTIVE).orElseGet(CategoryConfigValue::new));
    }
}
