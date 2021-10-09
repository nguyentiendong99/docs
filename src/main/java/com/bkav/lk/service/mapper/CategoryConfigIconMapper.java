package com.bkav.lk.service.mapper;
import com.bkav.lk.domain.CategoryConfigIcon;
import com.bkav.lk.dto.CategoryConfigIconDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryConfigIconMapper extends EntityMapper<CategoryConfigIconDTO,CategoryConfigIcon>{
    @Mapping(source = "healthFacilityId", target = "healthFacilities.id")
    CategoryConfigIcon toEntity(CategoryConfigIconDTO categoryConfigIconDTO);

    @Mapping(source = "healthFacilities.id", target = "healthFacilityId")
    CategoryConfigIconDTO toDto(CategoryConfigIcon categoryConfigIcon);

    default CategoryConfigIcon fromId(Long id) {
        if (id == null)
            return null;
        CategoryConfigIcon categoryConfigIcon = new CategoryConfigIcon();
        categoryConfigIcon.setId(id);
        return categoryConfigIcon;
    }

}
