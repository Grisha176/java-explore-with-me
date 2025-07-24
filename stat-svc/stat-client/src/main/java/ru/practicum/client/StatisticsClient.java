package ru.practicum.client;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.client.base.BaseClient;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class StatisticsClient extends BaseClient {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private String appName;

    @Autowired
    public StatisticsClient(@Value("${stats-server.url}") String serverUrl,
                            @Value("${app.name}") String appName,
                            RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build(),
                serverUrl
        );
        this.appName = appName;
    }

    public ResponseEntity<Object> create(HttpServletRequest request) {

        EndpointHitDto endpointHitCreateDto = EndpointHitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        return post(endpointHitCreateDto);
    }


    public ResponseEntity<Object> getStats(@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSS") LocalDateTime start, @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSS") LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("StatClient получение статистики");
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris);
        }

        String url = builder.build().toUriString();

        return get(url);
    }

}
