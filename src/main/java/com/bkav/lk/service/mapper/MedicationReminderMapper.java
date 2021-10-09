package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.MedicationReminder;
import com.bkav.lk.dto.MedicationReminderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicationReminderMapper extends EntityMapper<MedicationReminderDTO, MedicationReminder> {

    MedicationReminder toEntity(MedicationReminderDTO dto);

    MedicationReminderDTO toDto(MedicationReminder entity);

    default MedicationReminder fromId(Long id){
        if(id == null){
            return null;
        }
        MedicationReminder entity = new MedicationReminder();
        entity.setId(id);
        return entity;
    }

}
