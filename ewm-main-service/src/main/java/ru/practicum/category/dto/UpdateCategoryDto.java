package ru.practicum.category.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryDto {

    @Size(min = 1, max = 50, message = "Длина name должна составлять от 1 до 50 символов!")
    private String name;

}
