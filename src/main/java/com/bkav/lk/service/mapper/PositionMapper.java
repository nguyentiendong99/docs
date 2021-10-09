package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Position;
import com.bkav.lk.dto.PositionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionMapper  extends EntityMapper<PositionDTO, Position> {

    Position toEntity(PositionDTO positionDTO);

    PositionDTO toDto(Position position);

    default Position fromId(Long id){
        if(id == null){
            return null;
        }
        Position position = new Position();
        position.setId(id);
        return position;
    }

}
