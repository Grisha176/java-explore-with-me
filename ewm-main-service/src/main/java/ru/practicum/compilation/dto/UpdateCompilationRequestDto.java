package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateCompilationRequestDto {

    private List<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50,message = "Длина названия должна быть от 1 до 50 символов")
    private String title;
}
