package ru.practicum.category.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCategoryDto {

    @NotNull(message = "Название не может быть пустым")
    @Size(max = 50,min = 1,message = "Длина названия должна быть от 1 до 50 символов")
    private String name;

    private String description;

}
