package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Notification;
import com.bkav.lk.dto.NotificationDTO;
import com.bkav.lk.util.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {

    Notification toEntity(NotificationDTO dto);

    @Mapping(source = "createdDate", target = "friendlyFormat", qualifiedByName = "toFriendlyFormat")
    NotificationDTO toDto(Notification entity);

    default Notification fromId(Long id) {
        if (id == null) {
            return null;
        }
        Notification obj = new Notification();
        obj.setId(id);
        return obj;
    }

    @Named("toFriendlyFormat")
    default String toFriendlyFormat(Instant lastModifiedDate) {
        return DateUtils.friendlyDateTimeFormat7(lastModifiedDate);
    }
}
