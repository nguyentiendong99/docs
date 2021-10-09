package com.bkav.lk.service.mapper;


import com.bkav.lk.domain.CategoryConfigFieldMain;
import com.bkav.lk.dto.CategoryConfigFieldMainDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryConfigFieldMainMapper extends EntityMapper<CategoryConfigFieldMainDTO, CategoryConfigFieldMain> {

    @Mapping(source = "healthFacilityId", target = "healthFacilities.id")
    CategoryConfigFieldMain toEntity(CategoryConfigFieldMainDTO configFieldMainDTO);

    @Mapping(source = "healthFacilities.id", target = "healthFacilityId")
    CategoryConfigFieldMainDTO toDto(CategoryConfigFieldMain configFieldMain);

    default CategoryConfigFieldMain fromId(Long id) {
        if (id == null)
            return null;
        CategoryConfigFieldMain configFieldMain = new CategoryConfigFieldMain();
        configFieldMain.setId(id);
        return configFieldMain;
    }
}
