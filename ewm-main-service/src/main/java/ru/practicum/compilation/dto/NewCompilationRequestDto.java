package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationRequestDto {

    private List<Long> events;

    private Boolean pinned;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 1, max = 50,message = "Длина названия должна быть от 1 до 50 символов")
    private String title;

}
