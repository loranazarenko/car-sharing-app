package carsharingapp.mapper;

import carsharingapp.config.MapperConfig;
import carsharingapp.dto.UserRegistrationRequestDto;
import carsharingapp.dto.UserResponseDto;
import carsharingapp.model.Role;
import carsharingapp.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserRegistrationRequestDto requestDto);

    @Mapping(target = "roleIds", ignore = true)
    UserResponseDto toDto(User user);

    @AfterMapping
    default void setRoleIds(@MappingTarget UserResponseDto responseDto, User user) {
        Set<Long> roleIdsList = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        responseDto.setRoleIds(roleIdsList);
    }
}
