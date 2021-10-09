package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Subclinical;
import com.bkav.lk.dto.SubclinicalDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(SubclinicalMapperDecorator.class)
public interface SubclinicalMapper extends EntityMapper<SubclinicalDTO, Subclinical> {

    Subclinical toEntity(SubclinicalDTO subclinicalDTO);

    SubclinicalDTO toDto(Subclinical subclinical);

    default Subclinical fromId(Long id) {
        if (id == null)
            return null;
        Subclinical subclinical = new Subclinical();
        subclinical.setId(id);
        return subclinical;
    }

}
