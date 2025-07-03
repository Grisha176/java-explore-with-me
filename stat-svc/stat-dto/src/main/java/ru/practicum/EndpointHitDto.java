package ru.practicum;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {

    private Long id;
    private String app;
    private String uri;
    private String ip;
    private String timestamp;

}
