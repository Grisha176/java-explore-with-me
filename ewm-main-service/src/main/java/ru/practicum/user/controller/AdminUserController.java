package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) List<Long> ids, @RequestParam(value = "from", defaultValue = "0") int from, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Запрос на получени пользователей с параметрами,size:{},from{}", size, from);
        return userService.getAllUsers(ids, from, size);
    }

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("Запрос на создание пользователя: {}", newUserRequest);
        return userService.createUser(newUserRequest);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Запрос на удалении пользователя с id: {}", userId);
        userService.deleteUser(userId);
    }


}
