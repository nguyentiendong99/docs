package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Otp;
import com.bkav.lk.dto.OtpDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OtpMapper extends EntityMapper<OtpDTO, Otp> {

    Otp toEntity(OtpDTO dto);

    OtpDTO toDto(Otp entity);

    default Otp fromId(Long id){
        if(id == null){
            return null;
        }
        Otp obj = new Otp();
        obj.setId(id);
        return obj;
    }

}
