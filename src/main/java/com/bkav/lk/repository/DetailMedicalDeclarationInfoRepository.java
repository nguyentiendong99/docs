package com.bkav.lk.repository;

import com.bkav.lk.domain.DetailMedicalDeclarationInfo;
import com.bkav.lk.repository.custom.DetailMedicalDeclarationInfoRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailMedicalDeclarationInfoRepository extends JpaRepository<DetailMedicalDeclarationInfo, Long>, DetailMedicalDeclarationInfoRepositoryCustom {

    List<DetailMedicalDeclarationInfo> findAllByMedicalDeclarationInfo_Id(Long medicalDeclarationId);

}
