package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Area;
import com.bkav.lk.dto.AreaDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AreaMapper extends EntityMapper<AreaDTO, Area> {

    Area toEntity(AreaDTO areaDTO);

    AreaDTO toDto(Area area);

    default Area fromId(Long id) {
        if (id == null) {
            return null;
        }
        Area area = new Area();
        area.setId(id);
        return area;
    }
}
