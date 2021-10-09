package com.bkav.lk.service;

import com.bkav.lk.dto.DeviceDTO;

import java.util.List;
import java.util.Optional;

public interface DeviceService {
    Optional<DeviceDTO> findById(Long id);

    DeviceDTO save(DeviceDTO deviceDTO);

    Optional<DeviceDTO> findByUuid(String uuid);

    List<DeviceDTO> findByUserId(Long id);

    List<DeviceDTO> findByUserIdIn(List<Long> userIdList);

    void deleteByLogin(String login);

    void deleteById(Long id);
}
