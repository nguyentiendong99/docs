package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Academic;
import com.bkav.lk.dto.AcademicDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcademicMapper extends EntityMapper<AcademicDTO, Academic> {
    Academic toEntity(AcademicDTO academicDTO);

    AcademicDTO toDto(Academic academic);


    default Academic fromId(Long id) {
        if (id == null) {
            return null;
        }
        Academic academic = new Academic();
        academic.setId(id);
        return academic;
    }
}
