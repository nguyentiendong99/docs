package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Cls;
import com.bkav.lk.dto.ClsDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClsMapper extends EntityMapper<ClsDTO, Cls>{
    Cls toEntity(ClsDTO clsDTO);

    ClsDTO toDto(Cls cls);

    default Cls fromId(Long id) {
        if(id == null) {
            return null;
        }
        Cls cls = new Cls();
        cls.setId(id);
        return cls;
    }
}
