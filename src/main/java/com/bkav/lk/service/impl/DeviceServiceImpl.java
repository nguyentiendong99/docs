package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Device;
import com.bkav.lk.dto.DeviceDTO;
import com.bkav.lk.repository.DeviceRepository;
import com.bkav.lk.service.DeviceService;
import com.bkav.lk.service.mapper.DeviceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private static final String ENTITY_NAME = "devices";

    private final DeviceRepository deviceRepository;
    private final DeviceMapper devicesMapper;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceMapper devicesMapper) {
        this.deviceRepository = deviceRepository;
        this.devicesMapper = devicesMapper;
    }

    @Override
    public Optional<DeviceDTO> findById(Long id) {
        log.info("find by id Deveices: {}", id);
        return deviceRepository.findById(id).map(devicesMapper::toDto);
    }
    @Override
    public DeviceDTO save(DeviceDTO deviceDTO) {
        log.debug("Request to save Devices : {}", deviceDTO);
        Device device = devicesMapper.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        return devicesMapper.toDto(device);
    }

    @Override
    public Optional<DeviceDTO> findByUuid(String uuid) {
        log.debug("Request to find by uuid : {}", uuid);
        return deviceRepository.findByUuid(uuid).map(devicesMapper::toDto);
    }

    @Override
    public List<DeviceDTO> findByUserId(Long id) {
        log.debug("Request to find by userId : {}", id);
        return devicesMapper.toDto(deviceRepository.findByUserId(id));
    }

    @Override
    public List<DeviceDTO> findByUserIdIn(List<Long> userIdList) {
        return devicesMapper.toDto(deviceRepository.findByUserIdIn(userIdList));
    }

    @Override
    public void deleteByLogin(String login) {
        deviceRepository.deleteByUserLogin(login);
    }

    @Override
    public void deleteById(Long id) {
        deviceRepository.deleteById(id);
    }

}
