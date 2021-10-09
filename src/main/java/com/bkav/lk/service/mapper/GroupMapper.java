package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Authority;
import com.bkav.lk.domain.Group;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.GroupDTO;
import com.bkav.lk.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GroupMapper extends EntityMapper<GroupDTO, Group> {

    @Mappings({
            @Mapping(source = "groupDTO", target = "authorities", qualifiedByName = "fromAuthority")
    })
    Group toEntity(GroupDTO groupDTO);

    @Mappings({
            @Mapping(source = "group", target = "authorities", qualifiedByName = "toAuthority")
    })
    GroupDTO toDto(Group group);

    default Group fromId(Long id) {
        if (id == null)
            return null;
        Group group = new Group();
        group.setId(id);
        return group;
    }

    @Named("fromAuthority")
    default Set<Authority> authoritiesFromStrings(GroupDTO groupDTO) {
        Set<Authority> authorities = new HashSet<>();

        if (groupDTO.getAuthorities() != null) {
            authorities = groupDTO.getAuthorities().stream().map(string -> {
                Authority auth = new Authority();
                auth.setName(string);
                return auth;
            }).collect(Collectors.toSet());
        }

        return authorities;
    }

    @Named("toAuthority")
    default Set<String> authoritiesToStrings(Group group) {
        Set<String> authoritiesString = new HashSet<>();

        if (group.getAuthorities() != null) {
            authoritiesString = group.getAuthorities().stream().map(authority -> {
                return authority.getName();
            }).collect(Collectors.toSet());
        }

        return authoritiesString;
    }
}
