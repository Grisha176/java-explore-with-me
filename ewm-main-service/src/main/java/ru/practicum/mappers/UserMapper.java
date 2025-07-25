package ru.practicum.mappers;

import org.mapstruct.Mapper;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapToUser(NewUserRequest newUserRequest);

    UserDto mapToUserDto(User user);

    UserShortDto toUserShortDto(User user);
}
