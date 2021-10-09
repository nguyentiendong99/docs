package com.bkav.lk.repository;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.repository.custom.ActivityLogRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, ActivityLogRepositoryCustom {

   List<ActivityLog> findByContentIdAndContentTypeOrderByCreatedDateDesc(Long contentId, Integer contentType);
}
