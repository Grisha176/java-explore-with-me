package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long id);

    List<UserDto> getAllUsers(List<Long> ids,int from,int size);
}
