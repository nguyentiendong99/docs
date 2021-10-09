package com.bkav.lk.service.mapper;


import com.bkav.lk.domain.Authority;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDTO, User> {

    @Mappings({
            @Mapping(source = "userDTO", target = "authorities", qualifiedByName = "fromAuthority")
    })
    User toEntity(UserDTO userDTO);

    @Mappings({
            @Mapping(source = "user", target = "authorities", qualifiedByName = "toAuthority"),
            @Mapping(source = "position.id", target = "positionId"),
            @Mapping(source = "position.name", target = "positionName"),
            @Mapping(source = "department.id", target = "departmentId"),
            @Mapping(source = "department.name", target = "departmentName"),
            @Mapping(source = "city.shortName", target = "cityName"),
            @Mapping(source = "city.areaCode", target = "cityCode"),
            @Mapping(source = "district.shortName", target = "districtName"),
            @Mapping(source = "district.areaCode", target = "districtCode"),
            @Mapping(source = "ward.shortName", target = "wardName"),
            @Mapping(source = "ward.areaCode", target = "wardCode")
    })
    UserDTO toDto(User user);

    default User fromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    @Named("fromAuthority")
    default Set<Authority> authoritiesFromStrings(UserDTO userDTO) {
        Set<Authority> authorities = new HashSet<>();

        if (userDTO.getAuthorities() != null) {
            authorities = userDTO.getAuthorities().stream().map(string -> {
                Authority auth = new Authority();
                auth.setName(string);
                return auth;
            }).collect(Collectors.toSet());
        }

        return authorities;
    }

    @Named("toAuthority")
    default Set<String> authoritiesToStrings(User user) {
        Set<String> authoritiesString = new HashSet<>();

        if (user.getAuthorities() != null) {
            authoritiesString = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
        }

        return authoritiesString;
    }
}
