package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.UploadedFile;
import com.bkav.lk.dto.UploadedFileDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UploadedFileMapper extends EntityMapper<UploadedFileDTO, UploadedFile> {
    UploadedFile toEntity(UploadedFileDTO uploadedFileDTO);

    UploadedFileDTO toDto(UploadedFile uploadedFile);


    default UploadedFile fromId(Long id) {
        if (id == null) {
            return null;
        }
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setId(id);
        return uploadedFile;
    }
}
