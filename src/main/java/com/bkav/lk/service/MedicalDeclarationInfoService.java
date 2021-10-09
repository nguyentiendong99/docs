package com.bkav.lk.service;

import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.dto.DeclarationQuestionDTO;
import com.bkav.lk.dto.DetailMedicalDeclarationInfoDTO;
import com.bkav.lk.dto.MedicalDeclarationInfoDTO;
import com.bkav.lk.dto.MedicalDeclarationInfoVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public interface MedicalDeclarationInfoService {

    Page<MedicalDeclarationInfoDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    MedicalDeclarationInfo save(MedicalDeclarationInfoDTO medicalDeclarationInfoDTO);

    boolean isRequiredFieldsMissing(MedicalDeclarationInfoDTO medicalDeclarationInfoDTO);

    Optional<MedicalDeclarationInfoDTO> findOne(Long id);

    List<DetailMedicalDeclarationInfoDTO> findMedicalDeclarationInfoDetail(Long id);

    List<DeclarationQuestionDTO> getListQuestion();

    void delete(Long id);

    Page<MedicalDeclarationInfoVM> searchToExcel(MultiValueMap<String, String> queryParams, Pageable pageable);
}
