package com.bkav.lk.service.mapper;
import com.bkav.lk.domain.Device;
import com.bkav.lk.dto.DeviceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DeviceMapper extends EntityMapper<DeviceDTO, Device> {

    @Mapping(source = "userId", target = "user")
    Device toEntity(DeviceDTO deviceDTO);

    @Mapping(source = "user.id", target = "userId")
    DeviceDTO toDto(Device device);

    default Device fromId(Long id) {
        if (id == null)
            return null;
        Device device = new Device();
        device.setId(id);
        return device;
    }
}
