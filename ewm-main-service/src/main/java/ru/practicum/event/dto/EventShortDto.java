package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {

    @NotNull(message = "описание не может быть пустым")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой")
    private CategoryDto category;

    private Integer confirmedRequests;

    @NotNull(message = "Дата события не может быть пустой")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long id;

    @NotNull(message = "Создатель события не может быть null")
    private UserShortDto initiator;


    @NotNull(message = "статус не может быть пустым")
    private Boolean paid;

    @NotNull(message = "Заголовок не может быть пустым")
    private String title;

    private Integer views;
}
