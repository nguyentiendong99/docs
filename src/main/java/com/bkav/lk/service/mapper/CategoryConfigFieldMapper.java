package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.CategoryConfigField;
import com.bkav.lk.dto.CategoryConfigFieldDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author hieu.daominh
 */
@Mapper(componentModel = "spring")
public interface CategoryConfigFieldMapper extends EntityMapper<CategoryConfigFieldDTO, CategoryConfigField> {

    @Mappings({
            @Mapping(target = "healthFacilities.id", source = "configFieldDTO.healthFacilityId")
    })
    CategoryConfigField toEntity(CategoryConfigFieldDTO configFieldDTO);

    @Mappings({
            @Mapping(target = "healthFacilityId", source = "configField.healthFacilities.id")
    })
    CategoryConfigFieldDTO toDto(CategoryConfigField configField);

    default CategoryConfigField fromId(Long id) {
        if (id == null)
            return null;
        CategoryConfigField configField = new CategoryConfigField();
        configField.setId(id);
        return configField;
    }
}
