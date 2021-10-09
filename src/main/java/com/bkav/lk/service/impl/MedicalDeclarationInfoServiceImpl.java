package com.bkav.lk.service.impl;

import com.bkav.lk.domain.DeclarationQuestion;
import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.DeclarationQuestionRepository;
import com.bkav.lk.repository.DetailMedicalDeclarationInfoRepository;
import com.bkav.lk.repository.MedicalDeclarationInfoRepository;
import com.bkav.lk.repository.PatientRecordRepository;
import com.bkav.lk.service.MedicalDeclarationInfoService;
import com.bkav.lk.service.mapper.DeclarationQuestionMapper;
import com.bkav.lk.service.mapper.DetailMedicalDeclarationInfoMapper;
import com.bkav.lk.service.mapper.MedicalDeclarationInfoMapper;
import com.bkav.lk.service.mapper.PatientRecordMapper;
import com.bkav.lk.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalDeclarationInfoServiceImpl implements MedicalDeclarationInfoService {

    private final MedicalDeclarationInfoRepository medicalDeclarationInfoRepository;

    private final DetailMedicalDeclarationInfoRepository detailMedicalDeclarationInfoRepository;

    private final DeclarationQuestionRepository declarationQuestionRepository;

    private final MedicalDeclarationInfoMapper medicalDeclarationInfoMapper;

    private final DetailMedicalDeclarationInfoMapper detailMedicalDeclarationInfoMapper;

    private final DeclarationQuestionMapper declarationQuestionMapper;

    private final PatientRecordRepository patientRecordRepository;

    private final PatientRecordMapper patientRecordMapper;

    public MedicalDeclarationInfoServiceImpl(MedicalDeclarationInfoRepository medicalDeclarationInfoRepository,
                                             DetailMedicalDeclarationInfoRepository detailMedicalDeclarationInfoRepository, DeclarationQuestionRepository declarationQuestionRepository, MedicalDeclarationInfoMapper medicalDeclarationInfoMapper, DetailMedicalDeclarationInfoMapper detailMedicalDeclarationInfoMapper, DeclarationQuestionMapper declarationQuestionMapper, PatientRecordRepository patientRecordRepository, PatientRecordMapper patientRecordMapper) {
        this.medicalDeclarationInfoRepository = medicalDeclarationInfoRepository;
        this.detailMedicalDeclarationInfoRepository = detailMedicalDeclarationInfoRepository;
        this.declarationQuestionRepository = declarationQuestionRepository;
        this.medicalDeclarationInfoMapper = medicalDeclarationInfoMapper;
        this.detailMedicalDeclarationInfoMapper = detailMedicalDeclarationInfoMapper;
        this.declarationQuestionMapper = declarationQuestionMapper;
        this.patientRecordRepository = patientRecordRepository;
        this.patientRecordMapper = patientRecordMapper;
    }

    @Override
    public Page<MedicalDeclarationInfoDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<MedicalDeclarationInfoDTO> list = medicalDeclarationInfoMapper.toDto(medicalDeclarationInfoRepository.search(queryParams, pageable));
        return new PageImpl<>(list, pageable, medicalDeclarationInfoRepository.count(queryParams));
    }

    @Override
    public MedicalDeclarationInfo save(MedicalDeclarationInfoDTO medicalDeclarationInfoDTO) {
        return medicalDeclarationInfoRepository.save(medicalDeclarationInfoMapper.toEntity(medicalDeclarationInfoDTO));
    }

    @Override
    public boolean isRequiredFieldsMissing(MedicalDeclarationInfoDTO dto) {
        if (dto.getPatientRecordId() == null) {
            return true;
        }
        return false;
    }

    @Override
    public Optional<MedicalDeclarationInfoDTO> findOne(Long id) {
        return medicalDeclarationInfoRepository.findById(id).map(medicalDeclarationInfoMapper::toDto);
    }

    @Override
    public List<DetailMedicalDeclarationInfoDTO> findMedicalDeclarationInfoDetail(Long id) {
        return detailMedicalDeclarationInfoMapper.toDto(detailMedicalDeclarationInfoRepository.findAllByMedicalDeclarationInfo_Id(id));
    }

    @Override
    public List<DeclarationQuestionDTO> getListQuestion() {
        List<DeclarationQuestion> list = declarationQuestionRepository.findAllByStatusIsNot(Constants.ENTITY_STATUS.DEACTIVATE);
        return declarationQuestionMapper.toDto(list);
    }

    @Override
    public void delete(Long id) {
        medicalDeclarationInfoRepository.findById(id).ifPresent(item -> {
            item.setStatus(Constants.ENTITY_STATUS.DELETED);
            medicalDeclarationInfoRepository.save(item);
        });
    }

    @Override
    public Page<MedicalDeclarationInfoVM> searchToExcel(MultiValueMap<String, String> queryParams, Pageable pageable) {
        queryParams.set("pageIsNull", null);
        List<MedicalDeclarationInfoDTO> list = medicalDeclarationInfoMapper.toDto(medicalDeclarationInfoRepository.search(queryParams, pageable));
        List<MedicalDeclarationInfoVM> listMedicalVM = new ArrayList<>();
        list.forEach(item -> {
            List<DetailMedicalDeclarationInfoDTO> listDetailInfo = detailMedicalDeclarationInfoMapper.toDto(detailMedicalDeclarationInfoRepository.findAllByMedicalDeclarationInfo_Id(item.getId()));
            if (listDetailInfo.size() > 0) {
                MedicalDeclarationInfoVM medicalVM = new MedicalDeclarationInfoVM();
                medicalVM.setId(item.getId());
                medicalVM.setPatientRecordCode(item.getPatientRecordCode());
                medicalVM.setPatientRecordName(item.getPatientRecordName());
                if (item.getGender().toLowerCase().equals(Constants.GENDER.FEMALE)) {
                    medicalVM.setGender(Constants.VI_GENDER.FEMALE);
                } else if(item.getGender().toLowerCase().equals(Constants.GENDER.MALE)) {
                    medicalVM.setGender(Constants.VI_GENDER.MALE);
                } else {
                    medicalVM.setGender(Constants.VI_GENDER.OTHER);
                }
                DateTimeFormatter formatterByDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                medicalVM.setDob(formatterByDate.format(item.getDob().atZone(ZoneId.systemDefault()).toLocalDateTime()));
                Optional<PatientRecordDTO> patientRecordDTO = patientRecordRepository.findById(item.getPatientRecordId()).map(patientRecordMapper::toDto);
                PatientRecordDTO patientRecord = patientRecordDTO.get();
                medicalVM.setHeight(patientRecord.getHeight());
                medicalVM.setWeight(patientRecord.getWeight());
                medicalVM.setAddress(item.getAddress());
                medicalVM.setCreatedDate(formatterByDate.format(item.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDateTime()));
                medicalVM.setHealthInsuranceCode(patientRecord.getHealthInsuranceCode());
                medicalVM.setPhoneNumber(patientRecord.getPhone());
                medicalVM.setEmail(patientRecord.getEmail());

                listDetailInfo.forEach(ans -> {
                    if (ans.getQuestionType().equals(Constants.QUESTION_TYPE.TEXT)) {
                        medicalVM.setNationsGoTo(ans.getAnswer());
                    } else {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<AnswerMedicalDeclarationVM>>() {}.getType();
                        ArrayList<AnswerMedicalDeclarationVM> answer = gson.fromJson(ans.getAnswer(), listType);
                        answer.forEach(rep -> {
                            if (rep.getContent().equals(Constants.SYMPTOM.FEVER)) {
                                if (rep.getValue().equals(Constants.ENTITY_STATUS.ACTIVE)) {
                                    medicalVM.setFever(Constants.ANSWER_VALUE.YES);
                                } else {
                                    medicalVM.setFever(Constants.ANSWER_VALUE.NO);
                                }
                            }
                            if (rep.getContent().equals(Constants.SYMPTOM.DYSPNOEIC)) {
                                if (rep.getValue().equals(Constants.ENTITY_STATUS.ACTIVE)) {
                                    medicalVM.setDyspnoeic(Constants.ANSWER_VALUE.YES);
                                } else {
                                    medicalVM.setDyspnoeic(Constants.ANSWER_VALUE.NO);
                                }
                            }
                            if (rep.getContent().equals(Constants.SYMPTOM.SORE_THROAT)) {
                                if (rep.getValue().equals(Constants.ENTITY_STATUS.ACTIVE)) {
                                    medicalVM.setSoreThroat(Constants.ANSWER_VALUE.YES);
                                } else {
                                    medicalVM.setSoreThroat(Constants.ANSWER_VALUE.NO);
                                }
                            }
                        });
                    }
                });
                listMedicalVM.add(medicalVM);
            }
        });
        return new PageImpl<>(listMedicalVM, pageable, medicalDeclarationInfoRepository.count(queryParams));
    }
}
