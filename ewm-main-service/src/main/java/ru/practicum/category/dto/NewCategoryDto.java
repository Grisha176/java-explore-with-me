package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {

    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 50, min = 1, message = "Длина названия должна быть от 1 до 50 символов")
    private String name;
}
