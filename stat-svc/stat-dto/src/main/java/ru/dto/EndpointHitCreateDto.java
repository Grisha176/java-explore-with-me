package ru.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitCreateDto {

    private Long id;
    private String app;
    private String uri;
    private String ip;

}
