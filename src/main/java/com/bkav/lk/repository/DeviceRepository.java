package com.bkav.lk.repository;

import com.bkav.lk.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUuid(String uuid);

    List<Device> findByUserId(Long id);

    List<Device> findByUserIdIn(List<Long> userIdList);

    void deleteByUserLogin(String login);
}
