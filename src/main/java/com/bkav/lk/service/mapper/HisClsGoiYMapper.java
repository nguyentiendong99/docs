package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.HisClsGoiY;
import com.bkav.lk.dto.HisClsGoiYDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HisClsGoiYMapper extends EntityMapper<HisClsGoiYDTO, HisClsGoiY> {

    HisClsGoiY toEntity(HisClsGoiYDTO hisClsGoiYDTO);

    HisClsGoiYDTO toDto(HisClsGoiY hisClsGoiY);

    default HisClsGoiY fromId(Long id) {
        if(id == null) {
            return null;
        }
        HisClsGoiY hisClsGoiY = new HisClsGoiY();
        hisClsGoiY.setId(id);
        return hisClsGoiY;
    }
}
