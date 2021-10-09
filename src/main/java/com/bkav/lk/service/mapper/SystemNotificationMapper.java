package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.SystemNotification;
import com.bkav.lk.dto.SystemNotificationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {})
public interface SystemNotificationMapper extends EntityMapper<SystemNotificationDTO, SystemNotification> {

    SystemNotification toEntity(SystemNotificationDTO dto);

    SystemNotificationDTO toDto(SystemNotification entity);

    default SystemNotification fromId(Long id) {
        if (id == null) {
            return null;
        }
        SystemNotification obj = new SystemNotification();
        obj.setId(id);
        return obj;
    }
}
