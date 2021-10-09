package com.bkav.lk.repository;

import com.bkav.lk.domain.PatientCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientCardRepository extends JpaRepository<PatientCard, Long> {
    List<PatientCard> findAllByUserId(Long userId);

    PatientCard findByCardNumber(String cardNumber);

    PatientCard findByUserIdAndId(Long userId,Long id);
}
