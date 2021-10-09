package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Device;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DeviceDTO;
import com.bkav.lk.service.DeviceService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DeviceResource {
    private final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    private static final String ENTITY_NAME = "Devices";

    private final DeviceService deviceService;

    private final UserService userService;

    public DeviceResource(DeviceService deviceService, UserService userService) {
        this.deviceService = deviceService;
        this.userService = userService;
    }

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/device/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long id) {
        log.debug("REST request to get Devices : {}", id);
        Optional<DeviceDTO> devicesDTO = deviceService.findById(id);
        if (!devicesDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(devicesDTO);
    }

    @PostMapping("/device")
    public ResponseEntity<DeviceDTO> save(@RequestBody DeviceDTO deviceDTO) throws URISyntaxException {
        log.debug("REST request to save Devices : {}", deviceDTO);
        if (deviceDTO.getId() != null) {
            throw new BadRequestAlertException("A new Devices cannot already have an ID", ENTITY_NAME, "idexists");
        }

        // Xét trường hợp 2 tài khoản cùng đăng nhập trên 1 thiết bị sẽ có cùng firebaseToken
        Optional<DeviceDTO> oldDevice = deviceService.findByUuid(deviceDTO.getUuid());
        if (oldDevice.isPresent()){
            deviceService.deleteById(oldDevice.get().getId());
        }

        Optional<User> currentUser = userService.getUserWithAuthorities();
        List<DeviceDTO> deviceDTOList = deviceService.findByUserId(currentUser.get().getId());

        // if has device by userId
        if (deviceDTOList.size() > 0){

            // giu lai ban ghi moi nhat
            DeviceDTO devicesOpt = deviceDTOList.get(deviceDTOList.size() - 1);
            for (int i = 0; i < deviceDTOList.size() - 1; ++i){
                deviceService.deleteById(deviceDTOList.get(i).getId());
            }
            deviceDTO.setId(devicesOpt.getId());
        }
        if(currentUser.isPresent()){
            deviceDTO.setUserId(currentUser.get().getId());
        }

        DeviceDTO result = deviceService.save(deviceDTO);
        return ResponseEntity.created(new URI("/api/device/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @GetMapping("/device/userid/{id}")
    public ResponseEntity<List<DeviceDTO>> findByUserId(@PathVariable Long id){
        log.debug("REST request to find by User_id: {}", id);
        List<DeviceDTO> list = deviceService.findByUserId(id);
        return ResponseEntity.ok(list);
    }
}
