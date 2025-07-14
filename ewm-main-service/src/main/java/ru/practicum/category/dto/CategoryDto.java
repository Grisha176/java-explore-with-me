package ru.practicum.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {

    private Long id;
    @NotNull(message = "Имя не может быть пустым")
    private String name;
}
