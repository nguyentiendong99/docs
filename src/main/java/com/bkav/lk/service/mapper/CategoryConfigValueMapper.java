package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.CategoryConfigValue;
import com.bkav.lk.dto.CategoryConfigValueDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author hieu.daominh
 */
@Mapper(componentModel = "spring")
public interface CategoryConfigValueMapper extends EntityMapper<CategoryConfigValueDTO, CategoryConfigValue> {

    @Mappings({
            @Mapping(target = "field.id", source = "configValueDTO.fieldId")
    })
    CategoryConfigValue toEntity(CategoryConfigValueDTO configValueDTO);

    @Mappings({
            @Mapping(target = "fieldId", source = "configValue.field.id")
    })
    CategoryConfigValueDTO toDto(CategoryConfigValue configValue);

    default CategoryConfigValue fromId(Long id) {
        if (id == null)
            return null;
        CategoryConfigValue configValue = new CategoryConfigValue();
        configValue.setId(id);
        return configValue;
    }
}
