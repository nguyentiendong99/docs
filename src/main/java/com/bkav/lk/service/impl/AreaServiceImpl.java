package com.bkav.lk.service.impl;

import com.bkav.lk.dto.AreaDTO;
import com.bkav.lk.repository.AreaRepository;
import com.bkav.lk.service.AreaService;
import com.bkav.lk.service.mapper.AreaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AreaServiceImpl implements AreaService {

    private final Logger log = LoggerFactory.getLogger(AreaServiceImpl.class);

    private final AreaRepository areaRepository;

    private final AreaMapper areaMapper;

    public AreaServiceImpl(AreaRepository areaRepository, AreaMapper areaMapper) {
        this.areaRepository = areaRepository;
        this.areaMapper = areaMapper;
    }

    @Override
    public List<AreaDTO> findByParentCode(String parentCode) {
        log.debug("Request to get by parent code: {}", parentCode);
        return areaMapper.toDto(areaRepository.findAllByParentCode(parentCode));
    }

    @Override
    public List<AreaDTO> findByLevelAndStatus(Integer level, Integer status) {
        log.debug("Request to get all by level and status");
        return areaMapper.toDto(areaRepository.findByLevelAndStatus(level, status));
    }

    @Override
    public List<AreaDTO> findByNameAndParentCodeAndStatus(String name, String parentCode, Integer status) {
        log.debug("Request to get all by name and parentCode and status");
        String nameLike = "%" + name + "%";
        return areaMapper.toDto(areaRepository.findByStatusAndParentCodeAndNameLike(status, parentCode, nameLike));
    }
}
