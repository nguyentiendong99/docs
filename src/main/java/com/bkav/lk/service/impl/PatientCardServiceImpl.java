package com.bkav.lk.service.impl;

import com.bkav.lk.domain.PatientCard;
import com.bkav.lk.dto.PatientCardDTO;
import com.bkav.lk.repository.PatientCardRepository;
import com.bkav.lk.service.PatientCardService;
import com.bkav.lk.service.mapper.PatientCardMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientCardServiceImpl implements PatientCardService {
    private final PatientCardRepository patientCardRepository;

    private final PatientCardMapper patientCardMapper;

    public PatientCardServiceImpl(PatientCardRepository patientCardRepository, PatientCardMapper patientCardMapper) {
        this.patientCardRepository = patientCardRepository;
        this.patientCardMapper = patientCardMapper;
    }

    @Override
    public PatientCardDTO save(PatientCardDTO patientCardDTO) {
        PatientCard patientCard = patientCardRepository.save(patientCardMapper.toEntity(patientCardDTO));
        return patientCardMapper.toDto(patientCard);
    }

    @Override
    public List<PatientCardDTO> findAll(Long userId) {
        List<PatientCard> patientCardList = patientCardRepository.findAllByUserId(userId);
        return patientCardMapper.toDto(patientCardList);
    }

    @Override
    public PatientCardDTO update(PatientCardDTO patientCardDTO) {
        PatientCard patientCard = patientCardRepository.save(patientCardMapper.toEntity(patientCardDTO));
        return patientCardMapper.toDto(patientCard);
    }

    @Override
    public void delete(Long id) {
        patientCardRepository.deleteById(id);
    }

    @Override
    public PatientCardDTO findByCardNumber(String cardNumber) {
        PatientCard patientCard = patientCardRepository.findByCardNumber(cardNumber);
        return patientCardMapper.toDto(patientCard);
    }

    @Override
    public PatientCardDTO findByUserIdAndId(Long userId, Long id) {
        return patientCardMapper.toDto(patientCardRepository.findByUserIdAndId(userId, id));
    }
}
