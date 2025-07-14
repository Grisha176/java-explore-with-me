package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewUserRequest {

    @NotNull(message = "id не может быть пустым")
    private String name;
    @NotNull(message = "id не может быть пустым")
    @Email
    private String email;
}
