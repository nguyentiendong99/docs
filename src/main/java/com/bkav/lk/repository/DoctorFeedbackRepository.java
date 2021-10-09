package com.bkav.lk.repository;

import com.bkav.lk.domain.DoctorFeedback;
import com.bkav.lk.repository.custom.DoctorFeedbackRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorFeedbackRepository extends JpaRepository<DoctorFeedback, Long>, DoctorFeedbackRepositoryCustom {

    List<DoctorFeedback> findByDoctorIdAndStatus(Long doctorId, Integer status);

    List<DoctorFeedback> findAllByDoctorId(Long doctorId);

}
