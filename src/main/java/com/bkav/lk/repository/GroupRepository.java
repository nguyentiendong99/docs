package com.bkav.lk.repository;

import com.bkav.lk.domain.Group;
import com.bkav.lk.repository.custom.GroupRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {
    Optional<Group> findGroupByGroupName(String name);

    @Query("SELECT DISTINCT G.createdBy FROM Group G where G.status <> 0")
    List<String> getCreatedByUsers();

    Group findByIdAndStatus(Long id, Integer status);
}
