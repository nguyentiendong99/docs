package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Academic;
import com.bkav.lk.dto.AcademicDTO;
import com.bkav.lk.repository.AcademicRepository;
import com.bkav.lk.service.AcademicService;
import com.bkav.lk.service.mapper.AcademicMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AcademicServiceImpl implements AcademicService {
    private static final String ENTITY_NAME = "Academic";

    private static final Logger log = LoggerFactory.getLogger(AcademicService.class);


    private final AcademicRepository academicRepository;

    private final AcademicMapper academicMapper;

    public AcademicServiceImpl(AcademicRepository academicRepository, AcademicMapper academicMapper) {
        this.academicRepository = academicRepository;
        this.academicMapper = academicMapper;
    }

    @Override
    public Optional<AcademicDTO> findOne(Long id) {
        return academicRepository.findById(id).map(academicMapper::toDto);
    }

    @Override
    public Boolean checkExistCode(String code) {
        return academicRepository.existsByCodeAndStatus(code, Constants.ENTITY_STATUS.ACTIVE);
    }

    @Override
    public Page<AcademicDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        if (queryParams.containsKey("pageable")) {
            pageable = null;
        }
        List<Academic> groups = academicRepository.search(queryParams, pageable);
        if (pageable == null) {
            return new PageImpl<>(academicMapper.toDto(groups));
        }
        Page<Academic> page = new PageImpl<>(groups, pageable, academicRepository.count(queryParams));
        return page.map(academicMapper::toDto);
    }

    @Override
    public AcademicDTO findByAcademicName(String name) {
        Academic academic = academicRepository.findAcademicByNameAndStatus(name, Constants.ENTITY_STATUS.ACTIVE)
                .orElseThrow(() -> new BadRequestAlertException("Invalid name", ENTITY_NAME, "namenull"));
        return academicMapper.toDto(academic);
    }

    @Override
    public AcademicDTO save(AcademicDTO academicDTO) {
        log.debug("Request to save Academic : {}", academicDTO);
        if (StringUtils.isEmpty(academicDTO.getId())) {
            academicDTO.setCode(generateAcademicCode(academicDTO.getCode(), academicDTO.getName(), false));

        } else {
            Optional<Academic> academic = academicRepository.findById(academicDTO.getId());
            if (!academic.get().getCode().equals(academicDTO.getCode()) || !academic.get().getName().equals(academicDTO.getName())) {
                academicDTO.setCode(generateAcademicCode(academicDTO.getCode(), academicDTO.getName(),!academic.get().getName().equals(academicDTO.getName())));
            }
        }
        Academic academic = academicMapper.toEntity(academicDTO);
        academic = academicRepository.save(academic);
        return academicMapper.toDto(academic);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Academic academic = academicRepository.findById(id).
                orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"));
        academic.setStatus(Constants.ENTITY_STATUS.DELETED);
        academicRepository.save(academic);
    }

    @Override
    public boolean existCode(String code) {
        return academicRepository.existsByCode(code.toUpperCase());
    }

    @Override
    public List<AcademicDTO> findAll() {
        log.debug("Request to find Academics by health facility ID = {}");
        List<Academic> results = academicRepository.findByStatus(Constants.ENTITY_STATUS.ACTIVE);
        return academicMapper.toDto(results);
    }
    private String generateAcademicCode (String code, String name, boolean checkName) {
        int count = 0;
        Academic academic = null;
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
                academic = academicRepository.findTopByCode(newCode);
                if (Objects.isNull(academic)) {
                    break;
                }
                count++;
            }
        } else {
            newCode = code.trim();
            academic = academicRepository.findTopByCode(code);
            if (Objects.nonNull(academic)) {
                throw new BadRequestAlertException("Code already exist", ENTITY_NAME, "codeexists");
            }
        }
        return newCode;
    }
}
