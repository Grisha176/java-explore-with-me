package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.location.LocationDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @NotBlank(message = "аннотация не может быть пустой")
    @NotNull(message = "описание не может быть пустым")
    @Size(min = 20, max = 2000, message = "Длина аннотации должна не больше 2000 символов и не меньше 20")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой")
    private Long category;

    @NotBlank(message = "Описание не может быть пустым")
    @NotNull(message = "описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Длина описания должна не больше 7000 символов и не меньше 20")
    private String description;

    @NotNull(message = "Дата события не может быть пустой")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;


    @NotNull(message = "локация не может быть пустой")
    private LocationDto location;

    private Boolean paid = false;

    @Min(0)
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotNull(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120, message = "Длина названия должна не больше 120 символов и не меньше 3")
    private String title;

}
