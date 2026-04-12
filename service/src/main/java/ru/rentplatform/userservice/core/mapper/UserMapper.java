package ru.rentplatform.userservice.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.core.dao.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "isActive", source = "isActive")
    UserResponse toResponse(User user);
}
