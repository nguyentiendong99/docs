package com.bkav.lk.service.impl;

import com.bkav.lk.domain.DetailMedicalDeclarationInfo;
import com.bkav.lk.dto.DetailMedicalDeclarationInfoDTO;
import com.bkav.lk.repository.DetailMedicalDeclarationInfoRepository;
import com.bkav.lk.service.DetailMedicalDeclarationInfoService;
import com.bkav.lk.service.mapper.DetailMedicalDeclarationInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class DetailMedicalDeclarationInfoServiceImpl implements DetailMedicalDeclarationInfoService {

    private final DetailMedicalDeclarationInfoRepository detailMedicalDeclarationInfoRepository;

    private final DetailMedicalDeclarationInfoMapper detailMedicalDeclarationInfoMapper;

    public DetailMedicalDeclarationInfoServiceImpl(DetailMedicalDeclarationInfoRepository detailMedicalDeclarationInfoRepository, DetailMedicalDeclarationInfoMapper detailMedicalDeclarationInfoMapper) {
        this.detailMedicalDeclarationInfoRepository = detailMedicalDeclarationInfoRepository;
        this.detailMedicalDeclarationInfoMapper = detailMedicalDeclarationInfoMapper;
    }


    @Override
    public DetailMedicalDeclarationInfoDTO save(DetailMedicalDeclarationInfoDTO dto) {
        DetailMedicalDeclarationInfo entity = detailMedicalDeclarationInfoMapper.toEntity(dto);
        detailMedicalDeclarationInfoRepository.save(entity);
        return dto;
    }
}
