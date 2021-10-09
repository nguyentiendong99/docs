package com.bkav.lk.repository;

import com.bkav.lk.domain.DeclarationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeclarationQuestionRepository extends JpaRepository<DeclarationQuestion, Long> {

    List<DeclarationQuestion> findAllByStatusIsNot(Integer status);
}
