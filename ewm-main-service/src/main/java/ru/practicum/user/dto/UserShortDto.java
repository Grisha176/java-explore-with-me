package ru.practicum.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserShortDto {


    @NotNull(message = "id не может быть пустым")
    private Long id;
    @NotNull(message = "имя не может быть пустым")
    private String name;

}
