package com.bkav.lk.repository;

import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.repository.custom.MedicalDeclarationInfoRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalDeclarationInfoRepository extends JpaRepository<MedicalDeclarationInfo, Long>, MedicalDeclarationInfoRepositoryCustom {

    List<MedicalDeclarationInfo> findByPatientRecordIdAndStatusNot(Long patientRecordId, Integer status);
}
